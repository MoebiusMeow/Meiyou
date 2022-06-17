package com.example.meiyou.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.meiyou.databinding.ActivityLocationBinding;
import com.example.meiyou.databinding.ActivityLoginBinding;
import com.tencent.tencentmap.mapsdk.maps.MapView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LocationActivity extends AppCompatActivity {
    private ActivityLocationBinding binding;
    public WebView webView;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("选择位置");

        webView = binding.locationView;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        //webView.getSettings().setGeolocationDatabasePath( this.getFilesDir().getPath() );
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http://callback")) {
                    view.loadUrl(url);
                } else {
                    try {
                        String decode = URLDecoder.decode(url, "UTF-8");
                        Uri uri = Uri.parse(decode);

                        String[] latng = uri.getQueryParameter("latng").split(",");
                        String address = uri.getQueryParameter("name");
                        if (address.equals("我的位置")){
                            address = uri.getQueryParameter("addr");
                        }
                        Intent intent = new Intent();
                        intent.putExtra("address", address);
                        intent.putExtra("latitude",latng[0]);
                        intent.putExtra("longitude",latng[1]);
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        webView.loadUrl("https://mapapi.qq.com/web/mapComponents/locationPicker/v/index.html?search=1&type=0&backurl=http://callback&key=DEDBZ-MWH3R-KDFW6-WPIOS-EW3I6-YKFPE");
    }
}