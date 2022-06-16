package com.example.meiyou.model;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import okhttp3.HttpUrl;

public class User extends NetworkBasic {
    public String username = "", signature = "";
    public String email = "";
    public int uid = 0;
    public MutableLiveData<Uri> userprofile = new MutableLiveData<>();
    public int profile_id = 0;

    public void requestInfo(){
        status.postValue(Status.idle);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.userInfoUrl).newBuilder()
                .addQueryParameter("uid", String.valueOf(uid));
        NetworkConstant.get(NetworkConstant.userInfoUrl, true, getCommonNetworkCallback(response -> {
            if(response.code()!=200){
                status.postValue(Status.wrong);
                return;
            }
            JSONObject jsonObject = new JSONObject(response.body().string());
            username = jsonObject.getString("username");
            uid = jsonObject.getInt("uid");
            signature = jsonObject.getString("sig");
            profile_id = jsonObject.getInt("profileid");
            email = jsonObject.getString("mail");
            Log.d("TAG", "requestInfo: success");
            status.postValue(Status.success);
        }));
    }
    public void requestProfile(LifecycleOwner lifecycleOwner) {
        if (profile_id > 0) {
            Log.d("TAG", "requestProfile: id="+profile_id);
            GlobalResFileManager.requestFile(lifecycleOwner, profile_id, uri -> {
                this.userprofile.postValue(uri);
            });
        }
    }
}
