package com.example.meiyou.utils;

import com.example.meiyou.control.RegisterControl;
import com.example.meiyou.model.User;

public class GlobalData {
    private User user;
    private RegisterControl registerControl;
    private static GlobalData instant = null;
    private GlobalData(){
        registerControl = new RegisterControl();
        user = new User();
    }
    public static User getUser(){
        return getInstant().user;
    }
    public static RegisterControl getRegisterControl(){return getInstant().registerControl;}
    public static GlobalData getInstant(){
        if(instant == null){
            instant = new GlobalData();
        }
        return instant;
    }

}
