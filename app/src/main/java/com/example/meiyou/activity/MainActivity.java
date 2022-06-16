package com.example.meiyou.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivityMainBinding;
import com.example.meiyou.utils.GlobalData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = binding.navView;
        navView.setItemIconTintList(null);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_viewPost, R.id.navigation_newPost, R.id.navigation_message, R.id.navigation_mine)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        GlobalData.sig_post.observe(this, integer -> {
            if(integer == GlobalData.SIG_POST_SEND){
                switchToPostList(true);
            }
        });
    }

    public void switchToPostList(boolean ifFresh){
        View view = navView.findViewById(R.id.navigation_viewPost);
        view.performClick();
        GlobalData.sig_fresh.postValue(GlobalData.SIG_FRESH_IDLE);
    }

}