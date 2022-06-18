package com.example.meiyou.model;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.HttpUrl;

public class UserBanSender extends NetworkBasic {
    private int uid = 0;
    public UserBanSender(int uid){
        this.uid = uid;
    }
    public void setBan(boolean flag){
        status.postValue(Status.idle);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(
                flag? NetworkConstant.addBanUrl: NetworkConstant.removeBanUrl
        ).newBuilder()
                .addQueryParameter("uid", String.valueOf(uid));
        NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
                response -> {
                    if(response.code()!=200){
                        status.postValue(Status.wrong);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(
                            Objects.requireNonNull(response.body()).string());
                    errorCode = jsonObject.getInt("id");
                    status.postValue(Status.success);
                }
        ));
    }
}
