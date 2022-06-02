package com.example.meiyou.fragment;

import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.component.PostViewAdapter;
import com.example.meiyou.databinding.FragmentDraftlistBinding;
import com.example.meiyou.databinding.FragmentPostlistBinding;
import com.example.meiyou.model.DraftList;
import com.example.meiyou.utils.GlobalData;

public class DraftListFragment extends Fragment {
    FragmentDraftlistBinding binding;
    private PostViewAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDraftlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        RecyclerView mRecyclerView = binding.recycleViewDraft;
        mAdapter = new PostViewAdapter(this.getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        mAdapter.changeTail(false);

        return view;
    }

    public void load(){
        DraftList draftList = GlobalData.draftList;
        draftList.loadFromFile();
        // Append to Adapter
    }
}
