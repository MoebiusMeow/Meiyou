package com.example.meiyou.activity;

import static com.example.meiyou.model.PostList.MODE_SINGLE_POST;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivitySinglePostBinding;
import com.example.meiyou.fragment.PostListFragment;

public class SinglePostActivity extends AppCompatActivity {

    ActivitySinglePostBinding binding ;

    PostListFragment postListFragment;

    private int pid;

    public static final String EXTRA_PID = "con.example.Meiyou.SinglePost.pid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivitySinglePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        pid = Integer.valueOf(intent.getStringExtra(EXTRA_PID));
        if(pid <=0){
            setResult(RESULT_CANCELED);
            finish();
        }
        binding.textViewPid.setText("#"+pid);
        Log.d("SINGLE-POST", "onCreate: pid="+pid);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        postListFragment = new PostListFragment(MODE_SINGLE_POST);
        postListFragment.setPostID(pid);
        ft.replace(R.id.fragmentUser, postListFragment);
        ft.commit();

        binding.buttonReturnSinglePost.setOnClickListener(view->{
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}