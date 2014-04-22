package coursera.android101.week5;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class JabberActivity extends Activity {
    public static final String jabberUrl = "file:///android_asset/webcontent/jabberwocky.html";
    public static final String banderUrl = "file:///android_asset/webcontent/bandersnatch.html";
    private WebView jabberWebView;
    private String currentUrl = jabberUrl;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.jabber);
        jabberWebView = (WebView) findViewById(R.id.jabberWebView);
        jabberWebView.loadUrl(jabberUrl);
        currentUrl = jabberUrl;

        mediaPlayer = MediaPlayer.create(this, R.raw.horologium);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void wikipedia(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Jabberwocky")));
    }

    public void bandersnatch(View view) {
        if (currentUrl.equals(jabberUrl)) {
            jabberWebView.loadUrl(banderUrl);
            currentUrl = banderUrl;
        } else {
            jabberWebView.loadUrl(jabberUrl);
            currentUrl = jabberUrl;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

}
