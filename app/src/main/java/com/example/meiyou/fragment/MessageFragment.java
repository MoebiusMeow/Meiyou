package com.example.meiyou.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meiyou.R;
import com.example.meiyou.activity.SinglePostActivity;
import com.example.meiyou.databinding.FragmentFollowBinding;
import com.example.meiyou.model.User;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.GlobalResFileManager;
import com.example.meiyou.utils.NetworkBasic;
import com.example.meiyou.utils.NetworkConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.HttpUrl;

public class MessageFragment extends Fragment {

    public ArrayList<MessageModel> msg_list;
    public ArrayList<MessageModel> msg_pool;
    private RecyclerView recycler;
    private ListFetcher fetcher;

    private FragmentFollowBinding binding;

    public int uid;
    public String listURL;

    ActivityResultLauncher<Intent> activitySinglePostLauncher;

    public MessageFragment() {
        uid = GlobalData.getUser().uid;
        listURL = NetworkConstant.getMessageUrl;
    }

    public class ListFetcher extends NetworkBasic {
        public ListFetcher() {}
        public void fetch() {
            status.postValue(NetworkBasic.Status.idle);
            HttpUrl.Builder urlBuilder = HttpUrl.parse(listURL).newBuilder()
                    .addQueryParameter("uid", String.valueOf(uid))
                    .addQueryParameter("n", String.valueOf(10));
            if (msg_pool.size() > 0)
                urlBuilder.addQueryParameter("start", String.valueOf(msg_pool.get(msg_pool.size() - 1).msg_id - 1));
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

                        Log.d("fff", jsonObject.toString());
                        JSONArray list = jsonObject.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject line = list.getJSONObject(i);
                            //Log.d("fff", line.toString());
                            msg_pool.add(new MessageModel(
                                    line.getString("title"),
                                    line.getString("content"),
                                    line.getInt("pid"),
                                    line.getInt("msg_id"),
                                    line.getInt("sender_id"),
                                    line.getString("addtime")));
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
        public String title;
        public String content;
        public int pid;
        public int msg_id;
        public int sender_id;
        public String addtime;
        public MessageModel(String title, String content, int pid, int msg_id, int sender_id, String addtime) {
            this.title = title;
            this.content = content;
            this.pid = pid;
            this.msg_id = msg_id;
            this.sender_id = sender_id;
            this.addtime = addtime;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_title;
        private TextView txt_content;
        private TextView txt_time;
        private ImageView avatar;
        private MessageModel message;
        public MessageViewHolder(View itemView) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.textView_title);
            txt_content = itemView.findViewById(R.id.textView_content);
            txt_time = itemView.findViewById(R.id.textView_time);
            avatar = itemView.findViewById(R.id.imageView4);
        }
        @SuppressLint("SetTextI18n")
        public void bindModel(MessageModel message, LifecycleOwner lifecycleOwner) {
            this.message = message;
            txt_title.setText(message.title);
            if (message.content.length() <= 0)
                txt_content.setVisibility(View.GONE);
            txt_content.setText(message.content);
            txt_time.setText(message.addtime);
            avatar.setImageResource(R.drawable.mine);
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), SinglePostActivity.class);
                intent.putExtra(SinglePostActivity.EXTRA_PID, String.valueOf(message.pid));
                activitySinglePostLauncher.launch(intent);
            });
            User sender = new User();
            sender.uid = message.sender_id;
            sender.requestInfo();
            sender.status.observe(
                getViewLifecycleOwner(), status -> {
                    if (status == NetworkBasic.Status.success) {
                        if (sender.profile_id > 0) {
                            GlobalResFileManager.requestFile(lifecycleOwner, sender.profile_id, uri -> {
                                avatar.setImageURI(uri);
                            });
                        }
                    }
                }
            );
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowBinding.inflate(inflater, container, false);
        msg_list = new ArrayList<MessageModel>();
        msg_pool = new ArrayList<MessageModel>();
        recycler = binding.recyclerViewFollow;
        RecyclerView.Adapter<MessageViewHolder> recycler_adapter = new RecyclerView.Adapter<MessageViewHolder>() {
            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.message_card2_layout, parent, false);
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
                    fetcher.fetch();
                }
            }
        });

        activitySinglePostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> { });

        fetcher = new ListFetcher();
        fetcher.fetch();

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
