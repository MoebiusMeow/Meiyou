package com.example.meiyou.control;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.MultipartBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class FileUploader extends NetworkBasic {
    public int result_res_id = -1;

    private ContentResolver contentResolver;

    public FileUploader(ContentResolver contentResolver){
        this.contentResolver = contentResolver;
    }

    private static class CountingFileRequestBody extends RequestBody {

        private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

        private final InputStream file;
        private final ProgressListener listener;
        private final String contentType;

        public CountingFileRequestBody(InputStream file, String contentType, ProgressListener listener) {
            this.file = file;
            this.contentType = contentType;
            this.listener = listener;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(contentType);
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = Okio.source(file);
            long total = 0, read;
            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                this.listener.transferred((float) total/(total + file.available()));
            }
        }
        public interface ProgressListener { void transferred(float num);}
    }

    /*private String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }*/

    // Upload file to server
    public Call put(Uri uri, String type, CountingFileRequestBody.ProgressListener listener) throws FileNotFoundException {
        //Log.d("uwu", contentResolver.getType(uri));
        if (contentResolver.getType(uri) == null)
            throw new FileNotFoundException(uri.toString());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "uwu." + contentResolver.getType(uri).split("/")[1], new CountingFileRequestBody(
                        contentResolver.openInputStream(uri), type, listener
                ))
                .build();
        Call call = NetworkConstant.post(NetworkConstant.uploadUrl, requestBody,
                true, getCommonNetworkCallback(
                response -> {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    result_res_id = jsonObject.getInt("resid");
                    status.postValue(Status.success);
                }
        ));
        return call;
    }
}
