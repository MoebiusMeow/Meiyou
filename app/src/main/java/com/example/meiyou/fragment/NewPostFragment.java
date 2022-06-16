package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.R;
import com.example.meiyou.activity.NewContentActivity;
import com.example.meiyou.databinding.FragmentDraftlistBinding;
import com.example.meiyou.databinding.FragmentNewpostBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.utils.GlobalData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostFragment extends Fragment {
    FragmentNewpostBinding binding;
    DraftListFragment fragment;

    public int pid = -1;

    private ActivityResultLauncher<Intent> activityNewContentLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewpostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        activityNewContentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK && result.getData()!=null){
                        Intent data = result.getData();
                        int action = data.getIntExtra(NewContentActivity.ACTION_TYPE, -1);
                        if(action == NewContentActivity.ACTION_SAVE){
                            Post post = (Post) data.getSerializableExtra(NewContentActivity.POST_DATA);
                            post.datetime = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss").format(new Date());
                            GlobalData.draftList.add(post);
                            GlobalData.draftList.saveToFile();
                        }
                        if(action == NewContentActivity.ACTION_POST){
                            int pid = data.getIntExtra(NewContentActivity.POST_ID, -1);
                            this.pid = pid;
                            // notify main activity
                            GlobalData.sig_post.postValue(GlobalData.SIG_POST_SEND);
                        }
                    }
                });

        binding.buttonCreate.setOnClickListener(view1 -> {
            GlobalData.sig_post.postValue(GlobalData.SIG_POST_NOTHING);
            Intent intent = new Intent(getActivity(), NewContentActivity.class);
            activityNewContentLauncher.launch(intent);
        });

        return view;
    }
}
