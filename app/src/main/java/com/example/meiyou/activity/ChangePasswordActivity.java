package com.example.meiyou.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.meiyou.databinding.ActivityChangePasswordBinding;
import com.example.meiyou.databinding.ActivityEditUserInfoBinding;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

public class ChangePasswordActivity extends AppCompatActivity {
    ActivityChangePasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCancelEditPasswd.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        binding.buttonReturnPasswd.setOnClickListener(view ->{
            setResult(RESULT_CANCELED);
            finish();
        });

        binding.buttonConfirmEditPasswd.setOnClickListener( view -> {
            boolean ok = true;
            ok &= verifyPasswordNew1();
            ok &= verifyPasswordRepeat();
            if(!ok){
                Toast.makeText(this, "请按提示修改", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("TAG", "onCreate: old status:"+GlobalData.getUser().status.getValue());
            GlobalData.getUser().status.observe(this, status -> {
                if(status == NetworkBasic.Status.success &&
                    GlobalData.getUser().errorCode == 8977787){
                    GlobalData.getUser().status.postValue(NetworkBasic.Status.idle);
                    setResult(RESULT_OK);
                    Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if(status == NetworkBasic.Status.wrong){
                    switch(GlobalData.getUser().errorCode){
                        case 1:
                            binding.notifyPasswordOld.setText("密码有误");
                            binding.notifyPasswordOld.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "请检查旧密码", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(this, "修改失败...", Toast.LENGTH_SHORT).show();
                    }
                }
                if(status == NetworkBasic.Status.fail){
                    Toast.makeText(this, "网络好像有些问题...", Toast.LENGTH_SHORT).show();
                }
                binding.progressBarSubmitPassword.setVisibility(View.INVISIBLE);
            });
            GlobalData.getUser().updatePassword(
                    binding.editTextTextPasswordOld.getText().toString(),
                    binding.editTextTextPasswordNew1.getText().toString()
            );
        });
    }


    private boolean verifyPasswordNew1(){
        String passwd = binding.editTextTextPasswordNew1.getText().toString();
        if(!passwd.matches("^[0-9a-zA-Z]{6,18}$")) {
            binding.notifyPasswordNew1.setText("密码应由数字、字母组成，长度6到18位");
            binding.notifyPasswordNew1.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            binding.notifyPasswordNew1.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private boolean verifyPasswordRepeat(){
        if( !binding.editTextTextPasswordNew1.getText().toString()
                .equals( binding.editTextTextPasswordNew2.getText().toString() )) {
            binding.notifyPasswordNew2.setText("两次输入不一致");
            binding.notifyPasswordNew2.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            binding.notifyPasswordNew2.setVisibility(View.INVISIBLE);
            return true;
        }
    }
}