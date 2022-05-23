package com.example.meiyou.model;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.data.NetworkConstant;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class User {
    public String username, signature;
    private String token;
    public enum Status{
        idle,
        success,
        fail,
        wrong,
    }
    public MutableLiveData<Status> status = new MutableLiveData<Status>(Status.idle);

    public String getToken(){
        return  token;
    }

    public void networkLogin(String username, String password){
        status.setValue(Status.idle);
        this.username = username;
        password = getMD5(password);
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("passwd", password)
                .build();
        NetworkConstant.post(NetworkConstant.loginUrl, body, false, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { status.postValue(Status.fail); }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if(response.code()!=200){
                        status.postValue(Status.wrong);
                        Log.d("gg", "onResponse: "+response.body().string());
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    token = jsonObject.getString("token");
                    status.postValue(Status.success);

                    NetworkConstant.get(NetworkConstant.userInfoUrl, true, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) { status.setValue(Status.fail); }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response1) throws IOException {
                            try {
                                JSONObject jsonObject1 = new JSONObject(response1.body().string());
                                signature = jsonObject1.getString("sig");
                            }catch (Exception e) {
                                status.postValue(Status.fail);
                            }
                        }
                    });
                }catch (Exception e) {
                    status.postValue(Status.fail);
                }
            }
        });
    }




    private String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();
            StringBuffer strBuf = new StringBuffer();
            for (byte b:encryption) {
                if (Integer.toHexString(0xff & b).length() == 1)
                    strBuf.append("0");
                strBuf.append(Integer.toHexString(0xff & b));
            }
            return strBuf.toString();
        }
        catch (Exception e) {
            return "";
        }
    }



}
