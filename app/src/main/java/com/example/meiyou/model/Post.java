package com.example.meiyou.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.example.meiyou.R;


public class Post {
    public int pid;
    public String title = "";
    public String content = "";
    public String username = "";
    public String datetime = "";
    public int n_dianzan = 0;
    public int n_reply = 0;
    public int profile_id = -1;
    public Uri userProfileUri = null;


    public void get_single_post(){
        // discarded uwu
    }
}
