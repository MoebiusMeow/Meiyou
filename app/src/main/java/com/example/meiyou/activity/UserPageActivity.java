package com.example.meiyou.activity;

import static com.example.meiyou.model.PostList.MODE_USER_FIX;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivitySinglePostBinding;
import com.example.meiyou.databinding.ActivityUserPageBinding;
import com.example.meiyou.fragment.PostListFragment;
import com.example.meiyou.fragment.UserFragment;
import com.example.meiyou.model.User;

public class UserPageActivity extends AppCompatActivity {

    private ActivityUserPageBinding binding;
    private UserFragment userFragment = null;

    int uid = 0;

    public static final String USER_UID = "com.Meiyou.activityUserPage.uid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityUserPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = getIntent().getIntExtra(USER_UID, 0);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        userFragment = new UserFragment(uid);
        ft.replace(R.id.fragmentUserInActivity, userFragment);
        ft.commit();
    }
}