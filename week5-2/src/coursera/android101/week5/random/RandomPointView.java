package coursera.android101.week5.random;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RandomPointView extends SurfaceView implements SurfaceHolder.Callback {

    // system injected
    private Context context;
    private WorkerThread workerThread;

    public RandomPointView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get holder and register for callback of changes
        getHolder().addCallback(this);

        // make sure we get key events
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_UP ||
                ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            workerThread.resetAndPickNewPaint();
            return true;
        }

        if (ev.getActionMasked() != MotionEvent.ACTION_DOWN &&
                ev.getActionMasked() != MotionEvent.ACTION_POINTER_DOWN) {
            // do nothing and consume the event if it's not an interesting event to us
            /*
            to simplify things, I'm not handling move events. And I am not tracking pointers individually
             */
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
        // controls the running state of the thread
        private boolean running = false;
        // keeps track of upcoming motions to handle
        private Queue<PointF> motionQueue = new ConcurrentLinkedQueue<PointF>();
        // backing buffer to draw in - double buffer essentially
        private Bitmap buffer;
        // canvas to allow drawing operations into buffer
        private Canvas bufferCanvas;
        // current paint
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // current radius
        private int radius = 5;
        // current point
        private PointF point;

        WorkerThread(Context context, SurfaceHolder holder) {
            this.context = context;
            this.holder = holder;
            paint.setColor(Color.rgb(255, 255, 0)); // start with yellow
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
                    // give up thread control to handle motion
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
            motionQueue.addAll(points);
            Log.d("mt", "motionQueue has " + motionQueue.size() + " points");
        }

        public void resetAndPickNewPaint() {
            // pick new paint with 75% transparency
            float hsv[] = new float[3];
            Color.colorToHSV(paint.getColor(), hsv);
            hsv[0] = new Random().nextInt(360);
            paint.setColor(Color.HSVToColor(hsv));

            // reset radius and remove current point
            radius = 5;
            point = null;
        }

        private void drawMotionWithDots(Canvas canvas) {
            if (buffer == null) {
                // lazily initialize back buffer
                buffer = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(buffer);
            }
            if (motionQueue.isEmpty() && point != null) {
                // noting new to draw, so increase radius
                drawPointAndIncreaseRadius(point);
            } else {
                while (!motionQueue.isEmpty()) {
                    // drain motion queue and draw all points
                    point = motionQueue.remove();
                    drawPointAndIncreaseRadius(point);
                }
            }

            // draw buffer into real canvas
            canvas.drawBitmap(buffer, 0, 0, paint);
        }

        private void drawPointAndIncreaseRadius(PointF point) {
            bufferCanvas.drawCircle(point.x, point.y, radius, paint);
            radius++;
        }
    }

}

