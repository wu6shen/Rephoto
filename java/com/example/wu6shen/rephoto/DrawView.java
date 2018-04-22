package com.example.wu6shen.rephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    //private PointF centerPoint = new PointF(925, 190);
    /**pitch view center*/
    private PointF centerPoint = new PointF(540, 1920/2);

    /**pitch view range*/
    private float pitch_up = centerPoint.y + 120;
    private float pitch_down = centerPoint.y - 120;
    private float length[] = {100.f, 170.0f};
    private float high = 80;

    private float test_error = 0;

    private float lenCoordinate = 125;
    private float dleftx = 0;
    private float dupy = 0;
    private float dzy = 0;
    private float posRotate = 80;
    private float sizeRotate = 20;
    private float angleD = (float)Math.PI / 6;

    float angles[] = new float[6];
    PointF points[] = new PointF[6];

    public DrawView(Context context) {
        super(context);
        mHolder = this.getHolder();
        mHolder.addCallback(this);

        /**设置为透明*/
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        PointF xL = new PointF(centerPoint.x - lenCoordinate, centerPoint.y); float angleXL = 0;
        PointF xR = new PointF(centerPoint.x + lenCoordinate, centerPoint.y); float angleXR = (float)Math.PI;
        PointF yF = new PointF(centerPoint.x + lenCoordinate * (float)Math.cos(Math.PI / 6), centerPoint.y - lenCoordinate * (float)Math.sin(Math.PI / 6)); float angleYF = (float)Math.PI * 7 / 6;
        PointF yB = new PointF(centerPoint.x - lenCoordinate * (float)Math.cos(Math.PI / 6), centerPoint.y + lenCoordinate * (float)Math.sin(Math.PI / 6)); float angleYB = (float)Math.PI / 6;
        PointF zU = new PointF(centerPoint.x, centerPoint.y - lenCoordinate); float angleZU = (float)Math.PI * 3 / 2;
        PointF zD = new PointF(centerPoint.x, centerPoint.y + lenCoordinate); float angleZD = (float)Math.PI / 2;
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

    public void drawLines(List<Pair<PointF, PointF>> lines, int color) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(10);
            for (Pair<PointF, PointF> line : lines) {
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
            paint.setAntiAlias(true);
            /** draw Rect
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                mCanvas.drawLine((float)locateInfo.frame[i].x, (float)locateInfo.frame[i].y,
                        (float)locateInfo.frame[j].x, (float)locateInfo.frame[j].y, paint);
            }
            mCanvas.drawLine((float)locateInfo.cross[0].x, (float)locateInfo.cross[0].y,
                    (float)locateInfo.cross[1].x, (float)locateInfo.cross[1].y, paint);
            mCanvas.drawLine((float)locateInfo.cross[2].x, (float)locateInfo.cross[2].y,
                    (float)locateInfo.cross[3].x, (float)locateInfo.cross[3].y, paint);

             */
            float scale = (float)locateInfo.errors[5];
            if (scale == 0) scale = 1;
            else if (scale < 0) scale = 1f / (1 - (scale + 5) / 200f);
            else scale = 1 + (scale - 5) / 200f;
            PointF center = new PointF((float)locateInfo.center.x, (float)locateInfo.center.y);
            paint.setStyle(Paint.Style.FILL);
            mCanvas.drawCircle(center.x, center.y, 20, paint);

            paint.setStyle(Paint.Style.STROKE);
            PointF left_up = new PointF(center.x - 230 * scale, center.y - 150 * scale);
            Path path = new Path();
            path.moveTo(left_up.x, left_up.y + 70 * scale);
            path.lineTo(left_up.x, left_up.y);
            path.lineTo(left_up.x + 70 * scale, left_up.y);
            mCanvas.drawPath(path, paint);

            PointF right_up = new PointF(center.x + 230 * scale, center.y - 150 * scale);
            path.moveTo(right_up.x, right_up.y + 70 * scale);
            path.lineTo(right_up.x, right_up.y);
            path.lineTo(right_up.x - 70 * scale, right_up.y);
            mCanvas.drawPath(path, paint);

            PointF right_bottom = new PointF(center.x + 230 * scale, center.y + 150 * scale);
            path.moveTo(right_bottom.x, right_bottom.y - 70 * scale);
            path.lineTo(right_bottom.x,right_bottom.y);
            path.lineTo(right_bottom.x - 70 * scale, right_bottom.y);
            mCanvas.drawPath(path, paint);

            PointF left_bottom = new PointF(center.x - 230 * scale, center.y + 150 * scale);
            path.moveTo(left_bottom.x, left_bottom.y - 70 * scale);
            path.lineTo(left_bottom.x, left_bottom.y);
            path.lineTo(left_bottom.x + 70 * scale, left_bottom.y);
            mCanvas.drawPath(path, paint);
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
            paintDraw.setStyle(Paint.Style.STROKE);
            paintDraw.setStrokeWidth(8.0f);
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
            paintDraw.setColor(Color.parseColor("#C0FFFFFF"));
            RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
            mCanvas.drawOval(rect, paintDraw);


            rect = new RectF(center.x - b, center.y - a, center.x + b, center.y + a);
            effects = new DashPathEffect(list, -C / 4);
            paintDraw.setPathEffect(effects);
            paintDraw.setColor(Color.parseColor("#C0FFFFFF"));
            mCanvas.drawOval(rect, paintDraw);

            rect = new RectF(center.x - a, center.y - a, center.x + a, center.y + a);
            effects = new DashPathEffect(new float[]{1, 0}, 0);
            paintDraw.setPathEffect(effects);
            paintDraw.setColor(Color.parseColor("#C0FFFFFF"));
            mCanvas.drawOval(rect, paintDraw);
        }
    }

    public void drawHalfOvalFull(PointF center, float a, float b, boolean where, Paint paint) {
        float C = (float)(Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (3 * b + a))));
        float[] list = new float[2];
        list[0] = C / 2;
        list[1] = C / 2;
        DashPathEffect effects = new DashPathEffect(list, 0);
        if (a < b)
            effects = new DashPathEffect(list,  -C / 4);
        if (where) {
            effects = new DashPathEffect(list, C / 2);
            if (a < b)
                effects = new DashPathEffect(list,  C / 4);
        }
        paint.setPathEffect(effects);
        RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
        mCanvas.drawOval(rect, paint);
    }

    public void drawHalfOvalDot(PointF center, float a, float b, boolean where, Paint paint) {
        float C = (float)(Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (3 * b + a))));
        float[] list = new float[26];
        for (int i = 0; i < list.length; i++) list[i] = C / 50;
        list[25] = C / 2;
        DashPathEffect effects = new DashPathEffect(list, C / 50);
        if (a < b) {
            effects = new DashPathEffect(list, C / 50 - C / 4);
        }
        if (where) {
            effects = new DashPathEffect(list, C / 2 + C / 50);
            if (a < b) {
                effects = new DashPathEffect(list, C / 50 + C / 4);
            }
        }
        paint.setPathEffect(effects);
        RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
        mCanvas.drawOval(rect, paint);
    }
    public void drawSphereY(PointF center, float scale, float error) {
        if (mCanvas != null) {
            Paint paintDraw = new Paint();
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
            paintDraw.setColor(Color.parseColor("#A0FFFFFF"));
            RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
            mCanvas.drawOval(rect, paintDraw);


            rect = new RectF(center.x - b, center.y - a, center.x + b, center.y + a);
            effects = new DashPathEffect(list, -C / 4);
            paintDraw.setPathEffect(effects);
            if (Math.abs(error) < 0.01)
                paintDraw.setColor(Color.parseColor("#A0FF0060"));
            else
                paintDraw.setColor(Color.parseColor("#A0FFFFFF"));
            mCanvas.drawOval(rect, paintDraw);

            rect = new RectF(center.x - a, center.y - a, center.x + a, center.y + a);
            effects = new DashPathEffect(new float[]{1, 0}, 0);
            paintDraw.setPathEffect(effects);
            if (Math.abs(error) < 0.01)
                paintDraw.setColor(Color.parseColor("#A020D0EF"));
            else
                paintDraw.setColor(Color.parseColor("#A0FFFFFF"));
            mCanvas.drawOval(rect, paintDraw);

            if (error > 0.01) {
                float b_new = b + (a - b) * error;
                paintDraw.setColor(Color.parseColor("#C0FF0060"));
                drawHalfOvalFull(center, b_new, a, false, paintDraw);
                paintDraw.setColor(Color.parseColor("#A0FF0060"));
                drawHalfOvalDot(center, b_new, a, true, paintDraw);
                b_new = a - a * error;
                if (b_new < 0) b_new = -b_new;
                paintDraw.setColor(Color.parseColor("#C020D0EF"));
                drawHalfOvalFull(center, b_new, a, true, paintDraw);
                paintDraw.setColor(Color.parseColor("#A020D0EF"));
                drawHalfOvalDot(center, b_new, a, false, paintDraw);
            } else if (error < -0.01) {
                float b_new = b - (a + b) * Math.abs(error);
                if (b_new < 0) {
                    b_new = -b_new;
                    paintDraw.setColor(Color.parseColor("#C0FF0060"));
                    drawHalfOvalFull(center, b_new, a, true, paintDraw);
                    paintDraw.setColor(Color.parseColor("#A0FF0060"));
                    drawHalfOvalDot(center, b_new, a, false, paintDraw);
                } else {
                    paintDraw.setColor(Color.parseColor("#C0FF0060"));
                    drawHalfOvalFull(center, b_new, a, false, paintDraw);
                    paintDraw.setColor(Color.parseColor("#A0FF0060"));
                    drawHalfOvalDot(center, b_new, a, true, paintDraw);

                }
                b_new = a + (b - a) * Math.abs(error);
                paintDraw.setColor(Color.parseColor("#C020D0EF"));
                drawHalfOvalFull(center, b_new, a, false, paintDraw);
                paintDraw.setColor(Color.parseColor("#A020D0EF"));
                drawHalfOvalDot(center, b_new, a, true, paintDraw);


            }




        }
    }

    public void drawSphereX(PointF center, float scale, float error) {
        if (mCanvas != null) {
            Paint paintDraw = new Paint();
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
            if (Math.abs(error) < 0.01)
            paintDraw.setColor(Color.parseColor("#A0FF0060"));
            else
                paintDraw.setColor(Color.parseColor("#A020D0EF"));
            RectF rect = new RectF(center.x - a, center.y - b, center.x + a, center.y + b);
            mCanvas.drawOval(rect, paintDraw);


            rect = new RectF(center.x - b, center.y - a, center.x + b, center.y + a);
            effects = new DashPathEffect(list, -C / 4);
            paintDraw.setPathEffect(effects);
            paintDraw.setColor(Color.parseColor("#A020D0EF"));
            mCanvas.drawOval(rect, paintDraw);
            paintDraw.setColor(Color.parseColor("#C0FF0060"));
            mCanvas.drawOval(rect, paintDraw);

            rect = new RectF(center.x - a, center.y - a, center.x + a, center.y + a);
            effects = new DashPathEffect(new float[]{1, 0}, 0);
            paintDraw.setPathEffect(effects);
            if (Math.abs(error) < 0.01)
            paintDraw.setColor(Color.parseColor("#A020D0EF"));
            else
                paintDraw.setColor(Color.parseColor("#A020D0EF"));
            mCanvas.drawOval(rect, paintDraw);

            if (error > 0.01) {
                float b_new = b + (a - b) * error;
                paintDraw.setColor(Color.parseColor("#C0FF0060"));
                drawHalfOvalFull(center, a, b_new, false, paintDraw);
                paintDraw.setColor(Color.parseColor("#A0FF0060"));
                drawHalfOvalDot(center, a, b_new, true, paintDraw);
                b_new = a - a * error;
                if (b_new < 0) b_new = -b_new;
                //paintDraw.setColor(Color.parseColor("#C020D0EF"));
                paintDraw.setColor(Color.parseColor("#C0FF0060"));
                drawHalfOvalFull(center, a, b_new, true, paintDraw);
                //paintDraw.setColor(Color.parseColor("#A020D0EF"));
                paintDraw.setColor(Color.parseColor("#A0FF0060"));
                drawHalfOvalDot(center, a, b_new, false, paintDraw);
            } else if (error < -0.01) {
                float b_new = b - (a + b) * Math.abs(error);
                if (b_new < 0) {
                    b_new = -b_new;
                    paintDraw.setColor(Color.parseColor("#C0FF0060"));
                    drawHalfOvalFull(center, a, b_new, true, paintDraw);
                    paintDraw.setColor(Color.parseColor("#A0FF0060"));
                    drawHalfOvalDot(center, a, b_new, false, paintDraw);
                } else {
                    paintDraw.setColor(Color.parseColor("#C0FF0060"));
                    drawHalfOvalFull(center, a, b_new, false, paintDraw);
                    paintDraw.setColor(Color.parseColor("#A0FF0060"));
                    drawHalfOvalDot(center, a, b_new, true, paintDraw);

                }
                b_new = a + (b - a) * Math.abs(error);
                //paintDraw.setColor(Color.parseColor("#C020D0EF"));
                paintDraw.setColor(Color.parseColor("#C0FF0060"));
                drawHalfOvalFull(center, a, b_new, false, paintDraw);
                //paintDraw.setColor(Color.parseColor("#A020D0EF"));
                paintDraw.setColor(Color.parseColor("#A0FF0060"));
                drawHalfOvalDot(center, a, b_new, true, paintDraw);


            }




        }
    }

    public void drawText(String info, int color) {
        if (mCanvas != null) {
            Paint paint = new Paint();
        }
    }

    public void drawBack(float x1, float y1, float x2, float y2) {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#A0303030"));
            mCanvas.drawRect(x1, y1, x2, y2, paint);
        }
    }

    public void drawPhone(PointF center, float scale) {
        if (mCanvas != null) {
            Paint paintDraw = new Paint();
            /**
            Bitmap bitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.phone);
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Rect rect = new Rect((int)center.x - 60, (int)center.y - 120, (int)center.x + 60, (int)center.y + 120);
            mCanvas.drawBitmap(bitmap, src, rect, paintDraw);
             */
            Paint paintErase = new Paint();
            paintDraw.setStyle(Paint.Style.FILL);
            paintDraw.setColor(Color.parseColor("#C08F8F8F"));


            paintDraw.setColor(Color.parseColor("#808F8F8F"));
            paintDraw.setStyle(Paint.Style.STROKE);
            for (int i = 0; i < 10; i++) {
                RectF outRectF = new RectF(center.x - 85 * scale + i, center.y - 150 * scale - i, center.x + 85 * scale + i, center.y + 150 * scale - i);
                mCanvas.drawRoundRect(outRectF, 15 * scale, 15 * scale, paintDraw);
            }

            paintDraw.setStyle(Paint.Style.FILL);
            paintDraw.setColor(Color.parseColor("#C08F8F8F"));
            RectF outRectF = new RectF(center.x - 85 * scale, center.y - 150 * scale, center.x + 85 * scale, center.y + 150 * scale);
            mCanvas.drawRoundRect(outRectF, 15 * scale, 15 * scale, paintDraw);

            RectF inRectF = new RectF(center.x - 75 * scale, center.y - 135 * scale, center.x + 75 * scale, center.y + 105 * scale);

            /*
            paintErase.setAlpha(0);
            paintErase.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            */
            paintErase.setColor(Color.parseColor("#C0F0F0F0"));
            mCanvas.drawRoundRect(inRectF, 7.5f, 7.5f, paintErase);

            paintErase.setColor(Color.parseColor("#C0808080"));
            mCanvas.drawCircle(center.x, center.y + 255.0f/2 * scale, 12 * scale,paintErase);
            /**test
             */

        }
    }

    public void drawUpArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 80 * scale;
            dupy+=5;
            if (dupy > 100) dupy = 0;
            float sy = (-a + dupy - 50) * scale;
            for (; sy < a * scale ; sy += 100 * scale) {
                float y = sy;
                float ey = Math.min(a * scale, 50 * scale + y);
                y = Math.max(-a * scale, y);
                float x = -(float) Math.sqrt(1 - y * y / a / a) * b;
                Path mPath = new Path();
                mPath.moveTo(center.x + x, center.y + y);
                for (; y < ey; y += 0.1f) {
                    x = -(float) Math.sqrt(1 - y * y / a / a) * b;
                    mPath.lineTo(center.x + x, center.y + y);
                }
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);
                paint.setColor(Color.parseColor("#E000D0FF"));
                paint.setAntiAlias(true);
                mCanvas.drawPath(mPath, paint);
                double k = -a * a / b / b * x / y;
                double theta = Math.atan(k)+ Math.PI;
                if (y < 0) theta -= Math.PI;
                double length = 10 * scale;
                double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
                double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
                Path tri = new Path();
                tri.moveTo(center.x + x, center.y + y);
                tri.lineTo(center.x + (float) x1, center.y + (float) y1);
                tri.lineTo(center.x + (float) x2, center.y + (float) y2);
                tri.close();
                mCanvas.drawPath(tri, paint);
            }
        }
    }

    public void drawDownArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 80 * scale;
            dupy+=5;
            if (dupy > 100) dupy = 0;
            float sy = (a + 50 - dupy) * scale;
            for (; sy > -a * scale; sy -= 100 * scale) {
                float y = sy;
                float ey = Math.max(-a * scale, y - 50 * scale);
                y = Math.min(a * scale, y);
                float x = -(float) Math.sqrt(1 - y * y / a / a) * b;
                Path mPath = new Path();
                mPath.moveTo(center.x + x, center.y + y);
                for (; y > ey; y -= 0.1f) {
                    x = -(float) Math.sqrt(1 - y * y / a / a) * b;
                    mPath.lineTo(center.x + x, center.y + y);
                }
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);
                paint.setColor(Color.parseColor("#E000D0FF"));
                paint.setAntiAlias(true);
                mCanvas.drawPath(mPath, paint);
                double k = -a * a / b / b * x / y;
                double theta = Math.atan(k) + Math.PI;
                if (y > 0) theta -= Math.PI;

                double length = 10 * scale;
                double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
                double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
                Path tri = new Path();
                tri.moveTo(center.x + x, center.y + y);
                tri.lineTo(center.x + (float) x1, center.y + (float) y1);
                tri.lineTo(center.x + (float) x2, center.y + (float) y2);
                tri.close();
                mCanvas.drawPath(tri, paint);
            }
        }
    }

    public void drawZSArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 200 * scale;
            dzy-=5;
            if (dzy < -50) dzy = +50;
            float y = (30 + dzy) * scale;
            float x = -(float)Math.sqrt(1 - y * y / a / a) * b;
            float ey = (-30 + dzy) * scale;
            Path mPath = new Path();
            mPath.moveTo(center.x + x, center.y + y);
            for (; y > ey; y -= 0.1f) {
                x = -(float)Math.sqrt(1 - y * y / a / a) * b;
                mPath.lineTo(center.x + x, center.y + y);
            }
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5.0f);
            paint.setColor(Color.parseColor("#E000D0FF"));
            paint.setAntiAlias(true);
            mCanvas.drawPath(mPath, paint);
            double k = -a*a/b/b * x / y;
            double theta = -Math.abs(Math.atan(k)) + Math.PI;
            double length = 10 * scale;
            double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
            double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
            Path tri = new Path();
            tri.moveTo(center.x + x, center.y + y);
            tri.lineTo(center.x + (float)x1, center.y + (float)y1);
            tri.lineTo(center.x + (float)x2, center.y + (float)y2);
            tri.close();
            mCanvas.drawPath(tri, paint);
        }
    }

    public void drawZNArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 200 * scale;
            dzy+=5;
            if (dzy > 50) dzy = -50;
            float y = (-30 + dzy) * scale;
            float x = -(float)Math.sqrt(1 - y * y / a / a) * b;
            float ey = (30 + dzy) * scale;
            Path mPath = new Path();
            mPath.moveTo(center.x + x, center.y + y);
            for (; y < ey; y += 0.1f) {
                x = -(float)Math.sqrt(1 - y * y / a / a) * b;
                mPath.lineTo(center.x + x, center.y + y);
            }
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5.0f);
            paint.setColor(Color.parseColor("#E000D0FF"));
            paint.setAntiAlias(true);
            mCanvas.drawPath(mPath, paint);
            double k = -a*a/b/b * x / y;
            double theta = Math.abs(Math.atan(k)) + Math.PI;
            double length = 10 * scale;
            double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
            double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
            Path tri = new Path();
            tri.moveTo(center.x + x, center.y + y);
            tri.lineTo(center.x + (float)x1, center.y + (float)y1);
            tri.lineTo(center.x + (float)x2, center.y + (float)y2);
            tri.close();
            mCanvas.drawPath(tri, paint);
        }
    }
    public void drawRightArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 80 * scale;
            dleftx+=5;
            if (dleftx > 100) dleftx = 0;
            float sy = (a + 50 - dleftx) * scale;
            for (; sy > -a * scale; sy -= 100 * scale) {
                float x = sy;
                float ex = Math.max(-a * scale, (x - 50 * scale));
                x = Math.min(a * scale, x);
                float y = (float)Math.sqrt(1 - x * x / a / a) * b;
                Path mPath = new Path();
                mPath.moveTo(center.x + x, center.y + y);
                for (; x > ex; x -= 0.1f) {
                    y = (float)Math.sqrt(1 - x * x / a / a) * b;
                    mPath.lineTo(center.x + x, center.y + y);
                }
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);
                paint.setColor(Color.parseColor("#E000D0FF"));
                paint.setAntiAlias(true);
                mCanvas.drawPath(mPath, paint);
                double k = -b*b/a/a * x / y;
                double theta = Math.atan(k);
                double length = 10 * scale;
                double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
                double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
                Path tri = new Path();
                tri.moveTo(center.x + x, center.y + y);
                tri.lineTo(center.x + (float)x1, center.y + (float)y1);
                tri.lineTo(center.x + (float)x2, center.y + (float)y2);
                tri.close();
                mCanvas.drawPath(tri, paint);
            }
        }
    }

    public void drawLeftArrow(PointF center, float scale) {
        if (mCanvas != null) {
            float a = 200 * scale, b = 80 * scale;
            dleftx+=5;
            if (dleftx > 100) dleftx = 0;
            float sx = (-a + dleftx - 50) * scale;
            for (; sx < a * scale; sx += 100 * scale) {
                float x = sx;
                float ex = Math.min(a * scale, x + 50 * scale);
                x = Math.max(-a * scale, x);
                float y = (float) Math.sqrt(1 - x * x / a / a) * b;
                Path mPath = new Path();
                mPath.moveTo(center.x + x, center.y + y);
                for (; x < ex; x += 0.1f) {
                    y = (float) Math.sqrt(1 - x * x / a / a) * b;
                    mPath.lineTo(center.x + x, center.y + y);
                }
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);
                paint.setColor(Color.parseColor("#E000D0FF"));
                paint.setAntiAlias(true);
                mCanvas.drawPath(mPath, paint);
                double k = -b * b / a / a * x / y;
                double theta = Math.atan(k) + Math.PI;
                double length = 10 * scale;
                double x1 = x + length * Math.cos(theta + 0.5), y1 = y + length * Math.sin(theta + 0.5);
                double x2 = x + length * Math.cos(theta - 0.5), y2 = y + length * Math.sin(theta - 0.5);
                Path tri = new Path();
                tri.moveTo(center.x + x, center.y + y);
                tri.lineTo(center.x + (float) x1, center.y + (float) y1);
                tri.lineTo(center.x + (float) x2, center.y + (float) y2);
                tri.close();
                mCanvas.drawPath(tri, paint);
            }
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

    public void drawPitch(float pitch_error) {
        if (mCanvas != null) {
            //test_error += 1.f;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG), paint_font = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint_font.setStrokeWidth(3);
            paint.setStrokeWidth(10);
            /** draw center Circle
            paint.setColor(Color.parseColor("#C0FFFF00"));
            paint.setStyle(Paint.Style.FILL);
            mCanvas.drawCircle(centerPoint.x, centerPoint.y, 20, paint);
             */

            /** draw background
            paint.setColor(Color.parseColor("#C0303030"));
            mCanvas.drawLine(0, centerPoint.y, 1080.f, centerPoint.y, paint);
             */

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#C0FFFFFF"));
            paint_font.setColor(Color.parseColor("#C0FFFFFF"));
            paint_font.setTextSize(40);
            mCanvas.clipRect(new Rect((int)(centerPoint.x - length[1] - 50), (int)pitch_down, (int)(centerPoint.x + length[1] + 50), (int)pitch_up));
            float center_high = centerPoint.y + (pitch_error - (int)(pitch_error / high) * high), now_y = center_high;
            int center_num = (int)(pitch_error / high), length_cnt = center_num & 1, now_num = center_num;
            mCanvas.drawLine(centerPoint.x - length[length_cnt], now_y, centerPoint.x + length[length_cnt], now_y, paint);
            mCanvas.drawText(center_num + "", centerPoint.x + length[length_cnt] + 15, now_y + 10, paint_font);
            now_y = center_high + high;
            length_cnt = center_num & 1;
            now_num = center_num - 1;
            while (now_y < pitch_up) {
                length_cnt = length_cnt ^ 1;
                mCanvas.drawLine(centerPoint.x - length[length_cnt], now_y, centerPoint.x + length[length_cnt], now_y, paint);
                mCanvas.drawText(now_num + "", centerPoint.x + length[length_cnt] + 15, now_y + 10, paint_font);
                now_num--;
                now_y += high;
            }
            now_y = center_high - high;
            length_cnt = center_num & 1;
            now_num = center_num + 1;
            while (now_y > pitch_down) {
                length_cnt = length_cnt ^ 1;
                mCanvas.drawLine(centerPoint.x - length[length_cnt], now_y, centerPoint.x + length[length_cnt], now_y, paint);
                mCanvas.drawText(now_num + "", centerPoint.x + length[length_cnt] + 15, now_y + 10, paint_font);
                now_num++;
                now_y -= high;
            }
        }
    }

    private float angle_diff = (float)Math.PI / 18;
    private float radiu_in = 450.f, radiu_out = 500.f;
    private float radiu_font = 360;

    public void drawRoll(float roll_error) {
        if (mCanvas != null) {
            test_error += 0.01f;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG), paint_font = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(10);

            /** draw center Triangle*/
            paint.setColor(Color.parseColor("#C0FF5000"));
            paint.setStyle(Paint.Style.FILL);
            Path tri = new Path();
            tri.moveTo(centerPoint.x, centerPoint.y - radiu_out - 20);
            tri.lineTo(centerPoint.x - 25, centerPoint.y - radiu_out - 80);
            tri.lineTo(centerPoint.x + 25, centerPoint.y - radiu_out - 80);
            tri.close();
            mCanvas.drawPath(tri, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#C0FFFFFF"));
            paint_font.setStrokeWidth(3);
            paint_font.setColor(Color.parseColor("#C0FFFFFF"));
            paint_font.setTextAlign(Paint.Align.CENTER);
            paint_font.setTextSize(50);

            float center_angle = (float)Math.PI / 2 + (roll_error - (int)(roll_error / angle_diff) * angle_diff), now_angle = center_angle;
            int center_num = (int)(roll_error / angle_diff), now_num = center_num;
            if (now_num % 3 == 0) radiu_in = 420;
            else radiu_in = 450;
            PointF line_s , line_t;
            line_s = new PointF(centerPoint.x + radiu_in * (float)Math.cos(now_angle), centerPoint.y - radiu_in * (float)Math.sin(now_angle));
            line_t = new PointF(centerPoint.x + radiu_out * (float)Math.cos(now_angle), centerPoint.y - radiu_out * (float)Math.sin(now_angle));
            mCanvas.drawLine(line_s.x, line_s.y , line_t.x, line_t.y, paint);
            if (now_num % 3 == 0) {
                mCanvas.drawText(now_num + "", centerPoint.x + radiu_font * (float) Math.cos(now_angle), centerPoint.y - radiu_font * (float) Math.sin(now_angle), paint_font);
            }

            now_angle = center_angle + angle_diff;
            now_num = center_num - 1;
            while (now_angle < (float)Math.PI) {
                if (now_num % 3 == 0) radiu_in = 420;
                else radiu_in = 450;
                line_s = new PointF(centerPoint.x + radiu_in * (float)Math.cos(now_angle), centerPoint.y - radiu_in * (float)Math.sin(now_angle));
                line_t = new PointF(centerPoint.x + radiu_out * (float)Math.cos(now_angle), centerPoint.y - radiu_out * (float)Math.sin(now_angle));
                mCanvas.drawLine(line_s.x, line_s.y , line_t.x, line_t.y, paint);
                if (now_num % 3 == 0) {
                    mCanvas.drawText(now_num + "", centerPoint.x + radiu_font * (float) Math.cos(now_angle), centerPoint.y - radiu_font * (float) Math.sin(now_angle), paint_font);
                }

                now_angle += angle_diff;
                now_num--;
            }

            now_angle = center_angle - angle_diff;
            now_num = center_num + 1;
            while (now_angle >= 0.f) {
                if (now_num % 3 == 0) radiu_in = 420;
                else radiu_in = 450;
                line_s = new PointF(centerPoint.x + radiu_in * (float)Math.cos(now_angle), centerPoint.y - radiu_in * (float)Math.sin(now_angle));
                line_t = new PointF(centerPoint.x + radiu_out * (float)Math.cos(now_angle), centerPoint.y - radiu_out * (float)Math.sin(now_angle));
                mCanvas.drawLine(line_s.x, line_s.y , line_t.x, line_t.y, paint);
                if (now_num % 3 == 0) {
                    mCanvas.drawText(now_num + "", centerPoint.x + radiu_font * (float) Math.cos(now_angle), centerPoint.y - radiu_font * (float) Math.sin(now_angle), paint_font);
                }

                now_angle -= angle_diff;
                now_num++;
            }
        }
    }

    public void drawHorizon(float pitch_error, float roll_error) {
        if (mCanvas != null) {
            float zero_pos = centerPoint.y + pitch_error;
            float zero_angle = -roll_error;
            float lx = 0, ly = zero_pos + centerPoint.x * (float)Math.tan(zero_angle);
            float rx = 1080, ry = zero_pos - (rx - centerPoint.x) * (float)Math.tan(zero_angle);
            Path path=  new Path();
            path.moveTo(lx, ly);
            path.lineTo(rx, ry);
            path.lineTo(1080, 1900);
            path.lineTo(0, 1920);
            path.close();

            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#50FFFFFF"));
            paint.setStyle(Paint.Style.FILL);
            mCanvas.drawPath(path, paint);
            paint.setStrokeWidth(10);
            paint.setColor(Color.parseColor("#A02050F0"));
            mCanvas.drawLine(lx, ly, rx, ry, paint);
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
