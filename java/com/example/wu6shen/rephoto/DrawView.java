package com.example.wu6shen.rephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.core.Point;

import java.util.List;

/**
 * Created by wu6shen on 17-4-29.
 */

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    private Point centerPoint = new Point(925, 190);
    private float lenCoordinate = 125;
    private float posRotate = 80;
    private float sizeRotate = 20;
    private float angleD = (float)Math.PI / 6;

    float angles[] = new float[6];
    Point points[] = new Point[6];

    public DrawView(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);

        /**设置为透明*/
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        Point xL = new Point(centerPoint.x - lenCoordinate, centerPoint.y); float angleXL = 0;
        Point xR = new Point(centerPoint.x + lenCoordinate, centerPoint.y); float angleXR = (float)Math.PI;
        Point yF = new Point(centerPoint.x + lenCoordinate * Math.cos(Math.PI / 6), centerPoint.y - lenCoordinate * Math.sin(Math.PI / 6)); float angleYF = (float)Math.PI * 7 / 6;
        Point yB = new Point(centerPoint.x - lenCoordinate * Math.cos(Math.PI / 6), centerPoint.y + lenCoordinate * Math.sin(Math.PI / 6)); float angleYB = (float)Math.PI / 6;
        Point zU = new Point(centerPoint.x, centerPoint.y - lenCoordinate); float angleZU = (float)Math.PI * 3 / 2;
        Point zD = new Point(centerPoint.x, centerPoint.y + lenCoordinate); float angleZD = (float)Math.PI / 2;
        points[0] = xL; points[1] = xR;
        angles[0] = angleXL; angles[1] = angleXR;
        points[2] = yF; points[3] = yB;
        angles[2] = angleYF; angles[3] = angleYB;
        points[4] = zU; points[5] = zD;
        angles[4] = angleZU; angles[5] = angleZD;


    }

    public void getCanvas() {
        mCanvas = mHolder.lockCanvas();
    }

    public void update() {
        if (mCanvas != null) {
            mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**清屏幕*/
    public void clearDraw() {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
        }
    }

    /**画Bitmap*/
    public void drawBitmap(Bitmap bitmap) {
        if (mCanvas != null) {
            mCanvas.drawBitmap(bitmap, 0, 0, null);
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#A0000000"));
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);//空心矩形框
            mCanvas.drawRect(0, 0, 0 + bitmap.getWidth(), 0 + bitmap.getHeight(), paint);
        }
    }

    public void drawLines(List<Pair<Point, Point>> lines, int color) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(10);
            for (Pair<Point, Point> line : lines) {
                mCanvas.drawLine((float)line.first.x, (float)line.first.y,
                                 (float)line.second.x, (float)line.second.y, paint);
            }
        }
    }

    public void drawLocateInfo(LocateInfo locateInfo, int color) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(10);
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                mCanvas.drawLine((float)locateInfo.frame[i].x, (float)locateInfo.frame[i].y,
                        (float)locateInfo.frame[j].x, (float)locateInfo.frame[j].y, paint);
            }
            mCanvas.drawLine((float)locateInfo.cross[0].x, (float)locateInfo.cross[0].y,
                    (float)locateInfo.cross[1].x, (float)locateInfo.cross[1].y, paint);
            mCanvas.drawLine((float)locateInfo.cross[2].x, (float)locateInfo.cross[2].y,
                    (float)locateInfo.cross[3].x, (float)locateInfo.cross[3].y, paint);

            //drawInfoTest();
        }
    }

    public void drawInfoTest(String[] info) {
        if (mCanvas != null) {

            /**test
            PointF center = new PointF(900, 200);
            drawPhone(center, 50);

            if (info[0] == 1) {
                drawRotateYSArrow(new PointF(center.x, center.y - 150), 50);
            } else if (info[0] == -1) {
                drawRotateYNArrow(new PointF(center.x, center.y - 150), 50);
            }

            if (info[1] == 1) {
                drawRotateZNArrow(new PointF(center.x - 85, center.y - 150), 50);
            } else if (info[1] == -1) {
                drawRotateZSArrow(new PointF(center.x - 85, center.y - 150), 50);
            }

            if (info[2] == 1) {

                drawRotateXSArrow(new PointF(center.x - 85, center.y), 50);
            } else if (info[2] == -1) {

                drawRotateXNArrow(new PointF(center.x - 85, center.y), 50);
            }

            if (info[3] == 1) {
                drawRightArrow(new PointF(center.x - 20, center.y + 180), 0.8f);
            } else if (info[3] == -1) {
                drawLeftArrow(new PointF(center.x - 20, center.y + 180), 0.8f);
            }

            if (info[4] == 1) {
                drawUpArrow(new PointF(center.x + 120, center.y), 0.8f);
            } else if (info[4] == -1) {
                drawDownArrow(new PointF(center.x + 120, center.y), 0.8f);
            }

            if (info[5] == 1) {
                drawBehindArrow(new PointF(center.x, center.y + 20), 0.5f);
            } else if (info[5] == -1) {
                drawFrontArrow(new PointF(center.x, center.y - 20), 0.5f);
            }
             */
        }
    }

    public void drawSphere(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paintDraw = new Paint();
            paintDraw.setColor(Color.parseColor("#A0FFFFFF"));
            paintDraw.setStyle(Paint.Style.STROKE);
            paintDraw.setStrokeWidth(5.0f);
            paintDraw.setAntiAlias(true);
            float a = 200 * scale, b = 80 * scale;
            float C = (float)(Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (3 * b + a))));
            float[] list = new float[26];
            for (int i = 1; i < list.length; i++) {
                list[i] = C / 50;
            }
            list[0] = C / 2;
            DashPathEffect effects = new DashPathEffect(list, 0);
            paintDraw.setPathEffect(effects);
            RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
            mCanvas.drawOval(rect, paintDraw);
            rect = new RectF(center.x - b, center.y - a, center.x + b, center.y + a);
            effects = new DashPathEffect(list, -C / 4);
            paintDraw.setPathEffect(effects);
            mCanvas.drawOval(rect, paintDraw);
            rect = new RectF(center.x - a, center.y - a, center.x + a, center.y + a);
            effects = new DashPathEffect(new float[]{1, 0}, 0);
            paintDraw.setPathEffect(effects);
            mCanvas.drawOval(rect, paintDraw);
        }
    }

    public void drawText(String info, int color) {
        if (mCanvas != null) {
            Paint paint = new Paint();
        }
    }

    public void drawPhone(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paintDraw = new Paint();
            Paint paintErase = new Paint();
            paintDraw.setStyle(Paint.Style.FILL);
            paintDraw.setColor(Color.parseColor("#A0FFFFFF"));

            /**test*/
            paintDraw.setColor(Color.parseColor("#70FFFFFF"));
            paintDraw.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < 10; i++) {
                RectF outRectF = new RectF(center.x - 85 + i, center.y - 150 - i, center.x + 85 + i, center.y + 150 - i);
                mCanvas.drawRoundRect(outRectF, 15, 15, paintDraw);
            }

            paintDraw.setStyle(Paint.Style.FILL);
            paintDraw.setColor(Color.parseColor("#C0FFFFFF"));
            RectF outRectF = new RectF(center.x - 85, center.y - 150, center.x + 85, center.y + 150);
            mCanvas.drawRoundRect(outRectF, 15, 15, paintDraw);

            RectF inRectF = new RectF(center.x - 75, center.y - 135, center.x + 75, center.y + 105);

            paintErase.setAlpha(0);
            paintErase.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawRoundRect(inRectF, 7.5f, 7.5f, paintErase);

            mCanvas.drawCircle(center.x, center.y + 255.0f/2, 12,paintErase);

        }
    }

    public void drawFrontArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path mPath = new Path();

            /**1*/
            mPath.moveTo(center.x - 20 * scale, center.y - 40 * scale);
            /**2*/
            mPath.lineTo(center.x + 90 * scale, center.y - 40 * scale);
            /**3*/
            mPath.lineTo(center.x - 10 * scale, center.y + 40 * scale);
            /**4*/
            mPath.lineTo(center.x + 30 * scale, center.y + 40 * scale);
            /**5*/
            mPath.lineTo(center.x - 70 * scale, center.y + 80 * scale);
            /**6*/
            mPath.lineTo(center.x - 80 * scale, center.y + 40 * scale);
            /**7*/
            mPath.lineTo(center.x - 60 * scale, center.y + 40 * scale);

            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawBehindArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            PathEffect effect = new DashPathEffect(new float[] {3, 3, 3, 3}, 1f);
            paint.setPathEffect(effect);
            Path mPath = new Path();
            /**1*/
            mPath.moveTo(center.x + 10 * scale, center.y + 50 * scale);
            /**2*/
            mPath.lineTo(center.x - 80 * scale, center.y + 50 * scale);
            /**3*/
            mPath.lineTo(center.x + 10 * scale, center.y - 50 * scale);
            /**4*/
            mPath.lineTo(center.x - 10 * scale, center.y - 50 * scale);
            /**5*/
            mPath.lineTo(center.x + 60 * scale, center.y - 90 * scale);
            /**6*/
            mPath.lineTo(center.x + 70 * scale, center.y - 50 * scale);
            /**7*/
            mPath.lineTo(center.x + 50 * scale, center.y - 50 * scale);

            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }

    }

    public void drawDownArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path mPath = new Path();
            mPath.moveTo(center.x + 15 * scale, center.y - 110 * scale);
            mPath.lineTo(center.x - 15 * scale, center.y - 90 * scale);
            mPath.lineTo(center.x - 15 * scale, center.y + 90 * scale);
            mPath.lineTo(center.x - 30 * scale, center.y + 97.5f * scale);
            mPath.lineTo(center.x, center.y + 120 * scale);
            mPath.lineTo(center.x + 35 * scale, center.y + 65 * scale);
            mPath.lineTo(center.x + 15 * scale, center.y + 75 * scale);
            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawUpArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path mPath = new Path();
            mPath.moveTo(center.x - 15 * scale, center.y + 110 * scale);
            mPath.lineTo(center.x + 15 * scale, center.y + 90 * scale);
            mPath.lineTo(center.x + 15 * scale, center.y - 90 * scale);
            mPath.lineTo(center.x + 35 * scale, center.y - 110 * scale);
            mPath.lineTo(center.x, center.y - 120 * scale);
            mPath.lineTo(center.x - 35 * scale, center.y - 50 * scale);
            mPath.lineTo(center.x - 15 * scale, center.y - 70 * scale);
            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawLeftArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path mPath = new Path();
            /**1*/
            mPath.moveTo(center.x - 70 * scale, center.y - 10 * scale);
            /**2*/
            mPath.lineTo(center.x - 90 * scale, center.y + 10 * scale);
            /**3*/
            mPath.lineTo(center.x + 70 * scale, center.y + 10 * scale);
            /**4*/
            mPath.lineTo(center.x + 50 * scale, center.y + 30 * scale);
            /**5*/
            mPath.lineTo(center.x + 120 * scale, center.y);
            /**6*/
            mPath.lineTo(center.x + 100 * scale, center.y - 20 * scale);
            /**7*/
            mPath.lineTo(center.x + 90 * scale, center.y - 10 * scale);

            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawRightArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path mPath = new Path();
            /**1*/
            mPath.moveTo(center.x + 70 * scale, center.y - 10 * scale);
            /**2*/
            mPath.lineTo(center.x + 90 * scale, center.y + 10 * scale);
            /**3*/
            mPath.lineTo(center.x - 70 * scale, center.y + 10 * scale);
            /**4*/
            mPath.lineTo(center.x - 50 * scale, center.y + 30 * scale);
            /**5*/
            mPath.lineTo(center.x - 120 * scale, center.y);
            /**6*/
            mPath.lineTo(center.x - 100 * scale, center.y - 20 * scale);
            /**7*/
            mPath.lineTo(center.x - 90 * scale, center.y - 10 * scale);

            mPath.close();
            mCanvas.drawPath(mPath, paint);
        }

    }

    public void drawRotateZNArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path linePath = new Path();
            /**line*/
            linePath.moveTo(center.x + 15, center.y - 15);
            linePath.quadTo(center.x - 10, center.y - 10, center.x - 15, center.y + 15);
            mCanvas.drawPath(linePath, paint);

            Path mPath = new Path();
            /**Arrow*/
            paint.setStrokeWidth(5);
            mPath.moveTo(center.x - 18, center.y + 5);
            mPath.lineTo(center.x - 16, center.y + 16);
            mPath.lineTo(center.x - 7, center.y + 10);
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawRotateZSArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path linePath = new Path();
            /**line*/
            linePath.moveTo(center.x - 15, center.y + 15);
            linePath.quadTo(center.x - 10, center.y - 10, center.x + 15, center.y - 15);
            mCanvas.drawPath(linePath, paint);

            Path mPath = new Path();
            /**Arrow*/
            paint.setStrokeWidth(5);
            mPath.moveTo(center.x + 8, center.y - 22);
            mPath.lineTo(center.x + 16, center.y - 16);
            mPath.lineTo(center.x + 12, center.y - 6);
            mCanvas.drawPath(mPath, paint);
        }
    }

    public void drawRotateXNArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            Path linePath = new Path();
            /**line*/
            linePath.moveTo(center.x, center.y);
            linePath.quadTo(center.x + 7, center.y + 27, center.x + 33, center.y + 20);
            mCanvas.drawPath(linePath, paint);

            /**Arrow*/
            Path mPath = new Path();
            mPath.moveTo(center.x + 24, center.y + 16);
            mPath.lineTo(center.x + 33, center.y + 20);
            mPath.lineTo(center.x + 26, center.y + 26);
            mCanvas.drawPath(mPath, paint);

        }
    }

    public void drawRotateXSArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));

            Path linePath = new Path();
            /**line*/
            linePath.moveTo(center.x, center.y);
            linePath.quadTo(center.x + 7, center.y - 20, center.x + 40, center.y - 20);
            PathEffect effect = new DashPathEffect(new float[] {3, 3, 3, 3}, 1f);
            Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tPaint.setStyle(Paint.Style.STROKE);
            tPaint.setStrokeWidth(3);
            tPaint.setColor(Color.parseColor("#A0FFFFFF"));
            tPaint.setPathEffect(effect);
            mCanvas.drawPath(linePath, tPaint);

            /**Arrow*/
            Path mPath = new Path();
            mPath.moveTo(center.x + 31, center.y - 16);
            mPath.lineTo(center.x + 40, center.y - 20);
            mPath.lineTo(center.x + 33, center.y - 26);
            mCanvas.drawPath(mPath, paint);
        }

    }

    public void drawRotateYNArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));

            Path linePath = new Path();
            /**line*/
            linePath.moveTo(center.x, center.y);
            linePath.quadTo(center.x - 20, center.y + 20, center.x - 20, center.y + 50);
            mCanvas.drawPath(linePath, paint);

            /**Arrow*/
            Path mPath = new Path();
            mPath.moveTo(center.x - 13, center.y + 37);
            mPath.lineTo(center.x - 20, center.y + 50);
            mPath.lineTo(center.x - 24, center.y + 33);
            mCanvas.drawPath(mPath, paint);
        }

    }

    public void drawRotateYSArrow(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.parseColor("#A0FFFFFF"));
            PathEffect effect = new DashPathEffect(new float[] {3, 3, 3, 3}, 1f);
            Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tPaint.setStyle(Paint.Style.STROKE);
            tPaint.setStrokeWidth(3);
            tPaint.setColor(Color.parseColor("#A0FFFFFF"));
            tPaint.setPathEffect(effect);

            /**line*/
            Path linePath = new Path();
            linePath.moveTo(center.x, center.y);
            linePath.quadTo(center.x + 20, center.y + 20, center.x + 20, center.y + 50);
            mCanvas.drawPath(linePath, tPaint);

            /**Arrow*/
            Path mPath = new Path();
            mPath.moveTo(center.x + 13, center.y + 37);
            mPath.lineTo(center.x + 20, center.y + 50);
            mPath.lineTo(center.x + 24, center.y + 33);
            mCanvas.drawPath(mPath, paint);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("DrawView Created", "Start");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
