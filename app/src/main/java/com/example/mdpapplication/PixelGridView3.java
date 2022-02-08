package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.view.GestureDetectorCompat;

import java.util.HashSet;

public class PixelGridView3 extends View{
    private static final String TAG = "PixelGridView";

    private boolean mapDrawn = false;

    private static int[] curCoord;
    private static float cellSize;
    private static Cell[][] cells;

    private int numColumns, numRows;

    private String robotDirection = "None";

    private int counter = 1;
    private int[][] cellCounter;
    private boolean[][] cellChecked;

    private final Paint blackPaint = new Paint();
    private final Paint obstacleColor = new Paint();
    private final Paint robotColor = new Paint();
    private final Paint startColor = new Paint();
    private final Paint unexploredColor = new Paint();
    private final Paint exploredColor = new Paint();
    private final Paint obstacleDirectionColor = new Paint();
    private final Paint fastestPathColor = new Paint();
    private final Paint whitePaint = new Paint();

    private HashSet<Obstacle> obstacles;
    private SparseArray<Obstacle> obstaclePointer;

    private final GestureDetectorCompat gestureDetector;
    private final BluetoothConnectionHelper bluetooth;

    public static final String EVENT_SEND_MOVEMENT = "com.event.EVENT_SEND_MOVEMENT";

    /** Stores data about obstacle */
    private static class Obstacle {
        int id;
        int X;
        int Y;
        int targetID = -1;
        String direction = "None";

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

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return direction;
        }
    }

    private class Cell {
        float startX, startY, endX, endY;

        private Cell(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
    }

    public PixelGridView3(Context context) {
        this(context, null);
    }

    public PixelGridView3(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixelGridView,
                0, 0);
        this.numColumns = typedArray.getInt(R.styleable.PixelGridView_columns, 0);
        this.numRows = typedArray.getInt(R.styleable.PixelGridView_rows, 0);

        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        obstacleColor.setColor(Color.BLACK);
        robotColor.setColor(Color.GREEN);
        startColor.setColor(Color.CYAN);
        unexploredColor.setColor(Color.LTGRAY);
        exploredColor.setColor(Color.WHITE);
        obstacleDirectionColor.setColor(Color.WHITE);
        fastestPathColor.setColor(Color.MAGENTA);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(20);
        whitePaint.setTextAlign(Paint.Align.CENTER);
        
        bluetooth = new BluetoothConnectionHelper(context);
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_SEND_MOVEMENT));

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

    public void setCurCoord(int col, int row, String direction) {
        curCoord[0] = col;
        curCoord[1] = row;
        robotDirection = direction;

        invalidate();
    }

    public int[] getCurCoord(){
        return curCoord;
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

        cellSize = getWidth()/(numColumns+1);
        curCoord = new int[]{-1, -1};

        cellChecked = new boolean[numColumns][numRows];
        cellCounter = new int[numColumns][numRows];
        obstacles = new HashSet<Obstacle>(numColumns*numRows);
        obstaclePointer = new SparseArray<Obstacle>(numColumns*numRows);

        invalidate();
    }

    private void fixCount(int counter){
        for (Obstacle obstacle : obstacles) {
            if(obstacle.id > counter){
                obstacle.id--;
            }
        }
    }

    private void createCell() {
        cells = new Cell[numColumns + 1][numRows + 1];
        cellSize = getWidth()/(numColumns+1);

        for (int x = 0; x <= numColumns; x++)
            for (int y = 0; y <= numRows; y++)
                cells[x][y] = new Cell(x * cellSize + (cellSize / 30), y * cellSize + (cellSize / 30), (x + 1) * cellSize, (y + 1) * cellSize);
    }

    private void drawIndividualCell(Canvas canvas) {
        for (int x = 1; x <= numColumns; x++)
            for (int y = 0; y < numRows; y++)
                canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, unexploredColor);
    }

    private void drawGrid(Canvas canvas) {
        for (int y = 0; y <= numRows; y++)
            canvas.drawLine(cells[1][y].startX, cells[1][y].startY - (cellSize / 30), cells[20][y].endX, cells[20][y].startY - (cellSize / 30), blackPaint);
        for (int x = 0; x <= numColumns; x++)
            canvas.drawLine(cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][0].startY - (cellSize / 30), cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][19].endY + (cellSize / 30), blackPaint);
    }

    private void drawGridNumber(Canvas canvas) {
        for (int x = 1; x <= numColumns; x++) {
            if (x > 9)
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 5), cells[x][20].startY + (cellSize / 3), blackPaint);
            else
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 3), cells[x][20].startY + (cellSize / 3), blackPaint);
        }
        for (int y = 0; y < numRows; y++) {
            if ((20 - y) > 9)
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 2), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
            else
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 1.5f), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
        }
    }

    private void drawObstacle(Canvas canvas) {
        for (Obstacle obstacle : obstacles) {
            canvas.drawRect(obstacle.X * cellSize + (cellSize / 30), obstacle.Y * cellSize + (cellSize / 30), (obstacle.X + 1) * cellSize, (obstacle.Y + 1) * cellSize, obstacleColor);
            canvas.drawText(String.valueOf(obstacle.id), (obstacle.X + (float) 0.5) * cellSize, (obstacle.Y + (float) 0.65) * cellSize, whitePaint);
        }
    }

    private void drawRobot(Canvas canvas, int[] curCoord) {
        RectF rect;

        for (int i = 0; i < curCoord.length ; i++) {
            int col = curCoord[0];
            int row = convertRow(curCoord[1]);
            String direction = robotDirection;

            if(direction.equals("N")){
                rect = new RectF(col * cellSize, (row-1) * cellSize, (col + 2) * cellSize, (row + 1) * cellSize);
                Bitmap robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);
                canvas.drawBitmap(robotBitmap, null, rect, null);
            }
            else if(direction.equals("E")){
                rect = new RectF(col * cellSize, (row-1) * cellSize, (col + 2) * cellSize, (row + 1) * cellSize);
                Bitmap robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

                canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
            }
            else if(direction.equals("S")){
                rect = new RectF(col * cellSize, (row-1) * cellSize, (col + 2) * cellSize, (row + 1) * cellSize);
                Bitmap robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);

                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

                canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
            }
            else if(direction.equals("W")){
                rect = new RectF(col * cellSize, (row-1) * cellSize, (col + 2) * cellSize, (row + 1) * cellSize);
                Bitmap robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);

                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

                canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
            }
        }
    }

    private int convertRow(int row) {
        return (20 - row);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        if (!mapDrawn) {
            this.createCell();
            mapDrawn = true;
        }

        drawIndividualCell(canvas);
        drawGrid(canvas);
        drawGridNumber(canvas);
        drawObstacle(canvas);

        drawRobot(canvas, curCoord);
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

                column = (int) (event.getX() / cellSize);
                row = (int) (event.getY() / cellSize);

                // check if we've touched inside some circle
                if (column > 0 && row < 20) {
                    touchedObstacle = obtainTouchedObstacle(column, row);
                    touchedObstacle.X = column;
                    touchedObstacle.Y = row;
                    obstaclePointer.put(event.getPointerId(0), touchedObstacle);
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "ACTION_POINTER_DOWN");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                column = (int) (event.getX(actionIndex) / cellSize);
                row = (int) (event.getY(actionIndex) / cellSize);

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

                    column = (int) (event.getX(actionIndex) / cellSize);
                    row = (int) (event.getY(actionIndex) / cellSize);

                    Log.d(TAG, "ACTION_MOVE: Column: " + String.valueOf(column-(int)1) + " Row: " + String.valueOf(convertRow(row)-(int)1));

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

                column = (int) (event.getX(actionIndex) / cellSize);
                row = (int) (event.getY(actionIndex) / cellSize);

                Log.d(TAG, "ACTION_UP: Column: " + String.valueOf(column-(int)1) + " Row: " + String.valueOf(convertRow(row)-(int)1));

                touchedObstacle = getTouchedObstacle(column, row);

                if (column > 0 && row < 20) {
                    Obstacle overlappingObstacle = checkOverlappingObstacle(column, row, touchedObstacle.id);

                    if (overlappingObstacle != null) {
                        Log.w(TAG, "ACTION_UP: Overlapped ID: " + overlappingObstacle.id);
                        touchedObstacle.X = column + 1;
                    }
                }
                else{
                    if(touchedObstacle != null){
                        int deletedCount = touchedObstacle.id;
                        fixCount(deletedCount);
                        obstacles.remove(touchedObstacle);
                        counter--;
                    }
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
            int column = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            Log.d(TAG, "onLongPress: Column: " + String.valueOf(column-(int)1) + " Row: " + String.valueOf(convertRow(row)-(int)1));
            Log.d("Long press", "Pressed at: (" + column + "," + row + ")");
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_direction, null);
            PixelGridView3 pixelGrid = findViewById(R.id.pixelGrid);

            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(pixelGrid, Gravity.NO_GRAVITY, 0,row);

            // dismiss the popup window when touched
            popupView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
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
                Log.w(TAG, "checkOverlappingObstacle: Column: " + String.valueOf(obstacle.X-(int)1) + " Row: " + String.valueOf(convertRow(obstacle.Y)-(int)1) + " ID: " + obstacle.id);
                touched = obstacle;
                break;
            }
        }

        return touched;
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent.getAction().equals(EVENT_SEND_MOVEMENT)) {
                String message = intent.getStringExtra("key");
                int col = curCoord[0];
                int row = curCoord[1];
                String direction = robotDirection;

                if (message.equals("f")) {
                    if (direction.equals("N")) {
                        setCurCoord(col, row + 1, direction);
                    } else if (direction.equals("E")) {
                        setCurCoord(col + 1, row, direction);
                    } else if (direction.equals("S")) {
                        setCurCoord(col, row - 1, direction);
                    } else if (direction.equals("W")) {
                        setCurCoord(col - 1, row, direction);
                    }
                } else if (message.equals("r")) {
                    if (direction.equals("N")) {
                        setCurCoord(col, row - 1, direction);
                    } else if (direction.equals("E")) {
                        setCurCoord(col - 1, row, direction);
                    } else if (direction.equals("S")) {
                        setCurCoord(col, row + 1, direction);
                    } else if (direction.equals("W")) {
                        setCurCoord(col + 1, row, direction);
                    }
                } else if (message.equals("sl")) {
                    if (direction.equals("N")) {
                        setCurCoord(col - 1, row, direction);
                    } else if (direction.equals("E")) {
                        setCurCoord(col, row + 1, direction);
                    } else if (direction.equals("S")) {
                        setCurCoord(col + 1, row, direction);
                    } else if (direction.equals("W")) {
                        setCurCoord(col, row - 1, direction);
                    }
                } else if (message.equals("sr")) {
                    if (direction.equals("N")) {
                        setCurCoord(col + 1, row, direction);
                    } else if (direction.equals("E")) {
                        setCurCoord(col, row - 1, direction);
                    } else if (direction.equals("S")) {
                        setCurCoord(col - 1, row, direction);
                    } else if (direction.equals("W")) {
                        setCurCoord(col, row + 1, direction);
                    }
                } else if (message.equals("tl")) {
                    if (direction.equals("N")) {
                        direction = "W";
                    } else if (direction.equals("E")) {
                        direction = "N";
                    } else if (direction.equals("S")) {
                        direction = "E";
                    } else if (direction.equals("W")) {
                        direction = "S";
                    }

                    setCurCoord(col, row, direction);
                } else if (message.equals("tr")) {
                    if (direction.equals("N")) {
                        direction = "E";
                    } else if (direction.equals("E")) {
                        direction = "S";
                    } else if (direction.equals("S")) {
                        direction = "W";
                    } else if (direction.equals("W")) {
                        direction = "N";
                    }

                    setCurCoord(col, row, direction);
                }
            }
        }
    };
}