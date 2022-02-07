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
import android.view.MotionEvent;
import android.view.View;

public class PixelGridView extends View {
    private static final String TAG = "PixelGridView";
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private final Paint blackPaint = new Paint();
    private boolean[][] cellChecked;
    private final Paint whitePaint = new Paint();
    private int counter = 1;
    private int[][] cellCounter;

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixelGridView,
                0, 0);
        this.numColumns = typedArray.getInt(R.styleable.PixelGridView_columns, 0);
        this.numRows = typedArray.getInt(R.styleable.PixelGridView_rows, 0);

        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(20);
        whitePaint.setTextAlign(Paint.Align.CENTER);

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
        canvas.drawBitmap(scaledBitmap, 0.5f * cellWidth, 18 * cellHeight, null);

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j]) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            blackPaint);
                    Log.d(TAG, "onDraw: Column: " + i + " Row: " + j);
                    canvas.drawText(String.valueOf(cellCounter[i][j]), (i + (float)0.5) * cellWidth, (j + (float)0.65) * cellHeight,
                            whitePaint);
                }
            }
        }

        for (int i = 1; i < numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i < numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);
            Log.d(TAG, "onTouchEvent: Column: " + column + " Row: " + row);
            cellChecked[column][row] = !cellChecked[column][row];
            fixCount(column, row);
            invalidate();
        }

        return true;
    }
}