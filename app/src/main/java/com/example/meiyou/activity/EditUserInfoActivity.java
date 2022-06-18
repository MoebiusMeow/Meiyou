package com.example.meiyou.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.example.meiyou.R;
import com.example.meiyou.control.FileUploader;
import com.example.meiyou.databinding.ActivityEditUserInfoBinding;
import com.example.meiyou.model.User;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

import java.io.FileNotFoundException;

public class EditUserInfoActivity extends AppCompatActivity {

    private ActivityEditUserInfoBinding binding;

    private ActivityResultLauncher<Intent> activityImageSelectLauncher,
        activityEditPasswdLauncher;

    private int res_id = 0;


    private void setMask(boolean isVisible){
        if(binding != null) {
            if (!isVisible){
                binding.viewMask.setVisibility(View.INVISIBLE);
                binding.progressBarUserProfile.setVisibility(View.INVISIBLE);
            }
            else{
                binding.viewMask.setVisibility(View.VISIBLE);
                binding.progressBarUserProfile.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityEditUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBarUserProfile.setMax(100);



        activityImageSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = null;
                        if(data != null){
                            uri = data.getData();
                            Log.d("meow", uri.toString());
                        }
                        if(uri != null){
                            try {
                                FileUploader fileUploader = new FileUploader(getContentResolver());
                                Uri finalUri = uri;
                                fileUploader.status.observe(this, status -> {
                                    if (status == NetworkBasic.Status.success) {
                                        Drawable drawable = Drawable.createFromPath(finalUri.getPath());
                                        Log.d("TAG", "onCreate: Set uri="+finalUri);
                                        binding.imageButtonProfile.setImageURI(finalUri);
                                        res_id = fileUploader.result_res_id;
                                    }
                                    if (status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong) {
                                        Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show();
                                    }
                                    setMask(false);
                                });
                                fileUploader.put(uri, "image", num -> {
                                    binding.progressBarUserProfile.setProgress((int)(num*100));
                                });
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


        activityEditPasswdLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Log.d("TAG", "onCreate: qwqwqeqweqweqwewqewqe");
                        Intent intent = new Intent(this,LoginActivity.class);
                        intent.putExtra(LoginActivity.EXTRA_USERNAME,
                                GlobalData.getUser().username);
                        startActivity(intent);
                    }
                });

        binding.buttonStartEditPasswd.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            activityEditPasswdLauncher.launch(intent);
        });

        binding.buttonReturn2.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        binding.buttonCancelEditUser.setOnClickListener( view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        setMask(false);

        GlobalData.getUser().status.observe(this, new Observer<NetworkBasic.Status>() {
            @Override
            public void onChanged(NetworkBasic.Status status) {
                if(status == NetworkBasic.Status.success){
                    refreshUidisplay();
                }
                else{
                    Toast.makeText(EditUserInfoActivity.this, "好像网络出现了一些问题....", Toast.LENGTH_SHORT).show();
                }
                GlobalData.getUser().status.removeObserver(this);
            }
        });
        GlobalData.getUser().requestInfo();

        binding.imageButtonProfile.setOnClickListener(view -> {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            // Select local
            setMask(true);
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityImageSelectLauncher.launch(intent);
        });

        binding.editTextTextPersonUsername.setOnFocusChangeListener(((view, b) -> {
            if(!b) checkUsername();
        }));

        binding.editTextTextSignature.setOnFocusChangeListener(((view, b) -> {
            if(!b) checkSignature();
        }));

        binding.buttonConfirmEditUser.setOnClickListener(view -> {
            boolean ok = true;
            ok &= checkUsername();
            ok &= checkSignature();
            if(!ok){
                Toast.makeText(this, "请检查提示内容", Toast.LENGTH_SHORT).show();
                return;
            }

            User new_info = new User();
            new_info.profile_id = res_id;
            new_info.username = binding.editTextTextPersonUsername.getText().toString();
            new_info.signature = binding.editTextTextSignature.getText().toString();

            GlobalData.getUser().status.observe(EditUserInfoActivity.this, status -> {
                if(status == NetworkBasic.Status.success){
                    setResult(RESULT_OK);
                    Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if(status == NetworkBasic.Status.wrong || status == NetworkBasic.Status.fail){
                    Toast.makeText(this, "网络好像出现了一点问题...", Toast.LENGTH_SHORT).show();
                }
                binding.progressBarSubmitEdit.setVisibility(View.INVISIBLE);
            });
            binding.progressBarSubmitEdit.setVisibility(View.VISIBLE);
            GlobalData.getUser().updateInfo(new_info);
        });


    }

    private void refreshUidisplay(){
        this.res_id = GlobalData.getUser().profile_id;

        Uri uri = GlobalData.getUser().userprofile.getValue();
        if (uri == null) {
            binding.imageButtonProfile.setImageResource(R.drawable.user_profile_default);
        } else {
            Log.d("TAG", "onCreateView: put profile"+uri.getPath());
            Drawable drawable = Drawable.createFromPath(uri.getPath());
            binding.imageButtonProfile.setImageDrawable(drawable);
        }

        binding.textShowMail.setText(GlobalData.getUser().email);
        binding.editTextTextPersonUsername.setText(GlobalData.getUser().username);
        binding.editTextTextSignature.setText(GlobalData.getUser().signature);
    }

    private boolean checkUsername(){
        String username = binding.editTextTextPersonUsername.getText().toString();
        if(username.length() < 2 || username.length() > 10){
            binding.notifyUsername2.setText("用户名长度应在2到10之间");
            binding.notifyUsername2.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.notifyUsername2.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private boolean checkSignature(){
        String signature = binding.editTextTextSignature.getText().toString();
        if(signature.length() > 36 ){
            binding.notifySignature.setText("个性签名长度不超过36");
            binding.notifySignature.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.notifySignature.setVisibility(View.INVISIBLE);
            return true;
        }
    }

}