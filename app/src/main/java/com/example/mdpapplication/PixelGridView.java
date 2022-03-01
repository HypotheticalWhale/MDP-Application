package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PixelGridView extends View {
    private static final String TAG = "PixelGridView";
    public static final String EVENT_SEND_MOVEMENT = "com.event.EVENT_SEND_MOVEMENT";
    public static final String EVENT_TARGET_SCANNED = "com.event.EVENT_TARGET_SCANNED";
    public static final String EVENT_ROBOT_MOVES = "com.event.EVENT_ROBOT_MOVES";

    private boolean mapDrawn = false;
    private boolean showTargetID = false;

    private static float cellSize;
    private static Cell[][] cells;

    private Robot robot;

    private HashSet<Obstacle> obstacles;
    private SparseArray<Obstacle> obstaclePointer;
    private Queue<String> robotCommands;

    private static int numColumns, numRows;

    private int counter = 0;

    private final Paint blackPaint = new Paint();
    private final Paint obstacleColor = new Paint();
    private final Paint robotColor = new Paint();
    private final Paint startColor = new Paint();
    private final Paint unexploredColor = new Paint();
    private final Paint exploredColor = new Paint();
    private final Paint obstacleDirectionColor = new Paint();
    private final Paint fastestPathColor = new Paint();
    private final Paint whitePaint = new Paint();
    private final Paint yellowPaint = new Paint();
    private final Paint targetScannedColor = new Paint();

    @NonNull
    private final GestureDetectorCompat gestureDetector;
    private final BluetoothConnectionHelper bluetooth;

    private static final List<String> ValidTargetStrings = Arrays.asList("Alphabet_A", "Alphabet_B", "Alphabet_C",
            "Alphabet_D", "Alphabet_E", "Alphabet_F",
            "Alphabet_G", "Alphabet_H", "Alphabet_S",
            "Alphabet_T", "Alphabet_U", "Alphabet_V",
            "Alphabet_W", "Alphabet_X", "Alphabet_Y",
            "Alphabet_Z", "down_arrow", "bullseye",
            "eight", "five", "four", "left_arrow",
            "nine", "one", "right_arrow", "seven",
            "six", "stop", "three", "two", "up_arrow");

    JSONObject targetIdJSON;

    {
        try {
            targetIdJSON = new JSONObject("{\n" +
                    "    \"Alphabet_A\": 20,\n" +
                    "    \"Alphabet_B\": 21,\n" +
                    "    \"Alphabet_C\": 22,\n" +
                    "    \"Alphabet_D\": 23,\n" +
                    "    \"Alphabet_E\": 24,\n" +
                    "    \"Alphabet_F\": 25,\n" +
                    "    \"Alphabet_G\": 26,\n" +
                    "    \"Alphabet_H\": 27,\n" +
                    "    \"Alphabet_S\": 28,\n" +
                    "    \"Alphabet_T\": 29,\n" +
                    "    \"Alphabet_U\": 30,\n" +
                    "    \"Alphabet_V\": 31,\n" +
                    "    \"Alphabet_W\": 32,\n" +
                    "    \"Alphabet_X\": 33,\n" +
                    "    \"Alphabet_Y\": 34,\n" +
                    "    \"Alphabet_Z\": 35,\n" +
                    "    \"down_arrow\": 37,\n" +
                    "    \"eight\": 18,\n" +
                    "    \"five\": 15,\n" +
                    "    \"four\": 14,\n" +
                    "    \"left_arrow\": 39,\n" +
                    "    \"nine\": 19,\n" +
                    "    \"one\": 11,\n" +
                    "    \"right_arrow\": 38,\n" +
                    "    \"seven\": 17,\n" +
                    "    \"six\": 16,\n" +
                    "    \"stop\": 40,\n" +
                    "    \"three\": 13,\n" +
                    "    \"two\": 12,\n" +
                    "    \"up_arrow\": 36\n" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private final Context cachedContext;

    private List<String> ValidRobotMovementCommands = Arrays.asList("f", "b");
    private List<String> ValidRobotRotationCommands = Arrays.asList("l", "r");

    /**
     * Stores data about obstacle
     */
    public static class Obstacle implements Parcelable {
        int id;
        int X;
        int Y;
        int xOnGrid;
        int yOnGrid;
        String targetID;
        String direction = "None";

        Obstacle(int X, int Y, int xOnGrid, int yOnGrid, int id) {
            this.id = id;
            this.X = X;
            this.Y = Y;
            this.xOnGrid = xOnGrid;
            this.yOnGrid = yOnGrid;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setTargetID(String targetID) {
            this.targetID = targetID;
        }

        public String getTargetID() {
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

        public void setxOnGrid(int xOnGrid) {
            this.xOnGrid = xOnGrid;
        }

        public int getxOnGrid() {
            return xOnGrid;
        }

        public void setyOnGrid(int yOnGrid) {
            this.yOnGrid = yOnGrid;
        }

        public int getyOnGrid() {
            return yOnGrid;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.X);
            dest.writeInt(this.Y);
            dest.writeInt(this.xOnGrid);
            dest.writeInt(this.yOnGrid);
            dest.writeString(this.targetID);
            dest.writeString(this.direction);
        }

        public void readFromParcel(@NonNull Parcel source) {
            this.id = source.readInt();
            this.X = source.readInt();
            this.Y = source.readInt();
            this.xOnGrid = source.readInt();
            this.yOnGrid = source.readInt();
            this.targetID = source.readString();
            this.direction = source.readString();
        }

        protected Obstacle(@NonNull Parcel in) {
            this.id = in.readInt();
            this.X = in.readInt();
            this.Y = in.readInt();
            this.xOnGrid = in.readInt();
            this.yOnGrid = in.readInt();
            this.targetID = in.readString();
            this.direction = in.readString();
        }

        public static final Parcelable.Creator<Obstacle> CREATOR = new Parcelable.Creator<Obstacle>() {
            @NonNull
            @Override
            public Obstacle createFromParcel(@NonNull Parcel source) {
                return new Obstacle(source);
            }

            @NonNull
            @Override
            public Obstacle[] newArray(int size) {
                return new Obstacle[size];
            }
        };
    }

    public static class Robot {
        float[] robotSize;
        float[] xLength;
        float[] yLength;
        float[] X;
        float[] Y;
        String direction = "None";

        private Robot(float X, float Y, @NonNull float[] robotSize) {
            this.robotSize = Arrays.copyOf(robotSize, robotSize.length);

            this.X = new float[(int)Math.ceil(robotSize[0])];
            for (int i = 0; i < this.X.length; i++) {
                this.X[i] = X + i;
            }

            this.xLength = new float[(int)Math.ceil(robotSize[0])];
            for (int i = 0; i < this.xLength.length; i++) {
                this.xLength[i] = i;
            }

            this.Y = new float[(int)Math.ceil(robotSize[1])];
            for (int i = 0; i < this.Y.length; i++) {
                this.Y[i] = Y + i;
            }

            this.yLength = new float[(int)Math.ceil(robotSize[1])];
            for (int i = 0; i < this.yLength.length; i++) {
                this.yLength[i] = i;
            }

        }


        public float[] getRobotSize() {
            return robotSize;
        }

        public void setRobotSize(float[] robotSize) {
            this.robotSize = robotSize;
        }

        public float[] getXArray() {
            return X;
        }

        public void setXArray(float[] x) {
            X = x;
        }

        public float[] getYArray() {
            return Y;
        }

        public void setYArray(float[] y) {
            Y = y;
        }

        public float getX() {
            return X[0];
        }

        public void setX(float x) {
            if (x >= 17 && x < 18) {
                for (int i = 0; i < this.X.length; i++) {
                    this.X[i] = x + i - (x % 1);
                }
//                Arrays.fill(this.X, x - (x % 1));
            } else if (x >= 18 && x < 19) {
                for (int i = 0; i < this.X.length; i++) {
                    this.X[i] = x  + i - 1 - (x % 1);
                }
            } else if (x >= 19 && x < 20) {
                for (int i = 0; i < this.X.length; i++) {
                    this.X[i] = x + i - 2 - (x % 1);
                }
            } else {
                for (int i = 0; i < this.X.length; i++) {
                    this.X[i] = x + i;
                }
            }
        }

        public float getY() {
            return Y[0];
        }

        public void setY(float y) {
            if (y >= 17 && y < 18) {
                for (int i = 0; i < this.Y.length; i++) {
                    this.Y[i] = y + i - (y % 1);
                }
            } else if (y >= 18 && y < 19) {
                for (int i = 0; i < this.Y.length; i++) {
                    this.Y[i] = y  + i - 1 - (y % 1);
                }
            } else if (y >= 19 && y < 20) {
                for (int i = 0; i < this.Y.length; i++) {
                    this.Y[i] = y + i - 2 - (y % 1);
                }
            } else {
                for (int i = 0; i < this.Y.length; i++) {
                    this.Y[i] = y + i;
                }
            }
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    private static class Cell {
        float startX, startY, endX, endY;
        boolean explored = false;

        private Cell(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public boolean getExplored(){
            return this.explored;
        }

        public void setExplored(boolean explored){
            this.explored = explored;
        }
    }

    private static class ScannedObstacle {
        String target;
        int vertial;
        int horizontal;

        public ScannedObstacle(String target, int vertial, int horizontal) {
            this.target = target;
            this.vertial = vertial;
            this.horizontal = horizontal;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public int getVertial() {
            return vertial;
        }

        public void setVertial(int vertial) {
            this.vertial = vertial;
        }

        public int getHorizontal() {
            return horizontal;
        }

        public void setHorizontal(int horizontal) {
            this.horizontal = horizontal;
        }
    }

    public PixelGridView(@NonNull Context context) {
        this(context, null);
    }

    public PixelGridView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixelGridView, 0, 0);
        numColumns = typedArray.getInt(R.styleable.PixelGridView_columns, 0);
        numRows = typedArray.getInt(R.styleable.PixelGridView_rows, 0);
        typedArray.recycle();

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
        targetScannedColor.setColor(Color.WHITE);
        targetScannedColor.setTextSize(30);
        targetScannedColor.setTextAlign(Paint.Align.CENTER);
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStrokeWidth(6);

        bluetooth = MDPApplication.getBluetooth();
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_SEND_MOVEMENT));
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_TARGET_SCANNED));
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_ROBOT_MOVES));

        gestureDetector = new GestureDetectorCompat(context, new GestureListener());

        robot = new Robot(0, 0, new float[]{3f, 3f});
        robot.setDirection("N");
        obstacles = new HashSet<>(numColumns * numRows);
        obstaclePointer = new SparseArray<>(numColumns * numRows);

        robotCommands = new LinkedList<>();
        new robotCommandThread().start();

        cachedContext = context;
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

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setCurCoord(float col, float row, String direction) {
        Log.d(TAG, "setCurCoord: Column: " + col + " Row: " + row + " Direction: " + direction);

        float[] X = robot.getXArray().clone();
        float[] Y = robot.getYArray().clone();

        for (int i = 0; i < X.length; i++) {
            for (float y : Y) {
                Log.d(TAG, "setCurCoord: i:" + i + " X:" + X[i] + " Y:" + y);
                cells[(int) X[i] + 1][19 - (int) y].setExplored(true);
            }
        }

        robot.setX(col);
        robot.setY(row);
        robot.setDirection(direction);

        X = robot.getXArray().clone();
        Y = robot.getYArray().clone();

        for (int i = 0; i < X.length; i++) {
            for (float y : Y) {
                Log.d(TAG, "setCurCoord: i:" + i + " X:" + X[i] + " Y:" + y);
                cells[(int) X[i] + 1][19 - (int) y].setExplored(true);
            }
        }

        invalidate();
    }

    public void setRobot(float col, float row, String direction) {
        Log.d(TAG, "setRobot: Column: " + col + " Row: " + row + " Direction: " + direction);

        robot.setX(col);
        robot.setY(row);
        robot.setDirection(direction);

        invalidate();
    }

    public boolean getShowTargetID(){
        return this.showTargetID;
    }

    public void setShowTargetID(boolean showTargetID){
        this.showTargetID = showTargetID;
        invalidate();
    }

    @NonNull
    public float[] getCurCoord() {
        return new float[]{robot.getX(), robot.getY()};
    }

    public String getRobotDirection() {
        return robot.getDirection();
    }

    public HashSet<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(@NonNull HashSet<Obstacle> obstacles) {
        this.obstacles = new HashSet<>(obstacles);
        Log.d(TAG, "setObstacles: " + obstacles.size());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    public void resetGrid() {
        calculateDimensions();

        robot = new Robot(0, 0, new float[]{3f, 3f});
        robot.setDirection("N");
        obstacles = new HashSet<>(numColumns * numRows);
        obstaclePointer = new SparseArray<>(numColumns * numRows);
        counter = 0;

        for (int x = 1; x <= numColumns; x++){
            for (int y = 0; y < numRows; y++) {
                cells[x][y].setExplored(false);
            }
        }

        invalidate();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        cellSize = getWidth() / (numColumns + 1);
    }

    private void fixCount(int counter) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.id > counter) {
                obstacle.id--;
            }
        }
    }

    private void clearObstaclePointer() {
        Log.w(TAG, "clearObstaclePointer");

        obstaclePointer.clear();
    }

    @Nullable
    private Obstacle obtainTouchedObstacle(final int column, final int row) {
        Obstacle touchedObstacle = getTouchedObstacle(column, row);

        if (null == touchedObstacle) {
            if (column > 0 && column <= numColumns && row >= 0 && row < numRows) {
                touchedObstacle = new Obstacle(column, row, (column - 1), (convertRow(row) - 1), counter);
                counter++;

                Log.w(TAG, "Added Obstacle: id: " + touchedObstacle.id + " X: " + touchedObstacle.X + " Y: " + touchedObstacle.Y);
                obstacles.add(touchedObstacle);
            }
        }

        return touchedObstacle;
    }

    @Nullable
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

    @Nullable
    private Obstacle findObstacleByID(final int id) {
        Obstacle found = null;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.id == id) {
                found = obstacle;
                break;
            }
        }

        return found;
    }

    @Nullable
    private Obstacle findObstacleByGridCoord(int column, int row) {
        Obstacle found = null;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.xOnGrid == column && obstacle.yOnGrid == row) {
                found = obstacle;
                break;
            }
        }

        return found;
    }

    @Nullable
    private List<Obstacle> findObstacleUsingRobot(Robot robot, int noOfObstacle, List<ScannedObstacle> scanned) {
        class ObstacleExtraInfo {
            Obstacle obstacle;
            double distance;
            double angle;


            public ObstacleExtraInfo(Obstacle obstacle, double distance, double angle) {
                this.obstacle = obstacle;
                this.distance = distance;
                this.angle = angle;
            }

            public Obstacle getObstacle() {
                return obstacle;
            }

            public void setObstacle(Obstacle obstacle) {
                this.obstacle = obstacle;
            }

            public double getDistance() {
                return distance;
            }

            public void setDistance(double distance) {
                this.distance = distance;
            }

            public double getAngle() {
                return angle;
            }

            public void setAngle(double angle) {
                this.angle = angle;
            }
        }

        List<Obstacle> obstacleList = new ArrayList<>(noOfObstacle);
        List<ObstacleExtraInfo> obstacleExtraInfo = new ArrayList<>(obstacles.size());
        List<ObstacleExtraInfo> found = new ArrayList<>(obstacles.size());

        String direction = robot.getDirection();

        float startX = robot.getXArray()[0];
        float startY = robot.getYArray()[0];
        float middleX = robot.getXArray()[1];
        float middleY = robot.getYArray()[1];
        float endX = robot.getXArray()[2];
        float endY = robot.getYArray()[2];

        double X = 0, Y = 0;

        /**
         * startX = Left of robot when facing North, Back of robot when facing East, Right of robot when facing South, Front of robot when facing West
         * endX = Right of robot when facing North, Front of robot when facing East, Left of robot when facing South, Back of robot when facing West
         * startY = Back of robot when facing North, Right of robot when facing East, Front of robot when facing South, Left of robot when facing West
         * endY = Front of robot when facing North, Left of robot when facing East, Back of robot when facing South, Right of robot when facing West
         */

        if (direction.equals("N")) {
            X = middleX;
            Y = endY;
        } else if (direction.equals("E")) { // Rotate by 90 degrees (X,Y) to (-Y,X)
            X = -middleY;
            Y = endX;
        } else if (direction.equals("S")) { // Rotate by 180 degrees (X,Y) to (-X,-Y)
            X = -middleX;
            Y = -startY;
        } else if (direction.equals("W")) { // Rotate by 270 degrees (X,Y) to (Y,-X)
            X = middleY;
            Y = -startX;
        }

        Log.d(TAG, "findObstacleUsingRobot: direction: " + direction);

        for (Obstacle obstacle : obstacles) {
            int obsX = 0, obsY = 0;

            Log.d(TAG, "findObstacleUsingRobot: obstacle.id: " + obstacle.id);

            if (direction.equals("N")) {
                obsX = obstacle.xOnGrid;
                obsY = obstacle.yOnGrid;
            } else if (direction.equals("E")) {
                obsX = -obstacle.yOnGrid;
                obsY = obstacle.xOnGrid;
            } else if (direction.equals("S")) {
                obsX = -obstacle.xOnGrid;
                obsY = -obstacle.yOnGrid;
            } else if (direction.equals("W")) {
                obsX = obstacle.yOnGrid;
                obsY = -obstacle.xOnGrid;
            }

            double dist = Math.sqrt(Math.pow((obsX - X), 2) + Math.pow((obsY - Y), 2));

            Log.d(TAG, "findObstacleUsingRobot: dist: " + dist);

            double angleX = Math.toDegrees(Math.acos((obsX - X) / dist));
            double angleY = Math.toDegrees(Math.asin((obsY - Y) / dist));

            Log.d(TAG, "findObstacleUsingRobot: angleX: " + angleX);
            Log.d(TAG, "findObstacleUsingRobot: angleY: " + angleY);

            angleX = Math.round(angleX * 100) / 100;
            angleY = Math.round(angleY * 100) / 100;

            Log.d(TAG, "findObstacleUsingRobot: rounded angleX: " + angleX);
            Log.d(TAG, "findObstacleUsingRobot: rounded angleY: " + angleY);

            ObstacleExtraInfo temp = null;

            if (angleX == angleY) {
                temp = new ObstacleExtraInfo(obstacle, dist, angleX);
            } else if (angleY < 0) {
                temp = new ObstacleExtraInfo(obstacle, dist, angleY);
            } else if (angleX > angleY) {
                temp = new ObstacleExtraInfo(obstacle, dist, angleX);
            }

            obstacleExtraInfo.add(temp);
        }

        obstacleExtraInfo.sort((o1, o2) -> {
            if (o1.getDistance() > o2.getDistance()) {
                return 1;
            } else if (o1.getDistance() < o2.getDistance()) {
                return -1;
            }
            return -1;
        });

        int count = 0;

        for (ObstacleExtraInfo obstacle : obstacleExtraInfo) {
            if (count == noOfObstacle) {
                break;
            }
            Log.d(TAG, "findObstacleUsingRobot: obstaclesWithDistance: obstacle.getAngle(): " + obstacle.getAngle());

            if (obstacle.getAngle() >= 45 && obstacle.getAngle() <= 135) {
                Log.d(TAG, "findObstacleUsingRobot: obstaclesWithDistance: obstacle.getObstacle().id: " + obstacle.getObstacle().id);
                Log.d(TAG, "findObstacleUsingRobot: obstaclesWithDistance: obstacle.getDistance(): " + obstacle.getDistance());
                obstacleList.add(obstacle.getObstacle());
                found.add(obstacle);
                count++;
            }
        }

//        scanned.sort((o1, o2) -> {
//            if (o1.getVertial() > o2.getVertial()) {
//                return 1;
//            } else if (o1.getVertial() < o2.getVertial()) {
//                return -1;
//            }
//            return -1;
//        });
//
//        int i = 0;
//
//        for(Obstacle obstacle: obstacleList){
//            obstacle.targetID = scanned.get(i).getTarget();
//            i++;
//        }

        scanned.sort((o1, o2) -> {
            if (o1.getHorizontal() < o2.getHorizontal()) {
                return 1;
            } else if (o1.getHorizontal() > o2.getHorizontal()) {
                return -1;
            }
            return -1;
        });

        found.sort((o1, o2) -> {
            if (o1.getAngle() > o2.getAngle()) {
                return 1;
            } else if (o1.getAngle() < o2.getAngle()) {
                return -1;
            }
            return -1;
        });

        int i = 0;

        for (ObstacleExtraInfo obstacle : found) {
            obstacle.getObstacle().targetID = scanned.get(i).getTarget();
            i++;
        }

        invalidate();

        return obstacleList;
    }

    public void testDistance() {

        List<ScannedObstacle> scanned = new ArrayList<>();
        scanned.add(new ScannedObstacle("six", 1, 3));
        scanned.add(new ScannedObstacle("seven", 2, 1));
        scanned.add(new ScannedObstacle("eight", 3, 2));

        findObstacleUsingRobot(robot, 3, scanned);
    }

    @Nullable
    private Obstacle checkOverlappingObstacle(final int column, final int row, final int id) {
        Obstacle touched = null;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.X == column && obstacle.Y == row && obstacle.id != id) {
                Log.w(TAG, "checkOverlappingObstacle: Column: " + (obstacle.X - (int) 1) + " Row: " + (convertRow(obstacle.Y) - (int) 1) + " ID: " + obstacle.id);
                touched = obstacle;
                break;
            }
        }

        return touched;
    }

    private boolean checkMovable(float[] fX, float[] fY, @NonNull String direction, @NonNull String command) {
        int[] X = new int[fX.length];
        int[] Y = new int[fY.length];

        boolean roundUp = (command.equals("f") && direction.equals("N")) ||
                (command.equals("b") && direction.equals("S")) ||
                (command.equals("f") && direction.equals("E")) ||
                (command.equals("b") && direction.equals("W"));

        boolean roundDown = (command.equals("f") && direction.equals("S")) ||
                (command.equals("b") && direction.equals("N")) ||
                (command.equals("f") && direction.equals("W")) ||
                (command.equals("b") && direction.equals("E"));

        for(int i=0; i< fX.length;i++) {
            if (roundUp) {
                X[i] = Math.round(fX[i]);
            } else if (roundDown) {
                X[i] = (int) fX[i];
            }
        }
        for(int i=0; i< fY.length;i++){
            if (roundUp) {
                Y[i] = Math.round(fY[i]);
            } else if (roundDown) {
                Y[i] = (int) fY[i];
            }
        }

        Obstacle[] obstacleArray = new Obstacle[X.length];
        Log.d(TAG, "checkMovable: startX: " + X[0] + " middleX: " + X[1] + " endX: " + X[2] + " startY: " + Y[0] + " middleY: " + Y[1] + " endY: " + Y[2] + " Direction: " + direction);

        /**
         * startX = Left of robot when facing North, Back of robot when facing East, Right of robot when facing South, Front of robot when facing West
         * endX = Right of robot when facing North, Front of robot when facing East, Left of robot when facing South, Back of robot when facing West
         * startY = Back of robot when facing North, Right of robot when facing East, Front of robot when facing South, Left of robot when facing West
         * endY = Front of robot when facing North, Left of robot when facing East, Back of robot when facing South, Right of robot when facing West
         */

        if ((X[0] >= 0 && X[2] < numColumns && Y[0] >= 0 && Y[2] < numRows)) {
            if ((command.equals("f") && direction.equals("N")) ||
                    (command.equals("b") && direction.equals("S")) ||
                    (command.equals("sl") && direction.equals("E")) ||
                    (command.equals("sr") && direction.equals("W"))) {
                for (int i = 0; i < obstacleArray.length; i++) {
                    obstacleArray[i] = findObstacleByGridCoord(X[i], Y[2]);
                }
            } else if ((command.equals("f") && direction.equals("E")) ||
                    (command.equals("b") && direction.equals("W")) ||
                    (command.equals("sl") && direction.equals("S")) ||
                    (command.equals("sr") && direction.equals("N"))) {
                for (int i = 0; i < obstacleArray.length; i++) {
                    obstacleArray[i] = findObstacleByGridCoord(X[2], Y[i]);
                }
            } else if ((command.equals("f") && direction.equals("S")) ||
                    (command.equals("b") && direction.equals("N")) ||
                    (command.equals("sl") && direction.equals("W")) ||
                    (command.equals("sr") && direction.equals("E"))) {
                for (int i = 0; i < obstacleArray.length; i++) {
                    obstacleArray[i] = findObstacleByGridCoord(X[i], Y[0]);
                }
            } else if ((command.equals("f") && direction.equals("W")) ||
                    (command.equals("b") && direction.equals("E")) ||
                    (command.equals("sl") && direction.equals("N")) ||
                    (command.equals("sr") && direction.equals("S"))) {
                for (int i = 0; i < obstacleArray.length; i++) {
                    obstacleArray[i] = findObstacleByGridCoord(X[0], Y[i]);
                }
            }

            for (Obstacle obstacle : obstacleArray) {
                Log.d(TAG, "checkMovable: " + obstacle);
                if (obstacle != null)
                    return false;
            }
            return true;

        }
        return false;
    }

    private boolean checkPlaceable(int X, int Y) {
        Log.d(TAG, "checkPlaceable: startX: " + X + " startY:" + Y);

        float[] fX = robot.getXArray().clone();
        float[] fY = robot.getYArray().clone();

        int[] x = new int[fX.length];
        int[] y = new int[fY.length];

        for(int i=0; i< fX.length;i++){
            x[i] = (int) fX[i];
        }
        for(int i=0; i< fY.length;i++){
            y[i] = (int) fY[i];
        }

        for(int i = 0; i < x.length; i++){
            for(int j = 0; j<y.length; j++){
                if(x[i] == X && y[j] == Y)
                    return false;
            }
        }
        return true;
    }

    private void createCell() {
        cells = new Cell[numColumns + 1][numRows + 1];
        cellSize = getWidth() / (numColumns + 1);

        for (int x = 0; x <= numColumns; x++)
            for (int y = 0; y <= numRows; y++)
                cells[x][y] = new Cell(x * cellSize + (cellSize / 30), y * cellSize + (cellSize / 30), (x + 1) * cellSize, (y + 1) * cellSize);
    }

    private void drawIndividualCell(@NonNull Canvas canvas) {
        for (int x = 1; x <= numColumns; x++){
            for (int y = 0; y < numRows; y++) {
                if (cells[x][y].getExplored()) {
                    canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, exploredColor);
                } else {
                    canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, unexploredColor);

                }
            }
        }
    }

    private void drawGrid(@NonNull Canvas canvas) {
        for (int y = 0; y <= numRows; y++)
            canvas.drawLine(cells[1][y].startX, cells[1][y].startY - (cellSize / 30), cells[numRows][y].endX, cells[numRows][y].startY - (cellSize / 30), blackPaint);
        for (int x = 0; x <= numColumns; x++)
            canvas.drawLine(cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][0].startY - (cellSize / 30), cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][numRows - 1].endY + (cellSize / 30), blackPaint);
    }

    private void drawGridNumber(@NonNull Canvas canvas) {
        for (int x = 1; x <= numColumns; x++) {
            if (x > 9)
                canvas.drawText(Integer.toString(x - 1), cells[x][numRows].startX + (cellSize / 5), cells[x][numRows].startY + (cellSize / 3), blackPaint);
            else
                canvas.drawText(Integer.toString(x - 1), cells[x][numRows].startX + (cellSize / 3), cells[x][numRows].startY + (cellSize / 3), blackPaint);
        }
        for (int y = 0; y < numRows; y++) {
            if ((numRows - y) > 9)
                canvas.drawText(Integer.toString((numRows - 1) - y), cells[0][y].startX + (cellSize / 2), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
            else
                canvas.drawText(Integer.toString((numRows - 1) - y), cells[0][y].startX + (cellSize / 1.5f), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
        }
    }

    /**
     * "Alphabet_A", "Alphabet_B", "Alphabet_C",
     * "Alphabet_D", "Alphabet_E", "Alphabet_F",
     * "Alphabet_G", "Alphabet_H", "Alphabet_S",
     * "Alphabet_T", "Alphabet_U", "Alphabet_V",
     * "Alphabet_W", "Alphabet_X", "Alphabet_Y",
     * "Alphabet_Z", "bullseye", "down_arrow",
     * "eight", "five", "four", "left_arrow",
     * "nine", "one", "right_arrow", "seven",
     * "six", "stop", "three", "two", "up_arrow"
     **/
    private void drawObstacle(@NonNull Canvas canvas) {
        for (Obstacle obstacle : obstacles) {
            float startX = obstacle.X * cellSize + (cellSize / 30);
            float startY = obstacle.Y * cellSize + (cellSize / 30);
            float endX = (obstacle.X + 1) * cellSize;
            float endY = (obstacle.Y + 1) * cellSize;

            if (obstacle.targetID == null || obstacle.targetID.equals("bullseye")) {
                canvas.drawRect(startX, startY, endX, endY, obstacleColor);
                canvas.drawText(String.valueOf(obstacle.id), (obstacle.X + (float) 0.5) * cellSize, (obstacle.Y + (float) 0.65) * cellSize, whitePaint);
            } else if (ValidTargetStrings.contains(obstacle.targetID)) {
                if(showTargetID){
                    try {
                        canvas.drawRect(startX, startY, endX, endY, obstacleColor);
                        canvas.drawText(String.valueOf(targetIdJSON.getInt(obstacle.targetID)), (obstacle.X + (float) 0.5) * cellSize, (obstacle.Y + (float) 0.65) * cellSize, targetScannedColor);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    RectF rect = new RectF(startX, startY, endX, endY);
                    int resID = getResources().getIdentifier(obstacle.targetID, "drawable", cachedContext.getPackageName());
                    Bitmap obstacleBitmap = BitmapFactory.decodeResource(getResources(), resID);
                    canvas.drawBitmap(obstacleBitmap, null, rect, null);
                }
            }

            if (obstacle.direction.equals("N")) {
                canvas.drawLine(startX, startY + 3, endX, startY + 3, yellowPaint);
            } else if (obstacle.direction.equals("E")) {
                canvas.drawLine(endX - 3, startY, endX - 3, endY, yellowPaint);
            } else if (obstacle.direction.equals("S")) {
                canvas.drawLine(startX, endY - 3, endX, endY - 3, yellowPaint);
            } else if (obstacle.direction.equals("W")) {
                canvas.drawLine(startX + 3, startY, startX + 3, endY, yellowPaint);
            }
        }
    }

    private void drawRobot(@NonNull Canvas canvas) {
        RectF rect;

        float col = robot.getX() + 1;
        float row = convertRow(robot.getY());
        float[] robotSize = robot.getRobotSize().clone();
        String direction = robot.getDirection();

        rect = new RectF(col * cellSize, (row - robotSize[0]) * cellSize, (col + robotSize[1]) * cellSize, row * cellSize);

        Bitmap robotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);

        if (direction.equals("N")) {
            canvas.drawBitmap(robotBitmap, null, rect, null);
        } else if (direction.equals("E")) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

            canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
        } else if (direction.equals("S")) {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

            canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
        } else if (direction.equals("W")) {
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            Bitmap rotatedRobotBitmap = Bitmap.createBitmap(robotBitmap, 0, 0, robotBitmap.getWidth(), robotBitmap.getHeight(), matrix, true);

            canvas.drawBitmap(rotatedRobotBitmap, null, rect, null);
        }
    }

    private float convertRow(float row) {
        return (numRows - row);
    }

    private int convertRow(int row) {
        return (numRows - row);
    }

    private boolean containACommand(String receivedMessage, List<String> Commands){
        for(String command : Commands){
            if(receivedMessage.substring(0,1).equals(command)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
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

        drawRobot(canvas);
    }

    /**
     * Actual Coords Col: 1 - 20 Row: 0 - 19
     */
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        boolean handled = false;

        Obstacle touchedObstacle;
        int pointerId;
        int column;
        int row;
        int actionIndex = event.getActionIndex();

        gestureDetector.onTouchEvent(event);

        // get touch event coordinates and make Obstacle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.w(TAG, "ACTION_DOWN");
                // it"s the first pointer, so clear all existing pointers data
                clearObstaclePointer();

                column = (int) (event.getX() / cellSize);
                row = (int) (event.getY() / cellSize);

                // check if we"ve touched inside some Obstacle
                if (column > 0 && column <= numColumns && row >= 0 && row < numRows && checkPlaceable((column - 1), (convertRow(row) - 1))) {
                    touchedObstacle = obtainTouchedObstacle(column, row);
                    touchedObstacle.X = column;
                    touchedObstacle.Y = row;
                    touchedObstacle.xOnGrid = column - 1;
                    touchedObstacle.yOnGrid = convertRow(row) - 1;
                    obstaclePointer.put(event.getPointerId(0), touchedObstacle);
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "ACTION_POINTER_DOWN");
                // It secondary pointers, so obtain their ids and check Obstacles
                pointerId = event.getPointerId(actionIndex);

                column = (int) (event.getX(actionIndex) / cellSize);
                row = (int) (event.getY(actionIndex) / cellSize);

                // check if we"ve touched inside some Obstacle
                try{
                    touchedObstacle = obtainTouchedObstacle(column, row);
                    obstaclePointer.put(pointerId, touchedObstacle);
                    touchedObstacle.X = column;
                    touchedObstacle.Y = row;
                    touchedObstacle.xOnGrid = column - 1;
                    touchedObstacle.yOnGrid = convertRow(row) - 1;
                } catch(Exception e){
                    Log.e(TAG, "onTouchEvent: ", e);
                }


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

                    Log.d(TAG, "ACTION_MOVE: Column: " + (column - (int) 1) + " Row: " + (convertRow(row) - (int) 1));

                    touchedObstacle = obstaclePointer.get(pointerId);

                    if (null != touchedObstacle) {
                        touchedObstacle.X = column;
                        touchedObstacle.Y = row;
                        touchedObstacle.xOnGrid = column - 1;
                        touchedObstacle.yOnGrid = convertRow(row) - 1;
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

                Log.d(TAG, "ACTION_UP: Column: " + (column - (int) 1) + " Row: " + (convertRow(row) - (int) 1));

                touchedObstacle = getTouchedObstacle(column, row);

                if (column > 0 && column <= numColumns && row >= 0 && row < numRows) {
                    if (touchedObstacle != null) {
                        Obstacle overlappingObstacle = checkOverlappingObstacle(column, row, touchedObstacle.id);

                        while (overlappingObstacle != null || !checkPlaceable((column - 1), (convertRow(row) - 1))) {
                            //Log.w(TAG, "ACTION_UP: Overlapped ID: " + overlappingObstacle.id);
                            column++;
                            overlappingObstacle = checkOverlappingObstacle(column, row, touchedObstacle.id);
                        }

                        if (column > 0 && column <= numColumns && row >= 0 && row < numRows && checkPlaceable((column - 1), (convertRow(row) - 1))) {
                            touchedObstacle.Y = row;
                            touchedObstacle.X = column;
                            touchedObstacle.xOnGrid = column - 1;
                            touchedObstacle.yOnGrid = convertRow(row) - 1;

//                            bluetooth.write("{X: " + touchedObstacle.xOnGrid + ", Y:" + touchedObstacle.yOnGrid + ", id:" + touchedObstacle.id + " }");
                        } else {
                            int deletedCount = touchedObstacle.id;
                            fixCount(deletedCount);
                            counter--;
                            obstacles.remove(touchedObstacle);
//                            bluetooth.write("DELETE {X: " + touchedObstacle.xOnGrid + ", Y:" + touchedObstacle.yOnGrid + ", id:" + touchedObstacle.id + " }");
                        }
                    }
                } else {
                    if (touchedObstacle != null) {
                        int deletedCount = touchedObstacle.id;
                        fixCount(deletedCount);
                        counter--;
                        obstacles.remove(touchedObstacle);
//                        bluetooth.write("DELETE {X: " + touchedObstacle.xOnGrid + ", Y:" + touchedObstacle.yOnGrid + ", id:" + touchedObstacle.id + " }");
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
        public void onLongPress(@NonNull MotionEvent event) {
            int column = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            Log.d(TAG, "onLongPress: Column: " + (column - (int) 1) + " Row: " + (convertRow(row) - (int) 1));
            final Obstacle[] obstacle = {getTouchedObstacle(column, row)};
            if (obstacle[0] != null) {
                if (column > 0 && column <= numColumns && row >= 0 && row < numRows) {
                    // inflate the layout of the popup window
                    clearObstaclePointer();

                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.popup_direction, null);
                    PixelGridView pixelGrid = findViewById(R.id.pixelGrid);

                    // create the popup window
//                    int x = 0;
//                    int y = 0;
//                    int length = 0;
//
//                    int screenSize = getResources().getConfiguration().screenLayout &
//                            Configuration.SCREENLAYOUT_SIZE_MASK;
//
//                    switch(screenSize) {
//                        case Configuration.SCREENLAYOUT_SIZE_LARGE:
//                            length = (int) (cellSize + (cellSize / 30)) * 5;
//                            x = (int) (event.getX() - cellSize * 2);
//                            y = (int) (event.getY() + cellSize * 1.5);
//                            Log.d(TAG, "onLongPress: X: " + x + " Y: " + y);
//                            break;
//                        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
//                            length = (int) (cellSize + (cellSize / 30)) * 4;
//                            x = (int) (event.getX() - cellSize * 2);
//                            y = (int) (event.getY() + cellSize * 2.5);
//                            Log.d(TAG, "onLongPress: X: " + x + " Y: " + y);
//                            break;
//                        default:
//                            break;
//                    }

                    int length = (int) (cellSize + (cellSize / 30)) * 4;

                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, length, length, focusable);

                    // show the popup window
                    // which view you pass in doesn"t matter, it is only used for the window token
                    int x = (int) (event.getX() - cellSize * 2);
                    int y = (int) (event.getY() + cellSize * 2.5);
                    Log.d(TAG, "onLongPress: X: " + x + " Y: " + y);

                    popupWindow.showAtLocation(pixelGrid, Gravity.NO_GRAVITY, x, y);

                    ObstacleView obstacleGrid = popupView.findViewById(R.id.obstacleGrid);
                    obstacleGrid.setObstacle(obstacle[0]);
                    obstacleGrid.setShowTargetID(showTargetID);
                    obstacleGrid.setPopupWindow(popupWindow);


                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            obstacle[0] = obstacleGrid.getObstacle();
                            Log.d(TAG, "onDismiss: direction: " + obstacle[0].direction);
//                            bluetooth.write("DIRECTION {X: " + obstacle[0].xOnGrid + ", Y:" + obstacle[0].yOnGrid + ", id:" + obstacle[0].id + " direction: " + obstacle[0].direction + " }");
                            invalidate();
                        }
                    });
                }
            }
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            // Get extra data included in the Intent
            if (intent.getAction().equals(EVENT_SEND_MOVEMENT)) {
                String message = intent.getStringExtra("key");

                robotCommands.add(message);

            } else if (intent.getAction().equals(EVENT_TARGET_SCANNED)) {
//                String message = intent.getStringExtra("key");
//                    try {
//                        JSONObject jsonObj = new JSONObject(message);
//                        Log.d(TAG, "onReceive: " + jsonObj);
//
//                        int col = jsonObj.getInt("col");
//                        int row = convertRow(jsonObj.getInt("row"));
//                        String id = jsonObj.getString("id");
//                        String direction = jsonObj.getString("direction");
//
//                        Obstacle target = obtainTouchedObstacle(col, row);
//                        if (target != null) {
//                            target.targetID = id;
//                            target.direction = direction;
//
//                            Log.d(TAG, "onReceive: EVENT_TARGET_SCANNED: direction: " + target.direction);
//
//                            invalidate();
//                        } else {
//                            bluetooth.write("Wrong coordinate");
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                Log.d(TAG, "onReceive: EVENT_TARGET_SCANNED: " + intent.getStringExtra("key"));

                try {
                    String[] message = intent.getStringExtra("key").split(",");

                    int obstacleNo = Integer.parseInt(message[1].replace(" ", ""));
                    String targetID = message[2].replace(" ", "");

                    Obstacle target = findObstacleByID(obstacleNo);

                    if (target != null) {
                        target.targetID = targetID;
                    }

                    invalidate();

                } catch (Exception e) {
                    Log.e(TAG, "onReceive: EVENT_TARGET_SCANNED: ", e);
                }
            } else if (intent.getAction().equals(EVENT_ROBOT_MOVES)) {
                Log.d(TAG, "onReceive: EVENT_ROBOT_MOVES: " + intent.getStringExtra("key"));
                try {
                    String[] message = intent.getStringExtra("key").split(",");

                    int col = Integer.parseInt(message[1].replace(" ", ""));
                    int row = Integer.parseInt(message[2].replace(" ", ""));
                    String direction = message[3].replace(" ", "");

                    Log.d(TAG, "onReceive: EVENT_ROBOT_MOVES: Column:" + col + " Row:" + row + " Direction:" + direction);

                    setCurCoord(col, row, direction);
                } catch (Exception e) {
                    Log.e(TAG, "onReceive: EVENT_ROBOT_MOVES: ", e);
                }
            }
        }
    };

    private class robotCommandThread extends Thread {

        public robotCommandThread() {
        }

        public void run() {
            while (true) {
                if (!robotCommands.isEmpty()) {
                    /**
                     * X[0]: startX
                     * X[1]: middleX
                     * X[2]: endX
                     *
                     * Y[0]: startY
                     * Y[1]: middleY
                     * Y[2]: endY
                     */

                    String message = robotCommands.remove();

                    int distance = 1;
                    float fDistance = 0;
                    float[] X = robot.getXArray().clone();
                    float[] Y = robot.getYArray().clone();

                    String direction = robot.getDirection();

                    if (message.length() > 1) {
                        distance = Integer.parseInt(message.substring(1, 4)) / 10;
                        fDistance = Float.parseFloat(message.substring(1, 4)) / 10;
                        Log.d(TAG, "onReceive: distance: " + distance + " fdistance: " + fDistance);
                        fDistance = fDistance - distance;
                        if (fDistance != 0) {
                            distance = distance + 1;
                        }
                        message = message.substring(0, 1);
                    }

                    if (containACommand(message, ValidRobotMovementCommands)) {
                        if (distance != 0){
                            for (int count = 0; count < distance; count++) {
                                if ((message.equals("f") && direction.equals("N")) ||
                                        (message.equals("b") && direction.equals("S")) ||
                                        (message.equals("sl") && direction.equals("E")) ||
                                        (message.equals("sr") && direction.equals("W"))) {
                                    for (int i = 0; i < Y.length; i++) {
                                        if (count == distance - 1 && fDistance != 0) {
                                            Y[i] = Y[i] + fDistance;
                                        } else {
                                            Y[i] = Y[i] + 1;
                                        }
                                    }
                                    if (checkMovable(X, Y, direction, message)) {
                                        setCurCoord(X[0], Y[0], direction);
                                    } else {
                                        break;
                                    }
                                } else if ((message.equals("f") && direction.equals("E")) ||
                                        (message.equals("b") && direction.equals("W")) ||
                                        (message.equals("sl") && direction.equals("S")) ||
                                        (message.equals("sr") && direction.equals("N"))) {
                                    for (int i = 0; i < X.length; i++) {
                                        if (count == distance - 1 && fDistance != 0) {
                                            X[i] = X[i] + fDistance;
                                        } else {
                                            X[i] = X[i] + 1;
                                        }
                                    }
                                    if (checkMovable(X, Y, direction, message)) {
                                        setCurCoord(X[0], Y[0], direction);
                                    } else {
                                        break;
                                    }
                                } else if ((message.equals("f") && direction.equals("S")) ||
                                        (message.equals("b") && direction.equals("N")) ||
                                        (message.equals("sl") && direction.equals("W")) ||
                                        (message.equals("sr") && direction.equals("E"))) {
                                    for (int i = 0; i < Y.length; i++) {
                                        if (count == distance - 1 && fDistance != 0) {
                                            Y[i] = Y[i] - fDistance;
                                        } else {
                                            Y[i] = Y[i] - 1;
                                        }
                                    }
                                    if (checkMovable(X, Y, direction, message)) {
                                        setCurCoord(X[0], Y[0], direction);
                                    } else {
                                        break;
                                    }
                                } else if ((message.equals("f") && direction.equals("W")) ||
                                        (message.equals("b") && direction.equals("E")) ||
                                        (message.equals("sl") && direction.equals("N")) ||
                                        (message.equals("sr") && direction.equals("S"))) {
                                    for (int i = 0; i < X.length; i++) {
                                        if (count == distance - 1 && fDistance != 0) {
                                            X[i] = X[i] - fDistance;
                                        } else {
                                            X[i] = X[i] - 1;
                                        }
                                    }
                                    if (checkMovable(X, Y, direction, message)) {
                                        setCurCoord(X[0], Y[0], direction);
                                    } else {
                                        break;
                                    }
                                }

                                Log.d(TAG, "run: Count: " + count + " Distance: " + distance);

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (containACommand(message, ValidRobotRotationCommands)) {
                        for (int count = 0; count < 6; count++) {

                            if (count == 3) {
                                if (message.equals("l")) {
                                    if (direction.equals("N")) {
                                        direction = "W";
                                    } else if (direction.equals("E")) {
                                        direction = "N";
                                    } else if (direction.equals("S")) {
                                        direction = "E";
                                    } else if (direction.equals("W")) {
                                        direction = "S";
                                    }
                                } else if (message.equals("r")) {
                                    if (direction.equals("N")) {
                                        direction = "E";
                                    } else if (direction.equals("E")) {
                                        direction = "S";
                                    } else if (direction.equals("S")) {
                                        direction = "W";
                                    } else if (direction.equals("W")) {
                                        direction = "N";
                                    }
                                }
                            }

                            if (direction.equals("N")) {
                                for (int i = 0; i < Y.length; i++) {
                                    if (count == 2) {
                                        Y[i] = Y[i] + 0.7f;
                                    } else {
                                        Y[i] = Y[i] + 1;
                                    }
                                }
                                if (checkMovable(X, Y, direction, "f")) {
                                    setCurCoord(X[0], Y[0], direction);
                                } else {
                                    break;
                                }
                            } else if (direction.equals("E")) {
                                for (int i = 0; i < X.length; i++) {
                                    if (count == 2) {
                                        X[i] = X[i] + 0.7f;
                                    } else {
                                        X[i] = X[i] + 1;
                                    }
                                }
                                if (checkMovable(X, Y, direction, "f")) {
                                    setCurCoord(X[0], Y[0], direction);
                                } else {
                                    break;
                                }
                            } else if (direction.equals("S")) {
                                for (int i = 0; i < Y.length; i++) {
                                    if (count == 2) {
                                        Y[i] = Y[i] - 0.7f;
                                    } else {
                                        Y[i] = Y[i] - 1;
                                    }
                                }
                                if (checkMovable(X, Y, direction, "f")) {
                                    setCurCoord(X[0], Y[0], direction);
                                } else {
                                    break;
                                }
                            } else if (direction.equals("W")) {
                                for (int i = 0; i < X.length; i++) {
                                    if (count == 2) {
                                        X[i] = X[i] - 0.7f;
                                    } else {
                                        X[i] = X[i] - 1;
                                    }
                                }
                                if (checkMovable(X, Y, direction, "f")) {
                                    setCurCoord(X[0], Y[0], direction);
                                } else {
                                    break;
                                }
                            }

                            Log.d(TAG, "run: Count: " + count + " Distance: " + distance);

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (message.equals("s")) {

                    }
                }
            }
        }
    }



    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //add your code.
        getContext().unregisterReceiver(mMessageReceiver);
    }
}