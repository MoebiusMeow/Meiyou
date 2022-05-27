package com.example.meiyou.utils;

import android.app.Activity;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetworkConstant extends Activity {

    /* Constant of Network Urls */
    public static final String serverUrl = "http://47.94.139.96:8060/api";
    public static final String loginUrl = serverUrl + "/login";
    public static final String userInfoUrl = serverUrl + "/userinfo";
    public static final String registerUrl = serverUrl + "/register";
    public static final String validationUrl = serverUrl + "/valid";
    public static final String getSinglePost = serverUrl + "/postdetail";
    public static final String getMultiplePost = serverUrl + "/getpost";
    public static final String sendPost = serverUrl + "/post";

    /* Method to build http Call */
    public static final OkHttpClient client = new OkHttpClient();
    public static void post(String url, RequestBody body, Boolean login_required, Callback callback){
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        if(login_required)
            requestBuilder.header("Authorization", GlobalData.getUser().getToken());
        client.newCall(requestBuilder.build()).enqueue(callback);
    }
    public static void get(String url, Boolean login_required, Callback callback){
        Request.Builder requestBuilder = new Request.Builder().url(url).get();
        if(login_required)
            requestBuilder.header("Authorization", GlobalData.getUser().getToken());
        client.newCall(requestBuilder.build()).enqueue(callback);
    }
}
