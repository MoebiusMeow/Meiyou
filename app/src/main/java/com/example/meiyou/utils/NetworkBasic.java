package com.example.meiyou.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NetworkBasic {
    public enum Status{
        idle,
        success,
        fail,
        wrong,
    }
    public MutableLiveData<Status> status = new MutableLiveData<Status>(Status.idle);
    public int errorCode;

    public interface NetworkCallback{
        public void run(Response response) throws JSONException, IOException;
    }

    public Callback getCommonNetworkCallback(NetworkCallback func){
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                status.postValue(Status.fail);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try{
                    func.run(response);
                }
                catch(Exception e){
                    status.postValue(Status.fail);
                }
            }
        };
    }



}
