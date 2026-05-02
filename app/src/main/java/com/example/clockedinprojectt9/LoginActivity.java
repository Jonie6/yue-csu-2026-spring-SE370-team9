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
        executorService.execute(() -> {
            // Wipe existing data for a clean demo state
            db.clearAllTables();

            // Create Admin User
            User admin = new User(
                    "admin",
                    "admin@example.com",
                    PasswordUtils.hashPassword("password123"),
                    "Admin User",
                    "System Administrator",
                    null,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    true
            );
            long adminId = db.userDao().insert(admin);
            admin.setUserId(adminId);

            // Create Demo Users
            String[][] demoUserData = {
                    {"adam_m", "Adam Majed", "Software Engineer who loves hiking."},
                    {"jonas_g", "Jonas Graham", "Coffee enthusiast and local artist."},
                    {"luke_s", "Luke Schavel", "Always looking for new board game partners."},
                    {"ryland_c", "Ryland Cummings", "Fitness coach and marathon runner."},
                    {"tikhon_p", "Tikhon Peterson", "Movie buff and aspiring chef."}
            };

            for (String[] userData : demoUserData) {
                String username = userData[0];
                String displayName = userData[1];
                String bio = userData[2];

                User demoUser = new User(
                        username,
                        username + "@example.com",
                        PasswordUtils.hashPassword("password123"),
                        displayName,
                        bio,
                        null,
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        true
                );
                long demoUserId = db.userDao().insert(demoUser);

                // Create Friendship
                Friendship friendship = new Friendship(
                        adminId,
                        demoUserId,
                        adminId,
                        "ACCEPTED",
                        System.currentTimeMillis()
                );
                db.friendshipDao().sendFriendRequest(friendship);
            }

            final long finalAdminId = admin.getUserId();
            runOnUiThread(() -> {
                sessionManager.createLoginSession(finalAdminId);
                Toast.makeText(LoginActivity.this, "Demo Mode: Admin Logged In", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
}
