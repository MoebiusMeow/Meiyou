package com.example.meiyou.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.example.meiyou.R;
import com.example.meiyou.utils.GlobalData;

import java.io.Serializable;
import java.util.ArrayList;


public class Post implements Serializable {
    public int pid;
    public String title = "";
    public String content = "";
    public int res_type = GlobalData.FILE_TYPE_NONE;
    public ArrayList<Integer> res_ids = new ArrayList<>();
    public ArrayList<Uri> res_uri_list = new ArrayList<>();
    public String pos = null;
    public String username = "";
    public String datetime = "";
    public int n_dianzan = 0;
    public int n_reply = 0;
    public int profile_id = -1;
    public Uri userProfileUri = null;

    private static final long serialVersionUID = 19260817L;


    public void get_single_post(){
        // discarded uwu
    }
}
