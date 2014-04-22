package coursera.android101.week5;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class NasaActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadContent();
    }

    private void loadContent() {
        setContentView(R.layout.nasa);
        WebView webView = (WebView) findViewById(R.id.nasaWebView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl("file:///android_asset/webcontent/uofi-at-nasa.html");
    }
}
