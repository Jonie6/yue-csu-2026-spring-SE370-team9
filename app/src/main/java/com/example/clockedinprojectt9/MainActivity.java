package com.example.clockedinprojectt9;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clockedinprojectt9.databinding.ActivityMainBinding;
//import com.example.clockedinprojectt9.utils.DatabaseTestUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        
        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // Seed test data
        //DatabaseTestUtils.seedTestData(this);

        // Navigate to FriendsActivity when the button is clicked
        binding.friendsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
        });

        // Placeholder listeners for new buttons
        binding.createButton.setOnClickListener(v -> {
            // TODO: Implement Create functionality
        });

        binding.msgButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MainActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        binding.menuButton.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(MainActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_profile) {
                    android.widget.Toast.makeText(this, "Profile clicked", android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    new com.example.clockedinprojectt9.utils.SessionManager(MainActivity.this).logoutUser();
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }
}
