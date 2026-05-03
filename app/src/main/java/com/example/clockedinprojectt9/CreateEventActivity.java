package com.example.clockedinprojectt9;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clockedinprojectt9.databinding.ActivityCreateEventBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    private ActivityCreateEventBinding binding;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private long eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        if (eventId != -1) {
            binding.txtTitleHeader.setText("Edit Event");
            loadEventData();
        }

        setupVisibilitySpinner();
        
        binding.btnPickStart.setOnClickListener(v -> pickDateTime(startCalendar, true));
        binding.btnPickEnd.setOnClickListener(v -> pickDateTime(endCalendar, false));
        
        binding.btnCancelCreation.setOnClickListener(v -> finish());
        binding.btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void loadEventData() {
        executorService.execute(() -> {
            Event event = AppDataBase.getDatabase(this).eventDao().getEventById(eventId);
            if (event != null) {
                runOnUiThread(() -> {
                    binding.editTitle.setText(event.getTitle());
                    binding.editDescription.setText(event.getDescription());
                    binding.editLocation.setText(event.getLocation());
                    
                    startCalendar.setTimeInMillis(event.getStartTime());
                    endCalendar.setTimeInMillis(event.getEndTime());
                    updateLabel(startCalendar, true);
                    updateLabel(endCalendar, false);
                });
            }
        });
    }

    private void setupVisibilitySpinner() {
        String[] options = {"Public", "Friends Only", "Private"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVisibility.setAdapter(adapter);
    }

    private void pickDateTime(Calendar calendar, boolean isStart) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                updateLabel(calendar, isStart);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(Calendar calendar, boolean isStart) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        if (isStart) {
            binding.txtStartTime.setText(sdf.format(calendar.getTime()));
        } else {
            binding.txtEndTime.setText(sdf.format(calendar.getTime()));
        }
    }

    private void saveEvent() {
        String title = binding.editTitle.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String location = binding.editLocation.getText().toString().trim();
        String visibility = binding.spinnerVisibility.getSelectedItem().toString();

        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Title and Location are required", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = new SessionManager(this).getUserId();
        long now = System.currentTimeMillis();

        executorService.execute(() -> {
            if (eventId == -1) {
                Event event = new Event(
                        title, description, location,
                        startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis(),
                        userId, visibility, 0, false, now, now
                );
                AppDataBase.getDatabase(this).eventDao().insert(event);
            } else {
                Event event = AppDataBase.getDatabase(this).eventDao().getEventById(eventId);
                if (event != null) {
                    event.setTitle(title);
                    event.setDescription(description);
                    event.setLocation(location);
                    event.setStartTime(startCalendar.getTimeInMillis());
                    event.setEndTime(endCalendar.getTimeInMillis());
                    event.setVisibility(visibility);
                    event.setUpdatedAt(now);
                    AppDataBase.getDatabase(this).eventDao().update(event);
                }
            }
            
            runOnUiThread(() -> {
                Toast.makeText(CreateEventActivity.this, eventId == -1 ? "Event Created" : "Event Updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
