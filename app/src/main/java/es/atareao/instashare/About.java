package es.atareao.instashare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.Buffer;

public class About extends Activity {
    private WebView _webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Button button_about_exit=(Button)findViewById(R.id.activity_about_button_exit);
        button_about_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        _webView = (WebView)findViewById(R.id.webView);
        WebSettings settings = _webView.getSettings();
        settings.setDefaultFontSize(14);
        settings.setJavaScriptEnabled(true);
        _webView.setBackgroundColor(ContextCompat.getColor(this, R.color.Transparent));
        final Context context = this.getBaseContext();
        _webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String version ="";
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e1) {
                    e1.printStackTrace();
                }
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                runJavascript(String.format("replaceScript('$APPNAME$', '%s')", getString(R.string.app_name)));
                runJavascript(String.format("replaceScript('$VERSION$', '%s')",version));
                runJavascript(String.format("replaceScript('$APPDESCRIPTION$', '%s')",getString(R.string.app_description)));
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });
        BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.about));
        try {
            StringBuffer sb = new StringBuffer();
            while(bis.available()>0) {
                // read the byte and convert the integer to character
                sb.append((char)bis.read());
            }
            bis.close();
            _webView.loadData(sb.toString(), "text/html", "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }


        //_webView.loadUrl("file:///android_asset/www/about.html");
    }
    private void runJavascript(String sentence){
        _webView.loadUrl("javascript: " + sentence);
    }
}
