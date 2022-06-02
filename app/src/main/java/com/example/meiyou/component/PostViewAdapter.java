package com.example.meiyou.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.R;
import com.example.meiyou.model.Post;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


// Hold Post List
//
public class PostViewAdapter extends
        RecyclerView.Adapter<PostViewAdapter.PostCard>{
    private final LinkedList<PostInfo> mPostList = new LinkedList<>();
    private final LayoutInflater mInflater;
    public static final int MAX_TITLE_LEN = 20, MAX_CONTENT_LEN = 80;
    public static final int TYPE_POST_CARD = 0x01, TYPE_TAIL = 0x02, TYPE_EMPTY_TAIL = 0x03;

    public interface LoadMoreAction{
        void Onclick();
    }

    private LoadMoreAction loadMoreAction = () -> { };

    public void setOnLoadMoreAction(LoadMoreAction action){
        loadMoreAction = action;
    }

    public PostViewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mPostList.addLast(new PostInfo("点击加载更多...","", TYPE_TAIL));
    }


    // Hold Post Card data
    // Data passed to display should be process here
    public static class PostInfo{
        private final Post post;
        private int type = TYPE_POST_CARD;

        public static PostInfo fromPost(Post post){
            return new PostInfo(post);
        }

        public PostInfo(String title, String content, int type){
            post = new Post();
            setTitle(title);
            setContent(content);
            this.type = type;
        }

        public PostInfo(Post post ) {
            this.post = post;
        }
        public String getTitle(){ return post.title; }
        public String getContent(){ return post.content; }
        public int getType(){return type;}

        public void setTitle(String mTitle){
            this.post.title = mTitle.substring(0, Math.min(MAX_TITLE_LEN, mTitle.length()));
            if(mTitle.length()>MAX_TITLE_LEN){
                this.post.title = this.post.title.concat("...");
            }
        }

        public void setContent(String mContent){
            this.post.content = mContent.substring(0, Math.min(MAX_CONTENT_LEN, mContent.length()));
            if(mContent.length()>MAX_CONTENT_LEN){
                this.post.content = this.post.content.concat("...");
            }
        }

        public void setType(int type){
            if(type == TYPE_TAIL || type == TYPE_POST_CARD || type == TYPE_EMPTY_TAIL) {
                this.type = type;
                return;
            }
            this.type = TYPE_POST_CARD;
        }

        public String getPostIDString(){
            return "#"+ post.pid;
        }
    }

    class PostCard extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView titleView,contentView,loadMoreView, usernameView, datetimeView,
                pidView, zanView, commentView;
        public final ImageView userProfileView;
        final PostViewAdapter mAdapter;
        private final int mType;

        public static final String TAG_LOG = "PostViewAdapter";

        public PostCard(View itemView, PostViewAdapter adapter, int _type) {
            super(itemView);
            TextView contentView1, titleView1, loadMoreView1, usernameView1, datetimeView1,
                    pidView1, zanView1, commentView1;
            ImageView userProfileView1 = null;
            mType = _type;
            usernameView1 = titleView1 = contentView1 = loadMoreView1 = null;
            datetimeView1 = pidView1 = zanView1 = commentView1 = null;
            if(mType == TYPE_POST_CARD) {
                titleView1 = itemView.findViewById(R.id.post_card_title);
                contentView1 = itemView.findViewById(R.id.post_card_content);
                usernameView1 = itemView.findViewById(R.id.textPostUsername);
                datetimeView1 = itemView.findViewById(R.id.textDateTime);
                pidView1 = itemView.findViewById(R.id.textPostID);
                zanView1 = itemView.findViewById(R.id.textNZan);
                commentView1 = itemView.findViewById(R.id.textNReply);
                userProfileView1 = itemView.findViewById(R.id.postUserProfile);
            }
            else if(mType == TYPE_TAIL || mType == TYPE_EMPTY_TAIL){
                loadMoreView1 = itemView.findViewById(R.id.loadMoreText);
            }
            else{
                Log.d(TAG_LOG, "Unexpected type");
            }
            contentView = contentView1;
            titleView = titleView1;
            loadMoreView = loadMoreView1;
            usernameView = usernameView1;
            datetimeView = datetimeView1;
            pidView = pidView1;
            zanView = zanView1;
            commentView = commentView1;
            userProfileView = userProfileView1;
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View view) {
            if(mType == TYPE_TAIL){
                loadMoreAction.Onclick();
            }
            else{
                Log.d(TAG_LOG, "onClick: ViewHolder");
            }
        }
    }

    public void clear(){
        PostInfo mLastHolder = mPostList.getLast();
        mPostList.clear();
        mPostList.addLast(mLastHolder);
        notifyDataSetChanged();
    }

    public void addPost(PostInfo newPost){
        PostInfo mLastHolder = mPostList.getLast();
        mPostList.removeLast();
        mPostList.addLast(newPost);
        mPostList.addLast(mLastHolder);
        notifyDataSetChanged();
    }

    public void changeTail(boolean ifNoMore){
        PostInfo mLastHolder = mPostList.getLast();
        if(ifNoMore){
            mLastHolder.setType(TYPE_EMPTY_TAIL);
            mLastHolder.setTitle("没有更多内容了哦~");
        }
        else{
            mLastHolder.setType(TYPE_TAIL);
            mLastHolder.setTitle( "点击加载更多~");
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return mPostList.get(position).getType();
    }

    @NotNull
    @Override
    public PostCard onCreateViewHolder(@NonNull ViewGroup parent,
                                       int viewType) {
        // Inflate an item view.
        View tmpView;
        if(viewType == PostViewAdapter.TYPE_TAIL || viewType == PostViewAdapter.TYPE_EMPTY_TAIL){
            tmpView = mInflater.inflate(R.layout.component_loadmore, parent, false);
        }
        else {
            tmpView = mInflater.inflate(R.layout.component_postcard, parent, false);
        }
        return new PostCard(tmpView, this, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PostCard holder, int position) {
        PostInfo postInfo = mPostList.get(position);
        if(holder.mType == TYPE_POST_CARD) {
            holder.titleView.setText(postInfo.getTitle());
            holder.contentView.setText(postInfo.getContent());
            holder.pidView.setText(postInfo.getPostIDString());
            holder.commentView.setText(String.valueOf(postInfo.post.n_reply));
            holder.zanView.setText(String.valueOf(postInfo.post.n_dianzan));
            holder.usernameView.setText(postInfo.post.username);
            holder.datetimeView.setText(postInfo.post.datetime);
            if(postInfo.post.userProfileUri == null){
                holder.userProfileView.setImageResource(R.drawable.user_profile_default);
            }
            else{
                Drawable drawable = Drawable.createFromPath(postInfo.post.userProfileUri.getPath());
                holder.userProfileView.setImageDrawable(drawable);
                Log.d("Image", "set");
            }
        }
        else if(holder.mType == TYPE_TAIL || holder.mType == TYPE_EMPTY_TAIL){
            holder.loadMoreView.setText(postInfo.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

}
