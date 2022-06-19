package com.example.meiyou.activity;

import static com.example.meiyou.model.PostList.MODE_SEARCH;
import static com.example.meiyou.model.PostList.MODE_SINGLE_POST;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivitySearchPostBinding;
import com.example.meiyou.databinding.ActivitySinglePostBinding;
import com.example.meiyou.fragment.PostListFragment;

import java.io.Serializable;

public class SearchPostActivity extends AppCompatActivity {

    ActivitySearchPostBinding binding;

    public static final String PARAM_ALL = "com.Meiyou.search_all";

    PostListFragment postListFragment;

    public static class SearchParam implements Serializable {
        public String pattern_all, pattern_title, pattern_content,  pattern_user;
        public boolean if_text, if_image, if_video, if_audio;
        public SearchParam(){
            if_text = if_image = if_video = if_audio = true;
        }
    }
    SearchParam searchParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivitySearchPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchParam = (SearchParam) getIntent().getSerializableExtra(PARAM_ALL);
        if(searchParam == null){
            Log.d("Search", "onCreate: No Param");
            finish();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        postListFragment = new PostListFragment(MODE_SEARCH);
        postListFragment.setSearchParam(searchParam);
        ft.replace(R.id.fragmentPostList, postListFragment);
        ft.commit();

        binding.buttonReturnSearch.setOnClickListener(view -> finish());

    }
}