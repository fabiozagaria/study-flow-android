package it.studyflow.app;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class CalendarMonthView extends LinearLayout {
    public interface Listener { void selected(LocalDate date); }
    private LocalDate selected=LocalDate.now();
    private YearMonth displayed=YearMonth.now();
    private Map<LocalDate,CalendarIndex.Day> marks=new HashMap<>();
    private Listener listener;
    private final TextView title,summary;
    private final LinearLayout grid;
    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density); }
    public CalendarMonthView(Context c,AttributeSet attrs) {
        super(c,attrs); setOrientation(VERTICAL); setPadding(dp(8),dp(12),dp(8),dp(16));
        LinearLayout header=new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL);
        MaterialButton prev=new MaterialButton(c,null,com.google.android.material.R.attr.borderlessButtonStyle);prev.setText("‹");prev.setTextSize(28);prev.setContentDescription("Mese precedente");prev.setPadding(0,0,0,0); header.addView(prev,new LayoutParams(dp(48),dp(56)));
        title=new TextView(c);title.setTextSize(20);title.setGravity(Gravity.CENTER);header.addView(title,new LayoutParams(0,-2,1));
        MaterialButton next=new MaterialButton(c,null,com.google.android.material.R.attr.borderlessButtonStyle);next.setText("›");next.setTextSize(28);next.setContentDescription("Mese successivo");next.setPadding(0,0,0,0);header.addView(next,new LayoutParams(dp(48),dp(56)));addView(header);
        prev.setOnClickListener(v -> { displayed=displayed.minusMonths(1);render(); });next.setOnClickListener(v -> { displayed=displayed.plusMonths(1);render(); });
        LinearLayout weekdays=new LinearLayout(c);for(String day:new String[]{"L","M","M","G","V","S","D"}) { TextView text=new TextView(c);text.setText(day);text.setTextSize(13);text.setGravity(Gravity.CENTER);text.setTextColor(c.getColor(R.color.sf_muted));weekdays.addView(text,new LayoutParams(0,dp(32),1)); }addView(weekdays);
        grid=new LinearLayout(c);grid.setOrientation(VERTICAL);addView(grid,new LayoutParams(-1,-2));
        LinearLayout footer=new LinearLayout(c);footer.setGravity(Gravity.CENTER_VERTICAL);footer.setPadding(dp(8),dp(8),dp(8),0);
        summary=new TextView(c);summary.setTextSize(13);footer.addView(summary,new LayoutParams(0,-2,1));
        MaterialButton today=new MaterialButton(c,null,com.google.android.material.R.attr.borderlessButtonStyle);today.setText("Oggi");footer.addView(today,new LayoutParams(-2,dp(48)));today.setOnClickListener(v -> choose(LocalDate.now()));addView(footer);
        TextView legend=new TextView(c);android.text.SpannableString legendText=new android.text.SpannableString("● Attività   • Eventi   ○ Completate");
        legendText.setSpan(new android.text.style.ForegroundColorSpan(c.getColor(R.color.sf_primary)),0,1,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        legendText.setSpan(new android.text.style.ForegroundColorSpan(c.getColor(R.color.calendar_event)),13,14,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);legend.setText(legendText);legend.setTextColor(c.getColor(R.color.sf_muted));legend.setTextSize(12);legend.setPadding(dp(8),dp(4),dp(8),0);addView(legend);
        render();
    }
    public void setListener(Listener value) { listener=value; }
    public void setDate(LocalDate date) { selected=date;displayed=YearMonth.from(date);render(); }
    public void setDisplayedMonth(YearMonth month) { displayed=month;render(); }
    public YearMonth getDisplayedMonth() { return displayed; }
    public void setItems(List<Task> tasks,List<CalendarEvent> events) { marks=CalendarIndex.build(tasks,events);render(); }
    private void choose(LocalDate date) { setDate(date);if(listener!=null)listener.selected(date); }
    private GradientDrawable circle(int color) { GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);return d; }
    private void addWeek(LinearLayout week) { grid.addView(week,new LayoutParams(-1,dp(60))); }
    private void render() {
        Context c=getContext();title.setText(displayed.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.ITALIAN)));grid.removeAllViews();
        int totalTasks=0,totalEvents=0;for(Map.Entry<LocalDate,CalendarIndex.Day> entry:marks.entrySet()) if(YearMonth.from(entry.getKey()).equals(displayed)) { totalTasks+=entry.getValue().tasks;totalEvents+=entry.getValue().events; }
        summary.setText(totalTasks+" attività · "+totalEvents+" eventi");
        LinearLayout week=null; int dayIndex=0;
        for(LocalDate date:CalendarIndex.grid(displayed)) {
            if(dayIndex%7==0) { week=new LinearLayout(c); week.setOrientation(HORIZONTAL); addWeek(week); }
            dayIndex++;
            FrameLayout cell=new FrameLayout(c);cell.setMinimumHeight(dp(60));cell.setFocusable(true);cell.setClickable(true);
            cell.setLayoutParams(new LayoutParams(0,dp(60),1));
            FrameLayout plate=new FrameLayout(c);FrameLayout.LayoutParams plateParams=new FrameLayout.LayoutParams(dp(36),dp(36),Gravity.TOP|Gravity.CENTER_HORIZONTAL);plateParams.topMargin=dp(6);cell.addView(plate,plateParams);
            boolean inMonth=YearMonth.from(date).equals(displayed),picked=date.equals(selected),today=date.equals(LocalDate.now());
            if(picked) { GradientDrawable bg=circle(c.getColor(R.color.sf_accent));plate.setBackground(bg); }
            else if(today) { GradientDrawable bg=circle(android.graphics.Color.TRANSPARENT);bg.setStroke(dp(1),c.getColor(R.color.sf_primary));plate.setBackground(bg); }
            TextView number=new TextView(c);number.setGravity(Gravity.CENTER);number.setIncludeFontPadding(false);number.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);number.setText(String.valueOf(date.getDayOfMonth()));number.setTextSize(16);number.setTextColor(c.getColor(picked||inMonth ? R.color.sf_text : R.color.sf_muted));if(today)number.setTypeface(null,Typeface.BOLD);plate.addView(number,new FrameLayout.LayoutParams(-1,-1));
            LinearLayout dots=new LinearLayout(c);dots.setGravity(Gravity.CENTER);dots.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);CalendarIndex.Day items=marks.get(date);
            if(items!=null) {
                if(items.tasks>0) { View dot=new View(c);GradientDrawable d=circle(c.getColor(items.done==items.tasks ? R.color.sf_muted : R.color.sf_primary));if(items.done==items.tasks) {d.setColor(android.graphics.Color.TRANSPARENT);d.setStroke(dp(1),c.getColor(R.color.sf_muted));}dot.setBackground(d);LayoutParams lp=new LayoutParams(dp(6),dp(6));lp.setMargins(dp(2),0,dp(2),0);dots.addView(dot,lp); }
                if(items.events>0) { View dot=new View(c);dot.setBackground(circle(c.getColor(R.color.calendar_event)));LayoutParams lp=new LayoutParams(dp(4),dp(4));lp.setMargins(dp(2),0,dp(2),0);dots.addView(dot,lp); }
            }
            FrameLayout.LayoutParams dotParams=new FrameLayout.LayoutParams(-1,dp(10),Gravity.TOP);dotParams.topMargin=dp(44);cell.addView(dots,dotParams);
            cell.setSelected(picked);cell.setAlpha(inMonth||picked ? 1f : .5f);
            cell.setContentDescription(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy",Locale.ITALIAN))+(picked ? ", selezionato" : "")+(items==null ? ", nessun impegno" : ", "+items.tasks+" attività, "+items.done+" completate, "+items.events+" eventi"));
            cell.setOnClickListener(v -> choose(date));week.addView(cell);
        }
    }
}
