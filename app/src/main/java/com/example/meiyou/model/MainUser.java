package com.example.meiyou.model;


import android.util.Log;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import java.security.MessageDigest;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/* Hold MainUser data and handle some user related http request*/
public class MainUser extends User {

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
                    uid = jsonObject.getInt("uid");
                    Log.d("TAG", "login: success");
                    errorCode = 77889;
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


    public void updateInfo(User newInfo){
        status.postValue(Status.idle);
        RequestBody body = new FormBody.Builder()
                .add("username", newInfo.username)
                .add("signature", newInfo.signature)
                .add("profile", String.valueOf(newInfo.profile_id))
                .build();
        Log.d("TAG", "updateInfo: profileID = "+newInfo.profile_id);
        NetworkConstant.post(NetworkConstant.setUserInfoUrl, body, true, getCommonNetworkCallback(
                response -> {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (response.code() != 200) {
                        status.postValue(Status.wrong);
                        //errorCode = jsonObject.getInt("id");
                        Log.d("NETDCT", "onResponse: " + jsonObject.getString("message"));
                        return;
                    }
                    Log.d("TAG", "updatePassword: updateInfo success");
                    status.postValue(Status.success);
                }
        ));
    }

    public void updatePassword(String old_password, String new_password){
        Log.d("TAG", "updatePassword: status set to idel start");
        status.postValue(Status.idle);
        Log.d("TAG", "updatePassword: status set to idel");
        RequestBody body = new FormBody.Builder()
                .add("old", getMD5(old_password))
                .add("new", getMD5(new_password))
                .build();
        NetworkConstant.post(NetworkConstant.setPasswordUrl, body, true, getCommonNetworkCallback(
                response -> {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (response.code() != 200) {
                        status.postValue(Status.wrong);
                        if(response.code() == 400)
                            errorCode = jsonObject.getInt("id");
                        else
                            errorCode = 2;
                        Log.d("NETDCT", "onResponse: " + jsonObject.getString("message"));
                        return;
                    }
                    Log.d("TAG", "updatePassword: change passwd success");
                    errorCode = 8977787;
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
