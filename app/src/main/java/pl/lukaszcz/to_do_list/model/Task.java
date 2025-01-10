package pl.lukaszcz.to_do_list.model;

import java.io.Serializable;

public class Task implements Serializable {

    private String title;
    private String description;
    private boolean isCompleted;
    private int priority; // 1 = High, 2 = Medium, 3 = Low
    private long deadline;

    public Task(String title, String description, boolean isCompleted, int priority, long deadline) {
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.deadline = deadline;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
