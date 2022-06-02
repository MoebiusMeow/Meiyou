package com.example.meiyou.model;

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

    public void send_post(){
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("content", post.content)
                .add("title", post.title);
        if(post.res_type != GlobalData.FILE_TYPE_NONE){
            String res_ids_str = "";
            for(Integer res_id : post.res_ids){
                res_ids_str += res_id + ";";
            }
            if(res_ids_str.length()>0)
                res_ids_str = res_ids_str.substring(0, res_ids_str.length()-1);
            bodyBuilder = bodyBuilder
                    .add("res_type", String.valueOf(post.res_type))
                    .add("res_content", res_ids_str);
        }
        if(post.pos != null){
            bodyBuilder = bodyBuilder.add("pos", post.pos);
        }
        NetworkConstant.post(NetworkConstant.sendPostUrl, bodyBuilder.build(), true,
                getCommonNetworkCallback(response -> {
                    if(response.code() != 200){
                        status.postValue(Status.wrong);
                    }
                    JSONObject jsonObject = new JSONObject(
                            Objects.requireNonNull(response.body()).string());
                    pid = jsonObject.getInt("pid");
                    status.postValue(Status.success);
                }));
    }
}
