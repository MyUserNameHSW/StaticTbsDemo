package com.hsw.statictbsdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.x5library.Constants;
import com.tencent.x5library.TBSSdkManage;
import com.tencent.x5library.http.callback.TbsInstallListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private WebView webView;
    private Button btn;

    // tbs 内核存放目录+文件名
    private String tbsCoreFilePath;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tbsCoreFilePath = getFilesDir().getPath() + File.separator + Constants.X5CORE_FILE_NAME;
        showLoading();
        File file = new File(tbsCoreFilePath);
        if (!file.exists()) {
            copyAssetsToFile();
            if (!file.exists()) {
                Log.e(TAG, "内核文件不存在，可能未copy成功，please try again!");
                return;
            }
        }
        Executors.newCachedThreadPool().execute(this::initTbs);
    }

    /**
     * 复制 assets 目录下的core到Android文件目录
     * @throws IOException
     */
    private void copyAssetsToFile() {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("tbs_core.tbs");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] bytes = baos.toByteArray();
            String filePath = this.getFilesDir().getPath() + File.separator + Constants.X5CORE_FILE_NAME;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            Log.d(TAG, "copyAssetsToFile: 失败");
        }

    }

    /**
     *  首次加载大概会耗时5s-10s进行安装
     */
    private void initTbs() {
        TBSSdkManage tbsSdkManage = new TBSSdkManage.Builder(getApplicationContext())
                .localPath(tbsCoreFilePath).build();
        tbsSdkManage.setTbsListener(new TbsInstallListener() {
            @Override
            public void onDownloadFinish(int i) {

            }

            @Override
            public void onInstallFinish(int i) {
                // 安装完成后回调
                runOnUiThread(() -> {
                    hideLoading();
                    loadWebview();
                });
            }

            @Override
            public void onProgress(int i, int i1) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
        if (!tbsSdkManage.isInstallSuccess()) {
            Log.d(TAG, "initTbs: Tbs is not install, start install");
            tbsSdkManage.install();
        } else {
            Log.d(TAG, "initTbs: Tbs isInstallSuccess");
            runOnUiThread(() -> {
                hideLoading();
                loadWebview();
            });

        }
    }

    private void loadWebview() {
        if (webView != null) {
            return;
        }
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        final String url = "https://www.baidu.com/";
        webView.loadUrl(url);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout rootView = findViewById(R.id.root_view);
        rootView.addView(webView, layoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != webView) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != webView) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != webView) {
            webView.destroy();
        }
    }

    private void showLoading() {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.show();
    }

    private void hideLoading() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}