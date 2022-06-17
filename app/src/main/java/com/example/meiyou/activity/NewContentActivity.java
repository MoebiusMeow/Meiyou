package com.example.meiyou.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.example.meiyou.R;
import com.example.meiyou.component.UploadView;
import com.example.meiyou.control.FileUploader;
import com.example.meiyou.databinding.ActivityNewcontentBinding;
import com.example.meiyou.model.Post;
import com.example.meiyou.model.PostSender;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;

public class NewContentActivity extends AppCompatActivity {

    ActivityNewcontentBinding binding;
    private ActivityResultLauncher<Intent> activityImageSelectLauncher,
        activityVideoSelectLauncher, activityAudioSelectLauncher,
        activityLocationLauncher;

    GridLayout uploadListLayout ;

    private Integer attachedFiletype = null;

    private final ArrayList<Integer> resIDList = new ArrayList<>();
    private final ArrayList<Uri> fileUriList = new ArrayList<>();
    private final ArrayList<UploadView> uploadViews = new ArrayList<>();
    private static int currentIntentType = -1;
    private static final int TYPE_CAMERA = 0, TYPE_FILE = 1;
    Uri imageUri = null;

    private int nSelected = 0;

    public static final String ACTION_TYPE = "com.Meiyou.newContent.actionType",
        POST_DATA = "com.Meiyou.newContent.postData",
        POST_ID = "com.Meiyou.newContent.postID",
        POST_SAVED = "com.Meiyou.newContent.postSaved";
    public static final int ACTION_SAVE = 0, ACTION_POST = 1;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityNewcontentBinding.inflate(getLayoutInflater());

        // Get Intent message
        Post post = (Post) getIntent().getSerializableExtra(POST_SAVED);
        if(post != null){
            binding.editTextTitle.setText(post.title);
            binding.editTextContent.setText(post.content);
        }

        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(binding.getRoot());

        uploadListLayout = new GridLayout(this);
        uploadListLayout.setColumnCount(3);
        uploadListLayout.setRowCount(3);
        binding.areaUploadFile.addView(uploadListLayout);

        setCurrentFileType(SET_EMPTY);

        activityImageSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Intent data = result.getData();
                        Uri uri = null;
                        if(data != null && currentIntentType == TYPE_FILE){
                            uri = data.getData();
                            Log.d("meow", uri.toString());
                        }
                        if(currentIntentType == TYPE_CAMERA){
                            uri = imageUri;
                            Log.d("TAG", "onCreate: "+imageUri.toString());
                        }
                        if(uri != null){
                            setCurrentFileType(SET_IMG);
                            try {
                                doUpload(uri, "/image", "image");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        activityVideoSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Intent data = result.getData();
                        if (data != null){
                            Uri uri = data.getData();
                            setCurrentFileType(SET_VID);
                            try {
                                doUpload(uri, "/video", "video");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        activityAudioSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Intent data = result.getData();
                        if (data != null){
                            Uri uri = data.getData();
                            setCurrentFileType(SET_AUD);
                            try {
                                doUpload(uri, "/audio", "audio");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        activityLocationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK){
                        Intent data = result.getData();
                        if (data != null){
                            try {
                                Log.d("mmu", data.getStringExtra("address"));
                                binding.editTextContent.append(
                                        "[" + data.getStringExtra("address") + " " +
                                        "(" + data.getStringExtra("latitude") +
                                        ", " + data.getStringExtra("longitude") +
                                        ")]");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        binding.buttonAddImage.setOnClickListener(view -> {

            if(nSelected >=9){
                Toast.makeText(this, "最多上传9张图片哦~", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pop choice
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("上传图片");
            final String[] choices = {"从本地选取", "相机拍摄..."};
            builder.setItems(choices, (dialog, which) -> {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                // Select local
                if(which == 0){
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    currentIntentType = TYPE_FILE;
                    activityImageSelectLauncher.launch(intent);
                }
                // Take photo
                if(which == 1){
                    File photoFile;
                    try{
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    imageUri = FileProvider.getUriForFile(this,
                            "com.Meiyou.android.fileprovider",
                            photoFile);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    currentIntentType = TYPE_CAMERA;
                    activityImageSelectLauncher.launch(intent);
                }
            });
            builder.show();
        });

        binding.buttonAddVideo.setOnClickListener(view -> {

            if(nSelected >=1){
                Toast.makeText(this, "最多上传1张图片哦~", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pop a choice dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("上传视频");
            final String[] choices = {"从本地选取", "相机录像..."};
            builder.setItems(choices, (dialog, which) -> {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                // Select local
                if(which == 0){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    activityVideoSelectLauncher.launch(intent);
                }
                // Shoot video
                if(which == 1){
                    Intent intent = new Intent(
                            MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024L * 1024 * 200);
                    activityVideoSelectLauncher.launch(intent);
                }
            });
            builder.show();
        });

        binding.buttonAddAudio.setOnClickListener(view -> {

            if(nSelected >=1){
                Toast.makeText(this, "最多上传1个音频哦~", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("上传音频");
            final String[] choices = {"从本地选取", "录音..."};
            builder.setItems(choices, (dialog, which) -> {
                // Select local
                if(which == 0){
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    activityAudioSelectLauncher.launch(intent);
                }
                // Shoot video
                if(which == 1){
                    try {
                        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        activityAudioSelectLauncher.launch(intent);
                    }
                    catch(Exception e){
                        Toast.makeText(this, "没有找到录音应用诶", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
        });

        binding.buttonAddLocation.setOnClickListener(view -> {
            Intent intent = new Intent(NewContentActivity.this, LocationActivity.class);
            activityLocationLauncher.launch(intent);
        });

        binding.buttonReturn.setOnClickListener( view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        binding.buttonSave.setOnClickListener( view -> {
            Intent intent = new Intent();
            intent.putExtra(ACTION_TYPE, ACTION_SAVE);
            intent.putExtra(POST_DATA, buildPost());
            setResult(RESULT_OK, intent);
            finish();
        });

        binding.buttonRelease.setOnClickListener( view -> {
            binding.postMask.setVisibility(View.VISIBLE);
            PostSender postSender = new PostSender(buildPost());
            postSender.status.observe(this, status -> {
                binding.postMask.setVisibility(View.INVISIBLE);
                if(status == NetworkBasic.Status.success){
                    Intent intent = new Intent();
                    intent.putExtra(ACTION_TYPE, ACTION_POST);
                    intent.putExtra(POST_ID, postSender.pid);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else if (status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong){
                    Toast.makeText(this, "发送失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            });
            postSender.send_post();
        });


    }

    public static final int SET_EMPTY =100, SET_IMG = 0, SET_VID = 1, SET_AUD = 2;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setCurrentFileType(int type){

        Log.d("TAG", "setCurrentFileType: "+type);

        switch(type){
            case SET_EMPTY: this.attachedFiletype = GlobalData.FILE_TYPE_NONE; break;
            case SET_IMG: this.attachedFiletype = GlobalData.FILE_TYPE_IMG; break;
            case SET_VID: this.attachedFiletype = GlobalData.FILE_TYPE_VID; break;
            case SET_AUD: this.attachedFiletype = GlobalData.FILE_TYPE_AUD; break;
            default: this.attachedFiletype = GlobalData.FILE_TYPE_NONE; break;
        }

        if((type == SET_EMPTY) || (type == SET_IMG)){
            binding.buttonAddImage.setEnabled(true);
            binding.buttonAddImage.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddImage.setEnabled(false);
            binding.buttonAddImage.setImageTintList(getColorStateList(R.color.gray_100));
        }

        if(type == SET_EMPTY || type == SET_VID){
            binding.buttonAddVideo.setEnabled(true);
            binding.buttonAddVideo.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddVideo.setEnabled(false);
            binding.buttonAddVideo.setImageTintList(getColorStateList(R.color.gray_100));
        }

        if(type == SET_EMPTY || type == SET_AUD){
            binding.buttonAddAudio.setEnabled(true);
            binding.buttonAddAudio.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddAudio.setEnabled(false);
            binding.buttonAddAudio.setImageTintList(getColorStateList(R.color.gray_100));
        }

        binding.buttonAddLocation.setEnabled(true);
        binding.buttonAddLocation.setImageTintList(getColorStateList(R.color.green_400));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void doUpload(Uri uri, String fileType, String fileNameDisplay) throws FileNotFoundException {
        FileUploader fileUploader = new FileUploader(getContentResolver());

        // save uri
        fileUriList.add(uri);

        // put a uploading view
        UploadView uploadView = new UploadView(this, this);
        uploadView.setName(fileNameDisplay);
        uploadView.setImageUri(uri);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.rowSpec = GridLayout.spec((int)(nSelected/3),1,1f);
        params.columnSpec = GridLayout.spec((int)(nSelected%3),1,1f);
        params.setMargins(2,2,2,2);
        uploadListLayout.addView(uploadView, params);
        uploadViews.add(uploadView);
        nSelected ++;

        // Bind upload finish callback
        fileUploader.status.observe(this, status -> {
            Log.d("meow", status.toString());
            if (status == NetworkBasic.Status.success) {
                uploadView.setProgressBar(2.0f);
                resIDList.add(fileUploader.result_res_id);
            }
            if (status == NetworkBasic.Status.fail || status == NetworkBasic.Status.wrong) {
                Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show();
            }
        });

        // Send
        Log.d("TAG", "doUpload: "+ uri.getPath());
        Call call = fileUploader.put(uri, fileType, uploadView::setProgressBar);

        //On Cancel uploading or uploaded file
        uploadView.setCancelCallback(() -> {
            resIDList.remove(new Integer(fileUploader.result_res_id));
            fileUriList.remove(uri);
            uploadListLayout.removeView(uploadView);
            call.cancel();
            nSelected --;
            if(nSelected <= 0)
                setCurrentFileType(SET_EMPTY);
        });
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private Post buildPost(){
        Post post = new Post();
        post.title = binding.editTextTitle.getText().toString();
        post.content = binding.editTextContent.getText().toString();
        post.res_type = attachedFiletype;
        Log.d("TAG", "buildPost: res_type="+post.res_type);
        if(attachedFiletype != GlobalData.FILE_TYPE_NONE && attachedFiletype != GlobalData.FILE_TYPE_NONE){
            post.res_ids = (ArrayList<Integer>) resIDList.clone();
        }
        post.res_uri_list = (ArrayList<Uri>) fileUriList.clone();
        return post;
    }
}
