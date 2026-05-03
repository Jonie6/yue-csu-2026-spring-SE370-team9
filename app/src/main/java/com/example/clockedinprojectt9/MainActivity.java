package com.example.clockedinprojectt9;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.clockedinprojectt9.databinding.ActivityMainBinding;
import com.example.clockedinprojectt9.fragments.DashboardFragment;
import com.example.clockedinprojectt9.fragments.DiscoverFragment;
import com.example.clockedinprojectt9.fragments.FriendsFragment;
import com.example.clockedinprojectt9.fragments.MessagesFragment;
import com.example.clockedinprojectt9.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (itemId == R.id.nav_discover) {
                loadFragment(new DiscoverFragment());
                return true;
            } else if (itemId == R.id.nav_social) {
                loadFragment(new FriendsFragment());
                return true;
            } else if (itemId == R.id.nav_messages) {
                loadFragment(new MessagesFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        binding.fabCreateEvent.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateEventActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
