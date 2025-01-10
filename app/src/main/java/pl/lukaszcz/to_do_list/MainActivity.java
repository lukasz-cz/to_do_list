package pl.lukaszcz.to_do_list;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import pl.lukaszcz.to_do_list.adapter.TaskAdapter;
import pl.lukaszcz.to_do_list.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private static final String FILE_NAME = "tasks.json";
    private static final String CHANNEL_ID = "TASK_REMINDERS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        fabAddTask.setOnClickListener(v -> showAddEditTaskDialog(null));

        importTasksFromFile();

        createNotificationChannel();
        if (hasNotificationPermission()) {
            checkAndNotifyTasks();
        } else {
            requestNotificationPermission();
        }
    }
    private void exportTasksToFile() {
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            Gson gson = new Gson();
            String json = gson.toJson(taskList);
            writer.write(json);
            Toast.makeText(this, "Tasks exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importTasksFromFile() {
        try (FileInputStream fis = openFileInput(FILE_NAME);
             InputStreamReader reader = new InputStreamReader(fis);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            Gson gson = new Gson();
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();
            taskList = gson.fromJson(bufferedReader, taskListType);
            if (taskList == null) {
                taskList = new ArrayList<>();
            }
            taskAdapter.updateTasks(taskList);
            Toast.makeText(this, "Tasks imported successfully!", Toast.LENGTH_SHORT).show();

            if (hasNotificationPermission()) {
                checkAndNotifyTasks();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "No file to import!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                checkAndNotifyTasks();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Reminders for tasks with today's or overdue deadlines.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    private void checkAndNotifyTasks() {
        long now = System.currentTimeMillis();
        for (Task task : taskList) {
            if (!task.isCompleted() && task.getDeadline() > 0 && task.getDeadline() <= now) {
                String message = "Task: " + task.getTitle() + " is due or overdue!";
                sendNotification(message);
            }
        }
    }
    private void showAddEditTaskDialog(@Nullable Task task) {
        AddEditTaskDialogFragment dialogFragment = AddEditTaskDialogFragment.newInstance(task);
        dialogFragment.setOnTaskSavedListener(newTask -> {
            if (task == null) {
                taskList.add(newTask);
            } else {
                int position = taskList.indexOf(task);
                taskList.set(position, newTask);
            }
            sortTasksByDeadline();
            taskAdapter.notifyItemRangeChanged(0, taskAdapter.getItemCount());
        });
        dialogFragment.show(getSupportFragmentManager(), "AddEditTaskDialogFragment");
    }

    @Override
    public void onTaskCompletedChanged(Task task) {
        // empty
    }

    @Override
    public void onEditTask(Task task) {
        showAddEditTaskDialog(task);
    }

    @Override
    public void onDeleteTask(Task task) {
        int position = taskList.indexOf(task);
        taskList.remove(task);
        taskAdapter.notifyItemRemoved(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_by_name) {
            sortTasksByName();
            return true;
        } else if (id == R.id.sort_by_priority) {
            sortTasksByPriority();
            return true;
        } else if (id == R.id.sort_by_status) {
            sortTasksByStatus();
            return true;
        } else if (id == R.id.sort_by_deadline) {
            sortTasksByDeadline();
            return true;
        } else if (id == R.id.export_tasks) {
            exportTasksToFile();
            return true;
        } else if (id == R.id.import_tasks) {
            importTasksFromFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortTasksByName() {
        taskList.sort(Comparator.comparing(Task::getTitle));
        taskAdapter.updateTasks(taskList);
    }

    private void sortTasksByPriority() {
        taskList.sort(Comparator.comparingInt(Task::getPriority));
        taskAdapter.updateTasks(taskList);
    }

    private void sortTasksByStatus() {
        taskList.sort(Comparator.comparing(Task::isCompleted));
        taskAdapter.updateTasks(taskList);
    }

    private void sortTasksByDeadline() {
        taskList.sort(Comparator.comparingLong(Task::getDeadline));
        taskAdapter.updateTasks(taskList);
    }

}
