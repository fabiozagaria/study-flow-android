package it.studyflow.app.study.ui;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.studyflow.app.R;
import it.studyflow.app.study.data.TopicRow;
import java.util.*;
import java.util.function.Consumer;

final class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.Holder> {
  private List<TopicRow> rows = Collections.emptyList();
  private final Consumer<TopicRow> select;

  TopicAdapter(Consumer<TopicRow> select) {
    this.select = select;
  }

  void submit(List<TopicRow> values) {
    rows = values;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
    return new Holder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.row_study_topic, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Holder h, int position) {
    TopicRow t = rows.get(position);
    h.category.setText(t.subjectTitle + " · " + t.category);
    h.title.setText(t.title);
    h.info.setText(
        t.quizCount
            + " quiz · "
            + t.recallCount
            + " ripassi"
            + (t.readAt == null ? "" : " · Teoria letta"));
    h.itemView.setOnClickListener(v -> select.accept(t));
  }

  @Override
  public int getItemCount() {
    return rows.size();
  }

  static final class Holder extends RecyclerView.ViewHolder {
    final TextView category, title, info;

    Holder(View v) {
      super(v);
      category = v.findViewById(R.id.topicCategory);
      title = v.findViewById(R.id.topicTitle);
      info = v.findViewById(R.id.topicInfo);
    }
  }
}
