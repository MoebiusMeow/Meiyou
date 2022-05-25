package com.example.meiyou.control;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.HttpUrl;

public class RegisterControl extends NetworkBasic {
    public MutableLiveData<Integer> timeCountDown = new MutableLiveData<Integer>(0);
    public void requireCode(String email){
        status.postValue(Status.idle);
        Log.d("TESTDCY", "requireCode: in");
        if(timeCountDown.getValue() > 0)
            return;
        Log.d("TESTDCY", "requireCode: inin");
        HttpUrl url = HttpUrl.parse(NetworkConstant.validationUrl).newBuilder()
                .addQueryParameter("mail", email)
                .build();
        NetworkConstant.get(url.toString(), false, getCommonNetworkCallback(
            response -> {
                if(response.code() != 200){
                    status.postValue(Status.wrong);
                }
                status.postValue(Status.success);
                timeCountDown.postValue(60);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Integer new_time = timeCountDown.getValue() -1;
                        if(new_time < 0)
                            this.cancel();
                        else
                            timeCountDown.postValue(new_time);
                    }
                },1000, 1000);
            }
        ));
    }
}
