package app.xedigital.ai.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;

import app.xedigital.ai.R;

public class PDFViewActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        // Prevent screenshots and screen recordings
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        // Initialize views
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        webView.getSettings().setJavaScriptEnabled(true);

        // Restrict opening in external apps
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                return host == null || !host.contains("docs.google.com");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PDFViewActivity.this, "Error loading PDF: " + description, Toast.LENGTH_LONG).show();
            }
        });

        String pdfUrl = getIntent().getStringExtra("pdfUrl");

        if (pdfUrl == null || !pdfUrl.startsWith("https://")) {
            // Show an error message if the URL is invalid
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show the progress bar while loading
        progressBar.setVisibility(View.VISIBLE);

        String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + pdfUrl;
        webView.loadUrl(googleDocsUrl);
    }

    @Override
    public void onBackPressed() {
        // Handle back navigation within the WebView
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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