package com.example.meiyou.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.meiyou.R;
import com.example.meiyou.activity.SearchPostActivity;
import com.example.meiyou.databinding.FragmentNewpostBinding;
import com.example.meiyou.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {

    FragmentSearchBinding binding;
    public SearchFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.buttonStartSearch.setOnClickListener(view1->{
            SearchPostActivity.SearchParam searchParam = new SearchPostActivity.SearchParam();
            String temp;
            temp = binding.editTextMainSearch.getText().toString().trim();
            if(temp.length() > 0) searchParam.pattern_all = temp;
            temp = binding.editTextTitleSearch.getText().toString().trim();
            if(temp.length() > 0) searchParam.pattern_title = temp;
            temp = binding.editTextContentSearch.getText().toString().trim();
            if(temp.length() > 0) searchParam.pattern_content = temp;
            temp = binding.editTextUsernameSearch.getText().toString().trim();
            if(temp.length() > 0) searchParam.pattern_user = temp;

            searchParam.if_text = binding.checkBoxText.isChecked();
            searchParam.if_audio = binding.checkBoxAudio.isChecked();
            searchParam.if_video = binding.checkBoxVideo.isChecked();
            searchParam.if_image = binding.checkBoxImage.isChecked();

            Intent intent = new Intent(getActivity(), SearchPostActivity.class);
            intent.putExtra(SearchPostActivity.PARAM_ALL, searchParam);
            startActivity(intent);
        });

        return view;
    }
}
