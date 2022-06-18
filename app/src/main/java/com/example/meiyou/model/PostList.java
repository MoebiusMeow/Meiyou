package com.example.meiyou.model;

import static com.example.meiyou.model.Post.TYPE_REPLY;

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

    public static final int MODE_NEWEST = 0, MODE_HOT = 1, MODE_FOLLOW = 2, MODE_USER_FIX = 4,
            MODE_SINGLE_POST = 8;
    private int fix_user = 0;
    private int fix_pid = 0;

    public int len(){
        return postList.size();
    }
    public Post get(int index){
        return postList.get(index);
    }
    public void clear(){
        postList.clear();
    }
    public void setFixUser(int user){fix_user = user;}
    public void setFixPid(int pid){fix_pid = pid;}

    /* n:       how many posts pull from server
       mode:    order and filter of posts
       refresh: if request from start and clear all pulled posts

       [Note]: When internet operation is done, the public attribute <new_start>
            should be set to the index of the first(lowest) index of newly pulled posts.
     */
    public void pull_post(int n, int mode, boolean refresh){
        if(mode == MODE_SINGLE_POST){
            Log.d("SINGLE-POST", "pull_post: pid="+fix_pid+" len="+len());
            if(fix_pid == 0)return;
            if(len()<=0 || refresh){
                status.postValue(Status.idle);
                HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.getSinglePostUrl).newBuilder()
                        .addQueryParameter("pid", String.valueOf(fix_pid));
                NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
                        response -> {
                            if (response.code() != 200) {
                                if(response.code() == 404) errorCode = 404;
                                status.postValue(Status.wrong);
                                return;
                            }
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            Post post = new Post();
                            post.type = Post.TYPE_HEAD_POST;
                            post.loadFromJson(jsonObject, false);
                            postList.add(post);
                            pull_reply_as_post(n-1, refresh);
                        }));
                new_start = 0;
            }
            else{
                new_start = len();
                pull_reply_as_post(n, false);
            }
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.getMultiplePostUrl).newBuilder()
                .addQueryParameter("n", String.valueOf(n));
        if (len() > 0 && !refresh) {
            urlBuilder.addQueryParameter("start", String.valueOf(postList.get(len() - 1).pid - 1));
        }
        if ((MODE_NEWEST & mode) != 0) urlBuilder.addQueryParameter("order", "new");
        if ((MODE_HOT & mode) != 0) urlBuilder.addQueryParameter("order", "hot");
        if ((MODE_FOLLOW & mode) != 0) urlBuilder.addQueryParameter("filter", "follow");
        if ((MODE_USER_FIX & mode) != 0)
            urlBuilder.addQueryParameter("filter", String.valueOf(fix_user));
        HttpUrl url = urlBuilder.build();
        NetworkConstant.get(url.toString(), true, getCommonNetworkCallback(
                response -> {
                    if (response.code() != 200) {
                        if(response.code() == 404) errorCode = 404;
                        status.postValue(Status.wrong);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray postArray = jsonObject.getJSONArray("posts");
                    new_start = len();
                    for (int i = 0; i < postArray.length(); i++) {
                        JSONObject postObj = postArray.getJSONObject(i);
                        Post post = new Post();
                        post.loadFromJson(postObj, false);
                        postList.add(post);
                    }
                    status.postValue(Status.success);
                }
        ));
    }

    public void pull_reply_as_post(int n, boolean refresh){
        if(fix_pid <= 0)
            return;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NetworkConstant.getReplyUrl).newBuilder()
                .addQueryParameter("n", String.valueOf(n))
                .addQueryParameter("pid", String.valueOf(fix_pid));
        // Here, when use SINGLE_POST mode, the first item in post-list is a post
        // and reply start from the second
        if (len() > 1 && !refresh) {
            urlBuilder.addQueryParameter("start", String.valueOf(postList.get(len() - 1).pid - 1));
        }
        HttpUrl url = urlBuilder.build();
        NetworkConstant.get(url.toString(), true, getCommonNetworkCallback(
                response -> {
                    if (response.code() != 200) {
                        if(response.code() == 404) errorCode = 404;
                        status.postValue(Status.wrong);
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray postArray = jsonObject.getJSONArray("reply");
                    for (int i = 0; i < postArray.length(); i++) {
                        JSONObject postObj = postArray.getJSONObject(i);
                        Post post = new Post();
                        post.loadFromJson(postObj, true);
                        postList.add(post);
                    }
                    status.postValue(Status.success);
                }
        ));
    }

}
