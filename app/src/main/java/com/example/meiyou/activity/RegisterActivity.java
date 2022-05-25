package com.example.meiyou.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivityRegisterBinding;
import com.example.meiyou.model.User;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    EditText usernameText, mailText, passwordText, passwordTextRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usernameText = binding.editTextTextPersonName;
        mailText = binding.editTextTextEmailAddress;
        passwordText = binding.editTextTextPassword;
        passwordTextRepeat = binding.editTextTextPassword2;
        Button buttonGetCode = binding.buttonGetCode;

        binding.buttonGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ok = true;
                ok &= verifyEmail();
                if(ok) {
                    GlobalData.getRegisterControl().requireCode(mailText.getText().toString());
                }
                else{
                    Toast.makeText(RegisterActivity.this, "请检查邮箱格式", Toast.LENGTH_SHORT).show();
                }
            }
        });
        GlobalData.getUser().status.postValue(NetworkBasic.Status.idle);
        GlobalData.getUser().status.observe(this, new Observer<NetworkBasic.Status>() {
            @Override
            public void onChanged(NetworkBasic.Status status) {
                if(status.equals(NetworkBasic.Status.success)){
                    finish();
                }
                if(status.equals(NetworkBasic.Status.wrong)){
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterActivity.this, "注册失败，请按提示信息修改", Toast.LENGTH_SHORT).show();
                    int errorCode = GlobalData.getUser().errorCode;
                    if(errorCode == User.ERROR_VALIDATION_CODE){
                        binding.notifyCode.setText("验证码错误");
                        binding.notifyCode.setVisibility(View.VISIBLE);
                    }
                    if(errorCode == User.ERROR_EMAIL_EXIST){
                        binding.notifyEmail.setText("邮箱已被注册");
                        binding.notifyEmail.setVisibility(View.VISIBLE);
                    }
                    if(errorCode == User.ERROR_USERNAME_EXIST){
                        binding.notifyUsername.setText("用户名已被注册");
                        binding.notifyUsername.setVisibility(View.VISIBLE);
                    }
                }
                if(status.equals(NetworkBasic.Status.fail)){
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                }

            }
        });

        GlobalData.getRegisterControl().timeCountDown.observe(this, new Observer<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onChanged(Integer integer) {
                if(integer > 0) {
                    buttonGetCode.setBackgroundTintList(GlobalData.createColorStateList(
                            getColor(R.color.gray_400), getColor(R.color.gray_200)
                    ));
                    buttonGetCode.setText("重新发送("+String.valueOf(integer)+")");
                }
                else{
                    buttonGetCode.setBackgroundTintList(GlobalData.createColorStateList(
                            getColor(R.color.pink_200), getColor(R.color.pink_500)
                    ));
                    buttonGetCode.setText("获取验证码");
                }
            }
        });

        binding.editTextNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.notifyCode.setVisibility(View.INVISIBLE);
            }
        });

        usernameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) verifyUsername();
            }
        });

        mailText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) verifyEmail();
            }
        });
        
        passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) verifyPassword();
            }
        });

        passwordTextRepeat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) verifyPasswordRepeat();
            }
        });


        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ok = true;
                ok &= verifyPassword();
                ok &= verifyPasswordRepeat();
                ok &= verifyUsername();
                ok &= verifyEmail();

                if(ok) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    GlobalData.getUser().register(usernameText.getText().toString(),
                            passwordText.getText().toString(), mailText.getText().toString(),
                            Integer.valueOf(binding.editTextNumber.getText().toString()));
                }
                else{
                    Toast.makeText(RegisterActivity.this, "请按提示内容修改", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean verifyPassword(){
        String passwd = passwordText.getText().toString();
        if(!passwd.matches("^[0-9a-zA-Z]{6,18}$")) {
            binding.notifyPassword.setText("密码应由数字、字母组成，长度6到18位");
            binding.notifyPassword.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            binding.notifyPassword.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private boolean verifyPasswordRepeat(){
        if( !passwordText.getText().toString().equals( passwordTextRepeat.getText().toString() )) {
            binding.notifyPassword.setText("两次输入不一致");
            binding.notifyRepeatWrong.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            binding.notifyRepeatWrong.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private boolean verifyUsername(){
        int len = usernameText.getText().toString().length();
        if(len<2 || len >10){
            binding.notifyUsername.setText("用户名长度应为6到18位");
            binding.notifyUsername.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.notifyUsername.setVisibility(View.INVISIBLE);
            return true;
        }
    }
    
    private boolean verifyEmail(){
        if(!mailText.getText().toString().matches("^[\\w]+(\\.[\\w]+)*@[\\w]+(\\.[\\w]+)+$")){
            binding.notifyEmail.setText("请检查邮箱格式");
            binding.notifyEmail.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.notifyEmail.setVisibility(View.INVISIBLE);
            return true;
        }
    }
}