package com.example.meiyou.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.meiyou.R;
import com.example.meiyou.databinding.ActivityFollowBinding;
import com.example.meiyou.databinding.ActivityMessageBinding;
import com.example.meiyou.fragment.FollowFragment;
import com.example.meiyou.fragment.MessageFragment;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkConstant;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MessageActivity extends AppCompatActivity {
    private ActivityMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();

        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonReturn23.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        TabLayout tabLayout = binding.tabLayoutMessage;
        final ViewPager2 viewPager = binding.viewPagerMessage;
        viewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new MessageFragment();
                    default: return null;
                }
            }
            @Override public int getItemCount() {
                return 1;
            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                super.onPageSelected(position);
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }
}