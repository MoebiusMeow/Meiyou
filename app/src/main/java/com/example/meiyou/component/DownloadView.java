package com.example.meiyou.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.meiyou.R;

/**
 * TODO: document your custom view class.
 */
public class DownloadView extends ConstraintLayout {

    private ConstraintLayout mask;
    private ProgressBar progressBar;
    private ImageView imageView;
    private VideoView videoView;

    public DownloadView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DownloadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater.from(context).inflate(R.layout.component_download_progress, this, true);
        mask = findViewById(R.id.downloadMask);
        progressBar = findViewById(R.id.progressBarDownload);
        imageView = findViewById(R.id.imageViewDownload);
        videoView = findViewById(R.id.videoViewDownload);
    }

    public void setImageUri(Uri uri) {
        imageView.setVisibility(VISIBLE);
        imageView.setImageURI(uri);
        videoView.setVisibility(GONE);
        Log.d("TAG", "setImageUri: Set!");
    }

    public void setVideoUri(Uri uri) {
        videoView.setVisibility(VISIBLE);
        imageView.setVisibility(GONE);
        mask.setVisibility(VISIBLE);
        progressBar.setVisibility(VISIBLE);
        videoView.setOnPreparedListener(mediaPlayer -> {
            hideMask();
        });
        /*videoView.setOnClickListener(view -> {
            if (videoView.getDuration() == 0)
                return;
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
                Log.d("TAG", "start video");
            }
        });*/
        videoView.setVideoURI(uri);

        MediaController controller = new MediaController(this.getContext());
        videoView.setMediaController(controller);
        controller.setAnchorView(this);
        controller.setMediaPlayer(videoView);

        videoView.start();
        videoView.requestFocus();
        Log.d("TAG", "setVideoUri: Set!" + uri.toString());
    }

    public void setImageDrawable(Drawable drawable){
        imageView.setImageDrawable(drawable);
    }

    public void setImageResource(@DrawableRes int id){
        imageView.setImageResource(id);
    }

    public void hideMask(){
        mask.setVisibility(INVISIBLE);
        progressBar.setVisibility(INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            height = width;
            int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, h);
            Log.d("TAG", "onMeasure: "+width+" "+height);
        } else {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}