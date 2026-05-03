package com.example.clockedinprojectt9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clockedinprojectt9.databinding.ActivityLoginBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Friendship;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.PasswordUtils;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDataBase db;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDataBase.getDatabase(this);

        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.demoModeButton.setOnClickListener(v -> setupDemoMode());

        binding.registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = binding.usernameEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = db.userDao().getUserByUsername(username);
            runOnUiThread(() -> {
                if (user != null && PasswordUtils.checkPassword(password, user.getPasswordHash())) {
                    // Successful login
                    sessionManager.createLoginSession(user.getUserId());
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupDemoMode() {
        Toast.makeText(this, "Seeding demo data...", Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            // Wipe existing data
            db.clearAllTables();

            String hashedPw = PasswordUtils.hashPassword("password123");
            long now = System.currentTimeMillis();

            java.util.List<Long> demoUserIds = new java.util.ArrayList<>();
            java.util.List<Long> demoEventIds = new java.util.ArrayList<>();

            // Create Admin User
            User admin = new User("admin", "admin@example.com", hashedPw, "Admin User", "System Administrator", null, now, now, true);
            long adminId = db.userDao().insert(admin);

            // Create Demo Users and Events
            String[][] demoUserData = {
                    {"adam_m", "Adam Majed", "Software Engineer"},
                    {"jonas_g", "Jonas Graham", "Coffee enthusiast"},
                    {"luke_s", "Luke Schavel", "Board game fan"},
                    {"ryland_c", "Ryland Cummings", "Fitness coach"},
                    {"tikhon_p", "Tikhon Peterson", "Movie buff"}
            };

            for (String[] userData : demoUserData) {
                User demoUser = new User(userData[0], userData[0] + "@example.com", hashedPw, userData[1], userData[2], null, now, now, true);
                long demoUserId = db.userDao().insert(demoUser);
                demoUserIds.add(demoUserId);

                // Create Friendship
                db.friendshipDao().sendFriendRequest(new Friendship(adminId, demoUserId, adminId, "ACCEPTED", now));

                // Create Demo Event for Discovery (Ensure start/end times are valid)
                com.example.clockedinprojectt9.models.Event demoEvent = new com.example.clockedinprojectt9.models.Event(
                        userData[1] + "'s Event",
                        "Demo event at " + userData[1] + "'s place.",
                        "Demo City, St. " + (int)(Math.random() * 100),
                        now + 3600000,          // starts in 1 hour
                        now + 7200000,          // ends in 2 hours
                        demoUserId,
                        "Public",
                        20,
                        false,
                        now,
                        now
                );
                long eventId = db.eventDao().insert(demoEvent);
                demoEventIds.add(eventId);
            }

            // Randomize RSVPs for demo
            for (Long eventId : demoEventIds) {
                // Each event gets 1-3 random attendees
                int numAttendees = (int)(Math.random() * 3) + 1;
                java.util.Collections.shuffle(demoUserIds);
                for (int i = 0; i < Math.min(numAttendees, demoUserIds.size()); i++) {
                    long attendeeId = demoUserIds.get(i);
                    db.rsvpDao().insert(new com.example.clockedinprojectt9.models.RSVP(eventId, attendeeId, "Interested", now, now));
                    
                    // Add a welcome message to the group chat
                    db.messageDao().insert(new com.example.clockedinprojectt9.models.Message(attendeeId, null, eventId, "I'm coming!", now));
                }
                
                // Also add admin to some events
                if (Math.random() > 0.5) {
                    db.rsvpDao().insert(new com.example.clockedinprojectt9.models.RSVP(eventId, adminId, "Interested", now, now));
                    db.messageDao().insert(new com.example.clockedinprojectt9.models.Message(adminId, null, eventId, "Looking forward to this event!", now));
                }
            }

            runOnUiThread(() -> {
                sessionManager.createLoginSession(adminId);
                Toast.makeText(LoginActivity.this, "Demo Mode Active", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        });
    }
}
