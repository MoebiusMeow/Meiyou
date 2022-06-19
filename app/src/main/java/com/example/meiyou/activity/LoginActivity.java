package com.example.meiyou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.meiyou.model.MainUser;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.databinding.ActivityLoginBinding;
import com.example.meiyou.utils.NetworkBasic;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ProgressBar loadingProgressBar;
    private Button loginButton;
    private EditText usernameEditText, passwordEditText;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    public static final String EXTRA_USERNAME = "com.example.Meiyou.login.startusername";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> { });

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usernameEditText = binding.username;
        passwordEditText = binding.password;
        loginButton = binding.login;
        loadingProgressBar = binding.loading;

        //usernameEditText.setText("dcy11011");
        //passwordEditText.setText("111111"); // NOTE: change these into:
        // passwordEditText.setText("");
        // String username = getIntent().getStringExtra(EXTRA_USERNAME);
        // if(username != null) usernameEditText.setText(username);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        binding.buttonToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                activityResultLauncher.launch(intent);
            }
        });
        GlobalData.getUser().status.observe(this, new Observer<MainUser.Status>() {
            @Override
            public void onChanged(MainUser.Status status) {
                if(status == NetworkBasic.Status.success&&GlobalData.getUser().errorCode== 77889){
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    activityResultLauncher.launch(intent);
                }
                else if(status == NetworkBasic.Status.wrong){
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
                else if(status == NetworkBasic.Status.fail){
                    Toast.makeText(LoginActivity.this, "糟糕，网络好像开小差了", Toast.LENGTH_SHORT).show();
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private Boolean login(String username, String passwd){
        loadingProgressBar.setVisibility(View.VISIBLE);
        GlobalData.getUser().login(username, passwd);
        return true;
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}