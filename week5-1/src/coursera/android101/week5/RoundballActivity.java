package coursera.android101.week5;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class RoundballActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadContent();
    }

    private void loadContent() {
        setContentView(R.layout.roundball);
        WebView webView = (WebView) findViewById(R.id.roundballWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("file:///android_asset/webcontent/roundball/roundball.html");
    }
}
