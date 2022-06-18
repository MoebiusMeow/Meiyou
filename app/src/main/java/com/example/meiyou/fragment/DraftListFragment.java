package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
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

import com.example.meiyou.activity.NewContentActivity;
import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentDraftlistBinding;
import com.example.meiyou.databinding.FragmentPostlistBinding;
import com.example.meiyou.model.DraftList;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.UserBanSender;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

public class DraftListFragment extends Fragment {
    FragmentDraftlistBinding binding;
    private PostViewAdapter mAdapter;

    private ActivityResultLauncher<Intent> activityEditSavedLauncher;

    // record the post in draft list which need to remove when post send out
    private Post post_to_delete = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDraftlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Get in-memory draft list from GlobalData
        DraftList draftList = GlobalData.draftList;

        // Add edit saved draft callback
        activityEditSavedLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK && result.getData()!=null){
                        Intent data = result.getData();
                        int action = data.getIntExtra(NewContentActivity.ACTION_TYPE, -1);
                        if(action == NewContentActivity.ACTION_SAVE) {
                            Post post = (Post)
                                    data.getSerializableExtra(NewContentActivity.POST_DATA);
                            if(post_to_delete!=null)
                                draftList.remove(post_to_delete);
                            draftList.add(post);
                            draftList.saveToFile();
                            mAdapter.notifyDataSetChanged();
                        }
                        if(action == NewContentActivity.ACTION_POST) {
                            if(post_to_delete!=null) {
                                draftList.remove(post_to_delete);
                                draftList.saveToFile();
                            }
                            GlobalData.sig_post.postValue(GlobalData.SIG_POST_SEND);
                        }
                    }
                });

        RecyclerView mRecyclerView = binding.recycleViewDraft;
        mAdapter = new PostViewAdapter(this.getContext(), getViewLifecycleOwner());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        mAdapter.changeTail(true);
        mAdapter.setOnClickedPost(postInfo->{
            try {
                Intent intent = new Intent(getActivity(), NewContentActivity.class);
                intent.putExtra(NewContentActivity.POST_SAVED, postInfo.getPost());
                activityEditSavedLauncher.launch(intent);
                post_to_delete = postInfo.getPost();
            }
            catch (Exception e){
                Log.d("TAG", "onCreateView: "+e);
            }
        });


        draftList.setOnUpdateCallback(startIndex -> {
            if(startIndex <= 0) mAdapter.clear();
            for(int i=startIndex;i<draftList.len();i++) {
                PostViewAdapter.PostInfo postInfo = PostViewAdapter.PostInfo
                        .fromPost(draftList.get(i));
                postInfo.getPost().uid  = GlobalData.getUser().uid;
                postInfo.setStyle(PostViewAdapter.STYLE_NOT_PUBLISHED);
                mAdapter.addPost(i, postInfo);
            }
        });

        mAdapter.setOnClickedDelete(postInfo -> {
            AlertDialog alert=new AlertDialog.Builder(this.getActivity()).create();
            alert.setTitle("删除草稿");
            alert.setMessage("确认要删除草稿吗？（删除后无法恢复）");
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", ((dialogInterface, i) -> {
                return;
            }));
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "确定", ((dialogInterface, i) -> {
                draftList.remove(postInfo.getPost());
                draftList.saveToFile();
            }));
            alert.show();
        });

        load();

        return view;
    }

    public void load(){
        DraftList draftList = GlobalData.draftList;
        draftList.loadFromFile();
    }
}
