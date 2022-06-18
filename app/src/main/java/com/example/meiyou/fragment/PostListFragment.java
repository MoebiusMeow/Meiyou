package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_OK;
import static com.example.meiyou.model.Post.TYPE_HEAD_POST;
import static com.example.meiyou.model.PostList.MODE_SINGLE_POST;
import static com.example.meiyou.model.PostList.MODE_USER_FIX;
import static com.example.meiyou.utils.GlobalData.FILE_TYPE_NONE;
import static com.example.meiyou.utils.GlobalData.SIG_FORCE_REFRESH;
import static com.example.meiyou.utils.GlobalData.SIG_POST_DELETE;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.activity.SearchPostActivity;
import com.example.meiyou.activity.SinglePostActivity;
import com.example.meiyou.activity.UserPageActivity;
import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentPostlistBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.PostList;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

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

    private ActivityResultLauncher<Intent> activitySinglePostLauncher,
            activityUserPageLauncher;

    public interface OnRenewCallback{
        void onRenew(int count);
    }
    private OnRenewCallback onRenewCallback = count -> {
        Log.d("TAG", ": haode");
    };
    public void setOnRenewCallback(OnRenewCallback action){onRenewCallback = action;}
    public String getAbstract(){return postListModel.getAbstract();}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        RecyclerView mRecyclerView = binding.recycleView;
        mAdapter = new PostViewAdapter(this.getContext(), this.getViewLifecycleOwner());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.progressBar2.setVisibility(View.VISIBLE);

        activitySinglePostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                });

        activityUserPageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        GlobalData.sig_refresh.postValue(SIG_FORCE_REFRESH);
                    }
                });

        // on Post list get
        postListModel.status.observe(getViewLifecycleOwner(), status -> {

            binding.progressBar2.setVisibility(View.INVISIBLE);
            if(status == NetworkBasic.Status.fail ){
                Toast.makeText(getActivity(), "糟糕，网络好像不太通畅..", Toast.LENGTH_SHORT).show();
            }
            if(status == NetworkBasic.Status.wrong){
                if(postListModel.errorCode == 404 && mode == MODE_SINGLE_POST){
                    GlobalData.sig_post.postValue(SIG_POST_DELETE);
                    getActivity().finish();
                }
                Log.d("error", "onCreateView: error_code="+postListModel.errorCode);
                Toast.makeText(getActivity(), "发生了一些小错误...", Toast.LENGTH_SHORT).show();
            }

            if(status == NetworkBasic.Status.success){
                if(mode == MODE_SINGLE_POST )
                    Log.d("SINGLE-POST", "netreturn: newstart="+postListModel.new_start
                            +" end="+postListModel.len());
                for(int i = postListModel.new_start; i< postListModel.len(); i++){
                    Post post = postListModel.get(i);
                    if(post.type == TYPE_HEAD_POST)
                        mAdapter.clear();
                    if(mode == MODE_SINGLE_POST && i == 0)
                        post.type = TYPE_HEAD_POST;

                    mAdapter.addPost(i, PostViewAdapter.PostInfo.fromPost(post));

                    // Download user profile
                    if(post.profile_id >= 0) {
                        GlobalResFileManager.requestFile(getViewLifecycleOwner(), post.profile_id, uri -> {
                            post.userProfileUri = uri;
                            mAdapter.notifyDataSetChanged();
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

        if(mode!=MODE_SINGLE_POST)
            mAdapter.setOnClickedPost(postInfo -> {
                if(postInfo!=null && postInfo.getPost() != null) {
                    Intent intent = new Intent(getActivity(), SinglePostActivity.class);
                    Log.d("SINGLE-POST", "onCreateView: pid=" + postInfo.getPost().pid);
                    intent.putExtra(SinglePostActivity.EXTRA_PID, String.valueOf(postInfo.getPost().pid));
                    activitySinglePostLauncher.launch(intent);
                }
            });

        mAdapter.setOnClickedDelete(postInfo -> {
            AlertDialog alert=new AlertDialog.Builder(this.getActivity()).create();
            alert.setTitle("删除内容");
            alert.setMessage("确认要删除吗？（删除后不可恢复）");
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", ((dialogInterface, i) -> {
                return;
            }));
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "确定", ((dialogInterface, i) -> {
                postInfo.getPost().status.observe(getViewLifecycleOwner(), status -> {
                    if(status == NetworkBasic.Status.fail
                            || status == NetworkBasic.Status.wrong){
                        Toast.makeText(getActivity(), "删除失败，请稍后重试...", Toast.LENGTH_SHORT)
                                .show();
                    }
                    if(status == NetworkBasic.Status.success){
                        Toast.makeText(getActivity(), "删除成功.", Toast.LENGTH_SHORT)
                                .show();
                        refresh();
                    }
                });
                postInfo.getPost().request_remove();
            }));
            alert.show();
        });

        if(mode != MODE_USER_FIX)
            mAdapter.setOnClickedProfile(uid->{
                Intent intent = new Intent(getActivity(), UserPageActivity.class);
                intent.putExtra(UserPageActivity.USER_UID, uid);
                activityUserPageLauncher.launch(intent);
            });

        refresh();

        GlobalData.sig_refresh.observe(getViewLifecycleOwner(), integer -> {
            if(integer == GlobalData.SIG_FORCE_REFRESH){
                refresh();
            }
        });

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
    public void setPostID(int post_id){
        postListModel.setFixPid(post_id);
    }
    public void setSearchParam(SearchPostActivity.SearchParam param){postListModel.setSearchParam(param);}

    public void setOnClickedCard(PostViewAdapter.ClickedPostcardAction callback){
        mAdapter.setOnClickedPost(callback);
    }
}
