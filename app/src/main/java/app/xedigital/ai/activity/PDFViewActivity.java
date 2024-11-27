package app.xedigital.ai.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Objects;

import app.xedigital.ai.R;

public class PDFViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        // Prevent screenshots and screen recordings
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Restrict opening in external apps
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Only allow navigation within the Google Docs viewer domain
                return !Objects.requireNonNull(request.getUrl().getHost()).contains("docs.google.com");
            }
        });

        String pdfUrl = getIntent().getStringExtra("pdfUrl");
        String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + pdfUrl;

        webView.loadUrl(googleDocsUrl);
    }
}

//public class PDFViewActivity extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pdfview);
//
//        WebView webView = findViewById(R.id.webView);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.setWebViewClient(new WebViewClient());
//
//        String pdfUrl = getIntent().getStringExtra("pdfUrl");
//        String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + pdfUrl;
//
//        webView.loadUrl(googleDocsUrl);
//    }
//}