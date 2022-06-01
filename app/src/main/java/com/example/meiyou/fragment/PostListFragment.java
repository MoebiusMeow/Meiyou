package com.example.meiyou.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentPostlistBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.PostList;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;

public class PostListFragment extends Fragment {
    private FragmentPostlistBinding binding;
    private RecyclerView mRecyclerView;
    private PostViewAdapter mAdapter;

    private PostList postListModel = new PostList();
    private static final int N_POST_GET = 20;
    public int mode;

    public PostListFragment(int _mode){
        mode = _mode;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mRecyclerView = binding.recycleView;
        mAdapter = new PostViewAdapter(this.getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // on Post list get
        postListModel.status.observe(getViewLifecycleOwner(), new Observer<NetworkBasic.Status>() {
            @Override
            public void onChanged(NetworkBasic.Status status) {
                binding.progressBar2.setVisibility(View.INVISIBLE);
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
                                mAdapter.notifyDataSetChanged();
                                Log.d("Image", "onChanged: " + uri.toString());
                            });
                        }
                    }
                    boolean ifNoMore = (postListModel.len() - postListModel.new_start) != N_POST_GET;
                    mAdapter.changeTail(ifNoMore);
                }
            }
        });

        mAdapter.setOnLoadMoreAction(new PostViewAdapter.LoadMoreAction() {
            @Override
            public void Onclick() {
                load();
            }
        });

        fresh();

        return view;
    }

    public void load(){
        binding.progressBar2.setVisibility(View.VISIBLE);
        postListModel.pull_post(N_POST_GET, mode, false);
    }

    public void fresh(){
        mAdapter.clear();
        postListModel.clear();
        binding.progressBar2.setVisibility(View.VISIBLE);
        postListModel.pull_post(N_POST_GET, mode, true);
    }
}
