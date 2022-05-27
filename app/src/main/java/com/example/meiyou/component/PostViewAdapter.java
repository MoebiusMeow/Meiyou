package com.example.meiyou.component;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.R;
import com.example.meiyou.model.Post;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


// Hold Post List
public class PostViewAdapter extends
        RecyclerView.Adapter<PostViewAdapter.PostViewHolder>{
    private final LinkedList<PostInfo> mPostList = new LinkedList<PostInfo>();
    private final LayoutInflater mInflater;
    public static final int MAX_TITLE_LEN = 20, MAX_CONTENT_LEN = 80;
    public static final int TYPE_POST_CARD = 0x01, TYPE_TAIL = 0x02;

    public interface LoadMoreAction{
        public void Onclick();
    }

    private LoadMoreAction loadMoreAction = new LoadMoreAction() {
        @Override public void Onclick() { }
    };

    public void setOnLoadMoreAction(LoadMoreAction action){
        loadMoreAction = action;
    }

    public PostViewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mPostList.addLast(new PostInfo("点击加载更多...","", TYPE_TAIL));
    }


    // Hold Post Card data
    public static class PostInfo{
        private String mTitle = "";
        private String mContent = "";
        private int mtype = TYPE_POST_CARD;

        public static PostInfo fromPost(Post post){
            return new PostInfo(post.title, post.content, TYPE_POST_CARD);
        }

        public PostInfo(String mTitle, String mContent, int mType ) {
            this.setTitle(mTitle);
            this.setContent(mContent);
            this.setType(mType);
        }
        public String getTitle(){ return mTitle; }
        public String getContent(){ return mContent; }
        public int getType(){return mtype;}

        public boolean setTitle(String mTitle){
            this.mTitle = mTitle.substring(0, Math.min(MAX_TITLE_LEN, mTitle.length()));
            if(mTitle.length()>MAX_TITLE_LEN){
                this.mTitle = this.mTitle.concat("...");
                return true;
            }
            return false;
        }

        public  boolean setContent(String mContent){
            this.mContent = mContent.substring(0, Math.min(MAX_CONTENT_LEN, mContent.length()));
            if(mContent.length()>MAX_CONTENT_LEN){
                this.mTitle = this.mTitle.concat("...");
                return true;
            }
            return false;
        }

        public  boolean setType(int mtype){
            if(mtype == TYPE_TAIL || mtype == TYPE_POST_CARD) {
                this.mtype = mtype;
                return false;
            }
            this.mtype = TYPE_POST_CARD;
            return true;
        }
    }

    class PostViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final TextView titleView,contentView,loadMoreView;
        final PostViewAdapter mAdapter;
        private final int mType;

        public static final String TAG_LOG = "PostViewAdapter";

        public PostViewHolder(View itemView, PostViewAdapter adapter, int _type) {
            super(itemView);
            TextView contentView1, titleView1, loadMoreView1;
            mType = _type;
            titleView1 = contentView1 = loadMoreView1 = null;
            if(mType == TYPE_POST_CARD) {
                titleView1 = itemView.findViewById(R.id.post_card_title);
                contentView1 = itemView.findViewById(R.id.post_card_content);
            }
            else if(mType == TYPE_TAIL){
                loadMoreView1 = itemView.findViewById(R.id.loadMoreText);
            }
            else{
                Log.d(TAG_LOG, "Unexpected type");
            }
            contentView = contentView1;
            titleView = titleView1;
            loadMoreView = loadMoreView1;
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View view) {
            if(mType == TYPE_TAIL){
                loadMoreAction.Onclick();
                /*
                PostInfo mLastHolder = mPostList.getLast();
                mPostList.removeLast();
                for(int i=0; i<10 && mPostList.size()<20; i++)
                    mPostList.addLast(new PostInfo("标题"+ (mPostList.size() + 1), "测试样例内容", TYPE_POST_CARD));
                if(mPostList.size() == 20){
                    mLastHolder.mTitle = "没有更多内容了哦~";
                }
                mPostList.addLast(mLastHolder);
                mAdapter.notifyDataSetChanged();
                */
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
            mLastHolder.mTitle = "没有更多内容了哦~";
        }
        else{
            mLastHolder.mTitle = "点击加载更多~";
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return mPostList.get(position).getType();
    }

    @NotNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
        // Inflate an item view.
        View tmpView;
        if(viewType == PostViewAdapter.TYPE_TAIL){
            tmpView = mInflater.inflate(R.layout.component_loadmore, parent, false);
        }
        else {
            tmpView = mInflater.inflate(R.layout.component_postcard, parent, false);
        }
        return new PostViewHolder(tmpView, this, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        String mTitle = mPostList.get(position).getTitle();
        String mContent = mPostList.get(position).getContent();
        if(holder.mType == TYPE_POST_CARD) {
            holder.titleView.setText(mTitle);
            holder.contentView.setText(mContent);
        }
        else if(holder.mType == TYPE_TAIL){
            holder.loadMoreView.setText(mTitle);
        }
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

}
