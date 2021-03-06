package com.example.meiyou.fragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.meiyou.model.PostList.MODE_USER_FIX;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.meiyou.R;
import com.example.meiyou.activity.EditUserInfoActivity;
import com.example.meiyou.activity.FollowActivity;
import com.example.meiyou.activity.MessageActivity;
import com.example.meiyou.databinding.FragmentUserBinding;
import com.example.meiyou.model.UnreadMessageSender;
import com.example.meiyou.model.User;
import com.example.meiyou.model.UserBanSender;
import com.example.meiyou.model.UserFollowSender;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

public class UserFragment extends Fragment {
    FragmentUserBinding binding;
    PostListFragment postListFragment;

    private int uid = 0;
    private User user = null;
    private static MessagePolling polling;
    private Handler pollingHandler;
    private UnreadMessageSender pollingSender;

    private ActivityResultLauncher<Intent> followListLauncher, messagesLauncher;

    public UserFragment(){

    }

    public UserFragment(int uid){
        this.uid = uid;
    }

    private ActivityResultLauncher<Intent> activityEditInfoLauncher;

    class MessagePolling implements Runnable {
        @Override
        public void run() {
            pollingSender.request();
            pollingHandler.postDelayed(this, 5000);
        }
    }

    @SuppressLint("SetTextI18n")
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
            binding.buttonEditProfile.setVisibility(View.GONE);
            Log.d("USERINFO", "onCreateView: uid="+uid);
        }
        if(uid == GlobalData.getUser().uid){
            //binding.buttonFollow.setVisibility(View.INVISIBLE);
            binding.buttonFollow.setText("????????????");
            binding.buttonSetBan.setVisibility(View.GONE);
        } else {
            binding.buttonMyMessage.setVisibility(View.GONE);
        }
        binding.textBanned.setVisibility(View.INVISIBLE);

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

        followListLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        });

        messagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        });

        binding.messageAlertLayout.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            followListLauncher.launch(intent);
            binding.messageAlertLayout.setVisibility(View.GONE);
        });

        binding.buttonMyMessage.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            followListLauncher.launch(intent);
        });

        binding.buttonFollow.setOnClickListener(view1 -> {
            if (uid == GlobalData.getUser().uid) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                followListLauncher.launch(intent);
            } else {
                UserFollowSender followSender = new UserFollowSender(user.uid);
                followSender.status.observe(getViewLifecycleOwner(), status -> {
                    if (status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong) {
                        Toast.makeText(getActivity(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                    if (status == NetworkBasic.Status.success) {
                        getActivity().setResult(RESULT_OK);
                        Toast.makeText(getActivity(), user.followed ? "????????????" : "????????????", Toast.LENGTH_SHORT).show();
                        user.followed = followSender.flag;
                        setUiFollowed(user.followed);
                    }
                });
                followSender.setFollow(!user.followed);
            }
        });

        binding.buttonSetBan.setOnClickListener(view1 -> {
            AlertDialog alert=new AlertDialog.Builder(this.getActivity()).create();
            alert.setTitle("????????????");
            alert.setMessage("?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "??????", ((dialogInterface, i) -> {
                return;
            }));
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "??????", ((dialogInterface, i) -> {
                UserBanSender banSender = new UserBanSender(user.uid);
                banSender.status.observe(getViewLifecycleOwner(), status -> {
                    if(status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong){
                        Toast.makeText(getActivity(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                    if(status == NetworkBasic.Status.success){
                        getActivity().setResult(RESULT_OK);
                        Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
                        postListFragment.refresh();
                        binding.buttonSetBan.setVisibility(View.GONE);
                        binding.textBanned.setVisibility(View.VISIBLE);
                    }
                });
                banSender.setBan(true);
            }));
            alert.show();
        });

        binding.messageAlertLayout.setVisibility(View.GONE);

        pollingSender = new UnreadMessageSender(uid);
        polling = new MessagePolling();
        pollingSender.status.observe(getViewLifecycleOwner(), status -> {
            if (status == NetworkBasic.Status.success) {
                if (pollingSender.result <= 0 || uid != GlobalData.getUser().uid) {
                    binding.messageAlertLayout.setVisibility(View.GONE);
                } else {
                    binding.messageAlertLayout.setVisibility(View.VISIBLE);
                    binding.textViewMessageCount.setText(pollingSender.result + "????????????");
                }
            }
        });

        pollingHandler = new Handler(Looper.getMainLooper());
        pollingHandler.postDelayed(polling, 100);

        //refreshUserData();
        user.requestInfo();

        return view;
    }

    public void refreshUserData(){
        user.requestInfo();
        postListFragment.refresh();
    }

    public void setUiFollowed(boolean flag){
        if (uid == GlobalData.getUser().uid) {
            binding.buttonFollow.setText("????????????");
            binding.buttonFollow.setBackgroundTintList(
                    GlobalData.createColorStateList(
                            getActivity().getColor(R.color.pink_500)
                            , getActivity().getColor(R.color.pink_500)
                    ));
            binding.textFollowed.setVisibility(View.INVISIBLE);
            return;
        }
        if(!flag){
            binding.buttonFollow.setText("??????");
            binding.buttonFollow.setBackgroundTintList(
                    GlobalData.createColorStateList(
                    getActivity().getColor(R.color.pink_500)
                            , getActivity().getColor(R.color.pink_500)
            ));
            binding.textFollowed.setVisibility(View.INVISIBLE);
        }
        else{
            binding.buttonFollow.setText("????????????");
            binding.buttonFollow.setBackgroundTintList(
                    GlobalData.createColorStateList(
                            getActivity().getColor(R.color.gray_100)
                            , getActivity().getColor(R.color.gray_200)
                    ));
            binding.textFollowed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        pollingHandler.removeCallbacks(polling);
        super.onDestroy();
    }
}