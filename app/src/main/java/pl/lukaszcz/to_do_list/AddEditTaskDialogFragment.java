package pl.lukaszcz.to_do_list;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import pl.lukaszcz.to_do_list.model.Task;
import android.app.DatePickerDialog;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskDialogFragment extends DialogFragment {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDeadline;
    private final Calendar selectedDate = Calendar.getInstance();
    private Task task;
    private OnTaskSavedListener listener;
    private Spinner spinnerPriority;

    public static AddEditTaskDialogFragment newInstance(Task task) {
        AddEditTaskDialogFragment fragment = new AddEditTaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_edit_task, container, false);

        editTextTitle = view.findViewById(R.id.editTextTaskTitle);
        editTextDescription = view.findViewById(R.id.editTextTaskDescription);
        textViewDeadline = view.findViewById(R.id.textViewDeadline);
        Button buttonSetDeadline = view.findViewById(R.id.buttonSetDeadline);
        Button buttonSave = view.findViewById(R.id.buttonSaveTask);
        spinnerPriority = view.findViewById(R.id.spinnerTaskPriority);

        if (getArguments() != null) {
            task = (Task) getArguments().getSerializable("task");
            if (task != null) {
                editTextTitle.setText(task.getTitle());
                editTextDescription.setText(task.getDescription());
                spinnerPriority.setSelection(task.getPriority() - 1);

                if (task != null && task.getDeadline() != 0) {
                    selectedDate.setTimeInMillis(task.getDeadline());
                    updateDeadlineText();
                }
            }
        }

        buttonSetDeadline.setOnClickListener(v -> showDatePickerDialog());

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                editTextTitle.setError("Title cannot be empty");
                return;
            }

            if (task == null) {
                task = new Task(title, description, false, spinnerPriority.getSelectedItemPosition() + 1, selectedDate.getTimeInMillis());
            } else {
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(spinnerPriority.getSelectedItemPosition() + 1);
                task.setDeadline(selectedDate.getTimeInMillis());
            }

            if (listener != null) {
                listener.onTaskSaved(task);
            }

            dismiss();
        });

        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDeadlineText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set the dialog width to match the screen width
        if (getDialog() != null && getDialog().getWindow() != null) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setAttributes(params);
        }
    }

    private void updateDeadlineText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textViewDeadline.setText(dateFormat.format(selectedDate.getTime()));
    }

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }
}
