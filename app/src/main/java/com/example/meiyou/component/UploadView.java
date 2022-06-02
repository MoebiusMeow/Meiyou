package com.example.meiyou.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.meiyou.R;

public class UploadView extends RelativeLayout {


    private ProgressBar progressBar;
    private TextView textName, textProgress;
    private ImageButton buttonCancel;
    private ImageView imageView;

    private MutableLiveData<Float> progress = new MutableLiveData<>();

    public UploadView(Context context, LifecycleOwner lifecycleOwner) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_upload_progress, this, true);
        progressBar = findViewById(R.id.progressBarUpload);
        textName = findViewById(R.id.textName);
        textProgress = findViewById(R.id.textProgress);
        imageView = findViewById(R.id.imageViewUpload);

        progressBar.setMax(100);

        buttonCancel = findViewById(R.id.buttonCancelUpload);
        progress.observe(lifecycleOwner, aFloat -> _setProgress(aFloat));

    }

    public interface CancelCallback{
        public void onCancel();
    }

    public void setCancelCallback(CancelCallback callback){
        buttonCancel.setOnClickListener(view -> {
            callback.onCancel();
        });
    }

    public void setImageUri(Uri uri) {
        imageView.setImageURI(uri);
    }

    public void setProgressBar(float progress){
        this.progress.postValue(progress);
    }


    private void _setProgress(float progress){
        if(progress > 1.0){
            progress = 1.0f;
        }

        int iProgress =(int)(progress*100);
        Log.d("TAG", "_setProgress: "+String.valueOf(iProgress));
        progressBar.setProgress(iProgress);
        if(progress >= 1.0)
            textProgress.setText("已上传");
        else
            textProgress.setText(String.valueOf(iProgress) + "%");
    }

    public void setName(String filename){
        textName.setText(filename);
    }


}
