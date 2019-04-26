package com.github.tianmu19.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class Main1Activity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "main1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        TextView textView = findViewById(R.id.sample_text);
        textView.setText(BuildConfig.VERSION_NAME);
        //permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 1002);
            }
        }
        File file = new File(Environment.getExternalStorageDirectory(), "patch");
        if(!file.exists()){
            file.mkdirs();
        }
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installApk();
            }
        });
//        textView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(Main1Activity.this, "jump to demo2", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getBaseContext(),Demo2Activity.class));
//            }
//        },5000);
    }

    /**
     * 合成安装包
     *
     * @param oldApk 旧版本安装包 如1.1.0安装包
     * @param patch  查分包 patch文件
     * @param output 合成后的新版本apk安装包
     */
    public native void bsPath(String oldApk, String patch, String output);
    private void installApk() {
        new AsyncTask<Void,Void,File>(){
            @Override
            protected File doInBackground(Void... voids) {
                //获取现在运行的apk路径
                String oldApk = getApplicationInfo().sourceDir;

                // 获取拆分包的路径
                String patch = new File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath();

                // 获取合成之后的新apk的路径
                String output = createNewApk().getAbsolutePath();
                Log.e(TAG, "第三步成功==>");

                bsPath(oldApk, patch, output);
                Log.e(TAG, "第四步成功==>");

                return new File(output);
            }
            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                //已经合成了，调用该方法
                UriparseUtils.installApk(Main1Activity.this, file);
            }
        }.execute();
    }

    private File createNewApk() {
        File newApk = new File(Environment.getExternalStorageDirectory(), "newApk.apk");
        if(!newApk.exists()){
            try {
                newApk.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newApk;
    }
}
