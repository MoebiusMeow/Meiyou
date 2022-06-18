package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_CANCELED;
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
import com.example.meiyou.model.User;
import com.example.meiyou.model.UserFollowSender;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;

public class UserFragment extends Fragment {
    FragmentUserBinding binding;
    PostListFragment postListFragment;

    private int uid = 0;
    private User user = null;

    public UserFragment(){

    }

    public UserFragment(int uid){
        this.uid = uid;
    }

    private ActivityResultLauncher<Intent> activityEditInfoLauncher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        getActivity().setResult(RESULT_CANCELED);

        if(uid<=0) {
            uid = GlobalData.getUser().uid;
            user = GlobalData.getUser();
        }
        else{
            user = new User();
            user.uid = uid;
            binding.buttonReturnUserPage.setVisibility(View.VISIBLE);
            binding.buttonReturnUserPage.setOnClickListener(view1 ->{
                getActivity().finish();
            });
            Log.d("USERINFO", "onCreateView: uid="+uid);
        }
        if(uid == GlobalData.getUser().uid){
            binding.buttonFollow.setVisibility(View.INVISIBLE);
        }

        binding.imageUserUserProfile.setImageResource(R.drawable.user_profile_default);
        setUiFollowed(false);

        FragmentManager fm = getParentFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        postListFragment = new PostListFragment(MODE_USER_FIX);
        postListFragment.setUserID(uid);
        ft.replace(R.id.fragmentUser, postListFragment);
        ft.commit();

        activityEditInfoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        Log.d("TAG", "onCreateView: refresh userinfo");
                        refreshUserData();
                    }
                });


        user.userprofile.observe(getViewLifecycleOwner(), uri -> {
            if (uri == null) {
                binding.imageUserUserProfile.setImageResource(R.drawable.user_profile_default);
            } else {
                Log.d("TAG", "onCreateView: put profile"+uri.getPath());
                Drawable drawable = Drawable.createFromPath(uri.getPath());
                binding.imageUserUserProfile.setImageDrawable(drawable);
            }
        });

        user.status.observe(getViewLifecycleOwner(), status -> {
            if(status == NetworkBasic.Status.success){
                Log.d("TAG", "onCreateView: bind ui user info");
                binding.textUserpageName.setText(user.username);
                binding.textUserIntro.setText(user.signature);
                binding.textUserpageEmail.setText(user.email);
                setUiFollowed(user.followed);
                user.requestProfile(getViewLifecycleOwner());
            }
        });

        binding.buttonEditProfile.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), EditUserInfoActivity.class);
            activityEditInfoLauncher.launch(intent);
        });

        binding.buttonFollow.setOnClickListener(view1 -> {
            UserFollowSender followSender = new UserFollowSender(user.uid);
            followSender.status.observe(getViewLifecycleOwner(), status -> {
                if(status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong){
                    Toast.makeText(getActivity(), "关注失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
                if(status == NetworkBasic.Status.success){
                    getActivity().setResult(RESULT_OK);
                    Toast.makeText(getActivity(), user.followed? "取关成功":"关注成功", Toast.LENGTH_SHORT).show();
                    user.followed = !user.followed;
                    setUiFollowed(user.followed);
                }
            });
            followSender.setFollow(!user.followed);
        });


        //refreshUserData();
        user.requestInfo();

        return view;
    }

    public void refreshUserData(){
        user.requestInfo();
        postListFragment.refresh();
    }

    public void setUiFollowed(boolean flag){
        if(!flag){
            binding.buttonFollow.setText("关注");
            binding.buttonFollow.setBackgroundTintList(
                    GlobalData.createColorStateList(
                    getActivity().getColor(R.color.pink_500)
                            , getActivity().getColor(R.color.pink_500)
            ));
            binding.textFollowed.setVisibility(View.INVISIBLE);
        }
        else{
            binding.buttonFollow.setText("取消关注");
            binding.buttonFollow.setBackgroundTintList(
                    GlobalData.createColorStateList(
                            getActivity().getColor(R.color.gray_100)
                            , getActivity().getColor(R.color.gray_200)
                    ));
            binding.textFollowed.setVisibility(View.VISIBLE);
        }
    }
}