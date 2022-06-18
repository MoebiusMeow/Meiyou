package com.example.meiyou.model;

import static com.example.meiyou.utils.GlobalData.FILE_TYPE_NONE;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.example.meiyou.R;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;


public class Post extends NetworkBasic implements Serializable {
    public int pid;
    public int uid;
    public String title = "";
    public String content = "";
    public int res_type = FILE_TYPE_NONE;
    public ArrayList<Integer> res_ids = new ArrayList<>();
    public ArrayList<Uri> res_uri_list = new ArrayList<>();
    public String pos = null;
    public String username = "";
    public String datetime = "";
    public String zanDetail = "";
    public boolean if_zan = false;
    public int n_dianzan = 0;
    public int n_reply = 0;
    public int profile_id = -1;
    public Uri userProfileUri = null;
    public static final int TYPE_POST = 0, TYPE_REPLY = 1, TYPE_HEAD_POST = 2;
    public int type = TYPE_POST;

    private static final long serialVersionUID = 19260817L;


    public void loadFromJson(JSONObject postObj, boolean isReply) throws JSONException {
        if(isReply)
            type = TYPE_REPLY;

        if(isReply){
            this.pid = postObj.getInt("rid");
            this.title = "";
            this.n_reply = 0;
            this.n_dianzan = 0;
        }
        else{
            this.pid = postObj.getInt("pid");
            this.title = postObj.getString("title");
            this.n_dianzan = postObj.getInt("dianzan");
            this.n_reply = postObj.getInt("nreply");
            this.zanDetail = postObj.getString("dianzandetail");
            this.if_zan = postObj.getBoolean("if_zan");
            Log.d("Detail", "loadFromJson: "+zanDetail);
        }

        this.content = postObj.getString("content");
        this.username = postObj.getString("username");
        this.uid = postObj.getInt("uid");
        this.datetime = postObj.getString("datetime");
        try{
            this.pos = postObj.getString("pos");
            if(this.pos.equals("null")) this.pos = null;
        }
        catch (Exception e){
            this.pos = null;
        }

        String post_id_str = postObj.getString("resids");
        if (post_id_str != null && !post_id_str.equals("null")) {
            ArrayList<Integer> res_ids = new ArrayList<>();
            for (String id_str : post_id_str.split(";")) {
                if (!id_str.isEmpty() && isNumeric(id_str)) {
                    res_ids.add(Integer.valueOf(id_str));
                }
            }
            this.res_ids = res_ids;
        }
        else
            this.res_ids = null;

        String post_res_type = postObj.getString("restype");
        if (post_res_type != null && !post_res_type.equals("null")) {
            this.res_type = Integer.valueOf(post_res_type);
        }
        else
            this.res_type = FILE_TYPE_NONE;

        if (!postObj.isNull("userprofileid"))
            this.profile_id = postObj.getInt("userprofileid");
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    public void set_dianzan(int flag) {
        status.postValue(Status.idle);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.setDianzanUrl).newBuilder()
                .addQueryParameter("flag", String.valueOf(flag))
                .addQueryParameter("pid", String.valueOf(pid));
        HttpUrl url = urlBuilder.build();
        NetworkConstant.get(url.toString(), true, getCommonNetworkCallback(
                response -> {
                    if (response.code() != 200) {
                        status.postValue(Status.wrong);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    this.if_zan = jsonObject.getBoolean("if_zan");
                    this.n_dianzan = jsonObject.getInt("dianzan");
                    this.zanDetail = jsonObject.getString("dianzandetail");
                    status.postValue(Status.success);
                }
        ));
    }

    public void request_remove(){
        status.postValue(Status.idle);
        String url = type == TYPE_REPLY? NetworkConstant.removeReplyUrl
                : NetworkConstant.removePostUrl;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder()
                .addQueryParameter(type == TYPE_REPLY? "rid":"pid", String.valueOf(pid));
        NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
                response -> {
                    if (response.code() != 200) {
                        status.postValue(Status.wrong);
                        return;
                    }
                    status.postValue(Status.success);
                }
        ));
    }
}
