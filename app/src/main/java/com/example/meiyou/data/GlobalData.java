package com.example.meiyou.data;

import android.app.Activity;

import com.example.meiyou.model.User;

public class GlobalData {
    private User user;
    private static GlobalData instant = null;
    private GlobalData(){
        user = new User();
    }
    public static User getUser(){
        return getInstant().user;
    }
    public static GlobalData getInstant(){
        if(instant == null){
            instant = new GlobalData();
        }
        return instant;
    }

}
