package com.github.tianmu19.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Example of a call to a native method
        sample_text.text = BuildConfig.VERSION_NAME
        //permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 1002)
            }
        }
        val file = File(Environment.getExternalStorageDirectory(), "patch")
        if(!file.exists()){
            file.mkdirs()
        }
        btn_update.setOnClickListener { installApk() }
        sample_text.postDelayed({
            startActivity(Intent(this@MainActivity,Demo2Activity::class.java))
        },200)
    }

    /**
     * 合成安装包
     *
     * @param oldApk 旧版本安装包 如1.1.0安装包
     * @param patch  查分包 patch文件
     * @param output 合成后的新版本apk安装包
     */
    external fun bsPath(oldApk: String, patch: String, output: String)

    private fun installApk() {
        object : AsyncTask<Void, Void, File>() {
            override fun doInBackground(vararg voids: Void): File {

                //获取现在运行的apk路径
                val oldApk = applicationInfo.sourceDir
                Log.e(TAG, "第一步成功==>")

                // 获取拆分包的路径
                val patch = File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath()
                Log.e(TAG, "第二步成功==>")

                // 获取合成之后的新apk的路径
                val output = createNewApk().getAbsolutePath()
                Log.e(TAG, "第三步成功==>")

                bsPath(oldApk, patch, output)
                Log.e(TAG, "第四步成功==>")
                return File(output)
            }

            override fun onPostExecute(file: File) {
                super.onPostExecute(file)
                //已经合成了，调用该方法
                UriparseUtils.installApk(this@MainActivity, file)
            }
        }.execute()

    }

    private fun createNewApk(): File {
        val newApk = File(Environment.getExternalStorageDirectory(), "newApk.apk")
        if (!newApk.exists()) {
            try {
                newApk.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return newApk
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
