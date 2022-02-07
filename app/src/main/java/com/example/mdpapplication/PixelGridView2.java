package com.example.mdpapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import java.util.HashSet;

public class PixelGridView2 extends View{
    private static final String TAG = "PixelGridView";
    private int numColumns, numRows;
    private int cellWidth, cellHeight;

    private final Paint blackPaint = new Paint();
    private final Paint whitePaint = new Paint();

    private int counter = 1;
    private int[][] cellCounter;
    private boolean[][] cellChecked;

    private HashSet<Obstacle> obstacles;
    private SparseArray<Obstacle> obstaclePointer;

    private final GestureDetectorCompat gestureDetector;

    private final BluetoothConnectionHelper bluetooth;

    /** Stores data about obstacle */
    private static class Obstacle {
        int id;
        int X;
        int Y;
        int targetID;

        Obstacle(int X, int Y, int id) {
            this.id = id;
            this.X = X;
            this.Y = Y;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setTargetID(int targetID) {
            this.targetID = targetID;
        }

        public int getTargetID() {
            return targetID;
        }

        public void setX(int x) {
            X = x;
        }

        public int getX() {
            return X;
        }

        public void setY(int y) {
            Y = y;
        }

        public int getY() {
            return Y;
        }

    }

    public PixelGridView2(Context context) {
        this(context, null);
    }

    public PixelGridView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixelGridView,
                0, 0);
        this.numColumns = typedArray.getInt(R.styleable.PixelGridView_columns, 0);
        this.numRows = typedArray.getInt(R.styleable.PixelGridView_rows, 0);

        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(20);
        whitePaint.setTextAlign(Paint.Align.CENTER);

        bluetooth = new BluetoothConnectionHelper(context);

        gestureDetector = new GestureDetectorCompat(context, new GestureListener());
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows() {
        return numRows;
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

        cellChecked = new boolean[numColumns][numRows];
        cellCounter = new int[numColumns][numRows];
        obstacles = new HashSet<Obstacle>(numColumns*numRows);
        obstaclePointer = new SparseArray<Obstacle>(numColumns*numRows);

        invalidate();
    }

    private void fixCount(int column, int row){
        if(cellChecked[column][row]){
            cellCounter[column][row] = counter;
            counter++;
        }
        else{
            int deletedCount = cellCounter[column][row];
            cellCounter[column][row] = 0;
            counter--;

            for (int i = 0; i < numColumns; i++) {
                for (int j = 0; j < numRows; j++) {
                    if(cellCounter[i][j] > deletedCount){
                        cellCounter[i][j]--;
                    }
                }
            }

        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.robot);
        Bitmap scaledBitmap = scaleDown(bm, 100, true);
        canvas.drawBitmap(scaledBitmap, 1 * cellWidth, 18 * cellHeight, null);

        for (Obstacle obstacle : obstacles) {
            canvas.drawRect(obstacle.X * cellWidth, obstacle.Y * cellHeight, (obstacle.X + 1) * cellWidth, (obstacle.Y + 1) * cellHeight, blackPaint);
            canvas.drawText(String.valueOf(obstacle.id), (obstacle.X + (float) 0.5) * cellWidth, (obstacle.Y + (float) 0.65) * cellHeight, whitePaint);
        }

        for (int i = 1; i < numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i <= numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        Obstacle touchedObstacle;
        int pointerId;
        int column;
        int row;
        int actionIndex = event.getActionIndex();

        gestureDetector.onTouchEvent(event);

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.w(TAG, "ACTION_DOWN");
                // it's the first pointer, so clear all existing pointers data
                clearObstaclePointer();

                column = (int)(event.getX() / cellWidth);
                row = (int)(event.getY() / cellHeight);

                // check if we've touched inside some circle
                touchedObstacle = obtainTouchedObstacle(column, row);
                touchedObstacle.X = column;
                touchedObstacle.Y = row;
                obstaclePointer.put(event.getPointerId(0), touchedObstacle);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "ACTION_POINTER_DOWN");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                column = (int)(event.getX(actionIndex) / cellWidth);
                row = (int)(event.getY(actionIndex) / cellHeight);

                // check if we've touched inside some circle
                touchedObstacle = obtainTouchedObstacle(column, row);
                obstaclePointer.put(pointerId, touchedObstacle);
                touchedObstacle.X = column;
                touchedObstacle.Y = row;

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "ACTION_MOVE");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    column = (int)(event.getX(actionIndex) / cellWidth);
                    row = (int)(event.getY(actionIndex) / cellHeight);

                    Log.d(TAG, "ACTION_MOVE: Column: " + column + " Row: " + row);

                    touchedObstacle = obstaclePointer.get(pointerId);

                    if (null != touchedObstacle) {
                        touchedObstacle.X = column;
                        touchedObstacle.Y = row;
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                final int pointerEnd = event.getPointerCount();

                Log.w(TAG, "ACTION_UP");

                column = (int)(event.getX(actionIndex) / cellWidth);
                row = (int)(event.getY(actionIndex) / cellHeight);

                Log.d(TAG, "ACTION_UP: Column: " + column + " Row: " + row);

                touchedObstacle = getTouchedObstacle(column,row);

                if(column < 0 || row < 0){
                    obstacles.remove(touchedObstacle);
                    counter--;
                }

                Obstacle overlappingObstacle = checkOverlappingObstacle(column,row,touchedObstacle.id);
                bluetooth.write("ACTION_UP: Column: " + column + " Row: " + row);

                if(overlappingObstacle != null) {
                    Log.w(TAG, "ACTION_UP: Overlapped ID: " + overlappingObstacle.id);
                    touchedObstacle.X = column+1;
                }

                clearObstaclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                Log.w(TAG, "ACTION_POINTER_UP");
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                obstaclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                Log.w(TAG, "ACTION_CANCEL");

                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return handled;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);

            Log.d("Long press", "Pressed at: (" + column + "," + row + ")");

        }
    }

    private void clearObstaclePointer() {
        Log.w(TAG, "clearObstaclePointer");

        obstaclePointer.clear();
    }

    private Obstacle obtainTouchedObstacle(final int column, final int row) {
        Obstacle touchedObstacle = getTouchedObstacle(column, row);

        if (null == touchedObstacle) {
            touchedObstacle = new Obstacle(column, row, counter);
            counter++;

            Log.w(TAG, "Added circle " + touchedObstacle);
            obstacles.add(touchedObstacle);
        }

        return touchedObstacle;
    }

    private Obstacle getTouchedObstacle(final int column, final int row) {
        Obstacle touched = null;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.X == column && obstacle.Y == row) {
                touched = obstacle;
                break;
            }
        }

        return touched;
    }

    private Obstacle checkOverlappingObstacle(final int column, final int row, final int id) {
        Obstacle touched = null;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.X == column && obstacle.Y ==  row && obstacle.id != id) {
                Log.w(TAG, "checkOverlappingObstacle: Column: " + obstacle.X + " Row: " + obstacle.Y + " ID: " + obstacle.id);
                touched = obstacle;
                break;
            }
        }

        return touched;
    }
}