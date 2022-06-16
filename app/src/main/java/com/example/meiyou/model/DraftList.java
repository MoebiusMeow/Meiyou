package com.example.meiyou.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DraftList {

    public interface UpdateCallback{
        // indicate modified post start from <startIndex>
        // when all post renewed, <startIndex> should be 0
        void onUpdate(int startIndex);
    }

    private UpdateCallback updateCallback = startIndex -> {};

    private ArrayList<Post> postList = new ArrayList<>();
    private File dataFile;
    public DraftList(File fileToSave){
        dataFile = fileToSave;
        Log.d("TAG", "DraftList: Create");
        if(!dataFile.exists()){
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("TAG", "DraftList: mkdir"+dataFile.getPath());
        }
    }

    public int len(){ return postList.size(); }
    public Post get(int index){ return postList.get(index); }
    public void add(Post post){postList.add(post); updateCallback.onUpdate(len()-1);}
    public void remove(Post post){postList.remove(post); updateCallback.onUpdate(0);}
    public void setOnUpdateCallback(UpdateCallback callback){updateCallback = callback;}

    public void saveToFile(){
        try {
            FileOutputStream outputStream = new FileOutputStream(dataFile, false);
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
            objectStream.writeObject(postList);
            objectStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(){
        try {
            FileInputStream inputStream = new FileInputStream(dataFile);
            ObjectInputStream objectStream = new ObjectInputStream(inputStream);
            postList = (ArrayList<Post>) objectStream.readObject();
            objectStream.close();
            inputStream.close();
            updateCallback.onUpdate(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
