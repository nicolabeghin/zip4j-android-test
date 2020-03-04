package org.zip4j.test;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.zip4j.test.tasks.UnzipTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String APP_NAME = "zip4j-test";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkRequiredPermissions();
    }

    public void onExtract(View view) {
        if (checkRequiredPermissions()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Media.zip");
            if (!file.exists()) {
                Toast.makeText(this, "No Media.zip available in your Download folder", Toast.LENGTH_SHORT).show();
                return;
            }
            new UnzipTask(this, file).execute();
        }
    }

    private boolean checkRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(APP_NAME, "Requesting permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setTitle("Permission required").setMessage("Storage permission required").setPositiveButton("Yes", (dialogInterface, i) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)).create().show();
                return false;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            }
        }
        return true;
    }
}
