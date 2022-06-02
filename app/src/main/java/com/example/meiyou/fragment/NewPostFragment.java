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

import com.example.meiyou.R;
import com.example.meiyou.activity.NewContentActivity;
import com.example.meiyou.databinding.FragmentDraftlistBinding;
import com.example.meiyou.databinding.FragmentNewpostBinding;
import com.example.meiyou.model.Post;

public class NewPostFragment extends Fragment {
    FragmentNewpostBinding binding;

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
                        }
                        if(action == NewContentActivity.ACTION_POST){
                            int pid = data.getIntExtra(NewContentActivity.POST_ID, -1);
                            Log.d("TAG", "onCreateView: "+pid);
                        }
                    }
                });

        binding.buttonCreate.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), NewContentActivity.class);
            activityNewContentLauncher.launch(intent);
        });

        return view;
    }
}
