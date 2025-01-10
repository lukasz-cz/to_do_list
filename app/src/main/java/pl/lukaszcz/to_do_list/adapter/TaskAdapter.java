package pl.lukaszcz.to_do_list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import pl.lukaszcz.to_do_list.R;
import pl.lukaszcz.to_do_list.model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final OnTaskActionListener actionListener;

    public TaskAdapter(List<Task> taskList, OnTaskActionListener actionListener) {
        this.taskList = taskList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.checkBox.setChecked(task.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            actionListener.onTaskCompletedChanged(task);
        });

        holder.editButton.setOnClickListener(v -> actionListener.onEditTask(task));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteTask(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        CheckBox checkBox;
        ImageButton editButton;
        ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTaskTitle);
            checkBox = itemView.findViewById(R.id.checkboxTask);
            editButton = itemView.findViewById(R.id.buttonEditTask);
            deleteButton = itemView.findViewById(R.id.buttonDeleteTask);
        }
    }

    public interface OnTaskActionListener {
        void onTaskCompletedChanged(Task task);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    public void updateTasks(List<Task> newTaskList) {
        this.taskList = newTaskList;
        this.notifyItemRangeChanged(0, this.getItemCount());
    }
}
