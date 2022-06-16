package com.example.meiyou.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.meiyou.databinding.ActivityMainBinding;
import com.example.meiyou.databinding.ActivitySinglePostBinding;

public class SinglePostActivity extends AppCompatActivity {

    ActivitySinglePostBinding binding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivitySinglePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}