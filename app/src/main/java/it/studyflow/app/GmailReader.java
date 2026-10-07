package it.studyflow.app;
import android.text.Html;
import android.util.Base64;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
/** Only Gmail read endpoints; token remains in memory. */
public final class GmailReader {
    public static final String SCOPE="https://www.googleapis.com/auth/gmail.readonly";
    public static final int LIMIT=40;
    public static List<MailProposal> recent(String token, Set<String> imported) throws IOException, JSONException {
        JSONObject profile=get("profile",token);
        String email=profile.getString("emailAddress").toLowerCase(Locale.ROOT);
        // Gmail returns messages newest first. Inbox only; excludes trash and spam.
        JSONObject list=get("messages?maxResults="+LIMIT+"&q="+URLEncoder.encode("in:inbox newer_than:30d", "UTF-8"),token);
        JSONArray messages=list.optJSONArray("messages"); List<MailProposal> result=new ArrayList<>();
        if (messages==null) return result;
        for (int i=0;i<messages.length();i++) {
            if (Thread.currentThread().isInterrupted()) throw new IOException("Importazione interrotta.");
            String id=messages.getJSONObject(i).getString("id"), key="gmail:"+email+":"+id;
            if (imported.contains(key)) continue;
            JSONObject message=get("messages/"+id+"?format=full",token);
            JSONObject payload=message.optJSONObject("payload"); if (payload==null) continue;
            String subject=header(payload,"Subject"), sender=header(payload,"From");
            String body=body(payload); if (body.isEmpty()) body=message.optString("snippet");
            LocalDate received=Instant.ofEpochMilli(message.optLong("internalDate",System.currentTimeMillis())).atZone(ZoneId.systemDefault()).toLocalDate();
            EventExtractor.Result parsed=EventExtractor.extract(subject,body,received); if (parsed==null) continue;
            CalendarEvent event=new CalendarEvent(); event.sourceKey=key; event.title=subject.isEmpty() ? "Appuntamento da Gmail" : subject; event.date=parsed.date; event.time=parsed.time; event.notes="Importato da Gmail";
            result.add(new MailProposal(event,sender,body.substring(0,Math.min(800,body.length())),parsed.warning));
        }
        return result;
    }
    private static JSONObject get(String path,String token) throws IOException,JSONException {
        HttpURLConnection conn=(HttpURLConnection)new URL("https://gmail.googleapis.com/gmail/v1/users/me/"+path).openConnection();
        conn.setConnectTimeout(15000); conn.setReadTimeout(20000); conn.setRequestProperty("Authorization","Bearer "+token);
        try {
            int code=conn.getResponseCode();
            if (code==401) throw new IOException("Gmail richiede un nuovo accesso. Scollega e ricollega l’account.");
            if (code==403) throw new IOException("Accesso Gmail negato: verifica Gmail API, consenso OAuth e utenti di test in Google Cloud.");
            if (code!=200) throw new IOException("Gmail non disponibile (HTTP "+code+"). Riprova più tardi.");
            try (InputStream stream=conn.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[8192]; int n; while ((n=stream.read(buffer))!=-1) { if (output.size()+n>4*1024*1024) throw new IOException("Messaggio troppo grande da analizzare."); output.write(buffer,0,n); }
                return new JSONObject(output.toString("UTF-8"));
            }
        } finally { conn.disconnect(); }
    }
    private static String header(JSONObject payload,String name) {
        JSONArray headers=payload.optJSONArray("headers"); if (headers==null) return "";
        for (int i=0;i<headers.length();i++) { JSONObject h=headers.optJSONObject(i); if (h!=null && name.equalsIgnoreCase(h.optString("name"))) return h.optString("value"); } return "";
    }
    private static String body(JSONObject payload) {
        String plain=part(payload,"text/plain"); if (!plain.isEmpty()) return plain;
        String html=part(payload,"text/html"); return Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY).toString();
    }
    private static String part(JSONObject part,String mime) {
        if (!part.optString("filename").isEmpty()) return ""; // Do not read attachments.
        if (mime.equals(part.optString("mimeType"))) {
            JSONObject b=part.optJSONObject("body"); String data=b==null ? "" : b.optString("data");
            try { String text=new String(Base64.decode(data,Base64.URL_SAFE|Base64.NO_WRAP),StandardCharsets.UTF_8); return text.substring(0,Math.min(100000,text.length())); } catch (IllegalArgumentException e) { return ""; }
        }
        JSONArray parts=part.optJSONArray("parts"); if (parts==null) return "";
        for (int i=0;i<parts.length();i++) { JSONObject child=parts.optJSONObject(i); if(child!=null) { String text=part(child,mime); if(!text.isEmpty()) return text; } } return "";
    }
    private GmailReader() {}
}
