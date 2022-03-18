package com.example.mazegame.ui.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import java.util.List;

public class MazeView extends View implements OnTouchListener {

    private static final float DRAWABLE_PERCENT_TO_CELL = 0.45f;
    private static final float TRAIL_PERCENT_TO_WALL = 0.75f;
    private static final float PREV_TRAIL_PERCENT_TO_WALL = TRAIL_PERCENT_TO_WALL / 2f;
    private static final int WALL_COLOR = Color.WHITE;
    private static final int TRAIL_COLOR = Color.WHITE;
    private static final int PREV_TRAIL_COLOR = Color.LTGRAY;

    private Paint wallPaint, trailPaint, prevTrailPaint;
    private Bitmap playerBitmap, finishBitmap;
    private RectF playerRect, touchRect, startRect, finishRect;
    private Path trailPath, prevTrailPath;
    private List<RectF> walls;
    private OnMoveListener listener;

    private int hrzOffset, vrtOffset;
    private float touchOffsetX, touchOffsetY;
    private float additionalTouchPadding;
    private boolean touchesEnabled = true;
    private boolean drawTrail;

    public interface OnMoveListener {
        void onMove(RectF playerRect, RectF finishRect);
    }

    public MazeView(Context context) {
        super(context);
        init();
    }

    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //region ******************** ACTIONS **********************************************************

    public MazeView setDrawables(@DrawableRes int playerRes, @DrawableRes int finishRes) {
        playerBitmap = BitmapFactory.decodeResource(getResources(), playerRes);
        finishBitmap = BitmapFactory.decodeResource(getResources(), finishRes);
        return this;
    }

    public MazeView setMoveListener(OnMoveListener listener) {
        this.listener = listener;
        return this;
    }

    public MazeView enableTrail() {
        drawTrail = true;
        return this;
    }

    public void start(int cols, int rows, int cellSize, List<RectF> walls) {
        this.walls = walls;
        clear();
        prepareMaze(cols, rows, cellSize);
        restart();
    }

    public void restart() {
        touchesEnabled = true;
        playerRect = new RectF(startRect);
        prevTrailPath.set(trailPath);
        trailPath.reset();
        trailPath.moveTo(playerRect.centerX(), playerRect.centerY());
        invalidate();
    }

    public void clear() {
        prevTrailPath.reset();
        trailPath.reset();
    }

    public void stop() {
        touchesEnabled = false;
    }

    //endregion ACTIONS

    //region ********************** INIT ***********************************************************

    private void init() {
        trailPath = new Path();
        prevTrailPath = new Path();

        wallPaint = new Paint();
        wallPaint.setColor(WALL_COLOR);
        wallPaint.setStyle(Paint.Style.FILL);

        trailPaint = new Paint();
        trailPaint.setColor(TRAIL_COLOR);
        trailPaint.setStyle(Paint.Style.STROKE);
        trailPaint.setStrokeJoin(Paint.Join.ROUND);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        trailPaint.setPathEffect(new DashPathEffect(new float[] { 10f, 20f }, 0f));

        prevTrailPaint = new Paint(trailPaint);
        prevTrailPaint.setColor(PREV_TRAIL_COLOR);

        touchRect = new RectF();
        setOnTouchListener(this);
    }

    //endregion INIT

    //region ******************** OVERRIDE *********************************************************

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isMazeReady()) return;

        canvas.translate(hrzOffset, vrtOffset);
        if(drawTrail) {
            canvas.drawPath(prevTrailPath, prevTrailPaint);
            canvas.drawPath(trailPath, trailPaint);
        }
        for(RectF wall : walls) canvas.drawRect(wall, wallPaint);
        canvas.drawBitmap(finishBitmap, null, finishRect, null);
        canvas.drawBitmap(playerBitmap, null, playerRect, null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        if(!isMazeReady()) return true;

        // для удобства расширяем зону касания; зона отрисовки player'а
        // и расчета его пересечений при этом остается неизменной
        touchRect.set(
            playerRect.left - additionalTouchPadding,
            playerRect.top - additionalTouchPadding,
            playerRect.right + additionalTouchPadding,
            playerRect.bottom + additionalTouchPadding);
        boolean touchAccepted = touchesEnabled
            && touchRect.contains(eventX - hrzOffset, eventY - vrtOffset);

        if(touchAccepted)
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchOffsetX = eventX - playerRect.centerX();
                    touchOffsetY = eventY - playerRect.centerY();

                    if(listener != null) listener.onMove(playerRect, finishRect);
                    break;
                case MotionEvent.ACTION_MOVE:
                    playerRect.offset(
                        eventX - (playerRect.centerX() + touchOffsetX),
                        eventY - (playerRect.centerY() + touchOffsetY));
                    if(drawTrail) trailPath.lineTo(eventX - touchOffsetX, eventY - touchOffsetY);
                    invalidate();

                    if(listener != null) listener.onMove(playerRect, finishRect);
                    break;
                default: break;
            }

        return true;
    }

    //endregion OVERRIDE

    //region ********************* HELPERS *********************************************************

    private void prepareMaze(int cols, int rows, int cellSize) {
        hrzOffset = (getWidth() - cols * cellSize) / 2;
        vrtOffset = (getHeight() - rows * cellSize) / 2;

        RectF wall = !walls.isEmpty() ? walls.get(0) : null;
        float wallThickness = wall != null ? Math.min(wall.width(), wall.height()) : 0;
        trailPaint.setStrokeWidth(wallThickness * TRAIL_PERCENT_TO_WALL);
        prevTrailPaint.setStrokeWidth(wallThickness * PREV_TRAIL_PERCENT_TO_WALL);

        float drawableMargin = (cellSize - cellSize * DRAWABLE_PERCENT_TO_CELL) / 2f;
        additionalTouchPadding = drawableMargin / 2f;
        startRect = new RectF(
            drawableMargin,
            drawableMargin,
            cellSize - drawableMargin,
            cellSize - drawableMargin
        );
        finishRect = new RectF(
            (cols - 1) * cellSize + drawableMargin,
            (rows - 1) * cellSize + drawableMargin,
            cols * cellSize - drawableMargin,
            rows * cellSize - drawableMargin
        );
    }

    private boolean isMazeReady() {
        return playerRect != null && startRect != null && finishRect != null
            && playerBitmap != null && finishBitmap != null && walls != null;
    }

    //endregion HELPERS
}
