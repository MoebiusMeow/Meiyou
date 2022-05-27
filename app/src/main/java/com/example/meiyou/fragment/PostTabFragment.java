package com.example.meiyou.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.meiyou.R;
import com.example.meiyou.model.PostList;
import com.google.android.material.tabs.TabLayout;

public class PostTabFragment extends Fragment {

    private final PostListFragment newestList = new PostListFragment(PostList.MODE_NEWEST);
    private final PostListFragment hotList = new PostListFragment(PostList.MODE_HOT);
    private final PostListFragment followList = new PostListFragment(PostList.MODE_FOLLOW);

    public PostTabFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posttab, container, false);
        super.onCreate(savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.postListViewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()){
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0: return newestList;
                    case 1: return hotList;
                    case 2: return followList;
                    default: return null;
                }
            }
            @Override
            public int getCount() { return 3; }
        });
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
        return view;
    }


}
