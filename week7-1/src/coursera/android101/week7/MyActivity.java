package coursera.android101.week7;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.*;
import android.widget.EditText;
import android.widget.ImageView;

public class MyActivity extends Activity {

    private EditText userText;
    private ImageView result;
    private Paint paint = new Paint();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        userText = (EditText) findViewById(R.id.userText);
        result = (ImageView) findViewById(R.id.result);
        paint.setAntiAlias(true);
        configureTextWatcher();
    }

    private void updateResultView(final String s) {
        result.postDelayed(new Runnable() {
            @Override
            public void run() {
                int w = result.getWidth();
                int h = result.getHeight();
                Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);

                canvas.drawColor(Color.WHITE);

                paint.setColor(Color.YELLOW);
                canvas.drawCircle(w / 2, h / 2, 0.8f * Math.min(w, h) / 2, paint);

                paint.setColor(Color.BLUE);
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                Layout layout = new StaticLayout(s, new TextPaint(paint), w, Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
                canvas.save();
                canvas.translate(w / 2, (h - layout.getHeight()) / 2);
                layout.draw(canvas);
                canvas.restore();

                result.setImageBitmap(b);
            }
        }, 0);
    }

    private void configureTextWatcher() {
        userText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do
            }

            @Override
            public void afterTextChanged(final Editable s) {
                updateResultView(s.toString());
            }
        });
    }

}
