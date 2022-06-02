package com.example.meiyou.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ComponentLoadmoreBinding;
import com.example.meiyou.databinding.ComponentPostcardBinding;
import com.example.meiyou.databinding.ComponentUploadProgressBinding;
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
    public static final int STYLE_STANDARD = 0x01, STYLE_NOT_PUBLISHED = 0x02;

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
        private int style = STYLE_STANDARD;

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

        public void setStyle(int style){
            this.style = style;
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

        public ComponentPostcardBinding postCardBinding = null;

        public ComponentLoadmoreBinding loadMoreBinding = null;

        final PostViewAdapter mAdapter;
        private final int mType;

        public static final String TAG_LOG = "PostViewAdapter";

        public PostCard(View itemView, PostViewAdapter adapter, int _type) {
            super(itemView);
            if(_type == TYPE_POST_CARD)
                postCardBinding = ComponentPostcardBinding.bind(itemView);
            if(_type == TYPE_EMPTY_TAIL || _type == TYPE_TAIL)
                loadMoreBinding = ComponentLoadmoreBinding.bind(itemView);
            mType = _type;
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        public void bindPostInfo(PostInfo postInfo){
            if(mType == TYPE_POST_CARD && postCardBinding !=null) {
                postCardBinding.postCardTitle.setText(postInfo.getTitle());
                postCardBinding.postCardContent.setText(postInfo.getContent());
                postCardBinding.textPostID.setText(postInfo.getPostIDString());
                postCardBinding.textNReply.setText(String.valueOf(postInfo.post.n_reply));
                postCardBinding.textNZan.setText(String.valueOf(postInfo.post.n_dianzan));
                postCardBinding.textPostUsername.setText(postInfo.post.username);
                postCardBinding.textDateTime.setText(postInfo.post.datetime);
                if (postInfo.post.userProfileUri == null) {
                    postCardBinding.postUserProfile.setImageResource(R.drawable.user_profile_default);
                } else {
                    Drawable drawable = Drawable.createFromPath(postInfo.post.userProfileUri.getPath());
                    postCardBinding.postUserProfile.setImageDrawable(drawable);
                }
            }
            if(postInfo.style == STYLE_NOT_PUBLISHED){
                postCardBinding.FootArea.setVisibility(View.GONE);
                postCardBinding.postUserProfile.setVisibility(View.GONE);
                postCardBinding.textPostUsername.setVisibility(View.GONE);
                postCardBinding.textPostID.setVisibility(View.GONE);
            }
        }

        public void bindTailTitle(String title){
            if((mType == TYPE_EMPTY_TAIL || mType == TYPE_TAIL) && loadMoreBinding !=null ){
                loadMoreBinding.loadMoreText.setText(title);
            }
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
            holder.bindPostInfo(postInfo);
        }
        else if(holder.mType == TYPE_TAIL || holder.mType == TYPE_EMPTY_TAIL){
            holder.bindTailTitle(postInfo.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

}
