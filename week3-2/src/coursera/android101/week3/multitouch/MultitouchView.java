package coursera.android101.week3.multitouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultitouchView extends SurfaceView implements SurfaceHolder.Callback {

    // system injected
    private Context context;
    private WorkerThread workerThread;

    public MultitouchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get holder and register for callback of changes
        getHolder().addCallback(this);

        // make sure we get key events
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() != MotionEvent.ACTION_DOWN &&
                ev.getActionMasked() != MotionEvent.ACTION_POINTER_DOWN &&
                ev.getActionMasked() != MotionEvent.ACTION_MOVE) {
            // do nothing and consume the event if it's not an interesting event to us
            return true;
        }

        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();

        List<PointF> points = new LinkedList<PointF>();

        for (int h = 0; h < historySize; h++) {
            for (int p = 0; p < pointerCount; p++) {
                Log.d("mt", String.format("pointer %d: (%f,%f)", ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h)));
                points.add(new PointF(ev.getHistoricalX(p, h), ev.getHistoricalY(p, h)));
            }
        }

        for (int p = 0; p < pointerCount; p++) {
            Log.d("mt", String.format("pointer %d: (%f,%f)", ev.getPointerId(p), ev.getX(p), ev.getY(p)));
            points.add(new PointF(ev.getX(p), ev.getY(p)));
        }

        // send touch event to thread to process
        workerThread.handleMotion(points);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (workerThread == null || workerThread.getState() == Thread.State.TERMINATED) {
            workerThread = new WorkerThread(context, holder);
            workerThread.setRunning(true);
            workerThread.start();
        }

        Log.d("mt", "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // ??? what to do here, what changes are there
        Log.d("mt", "surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // kill the thread
        workerThread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                workerThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // nothing to do here
            }
        }
        Log.d("mt", "surface destroyed");
    }

    class WorkerThread extends Thread {

        private final Context context;
        private final SurfaceHolder holder;
        private Paint paint;
        private boolean running = false;
        private Queue<PointF> montionQueue = new ConcurrentLinkedQueue<PointF>();
        private Bitmap buffer;
        private Canvas bufferCanvas;

        WorkerThread(Context context, SurfaceHolder holder) {
            this.context = context;
            this.holder = holder;

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setARGB(255, 255, 255, 0);
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Log.d("mt", "starting thread run()");

            while (running) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas();
                    // draw based on touch event
                    drawMotionWithDots(c);
                    yield();
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }

            Log.d("mt", "exiting thread run()");
        }

        public void handleMotion(List<PointF> points) {
            montionQueue.addAll(points);
            Log.d("mt", "motionQueue has " + montionQueue.size() + " points");
        }

        private void drawMotionWithDots(Canvas canvas) {
            if (buffer == null) {
                buffer = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(buffer);
            }
            while (!montionQueue.isEmpty()) {
                PointF center = montionQueue.remove();
                bufferCanvas.drawCircle(center.x, center.y, 5, paint);
            }
            canvas.drawBitmap(buffer, 0, 0, paint);
        }
    }

}

