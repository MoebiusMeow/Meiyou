package com.example.meiyou.model;

import android.util.Log;

import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PostSender extends NetworkBasic {
    private Post post;
    public int pid = -1;
    public PostSender(Post post){
        this.post = post;
    }

    public void send_post(boolean isPost){
        FormBody.Builder bodyBuilder = new FormBody.Builder().add("content", post.content);
        if(isPost)
            bodyBuilder.add("title", post.title);
        else
            bodyBuilder.add("pid", String.valueOf(post.pid));
        if(post.res_type != GlobalData.FILE_TYPE_NONE){
            String res_ids_str = "";
            for(Integer res_id : post.res_ids){
                res_ids_str += res_id + ";";
            }
            if(res_ids_str.length()>0)
                res_ids_str = res_ids_str.substring(0, res_ids_str.length()-1);
            Log.d("res_type", "send_post: "+ post.res_type);
            bodyBuilder = bodyBuilder
                    .add("res_type", String.valueOf(post.res_type))
                    .add("res_content", res_ids_str);
        }
        if(post.pos != null){
            bodyBuilder = bodyBuilder.add("pos", post.pos);
        }
        String url = isPost? NetworkConstant.sendPostUrl:NetworkConstant.replyPostUrl;
        NetworkConstant.post(url, bodyBuilder.build(), true,
                getCommonNetworkCallback(response -> {
                    if(response.code() != 200){
                        status.postValue(Status.wrong);
                        JSONObject jsonObject = new JSONObject(
                                Objects.requireNonNull(response.body()).string());
                        //int id = jsonObject.getInt("id");
                        Log.d("TAG", "send_post [wrong]:"+"  message="
                                +jsonObject.getString("message"));
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(
                            Objects.requireNonNull(response.body()).string());
                    if(isPost)
                        pid = jsonObject.getInt("pid");
                    else
                        pid = jsonObject.getInt("rid");
                    status.postValue(Status.success);
                }));
    }
}
