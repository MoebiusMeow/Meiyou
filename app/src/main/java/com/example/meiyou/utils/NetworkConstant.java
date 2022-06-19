package com.example.meiyou.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkConstant extends Activity {

    /* Constant of Network Urls */
    public static final String serverUrl = "http://47.94.139.96:8060/api";
    public static final String loginUrl = serverUrl + "/login";
    public static final String userInfoUrl = serverUrl + "/userinfo";
    public static final String registerUrl = serverUrl + "/register";
    public static final String validationUrl = serverUrl + "/valid";
    public static final String getSinglePostUrl = serverUrl + "/postdetail";
    public static final String getMultiplePostUrl = serverUrl + "/getpost";
    public static final String sendPostUrl = serverUrl + "/post";
    public static final String replyPostUrl = serverUrl + "/reply";
    public static final String downloadUrl = serverUrl + "/download";
    public static final String uploadUrl = serverUrl + "/upload";
    public static final String setUserInfoUrl = serverUrl + "/setinfo";
    public static final String setPasswordUrl = serverUrl + "/setpasswd";
    public static final String getReplyUrl = serverUrl + "/getreply";
    public static final String setDianzanUrl = serverUrl + "/dianzanpost";
    public static final String removePostUrl = serverUrl + "/removepost";
    public static final String removeReplyUrl = serverUrl + "/removereply";
    public static final String addFollowUrl = serverUrl + "/addfollow";
    public static final String removeFollowUrl = serverUrl + "/removefollow";
    public static final String followListUrl = serverUrl + "/followlist";
    public static final String addBanUrl = serverUrl + "/addban";
    public static final String removeBanUrl = serverUrl + "/removeban";
    public static final String banListUrl = serverUrl + "/banlist";
    public static final String getMessageUrl = serverUrl + "/getmessage";
    public static final String countUnreadUrl = serverUrl + "/countunread";
    public static final String searchPostUrl = serverUrl + "/search";



    /* Method to build http Call */
    public static final OkHttpClient client = new OkHttpClient.Builder()
            //.callTimeout(3, TimeUnit.SECONDS)
            //.writeTimeout(2, TimeUnit.SECONDS)
            //.readTimeout(2, TimeUnit.SECONDS)
            .build();

    private static ConnectionPool mConnectionPool=new ConnectionPool(1000, 30, TimeUnit.MINUTES);
    public static Call post(String url, RequestBody body, Boolean login_required, Callback callback){
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body)
                .header("Connection", "close");
        if(login_required)
            requestBuilder.header("Authorization", GlobalData.getUser().getToken());
        Call call = client.newCall(requestBuilder.build());
        call.enqueue(callback);
        return call;
    }
    public static Call get(String url, Boolean login_required, Callback callback){
        Request.Builder requestBuilder = new Request.Builder().url(url).get()
                .header("Connection", "close");
        if(login_required)
            requestBuilder.header("Authorization", GlobalData.getUser().getToken());
        Call call = client.newCall(requestBuilder.build());
        call.enqueue(callback);
        return call;
    }
    public static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(dispositionHeader)) {
            dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader.replace("filename*=utf-8", "");
            String[] strings = dispositionHeader.split("; ");
            if (strings.length > 1) {
                dispositionHeader = strings[1].replace("filename=", "");
                dispositionHeader = dispositionHeader.replace("\"", "");
                return dispositionHeader;
            }
            return "";
        }
        return "";
    }
}
