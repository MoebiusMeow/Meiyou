package com.example.meiyou.model;

import android.util.Log;

import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;

public class PostList extends NetworkBasic {
    private ArrayList<Post> postList =  new ArrayList<Post>();
    public int nLastGet = 0;
    public int new_start = 0;

    public static final int MODE_NEWEST = 0, MODE_HOT = 1, MODE_FOLLOW = 2;

    public int len(){
        return postList.size();
    }
    public Post get(int index){
        return postList.get(index);
    }
    public void clear(){
        postList.clear();
    }

    public void pull_post(int n, int mode, boolean refresh){
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.getMultiplePost).newBuilder()
            .addQueryParameter("n", String.valueOf(n));
        if(len() > 0 && !refresh){
            urlBuilder.addQueryParameter("start", String.valueOf(postList.get(len()-1).pid -1));
        }
        switch (mode){
            case MODE_NEWEST:   urlBuilder.addQueryParameter("order", "new"); break;
            case MODE_HOT:      urlBuilder.addQueryParameter("order", "hot"); break;
            case MODE_FOLLOW:   urlBuilder.addQueryParameter("order", "followednew");
                break;
        }
        HttpUrl url = urlBuilder.build();
        NetworkConstant.get(url.toString(), true, getCommonNetworkCallback(
            response -> {
                if(response.code()!=200){
                    status.postValue(Status.wrong);
                    return;
                }
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray postArray = jsonObject.getJSONArray("posts");
                new_start = len();
                Log.d("NETDCY", "pull_post: " + String.valueOf(postArray.length()));
                for (int i=0; i< postArray.length(); i++){
                    JSONObject postObj = postArray.getJSONObject(i);
                    Post post = new Post();
                    post.pid = postObj.getInt("pid");
                    post.title = postObj.getString("title");
                    post.content = postObj.getString("content");
                    post.username = postObj.getString("username");
                    post.n_dianzan = postObj.getInt("dianzan");
                    post.n_reply = postObj.getInt("nreply");
                    postList.add(post);
                }
                status.postValue(Status.success);
            }
        ));
    }
}
