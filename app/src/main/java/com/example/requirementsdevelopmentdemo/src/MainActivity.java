package com.example.requirementsdevelopmentdemo.src;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.requirementsdevelopmentdemo.databinding.ActivityMainBinding;
import com.example.requirementsdevelopmentdemo.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 调用分享功能
                sharePdfFile();
            }
        });

        // 初始化 WebView
        webView = new WebView(this);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("file://" + getFilesDir().getAbsolutePath() + "/pdf/sample.pdf");

        // 假设你已经有了一个FloatingActionButton用于打印
        binding.fabPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 调用打印功能
                printPdfFile();
            }
        });
    }

    private void sharePdfFile() {
        File pdfFile = new File(getFilesDir(), "pdf/sample.pdf");
        Uri pdfFileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享PDF文件");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "请查看附件中的PDF文件");

        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void printPdfFile() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Document";

        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdfprint", PRINT_SERVICE, 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintJob printJob = printManager.print(jobName, printAdapter, attributes);

        // 定时检查打印作业状态
        checkPrintJobStatus(printJob);
    }

    private void checkPrintJobStatus(PrintJob printJob) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (printJob.isCompleted()) {
                    Snackbar.make(binding.getRoot(), "打印完成", Snackbar.LENGTH_SHORT).show();
                } else if (printJob.isCancelled()) {
                    Snackbar.make(binding.getRoot(), "打印取消", Snackbar.LENGTH_SHORT).show();
                } else if (printJob.isFailed()) {
                    Snackbar.make(binding.getRoot(), "打印失败", Snackbar.LENGTH_SHORT).show();
                } else {
                    // 如果打印作业还没有完成，继续检查状态
                    handler.postDelayed(this, 1000); // 1秒后再次检查
                }
            }
        }, 1000); // 初始延迟1秒
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }
    }
}
