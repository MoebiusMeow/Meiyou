package com.example.meiyou.component;

import static com.example.meiyou.model.Post.TYPE_HEAD_POST;
import static com.example.meiyou.model.Post.TYPE_REPLY;
import static com.example.meiyou.utils.GlobalData.FILE_TYPE_AUD;
import static com.example.meiyou.utils.GlobalData.FILE_TYPE_IMG;
import static com.example.meiyou.utils.GlobalData.FILE_TYPE_NONE;
import static com.example.meiyou.utils.GlobalData.FILE_TYPE_VID;
import static com.example.meiyou.utils.GlobalData.getContext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ComponentLoadmoreBinding;
import com.example.meiyou.databinding.ComponentPostcardBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;


// Hold Post List
//
public class PostViewAdapter extends
        RecyclerView.Adapter<PostViewAdapter.PostCard>{
    private final LinkedList<PostInfo> postInfoList = new LinkedList<>();
    private final LayoutInflater mInflater;
    private LifecycleOwner pLifecycle;
    public static final int MAX_TITLE_LEN = 20, MAX_CONTENT_LEN = 80;
    public static final int TYPE_POST_CARD = 0x01, TYPE_TAIL = 0x02, TYPE_EMPTY_TAIL = 0x03;
    public static final int STYLE_STANDARD = 0x01, STYLE_NOT_PUBLISHED = 0x02;

    public interface LoadMoreAction{
        void Onclick();
    }
    public interface ClickedPostcardAction{
        void Onclick(PostInfo postInfo);
    }
    public interface ClickedDeleteAction{
        void Onclick(PostInfo postInfo);
    }

    private LoadMoreAction loadMoreAction = () -> { };
    private ClickedPostcardAction clickedPostcardAction = postInfo -> {
        Log.d("PostInfo", ": res_id="+postInfo.post.res_ids);
    };

    public void setOnLoadMoreAction(LoadMoreAction action){
        loadMoreAction = action;
    }

    public void setOnClickedPost(ClickedPostcardAction action){ clickedPostcardAction = action; }

    public PostViewAdapter(Context context, LifecycleOwner lifecycle) {
        mInflater = LayoutInflater.from(context);
        postInfoList.addLast(new PostInfo("点击加载更多...","", TYPE_TAIL));
        pLifecycle = lifecycle;
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
        public Post getPost(){return post;}

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
        private PostInfo postInfo;

        Context parentContext;

        public static final String TAG_LOG = "PostViewAdapter";

        private ArrayList<DownloadView> attatchedViewList = new ArrayList<>();
        private int resType = FILE_TYPE_NONE;

        public PostCard(View itemView, PostViewAdapter adapter, Context context, int _type) {
            super(itemView);
            parentContext = context;
            if(_type == TYPE_POST_CARD)
                postCardBinding = ComponentPostcardBinding.bind(itemView);
            if(_type == TYPE_EMPTY_TAIL || _type == TYPE_TAIL)
                loadMoreBinding = ComponentLoadmoreBinding.bind(itemView);
            mType = _type;
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        public void bindPostInfo(PostInfo postInfo){
            this.postInfo = postInfo;
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
                    Log.d("TAG", "bindPostInfo: bind post profile"+postInfo.post.userProfileUri.getPath());
                    Drawable drawable = Drawable.createFromPath(postInfo.post.userProfileUri.getPath());
                    postCardBinding.postUserProfile.setImageDrawable(drawable);
                }
            }
            if(postInfo.style == STYLE_NOT_PUBLISHED) {
                postCardBinding.FootArea.setVisibility(View.GONE);
                postCardBinding.postUserProfile.setVisibility(View.GONE);
                postCardBinding.textPostUsername.setVisibility(View.GONE);
                postCardBinding.textPostID.setVisibility(View.GONE);
            }
            if(postInfo.post.type != TYPE_HEAD_POST){
                postCardBinding.textViewZanlist.setVisibility(View.GONE);
            }
            if(postInfo.post.type == TYPE_REPLY){
                postCardBinding.postCardTitle.setVisibility(View.GONE);
                postCardBinding.FootArea.setVisibility(View.GONE);
            }
            if(postInfo.post.type == TYPE_HEAD_POST){
                postCardBinding.postCardTitle.setTextSize(24);
                String dianzanDetail = postInfo.post.zanDetail;
                postCardBinding.textViewZanlist.setText(dianzanDetail);
                postCardBinding.replyNumberLayout.setVisibility(View.GONE);
            }
            if (postCardBinding.imageView3.getVisibility() == View.VISIBLE) {
                setDianzan(postInfo.post.if_zan);
                postCardBinding.imageView3.setOnClickListener(view -> {
                    postInfo.post.set_dianzan(postInfo.post.if_zan ? 0 : 1);
                });
                postInfo.post.status.observe(pLifecycle, status -> {
                    if (status == NetworkBasic.Status.success) {
                        setDianzan(postInfo.post.if_zan);
                    }
                });
            }
            if (postInfo.post.uid == GlobalData.getUser().uid){
                postCardBinding.layoutToDelete.setVisibility(View.VISIBLE);
            }
            else{
                postCardBinding.layoutToDelete.setVisibility(View.INVISIBLE);
            }
        }

        public void createAttachmentView(){
            Context context = parentContext;
            if(postInfo != null && postInfo.post != null && postInfo.post.res_ids != null) {
                ArrayList<Integer> id_list = postInfo.post.res_ids;
                int type = postInfo.post.res_type;
                if (type != resType) {
                    resType = type;
                    int n = id_list.size();
                    if (n > 0) {
                        GridLayout gridLayout = postCardBinding.postResGrid;
                        gridLayout.removeAllViews();
                        attatchedViewList.clear();
                        for (int i = 0; i < n; i++) {
                            DownloadView downloadView = new DownloadView(context);
                            downloadView.setImageResource(
                                    type == FILE_TYPE_IMG ? R.drawable.defaultimage :
                                    type == FILE_TYPE_VID ? R.drawable.video :
                                    type == FILE_TYPE_AUD ? R.drawable.audio :
                                    R.drawable.ic_dashboard_black_24dp);
                            if (type == FILE_TYPE_AUD) {
                                downloadView.setRatio(0.2f);
                                downloadView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            }

                            attatchedViewList.add(downloadView);
                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 0;
                            params.rowSpec = GridLayout.spec((int)(i/3),1,1f);
                            params.columnSpec = GridLayout.spec((int)(i%3),1,1f);
                            params.setMargins(2,2,2,2);
                            gridLayout.addView(downloadView, params);
                        }
                    }
                }
            }
        }

        public void updateAttatchmentView(){
            if(postInfo != null && postInfo.post != null) {
                ArrayList<Uri> uri_list = postInfo.post.res_uri_list;
                Log.d("TAG", uri_list.toString());
                for (int i = 0; i < attatchedViewList.size(); i++) {
                    if(i>=uri_list.size())break;
                    DownloadView view = attatchedViewList.get(i);
                    Uri uri = uri_list.get(i);
                    if (uri != null) {
                        //Log.d("Post Attatcj=h", "updateAttatchmentView: "+uri.getPath());
                        //Drawable drawable = Drawable.createFromPath(uri.getPath());
                        //view.setImageDrawable(drawable);
                        Log.d("TAG", String.valueOf(postInfo.post.res_type));
                        view.hideMask();
                        if (postInfo.post.res_type == FILE_TYPE_IMG)
                            view.setImageUri(uri);
                        else if (postInfo.post.res_type == FILE_TYPE_VID)
                            view.setVideoUri(uri);
                        else if (postInfo.post.res_type == FILE_TYPE_AUD) {
                            view.setImageResource(R.drawable.music);
                            view.setAudioUri(uri);
                            //view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }
                }
            }
        }

        public void setDianzan(boolean if_zan) {
            if (if_zan) {
                postCardBinding.imageView3.setImageTintList(parentContext.getColorStateList(R.color.pink_500));
            } else {
                postCardBinding.imageView3.setImageTintList(parentContext.getColorStateList(R.color.green_400));
            }
            postCardBinding.textNZan.setText(String.valueOf(postInfo.post.n_dianzan));
            postCardBinding.textViewZanlist.setText(String.format("%s", postInfo.post.zanDetail));
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
            } else if (mType != TYPE_EMPTY_TAIL) {
                clickedPostcardAction.Onclick(this.postInfo);
            }
        }
    }

    public void clear(){
        PostInfo mLastHolder = postInfoList.getLast();
        postInfoList.clear();
        postInfoList.addLast(mLastHolder);
        notifyDataSetChanged();
    }

    public void addPost(PostInfo newPost){
        Log.d("TAG", "addPost: id="+newPost.getPost().pid);
        PostInfo mLastHolder = postInfoList.getLast();
        postInfoList.removeLast();
        postInfoList.addLast(newPost);
        postInfoList.addLast(mLastHolder);
        notifyDataSetChanged();
    }

    public void changeTail(boolean ifNoMore){
        PostInfo mLastHolder = postInfoList.getLast();
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
        return postInfoList.get(position).getType();
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
        PostCard postCard = new PostCard(tmpView, this, parent.getContext(), viewType);
        return postCard;
    }

    @Override
    public void onBindViewHolder(@NonNull PostCard holder, int position) {
        PostInfo postInfo = postInfoList.get(position);
        if(holder.mType == TYPE_POST_CARD) {
            holder.bindPostInfo(postInfo);
        }
        else if(holder.mType == TYPE_TAIL || holder.mType == TYPE_EMPTY_TAIL){
            holder.bindTailTitle(postInfo.getTitle());
        }
        holder.createAttachmentView();
        holder.updateAttatchmentView();
    }

    @Override
    public int getItemCount() {
        return postInfoList.size();
    }

}
