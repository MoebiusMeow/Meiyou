package com.example.meiyou.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.meiyou.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostCard extends RelativeLayout {
    @BindView(R.id.post_card_title) TextView title_text;
    @BindView(R.id.post_card_content) TextView content_text;

    private View mView;
    public PostCard(Context context) {
        super(context);
    }
    public PostCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PostCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mView = LayoutInflater.from(context).inflate(R.layout.component_postcard,this,true);
        ButterKnife.bind(mView);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyPostCard);
        title_text.setText(a.getString(R.styleable.MyPostCard_CardTitle));
        content_text.setText(a.getString(R.styleable.MyPostCard_CardContent));
    }
}
