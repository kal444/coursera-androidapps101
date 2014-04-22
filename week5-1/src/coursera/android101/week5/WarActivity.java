package coursera.android101.week5;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class WarActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadContent();
    }

    private void loadContent() {
        setContentView(R.layout.war);
        WebView webView = (WebView) findViewById(R.id.warWebView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl("file:///android_asset/webcontent/waroftheworlds.html");
    }
}
