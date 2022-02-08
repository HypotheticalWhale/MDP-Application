package com.example.mdpapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import java.util.HashSet;

public class ObstacleGridView extends View{
    private static final String TAG = "ObstacleGrid";
    private int numColumns, numRows;
    private int cellWidth, cellHeight;

    private final Paint blackPaint = new Paint();
    private final Paint whitePaint = new Paint();
    private final Paint yellowPaint = new Paint();

    private PixelGridView3.Obstacle obstacle;
    private PopupWindow popupWindow;

    float x1, x2, y1, y2, dx, dy;

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
    }

    public void setObstacle(PixelGridView3.Obstacle obstacle) {
        this.obstacle = obstacle;

        invalidate();
    }

    public PixelGridView3.Obstacle getObstacle() {
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
            canvas.drawLine(0, 0, cellWidth , 0, yellowPaint);
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
        switch(event.getAction()) {
            case(MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                break;
            case(MotionEvent.ACTION_UP): {
                x2 = event.getX();
                y2 = event.getY();
                dx = x2-x1;
                dy = y2-y1;

                // Use dx and dy to determine the direction of the move
                if(Math.abs(dx) > Math.abs(dy)) {
                    if(dx>0)
                        obstacle.direction = "E";
                    else
                        obstacle.direction = "W";
                } else {
                    if(dy>0)
                        obstacle.direction = "S";
                    else
                        obstacle.direction = "N";
                }
                Log.d(TAG, "onTouchEvent: direction: " + obstacle.direction);
                popupWindow.dismiss();
                break;
            }
        }
        return true;
    }
}