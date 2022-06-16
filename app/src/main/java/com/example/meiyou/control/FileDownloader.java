package com.example.meiyou.control;

import android.net.Uri;
import android.util.Log;

import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import java.io.File;

import okhttp3.HttpUrl;
import okio.BufferedSink;
import okio.Okio;

public class FileDownloader extends NetworkBasic {
    public Uri result = null;

    public void get(int res_id) {
        HttpUrl profileDownloadUrl = HttpUrl.parse(NetworkConstant.downloadUrl).newBuilder()
                .addQueryParameter("resid", String.valueOf(res_id))
                .build();
        NetworkConstant.get(profileDownloadUrl.toString(), true, getCommonNetworkCallback(
                response1 -> {

                    String[] temp = NetworkConstant.getHeaderFileName(response1).split("\\.");

                    String extension = temp[temp.length - 1];
                    File outputFile = File.createTempFile("temp", extension,
                            GlobalData.getContext().getCacheDir());
                    BufferedSink sink = Okio.buffer(Okio.sink(outputFile));

                    sink.writeAll(response1.body().source());
                    sink.close();
                    result = Uri.fromFile(outputFile);
                    Log.d("Download", "pull_post: " + result.toString());
                    status.postValue(Status.success);
                }
        ));
    }

}
