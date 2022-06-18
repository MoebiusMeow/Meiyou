package com.example.meiyou.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.example.meiyou.control.RegisterControl;
import com.example.meiyou.model.DraftList;
import com.example.meiyou.model.MainUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalData extends Application {
    private static Context context = null;
    private static MainUser mainUser= new MainUser();
    private static RegisterControl registerControl= new RegisterControl();

    public static final int FILE_TYPE_NONE  = 0;
    public static final int FILE_TYPE_IMG   = 10;
    public static final int FILE_TYPE_VID   = 20;
    public static final int FILE_TYPE_AUD   = 30;


    public static DraftList draftList;


    // Used to notify main activity to switch to post list fragment when new post send
    public static MutableLiveData<Integer> sig_post = new MutableLiveData<>();
    public static final Integer SIG_POST_SEND = 1, SIG_POST_NOTHING = 0, SIG_POST_DELETE = 2;

    public static MutableLiveData<Integer> sig_refresh = new MutableLiveData<>();
    public static final Integer SIG_FORCE_REFRESH = 1, SIG_NO_REFRESH = 0;

    public static MutableLiveData<Integer> sig_to_home = new MutableLiveData<>();
    public static final Integer SIG_TO_HOME_DO = 1, SIG_TO_HOME_IDLE = 0;


    @Override
    public void onCreate(){
        super.onCreate();
        context=getApplicationContext();
        draftList = new DraftList(new File(getFilesDir(), "drafts.data"));
        sig_refresh.postValue(SIG_NO_REFRESH);
    }
    public static Context getContext(){return context;}
    public static MainUser getUser(){
        return mainUser;
    }
    public static RegisterControl getRegisterControl(){return registerControl;}


    public static ColorStateList createColorStateList(int pressed, int normal) {
        //状态
        int[][] states = new int[2][];
        //按下
        states[0] = new int[] {android.R.attr.state_pressed};
        //默认
        states[1] = new int[] {};

        //状态对应颜色值（按下，默认）
        int[] colors = new int[] { pressed, normal};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }




}
