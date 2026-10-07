package it.studyflow.app;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.Holder> {
    public interface Actions { void toggle(Task t, boolean done); void edit(Task t); }
    private List<Task> items = new ArrayList<>();
    private final Actions actions;
    public TaskAdapter(Actions actions) { this.actions = actions; }
    public void submit(List<Task> tasks) { items = new ArrayList<>(tasks); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_task, parent, false)); }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Task t = items.get(position); h.title.setText(t.title);
        h.detail.setText(t.subject + "\n" + java.time.LocalDate.parse(t.due).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)) + " · " + t.dueTime + " · " + new String[]{"Bassa", "Media", "Alta"}[t.priority]);
        h.done.setOnCheckedChangeListener(null); h.done.setChecked(t.done);
        h.done.setOnCheckedChangeListener((button, checked) -> actions.toggle(t, checked));
        h.itemView.setOnClickListener(v -> actions.edit(t)); h.itemView.setAlpha(t.done ? 0.6f : 1f);
    }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, detail; final CheckBox done;
        Holder(View v) { super(v); title = v.findViewById(R.id.title); detail = v.findViewById(R.id.detail); done = v.findViewById(R.id.done); }
    }
}
