package com.example.clockedinprojectt9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clockedinprojectt9.databinding.ActivityRegisterBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AppDataBase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDataBase.getDatabase(this);

        binding.registerButton.setOnClickListener(v -> registerUser());

        binding.loginTextView.setOnClickListener(v -> {
            finish(); // Go back to LoginActivity
        });
    }

    private void registerUser() {
        String username = binding.regUsernameEditText.getText().toString().trim();
        String email = binding.regEmailEditText.getText().toString().trim();
        String password = binding.regPasswordEditText.getText().toString().trim();
        String confirmPassword = binding.regConfirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User existingUser = db.userDao().getUserByUsername(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show());
                return;
            }

            User newUser = new User(
                    username,
                    email,
                    password, // Should be hashed in a real app
                    username, // Display name defaults to username
                    "",       // bio
                    "",       // profileImageUri
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    true
            );

            db.userDao().insert(newUser);

            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                finish(); // Go back to login
            });
        });
    }
}
