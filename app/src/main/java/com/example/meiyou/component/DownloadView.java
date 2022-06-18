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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.meiyou.R;

import java.io.IOException;

/**
 * TODO: document your custom view class.
 */
public class DownloadView extends ConstraintLayout {

    private ConstraintLayout mask;
    private ProgressBar progressBar;
    private ImageView imageView;
    private VideoView videoView;
    private float ratio = 1.0f;
    private MediaController controller = null;
    MediaPlayer player = null;

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

    public void setRatio(float r) {
        ratio = r;
    }

    public void setScaleType(ImageView.ScaleType type) {
        imageView.setScaleType(type);
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

        controller = new MediaController(this.getContext());
        videoView.setMediaController(controller);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);

        videoView.start();
        videoView.requestFocus();
        Log.d("TAG", "setVideoUri: Set!" + uri.toString());
    }

    public void setAudioUri(Uri uri) {
        player = new MediaPlayer();
        imageView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                controller.hide();
                if (player.isPlaying()) {
                    player.pause();
                }
            }
        });
        player.setOnPreparedListener(mediaPlayer -> {
            controller = new MediaController(this.getContext());
            imageView.setFocusable(true);
            imageView.setOnClickListener(view -> {
                try {
                    controller.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            controller.setAnchorView(imageView);
            controller.setMediaPlayer(new MediaController.MediaPlayerControl() {
                @Override public void start() { player.start(); }
                @Override public void pause() { player.stop(); }
                @Override public int getDuration() { return player.getDuration(); }
                @Override public int getCurrentPosition() { return player.getCurrentPosition(); }
                @Override public void seekTo(int i) { player.seekTo(i); }
                @Override public boolean isPlaying() { return player.isPlaying(); }
                @Override public int getBufferPercentage() { return 100; }
                @Override public boolean canPause() { return true; }
                @Override public boolean canSeekBackward() { return true; }
                @Override public boolean canSeekForward() { return true; }
                @Override public int getAudioSessionId() { return player.getAudioSessionId(); }
            });
        });
        try {
            Log.d("meow", uri.toString());
            player.setDataSource(getContext(), uri);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void doPause() {
        if (player != null) {
            player.pause();
        }
    }

    public void clear() {
        if (controller != null) {
            controller.hide();
            player.release();
            controller = null;
            player = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            height = (int)(width * ratio);
            int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, h);
            Log.d("TAG", "onMeasure: "+width+" "+height);
        } else {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void finalize()
    {
        clear();
    }
}