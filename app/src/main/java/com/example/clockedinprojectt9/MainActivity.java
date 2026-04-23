package com.example.clockedinprojectt9;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clockedinprojectt9.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.titleText.setText("cLockedInProjectT9");
        binding.subtitleText.setText("Java + XML base project is working");

        binding.actionButton.setOnClickListener(v ->
                binding.statusText.setText("MainActivity button works")
        );

        binding.logoutButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
