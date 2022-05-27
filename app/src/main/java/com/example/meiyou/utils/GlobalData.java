package com.example.meiyou.utils;

import android.content.res.ColorStateList;

import com.example.meiyou.control.RegisterControl;
import com.example.meiyou.model.MainUser;

public class GlobalData {
    private MainUser mainUser;
    private RegisterControl registerControl;
    private static GlobalData instant = null;
    private GlobalData(){
        registerControl = new RegisterControl();
        mainUser = new MainUser();
    }
    public static MainUser getUser(){
        return getInstant().mainUser;
    }
    public static RegisterControl getRegisterControl(){return getInstant().registerControl;}
    public static GlobalData getInstant(){
        if(instant == null){
            instant = new GlobalData();
        }
        return instant;
    }


    public static ColorStateList createColorStateList(int pressed, int normal) {
        //状态
        int[][] states = new int[2][];
        //按下
        states[0] = new int[] {android.R.attr.state_pressed};
        //默认
        states[1] = new int[] {};

        //状态对应颜色值（按下，默认）
        int[] colors = new int[] { pressed, normal};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

}
