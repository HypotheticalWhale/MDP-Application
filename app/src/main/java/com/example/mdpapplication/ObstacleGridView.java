package com.example.mdpapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

public class ObstacleGridView extends View {
    private static final String TAG = "ObstacleGrid";
    private int numColumns, numRows;
    private int cellWidth, cellHeight;

    private final Paint blackPaint = new Paint();
    private final Paint whitePaint = new Paint();
    private final Paint yellowPaint = new Paint();

    private final GestureDetector gestureDetector;

    private PixelGridView.Obstacle obstacle;
    private PopupWindow popupWindow;


    public ObstacleGridView(Context context) {
        this(context, null);
    }

    public ObstacleGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixelGridView,
                0, 0);
        this.numColumns = typedArray.getInt(R.styleable.PixelGridView_columns, 0);
        this.numRows = typedArray.getInt(R.styleable.PixelGridView_rows, 0);

        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(80);
        whitePaint.setTextAlign(Paint.Align.CENTER);
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStrokeWidth(70);

        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setObstacle(PixelGridView.Obstacle obstacle) {
        this.obstacle = obstacle;

        invalidate();
    }

    public PixelGridView.Obstacle getObstacle() {
        return obstacle;
    }

    public void setPopupWindow(PopupWindow popupWindow) {
        this.popupWindow = popupWindow;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        Log.d(TAG, "onDraw: direction: " + obstacle.direction);

        if (obstacle.direction == "N") {
            canvas.drawRect(0, 0, cellWidth, cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), 0.5f * cellWidth, 0.65f * cellHeight, whitePaint);
            canvas.drawLine(0, 0, cellWidth, 0, yellowPaint);
        } else if (obstacle.direction == "E") {
            canvas.drawRect(0, 0, cellWidth, cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), 0.5f * cellWidth, 0.65f * cellHeight, whitePaint);
            canvas.drawLine(cellWidth, 0, cellWidth, cellHeight, yellowPaint);
        } else if (obstacle.direction == "S") {
            canvas.drawRect(0, 0, cellWidth, cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), 0.5f * cellWidth, 0.65f * cellHeight, whitePaint);
            canvas.drawLine(0, cellHeight, cellWidth, cellHeight, yellowPaint);
        } else if (obstacle.direction == "W") {
            canvas.drawRect(0, 0, cellWidth, cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), 0.5f * cellWidth, 0.65f * cellHeight, whitePaint);
            canvas.drawLine(0, 0, 0, cellHeight, yellowPaint);
        } else {
            canvas.drawRect(0, 0, cellWidth, cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), 0.5f * cellWidth, 0.65f * cellHeight, whitePaint);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            obstacle.direction = "E";
                        } else {
                            obstacle.direction = "W";
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        obstacle.direction = "S";
                    } else {
                        obstacle.direction = "N";
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            Log.d(TAG, "onFling: direction: " + obstacle.direction);
            popupWindow.dismiss();

            return result;
        }
    }
}