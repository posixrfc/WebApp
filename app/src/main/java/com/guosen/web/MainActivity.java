package com.guosen.web;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
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
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        _webView.clearCache(true);
        _webView.addJavascriptInterface(this, "native");
        _webView.loadUrl("http://www.o6o4.com");
        //_webView.loadUrl("http://125.88.183.165:8082");
        //_webView.loadUrl("http://172.24.177.42:8099");
    }
    public String digest(String src) throws Exception {
        if (null == src || src.length() == 0) return null;
        byte[] bytes = src.getBytes("ASCII");
        MessageDigest digest = MessageDigest.getInstance("MD5");
        bytes = digest.digest(bytes);
        return new String(bytes, "ASCII");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String permissions[] = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE
        };
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);// 进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
        //Log.e("id", getDeviceID());
    }

    @JavascriptInterface
    public String getDeviceID() {
        SharedPreferences sp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String tmpID, pdaID = null;//sp.getString("webid", null);
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
                pdaID = res1.toString().replaceAll(":", "");
                Log.e("pdaIDmac." + nif.getName(), pdaID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpID = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (null == tmpID) {
            tmpID = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimSerialNumber();
            if (null != tmpID) {
                Log.e("tmpID.SerialNumber", tmpID);
            }
        } else {
            Log.e("tmpID.getDeviceId", tmpID);
        }
        if (tmpID != null && null != pdaID) {
            pdaID = tmpID + pdaID;
        } else if (null == tmpID && null == pdaID) {
            pdaID = UUID.randomUUID().toString().replaceAll("-", "");
            Log.e("pdaID.UUID", pdaID);
        } else if (null == pdaID) {
            pdaID = tmpID;
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
