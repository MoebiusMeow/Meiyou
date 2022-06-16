package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.R;
import com.example.meiyou.activity.EditUserInfoActivity;
import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentUserBinding;
import com.example.meiyou.model.MainUser;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.PostList;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;

public class UserFragment extends Fragment {
    FragmentUserBinding binding;
    protected PostViewAdapter mAdapter;
    protected final PostList postListModel = new PostList();
    protected int user_id = -1;
    protected final int N_POST_GET = 20;
    public UserFragment(){

    }

    private ActivityResultLauncher<Intent> activityEditInfoLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if(user_id < 0) this.user_id = GlobalData.getUser().uid;
        postListModel.setFixUser(this.user_id);


        RecyclerView mRecyclerView = binding.recycleViewUser;
        mAdapter = new PostViewAdapter(this.getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //if(true)return view;

        activityEditInfoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        Log.d("TAG", "onCreateView: refresh userinfo");
                        refreshUserData();
                    }
                });


        postListModel.status.observe(getViewLifecycleOwner(), status -> {
            binding.progressBar4.setVisibility(View.INVISIBLE);
            if(status == NetworkBasic.Status.fail ){
                Toast.makeText(getActivity(), "糟糕，网络好像不太通畅..", Toast.LENGTH_SHORT).show();
            }
            if(status == NetworkBasic.Status.wrong){
                Toast.makeText(getActivity(), "发生了一些小错误...", Toast.LENGTH_SHORT).show();
            }
            if(status == NetworkBasic.Status.success){
                for(int i = postListModel.new_start; i< postListModel.len(); i++){
                    Post post = postListModel.get(i);
                    mAdapter.addPost(PostViewAdapter.PostInfo.fromPost(post));

                    // Download user profile
                    if(post.profile_id >= 0) {
                        GlobalResFileManager.requestFile(getViewLifecycleOwner(), post.profile_id, uri -> {
                            post.userProfileUri = uri;
                            Log.d("TAG", "onCreateView: put post profile"+uri.getPath());
                            mAdapter.notifyDataSetChanged();
                        });
                    }
                }
                boolean ifNoMore = (postListModel.len() - postListModel.new_start) != N_POST_GET;
                mAdapter.changeTail(ifNoMore);
            }
        });


        GlobalData.getUser().userprofile.observe(getViewLifecycleOwner(), uri -> {
            if (uri == null) {
                binding.imageUserUserProfile.setImageResource(R.drawable.user_profile_default);
            } else {
                Log.d("TAG", "onCreateView: put profile"+uri.getPath());
                Drawable drawable = Drawable.createFromPath(uri.getPath());
                binding.imageUserUserProfile.setImageDrawable(drawable);
            }
        });

        GlobalData.getUser().status.observe(getViewLifecycleOwner(), status -> {
            if(status == NetworkBasic.Status.success){
                Log.d("TAG", "onCreateView: bind ui user info");
                MainUser user = GlobalData.getUser();
                binding.textUserpageName.setText(user.username);
                binding.textUserIntro.setText(user.signature);
                binding.textUserpageEmail.setText(user.email);
                user.requestProfile(getViewLifecycleOwner());
            }
        });

        binding.buttonEditProfile.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), EditUserInfoActivity.class);
            activityEditInfoLauncher.launch(intent);
        });

        mAdapter.setOnLoadMoreAction(()->load());

        refreshUserData();

        return view;
    }

    public void refreshUserData(){
        GlobalData.getUser().requestInfo();
        refresh();
    }

    public void load(){
        postListModel.pull_post(N_POST_GET, PostList.MODE_USER_FIX, false);
    }

    public void refresh(){
        mAdapter.clear();
        postListModel.clear();
        postListModel.pull_post(N_POST_GET, PostList.MODE_USER_FIX, true);
    }

    public void setUserID(int user_id){
        this.user_id = user_id;
    }
}