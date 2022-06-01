package com.example.meiyou.model;


import android.util.Log;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import java.security.MessageDigest;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/* Hold MainUser data and handle some user related http request*/
public class MainUser extends NetworkBasic {
    public String username = "", signature = "";
    public String email = "";
    private String token = "";

    public String getToken(){
        return  token;
    }

    public static final int
            ERROR_VALIDATION_CODE    = 11,
            ERROR_USERNAME_EXIST     = 2,
            ERROR_EMAIL_EXIST        = 3,
            ERROR_TELEPHONE_EXIST    = 4,
            ERROR_USERNAME_FORMAT    = 10;


    public void login(String username, String password){
        status.postValue(Status.idle);
        this.username = username;
        password = getMD5(password);
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("passwd", password)
                .build();
        NetworkConstant.post(NetworkConstant.loginUrl, body, false, getCommonNetworkCallback(
                response -> {
                    if(response.code()!=200){
                        status.postValue(Status.wrong);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    token = jsonObject.getString("token");
                    status.postValue(Status.success);
                }
        ));
    }

    public void register(String username, String password, String email, int code){
        status.postValue(com.example.meiyou.model.MainUser.Status.idle);
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("passwd", getMD5(password))
                .add("valid_type", "mail")
                .add("valid_content", email)
                .add("code", String.valueOf(code))
                .build();
        NetworkConstant.post(NetworkConstant.registerUrl, body, false, getCommonNetworkCallback(
                response -> {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (response.code() != 200) {
                        status.postValue(Status.wrong);
                        errorCode = jsonObject.getInt("id");
                        Log.d("NETDCT", "onResponse: " + jsonObject.getString("message"));
                        return;
                    }
                    MainUser.this.username = username;
                    status.postValue(Status.success);
                }
        ));
    }

    public static String getMD5(String info) {
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
