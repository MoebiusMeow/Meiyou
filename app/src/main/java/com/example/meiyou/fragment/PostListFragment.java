package com.example.meiyou.fragment;

import static com.example.meiyou.utils.GlobalData.FILE_TYPE_NONE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentPostlistBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.PostList;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;

import java.util.ArrayList;

public class PostListFragment extends Fragment {
    protected FragmentPostlistBinding binding;
    protected PostViewAdapter mAdapter;

    protected final PostList postListModel = new PostList();
    protected static final int N_POST_GET = 20;
    public int mode;

    public PostListFragment(int _mode){
        mode = _mode;
    }
    public PostListFragment(){
        mode = 4;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        RecyclerView mRecyclerView = binding.recycleView;
        mAdapter = new PostViewAdapter(this.getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // on Post list get
        postListModel.status.observe(getViewLifecycleOwner(), status -> {
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

                    // Download attatchment
                    if(post.res_type != FILE_TYPE_NONE){
                        ArrayList<Integer> res_ids = post.res_ids;
                        post.res_uri_list.clear();
                        for(int res_id:res_ids) post.res_uri_list.add(null);
                        for (int j=0; j<post.res_ids.size(); j++) {
                            int res_id = res_ids.get(j);
                            int finalJ = j;
                            GlobalResFileManager.requestFile(getViewLifecycleOwner(), res_id, uri -> {
                                post.res_uri_list.set(finalJ, uri);
                                mAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                }
                boolean ifNoMore = (postListModel.len() - postListModel.new_start) != N_POST_GET;
                mAdapter.changeTail(ifNoMore);
            }
        });

        mAdapter.setOnLoadMoreAction(() -> load());

        refresh();

        return view;
    }

    public void load(){
        binding.progressBar2.setVisibility(View.VISIBLE);
        postListModel.pull_post(N_POST_GET, mode, false);
    }

    public void refresh(){
        mAdapter.clear();
        postListModel.clear();
        binding.progressBar2.setVisibility(View.VISIBLE);
        postListModel.pull_post(N_POST_GET, mode, true);
    }

    public void setUserID(int user_id){
        postListModel.setFixUser(user_id);
    }
}
