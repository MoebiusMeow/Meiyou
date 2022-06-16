package com.example.meiyou.utils;

import android.util.Log;

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
    public static final int N_RETRY = 3;

    public interface NetworkCallback{
        public void run(Response response) throws JSONException, IOException;
    }

    public Callback getCommonNetworkCallback(NetworkCallback func){
        return new Callback() {
            private int retry = N_RETRY;
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if(this.retry <=0){
                    Log.e("Network", "onResponse: ", e);
                    status.postValue(Status.fail);
                }
                else {
                    Log.d("Network", "retry a request ... ");
                    this.retry -= 1;
                    call.clone().enqueue(this);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try{
                    func.run(response);
                }
                catch (JSONException e){
                    Log.e("Network", "onResponse: <JSONError>", e);
                    status.postValue(Status.wrong);
                }
                catch(Exception e){
                    if(this.retry <=0){
                        Log.e("Network", "onResponse: ", e);
                        status.postValue(Status.wrong);
                    }
                    else {
                        Log.d("Network", "retry a request ... ");
                        this.retry -= 1;
                        call.clone().enqueue(this);
                    }
                }
            }
        };
    }



}
