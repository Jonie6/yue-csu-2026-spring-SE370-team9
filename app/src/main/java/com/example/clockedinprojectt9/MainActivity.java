package com.example.clockedinprojectt9;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clockedinprojectt9.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        
        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        binding.titleText.setText("cLockedIn");
        binding.subtitleText.setText("Productivity rebuilt");

        // Navigate to FriendsActivity when the button is clicked
        binding.friendsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(v -> {
            new com.example.clockedinprojectt9.utils.SessionManager(MainActivity.this).logoutUser();
            android.content.Intent intent = new android.content.Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
