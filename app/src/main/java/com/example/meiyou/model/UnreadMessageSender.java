package com.example.meiyou.model;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import okhttp3.HttpUrl;

public class UnreadMessageSender extends NetworkBasic {
    public int uid = 0;
    public int result = 0;
    public UnreadMessageSender(int uid){
        this.uid = uid;
    }
    public UnreadMessageSender request(){
        status.postValue(Status.idle);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.countUnreadUrl).newBuilder()
                .addQueryParameter("uid", String.valueOf(uid));
        result = 0;
        NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
            response -> {
                if(response.code()!=200){
                    status.postValue(Status.wrong);
                    return;
                }
                JSONObject jsonObject = new JSONObject(response.body().string());
                result = jsonObject.getInt("count");
                status.postValue(Status.success);
            }
        ));
        return this;
    }
}
