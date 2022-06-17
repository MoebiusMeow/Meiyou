package com.example.meiyou.utils;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.meiyou.control.FileDownloader;

import java.util.HashMap;
import java.util.Map;


public class GlobalResFileManager extends Activity {
    private static HashMap<Integer, MutableLiveData<Uri>> resFileDict = new HashMap<>();
    public interface ResFileCallback{
        public void onDone(Uri uri);
    }
    public static synchronized void requestFile(LifecycleOwner lifecycleOwner, int res_id, ResFileCallback callback){
        if(resFileDict.containsKey(res_id)){
            if(resFileDict.get(res_id).getValue() != null)
                callback.onDone(resFileDict.get(res_id).getValue());
            else
                resFileDict.get(res_id).observe(lifecycleOwner, uri1 -> callback.onDone(uri1));
        }
        else{
            FileDownloader fileDownloader  = new FileDownloader();
            MutableLiveData<Uri> uri = new MutableLiveData<>();
            uri.observe(lifecycleOwner, uri1 -> callback.onDone(uri1));
            resFileDict.put(res_id, uri);

            fileDownloader.status.observe(lifecycleOwner, status -> {
                if(status == NetworkBasic.Status.success){
                    Log.d("Networkdcy", "requestFile: success");
                    uri.postValue(fileDownloader.result);
                }
            });

            fileDownloader.get(res_id);
        }
    }
}
