package com.example.meiyou.activity;

import android.Manifest;
import android.content.DialogInterface;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.meiyou.R;
import com.example.meiyou.component.UploadView;
import com.example.meiyou.control.FileUploader;
import com.example.meiyou.databinding.ActivityNewcontentBinding;
import com.example.meiyou.utils.GlobalData;
import com.example.meiyou.utils.NetworkBasic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

public class NewContentActivity extends AppCompatActivity {

    ActivityNewcontentBinding binding;
    private ActivityResultLauncher<Intent> activityImageSelectLauncher,
        activityVideoSelectLauncher;

    LinearLayout uploadListLayout ;

    private Integer type = null;

    private ArrayList<Integer> resIDList = new ArrayList<>(), canceledList = new ArrayList<>();
    private static int currentIntentType = -1;
    private static final int TYPE_CAMERA = 0, TYPE_FILE = 1;
    Uri imageUri = null;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding  = ActivityNewcontentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uploadListLayout = new LinearLayout(this);
        uploadListLayout.setOrientation(LinearLayout.VERTICAL);
        binding.areaUploadStatus.addView(uploadListLayout);

        setButtonActive(SET_ALL);

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
                            setButtonActive(SET_IMG);
                            type = GlobalData.FILE_TYPE_IMG;
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
                            setButtonActive(SET_VID);
                            type = GlobalData.FILE_TYPE_VID;
                            try {
                                doUpload(uri, "/video", "video");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        binding.buttonAddImage.setOnClickListener(view -> {
            // Pop choice
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("上传图片");
            final String[] choices = {"从本地选取", "相机拍摄..."};
            builder.setItems(choices, (dialog, which) -> {
                // Select local
                if(which == 0){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    currentIntentType = TYPE_FILE;
                    activityImageSelectLauncher.launch(intent);
                }
                // Take photo
                if(which == 1){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    File photoFile = null;
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
            // Pop a choice dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("上传视频");
            final String[] choices = {"从本地选取", "相机录像..."};
            builder.setItems(choices, (dialog, which) -> {
                // Select local
                if(which == 0){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    activityVideoSelectLauncher.launch(intent);
                }
                // Shoot video
                if(which == 1){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    Intent intent = new Intent(
                            MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024L * 1024 * 200);
                    activityVideoSelectLauncher.launch(intent);
                }
            });
            builder.show();
        });
    }

    public static final int SET_ALL=-1, SET_IMG = 0, SET_VID = 1, SET_AUD = 2;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setButtonActive(int type){
        if(type == SET_ALL || type == SET_IMG){
            binding.buttonAddImage.setEnabled(true);
            binding.buttonAddImage.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddImage.setEnabled(false);
            binding.buttonAddImage.setImageTintList(getColorStateList(R.color.gray_100));
        }

        if(type == SET_ALL || type == SET_VID){
            binding.buttonAddVideo.setEnabled(true);
            binding.buttonAddVideo.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddVideo.setEnabled(false);
            binding.buttonAddVideo.setImageTintList(getColorStateList(R.color.gray_100));
        }

        if(type == SET_ALL || type == SET_AUD){
            binding.buttonAddAudio.setEnabled(true);
            binding.buttonAddAudio.setImageTintList(getColorStateList(R.color.green_400));
        }
        else{
            binding.buttonAddAudio.setEnabled(false);
            binding.buttonAddAudio.setImageTintList(getColorStateList(R.color.gray_100));
        }

    }

    public void doUpload(Uri uri, String fileType, String fileName) throws FileNotFoundException {
        FileUploader fileUploader = new FileUploader(getContentResolver());

        UploadView uploadView = new UploadView(this, this);
        uploadView.setName(fileName);
        uploadListLayout.addView(uploadView);

        // Bind upload finish callback
        fileUploader.status.observe(this, status -> {
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
            canceledList.add(fileUploader.result_res_id);
            uploadView.setVisibility(View.GONE);
            call.cancel();
        });
    }

    private String currentPhotoPath;

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
