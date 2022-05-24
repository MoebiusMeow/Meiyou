package com.example.meiyou.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivityLoginBinding;
import com.example.meiyou.databinding.ActivityRegisterBinding;
import com.example.meiyou.utils.GlobalData;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    EditText usernameText, mailText, passwordText, passwordText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_register);

        usernameText = binding.editTextTextPersonName;
        mailText = binding.editTextTextEmailAddress;
        passwordText = binding.editTextTextPassword;
        passwordText2 = binding.editTextTextPassword2;

        binding.buttonGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalData.getRegisterControl().requireCode(mailText.getText().toString());
            }
        });

        GlobalData.getRegisterControl().timeCountDown.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });
    }
}