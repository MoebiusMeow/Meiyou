package com.example.meiyou.component;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.R;

public class UploadView extends RelativeLayout {


    private ProgressBar progressBar;
    private TextView textName, textProgress;
    private ImageButton buttonCancel;
    private ImageView imageView;
    private ConstraintLayout mask;

    private MutableLiveData<Float> progress = new MutableLiveData<>();

    public UploadView(Context context, LifecycleOwner lifecycleOwner) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_upload_progress, this, true);
        progressBar = findViewById(R.id.progressBarUpload);
        textName = findViewById(R.id.textName);
        textProgress = findViewById(R.id.textProgress);
        imageView = findViewById(R.id.imageViewUpload);
        mask = findViewById(R.id.uploadMask);

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
        Log.d("TAG", "setImageUri: Set!");
    }

    public void setProgressBar(float progress){
        this.progress.postValue(progress);
    }

    public void setCancelButtonVisible(boolean if_visible){
        if(if_visible) buttonCancel.setVisibility(VISIBLE);
        else buttonCancel.setVisibility(INVISIBLE);
    }


    private void _setProgress(float progress){
        if(progress > 1.0){
            progress = 1.0f;
        }

        int iProgress =(int)(progress*100);
        Log.d("TAG", "_setProgress: "+String.valueOf(iProgress));
        progressBar.setProgress(iProgress);
        if(progress >= 1.0) {
            textProgress.setText("已上传");
            mask.setVisibility(View.INVISIBLE);
        }
        else
            textProgress.setText(String.valueOf(iProgress) + "%");
    }

    public void setName(String filename){
        Log.d("TAG", "setName: " + imageView.getHeight());
        textName.setText(filename);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //如果宽度指定特定值，并且高度未指定特定值（让高度等于宽度就保证了宽高相等）
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width;
            if (heightMode == MeasureSpec.AT_MOST) {//这里还考虑了高度受上限的情况（比如父容器固定了高度）
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            //setMeasuredDimension(500, 500);
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, h);
            Log.d("TAG", "onMeasure: "+width+" "+height);
        } else {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
