package com.guosen.web;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    WebView _webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        _webView = findViewById(R.id.wv);
        WebSettings settings = _webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        _webView.clearCache(true);
        _webView.addJavascriptInterface(this, "native");
        //_webView.loadUrl("http://www.o6o4.com");
        //_webView.loadUrl("http://125.88.183.165:8082");
        _webView.loadUrl("http://172.24.177.42:8099");
        Log.e("id", getDeviceID());//DC-55-83-E3-0B-A5
    }
    public String digest(String src) throws Exception {
        if (null == src || src.length() == 0) return null;
        byte[] bytes = src.getBytes("ASCII");
        MessageDigest digest = MessageDigest.getInstance("MD5");
        bytes = digest.digest(bytes);
        return new String(bytes, "ASCII");
    }
    @JavascriptInterface
    public String getDeviceID() {
        SharedPreferences sp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String pdaID = sp.getString("webid", null);
        if (null != pdaID) return pdaID;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) break;
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) res1.append(String.format("%02X:", b));
                if (res1.length() != 0) res1.deleteCharAt(res1.length() - 1);
                pdaID = res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (null == pdaID) {
            pdaID = UUID.randomUUID().toString().replaceAll("-", "");
        } else {
            pdaID = pdaID.replaceAll(":", "");
            //try{pdaID = digest(pdaID);}catch (Exception ex){}
        }
        sp.edit().putString("webid", pdaID).apply();
        return pdaID;
    }
    @JavascriptInterface
    public void setData(String key, String val) {
        if (null == key || key.length() == 0 || null == val || val.length() == 0) return;
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString(key, val).apply();
    }
    @JavascriptInterface
    public String getData(String key) {
        if (null == key || key.length() == 0) return null;
        return getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(key, null);
    }
    @JavascriptInterface
    public void delData(String key) {
        if (null == key || key.length() == 0) return;
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().remove(key).apply();
    }
}
