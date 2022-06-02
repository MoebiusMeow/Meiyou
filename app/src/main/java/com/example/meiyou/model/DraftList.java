package com.example.meiyou.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DraftList {
    private ArrayList<Post> postList;
    private File dataFile;
    public DraftList(File fileToSave){
        dataFile = fileToSave;
        if(!dataFile.exists()){
            dataFile.mkdirs();
        }
    }

    public int len(){ return postList.size(); }
    public Post get(int index){ return postList.get(index); }

    public void saveToFile(){
        try {
            FileOutputStream outputStream = new FileOutputStream(dataFile);
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
            objectStream.writeObject(postList);
            objectStream.close();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
