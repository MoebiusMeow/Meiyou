package com.example.meiyou.model;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import okhttp3.HttpUrl;

public class UserFollowSender extends NetworkBasic {
    private int uid = 0;
    public UserFollowSender(int uid){
        this.uid = uid;
    }
    public void setFollow(boolean flag){
        status.postValue(Status.idle);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(
                flag? NetworkConstant.addFollowUrl: NetworkConstant.removeFollowUrl
        ).newBuilder()
                .addQueryParameter("uid", String.valueOf(uid));
        NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
                response -> {
                    if(response.code()!=200){
                        status.postValue(Status.wrong);
                        return;
                    }
                    status.postValue(Status.success);
                }
        ));
    }
}
