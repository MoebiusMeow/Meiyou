package com.example.meiyou.activity;

import static com.example.meiyou.model.PostList.MODE_SINGLE_POST;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private ActivityResultLauncher<Intent> activityNewReplyLauncher;

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
        Log.d("POSTPID", "onCreate: pid="+pid);
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
        postListFragment.setOnRenewCallback(count -> {
            Log.d("Change", "onCreate: "+count);
            if(count <=0 ){
                finish();
            }
        });
        ft.replace(R.id.fragmentUser, postListFragment);
        ft.commit();

        activityNewReplyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    postListFragment.refresh();
                });

        binding.buttonReturnSinglePost.setOnClickListener(view->{
            binding.fragmentUser.removeAllViews();
            setResult(RESULT_CANCELED);
            finish();
        });

        binding.constrainNewReply.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, NewContentActivity.class);
            intent1.putExtra(NewContentActivity.POST_CONTENT_PID, pid);
            Log.d("Reply", "onCreate: "+pid);
            activityNewReplyLauncher.launch(intent1);
        });

        binding.buttonshare.setOnClickListener(view -> {
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            String title = "标题";
            String extraText="[草莓波球论坛]"+postListFragment.getAbstract();
            share.putExtra(Intent.EXTRA_TEXT, extraText);
            if (title != null) {
                share.putExtra(Intent.EXTRA_SUBJECT, title);
            }
            startActivity(Intent.createChooser(share, "分享一下"));
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}