package org.uwtech.mijiagui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.internal.observers.DisposableLambdaObserver;
import io.reactivex.observers.DisposableObserver;

public class MainActivity extends AppCompatActivity {

    public final static String BLE_BROADCAST_MSG = "org.uwtech.mijiagui.message";
    public final static int Overlay_REQUEST_CODE = 251;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            checkDrawOverlayPermission();
        }

        finish();

    }

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Overlay_REQUEST_CODE);
            } else {
                openFloatingWindow();
            }
        } else {
            openFloatingWindow();
        }
    }

    private void openFloatingWindow() {
        Intent intent = new Intent(this, FloatingWindow.class);
        stopService(intent);
        startService(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Settings.canDrawOverlays(this)) {
            openFloatingWindow();
        }
    }
}
