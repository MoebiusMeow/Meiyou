package com.example.meiyou.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meiyou.R;
import com.example.meiyou.databinding.FragmentFollowBinding;
import com.example.meiyou.model.User;
import com.example.meiyou.model.UserBanSender;
import com.example.meiyou.model.UserFollowSender;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;

import butterknife.internal.ListenerClass;
import kotlin.Function;
import okhttp3.HttpUrl;

public class FollowFragment extends Fragment {

    public ArrayList<MessageModel> msg_list;
    public ArrayList<MessageModel> msg_pool;
    private RecyclerView recycler;
    private ListFetcher fetcher;
    private String senderClass;

    private FragmentFollowBinding binding;

    public int uid;
    public String listURL;

    public FollowFragment() {
    }

    public FollowFragment(int uid, String url, String senderClass) {
        this.uid = uid;
        listURL = url;
        this.senderClass = senderClass;
    }

    public class ListFetcher extends NetworkBasic {
        public ListFetcher() {
            status.postValue(NetworkBasic.Status.idle);
            HttpUrl.Builder urlBuilder = HttpUrl.parse(listURL).newBuilder()
                    .addQueryParameter("uid", String.valueOf(uid));
            NetworkConstant.get(urlBuilder.build().toString(), true, getCommonNetworkCallback(
                    response -> {
                        if (response.code() != 200) {
                            Toast.makeText(getActivity(), "糟糕，网络好像不太通畅..", Toast.LENGTH_SHORT).show();
                            if(response.code() == 404) errorCode = 404;
                            status.postValue(NetworkBasic.Status.wrong);
                            return;
                        }
                        binding.progressBar5.setVisibility(View.INVISIBLE);
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        //Log.d("fff", jsonObject.toString());
                        JSONArray list = jsonObject.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject line = list.getJSONObject(i);
                            //Log.d("fff", line.toString());
                            msg_pool.add(new MessageModel(
                                    line.getString("username"), line.getInt("uid"),
                                    line.isNull("imgid") ? -1 : line.getInt("imgid")));
                        }
                        status.postValue(Status.success);
                    }));
            status.observe(getViewLifecycleOwner(), status -> {
                if (status == Status.success) {
                    fetchMessage(10);
                }
            });
        }
    }

    public static class MessageModel {
        public String name;
        public int uid;
        public int imgid;
        public Boolean active;
        public MessageModel(String name, int uid, int imgid) {
            this.name = name;
            this.uid = uid;
            this.imgid = imgid;
            this.active = true;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_name;
        private ImageView avatar;
        private Button btn;
        private MessageModel message;
        public MessageViewHolder(View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.textView_name);
            avatar = itemView.findViewById(R.id.imageView4);
            btn = itemView.findViewById(R.id.buttonFollow3);
        }
        @SuppressLint("SetTextI18n")
        public void bindModel(MessageModel message, LifecycleOwner lifecycleOwner) {
            this.message = message;
            txt_name.setText(message.name);
            avatar.setImageResource(R.drawable.user_profile_default);
            if (message.imgid >= 0) {
                GlobalResFileManager.requestFile(lifecycleOwner, message.imgid, uri -> {
                    avatar.setImageURI(uri);
                });
            }
            Runnable updateButton = () -> {
                btn.setText((message.active ? "取消" : "") + (senderClass.equals("follow") ? "关注" : "屏蔽"));
                btn.setBackgroundTintList(getContext().getColorStateList(
                        message.active ^ senderClass.equals("follow") ? R.color.pink_500 : R.color.gray_100));
            };
            updateButton.run();
            btn.setOnClickListener(view -> {
                NetworkBasic sender;
                Callable<Boolean> getter;
                if (senderClass.equals("follow")) {
                    sender = (new UserFollowSender(message.uid)).setFollow(!message.active);
                    getter = () -> ((UserFollowSender)sender).flag;
                } else {
                    sender = (new UserBanSender(message.uid)).setBan(!message.active);
                    getter = () -> ((UserBanSender)sender).flag;
                }
                sender.status.observe(
                    getViewLifecycleOwner(), status -> {
                        if (status == NetworkBasic.Status.success) {
                            try { message.active = getter.call(); }
                            catch (Exception e) { e.printStackTrace(); }
                            updateButton.run();
                        }
                    }
                );
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowBinding.inflate(inflater, container, false);
        msg_list = new ArrayList<MessageModel>();
        msg_pool = new ArrayList<MessageModel>();
        recycler = binding.recyclerViewFollow;
        Adapter<MessageViewHolder> recycler_adapter = new Adapter<MessageViewHolder>() {
            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.message_card_layout, parent, false);
                return new MessageViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
                MessageModel msg = msg_list.get(position);
                holder.bindModel(msg, getViewLifecycleOwner());
            }

            @Override
            public int getItemCount() {
                return msg_list.size();
            }
        };
        recycler.setAdapter(recycler_adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recycler.canScrollVertically(1)) {
                    fetchMessage(10);
                }
            }
        });

        fetcher = new ListFetcher();
        return binding.getRoot();
    }

    public void fetchMessage(int a) {
        while (a-- > 0) {
            if (msg_list.size() >= msg_pool.size())
                return;
            int pos = msg_list.size();
            msg_list.add(msg_pool.get(pos));
            Objects.requireNonNull(recycler.getAdapter()).notifyItemInserted(pos);
        }
    }
}