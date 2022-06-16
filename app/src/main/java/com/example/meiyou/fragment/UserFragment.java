package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_OK;

import static com.example.meiyou.model.PostList.MODE_USER_FIX;

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
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
    PostListFragment postListFragment;
    public UserFragment(){

    }

    private ActivityResultLauncher<Intent> activityEditInfoLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        FragmentManager fm = getParentFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        postListFragment = new PostListFragment(MODE_USER_FIX);
        postListFragment.setUserID(GlobalData.getUser().uid);
        ft.replace(R.id.fragmentUser, postListFragment);
        ft.commit();

        activityEditInfoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        Log.d("TAG", "onCreateView: refresh userinfo");
                        refreshUserData();
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


        //refreshUserData();
        GlobalData.getUser().requestInfo();

        return view;
    }

    public void refreshUserData(){
        GlobalData.getUser().requestInfo();
        postListFragment.refresh();
    }
}