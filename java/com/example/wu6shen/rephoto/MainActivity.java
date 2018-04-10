package com.example.wu6shen.rephoto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("Opencv Init", "error");
        }
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    /**计算图片大小192 * 180*/
    private static double scaleCalImage = 0.25;
    /**框占图片大小*/
    private static double scaleFrameSize = 3.0 / 5;


    private MediaRecorder mRecorder;//音视频录制类
    long st, ed;
    AlertDialog.Builder builder;
    String[] single_list = {"New", "Old", "AlphaBlend"};
    String folderName;
    private int paintColor = Color.RED;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private CameraView cameraView;
    private DrawView drawView;

    GetPathFromUri4kitkat getPathFromUri4kitkat = new GetPathFromUri4kitkat();
    private int showPhotosId = 0;
    private int photosOk = -2;
    /**
     * photosOk = -1 没有对照图片
     * photosOk = 0 没有开始做匹配
     * photosOk = 1 or 2 匹配开始
     * photosOk = 3 匹配结束
     * phtotsOk = 4 选择该图片
     */

    private Bitmap originBitmap;
    private Bitmap bestBitmap;
    private Bitmap lookBitmap;
    private Bitmap nowBitmap;
    private Bitmap matchBitmap;
    private Bitmap alphaDisplay;
    private double bestScore;
    private Mat src1 = new Mat();
    private Mat src2 = new Mat();
    private Mat src3 = new Mat();
    private TextView textImageView;

    private Size previewSize;
    private LocateInfo originLocate;
    private LocateInfo nowLocate;
    private LocateInfo testLocate;
    private double nowScore;
    private double stopScore = 97;
    private String[] info = new String[6];
    private String textInfo;
    private String imageName;
    private TextView textInfoView;
    private TextView textSaveView;

    private Button takePhotoButton;
    private Button openButton;
    private Button okButton;
    private Button clearButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//隐藏状态栏
        FrameLayout preview = (FrameLayout) findViewById(R.id.preview);


        /**绘画窗口*/
        drawView = new DrawView(this);
        preview.addView(drawView);
        drawView.setZOrderOnTop(true);
        drawView.setZOrderMediaOverlay(true);

        /**照相机*/
        cameraView = new CameraView(this);
        preview.addView(cameraView);

        /**text*/
        textInfoView = (TextView) findViewById(R.id.info);
        textInfoView.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 6; i++) info[i] = MyUtility.okInfo;
        getTextInfo();
        textInfoView.setText(textInfo);
        textInfoView.setTextColor(Color.RED);

        textSaveView = (TextView) findViewById(R.id.saveText);
        textSaveView.setVisibility(View.INVISIBLE);
        textSaveView.setText("是否保存");
        textSaveView.setTextSize(20);
        textSaveView.setBackgroundResource(R.drawable.text_view_border);
        textSaveView.setTextColor(Color.WHITE);

        textImageView = (TextView) findViewById(R.id.image);
        textImageView.setVisibility(View.INVISIBLE);

        /**take photo按钮*/
        takePhotoButton = (Button) findViewById(R.id.take_pircture);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mCameraPictureCallback);
            }
        });

        openButton = (Button) findViewById(R.id.openButton);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photosOk == -2) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 100);
                }
            }
        });


        okButton = (Button) findViewById(R.id.testButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 */
                if (photosOk == -1) {
                            textSaveView.setVisibility(View.GONE);
                            //Toast.makeText(MainActivity.this, "The name is : " + edit.getText().toString(), Toast.LENGTH_SHORT).show();
                            MyUtility.saveImageToGallery(getApplicationContext(), originBitmap, "rephoto", System.currentTimeMillis() + "-ori.jpg");
                            imageName = System.currentTimeMillis() + "";
                            initTrack(src1.getNativeObjAddr());
                            photosOk = 0;
                            takePhotoButton.setVisibility(View.VISIBLE);

                } else if (photosOk == -2) {
                } else if (photosOk == 3) {
                    photosOk = 4;
                    takePhotoButton.setVisibility(View.INVISIBLE);
                    //okButton.setText("SAVE");

                    textImageView.setVisibility(View.VISIBLE);
                    textImageView.setText("Now Image");
                    textImageView.setTextColor(Color.RED);
                } else if (photosOk == 4) {
                    String saveFileName, saveTxt;
                    long now = System.currentTimeMillis();
                    if (paintColor == Color.RED) {
                        saveFileName = imageName + "-kf.jpg";
                        saveTxt = imageName + "-kf.txt";
                    }
                    else if (paintColor == Color.GREEN) {
                        saveFileName = imageName + "-old.jpg";
                        saveTxt = imageName + "-old.txt";
                    }
                    else {
                        saveFileName = imageName + "-ab.jpg";
                        saveTxt = imageName + "-ab.txt";
                    }
                    Toast.makeText(MainActivity.this, "saved as " + saveFileName, Toast.LENGTH_SHORT).show();
                    MyUtility.saveImageToGallery(getApplicationContext(), bestBitmap, "rephoto", saveFileName);
                    MyUtility.writeFileSdcardFile(getApplicationContext(), saveTxt, (ed - st) + "ms");
                }
            }
        });
        okButton.setVisibility(View.GONE);

        /**clear*/
        clearButton = (Button) findViewById(R.id.close_button);
        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * destroyCamera();
                 * finish();
                 */
                for (int i = 0; i < 6; i++) info[i] = MyUtility.okInfo;
                nowScore = 0;
                bestScore = -1;
                stopScore = 97;

                getTextInfo();
                textInfoView.setText(textInfo);

                drawView.getCanvas();
                drawView.clearDraw();
                drawView.drawBack(0.f, (float)previewSize.height / 5 * 4, (float)previewSize.width, (float)previewSize.height);
                switch (photosOk) {
                    case -2:
                        break;
                    case -1:
                        photosOk = -2;
                        takePhotoButton.setVisibility(View.VISIBLE);
                        clearButton.setVisibility(View.GONE);
                        okButton.setVisibility(View.GONE);
                        openButton.setVisibility(View.VISIBLE);
                        textSaveView.setVisibility(View.INVISIBLE);
                        break;
                    case 0:
                        photosOk = -2;
                        clearButton.setVisibility(View.GONE);
                        okButton.setVisibility(View.GONE);
                        openButton.setVisibility(View.VISIBLE);
                        textInfoView.setVisibility(View.INVISIBLE);
                        break;
                    case 4:
                        takePhotoButton.setVisibility(View.VISIBLE);
                        //takePhotoButton.setText("Start");

                        okButton.setVisibility(View.INVISIBLE);
                        //okButton.setText("OK");

                        textImageView.setVisibility(View.INVISIBLE);
                        textInfoView.setVisibility(View.INVISIBLE);

                        photosOk = 0;
                        drawView.drawBitmap(lookBitmap);
                        break;
                    default:
                        photosOk = 0;

                        takePhotoButton.setVisibility(View.VISIBLE);
                        textInfoView.setVisibility(View.INVISIBLE);
                        //takePhotoButton.setText("Start");

                        okButton.setVisibility(View.INVISIBLE);

                        //stopRecord();
                        drawView.drawBitmap(lookBitmap);
                        break;
                }
                drawView.update();
            }
        });

        hideNavigationBar();
        builder = new AlertDialog.Builder(this);
    }
    public void hideNavigationBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

        try {
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            Log.i("ASD", "start");
            Uri selectedImage = data.getData();
            if (selectedImage != null) {

                Log.i("uri", selectedImage.getPath());
                String imagePath = getPathFromUri4kitkat.getPath(MainActivity.this, selectedImage);
                imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("-"));
                Log.i("ASD", "" + imageName);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(selectedImage));
                    Log.i("Activity", bitmap.getWidth() + " " + bitmap.getHeight());
                    originBitmap = scaleBitmap(bitmap, (float) (1080.0f / bitmap.getWidth()), (float) (1920.0f / bitmap.getHeight()));
                    lookBitmap = scaleBitmap(originBitmap, (float) (scaleCalImage * 1.3), (float) (scaleCalImage * 1.3));
                    Utils.bitmapToMat(scaleBitmap(originBitmap, (float) scaleCalImage, (float) scaleCalImage), src1);
                    initTrack(src1.getNativeObjAddr());
                    photosOk = 0;
                    okButton.setVisibility(View.GONE);
                    openButton.setVisibility(View.GONE);
                    clearButton.setVisibility(View.VISIBLE);
                    startCameraPreview();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {
        destroyCamera();
        finish();
        return ;
    }

    private void initLines() {
        originLocate = new LocateInfo(previewSize.width, previewSize.height, scaleFrameSize);
    }

    private void calculateLinesFrom(Bitmap bitmap) {
        if (paintColor == -1) {
            Mat m1 = new Mat();
            Mat m2 = new Mat();
            Mat m3 = new Mat();
            Utils.bitmapToMat(scaleBitmap(originBitmap, (float) (scaleCalImage * 0.7), (float) (scaleCalImage * 0.7)), m1);
            Utils.bitmapToMat(scaleBitmap(nowBitmap, (float) (scaleCalImage * 0.7), (float) (scaleCalImage * 0.7)), m2);
            alphaBlend(m1.getNativeObjAddr(), m2.getNativeObjAddr(), m3.getNativeObjAddr());
            Log.i("info", m3.width() +"");
            alphaDisplay = Bitmap.createBitmap(m3.width(), m3.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(m3, alphaDisplay);
            Log.i("info", m3.height() +"");
            return ;
        }
        Utils.bitmapToMat(bitmap, src2);
        Mat result = new Mat();
        int a = 0;

        /** draw Match
         nowScore = MatchPhotoRANSACD(src1.getNativeObjAddr(), src2.getNativeObjAddr(), src3.getNativeObjAddr(), result.getNativeObjAddr());
         if (!src3.empty()) {
         matchBitmap = Bitmap.createBitmap(src3.width(), src3.height(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(src3, matchBitmap);
         matchBitmap = scaleBitmap(matchBitmap, (float)2, (float)2);
         }
         */
        nowScore = MatchPhotoRANSAC(src1.getNativeObjAddr(), src2.getNativeObjAddr(), result.getNativeObjAddr());
        if (!result.empty()) {
            nowLocate = new LocateInfo(originLocate, result, scaleCalImage);
            Log.i("ASD", "AAAAAA");
        } else {
            Log.i("ASD", "bbbbbbbbb");

        }

        /**
         MatchPhotoLMEDS(src1.getNativeObjAddr(), src2.getNativeObjAddr(), result.getNativeObjAddr());
         if (!result.empty()) {
         testLocate = new LocateInfo(originLocate, result, scaleCalImage);
         }
         */

        info = nowLocate.getInfo(originLocate);
        for (int i = 0; i < 6; i++) {
            //Log.i("Info", info[i]);
        }

        //nowScore = MyUtility.getSimilarity(src1, src2);
        if (nowScore > bestScore + 1e-5) {
            Log.i("ASD", nowScore + "----" + bestScore);
            bestScore = nowScore;
            bestBitmap = nowBitmap;
            if (bestScore > stopScore) {
                clearTracker();
                nowBitmap = bestBitmap;
                okButton.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.VISIBLE);
                takePhotoButton.setVisibility(View.INVISIBLE);
                takePhotoButton.setBackgroundResource(R.drawable.snap_button);
                //bestBitmap = bitmap;
                textInfoView.setVisibility(View.INVISIBLE);
                //takePhotoButton.setVisibility(View.INVISIBLE);
                //okButton.setText("SAVE");
                ed = System.currentTimeMillis();
                photosOk = 3;
                AlertDialog.Builder goOn = new AlertDialog.Builder(MainActivity.this);
                goOn.setTitle("Stop or not");
                goOn.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopScore = 97;
                    }
                });
                goOn.setNegativeButton("Go on", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        photosOk = 1;
                        textInfoView.setVisibility(View.VISIBLE);
                        okButton.setVisibility(View.INVISIBLE);
                        clearButton.setVisibility(View.GONE);
                        takePhotoButton.setVisibility(View.INVISIBLE);
                        stopScore += 0.5;
                    }
                });
                goOn.show();
                //stopRecord();
            }
        }


        getTextInfo();
        textInfoView.setText(textInfo);

        /**
         MatchPhotoLMEDS(src1.getNativeObjAddr(), src2.getNativeObjAddr(), result.getNativeObjAddr());
         if (!result.empty()) {
         calculateLinesL(result);

         }
         */
    }


    private Camera.PictureCallback mCameraPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            startCameraPreview();
            if (photosOk == -2) printSupportedSize();

            final Bitmap bitmap = rotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 90);
            if (bitmap != null) {
                Log.i("Take Picture", bitmap.getWidth() + "," + bitmap.getHeight());
                switch (photosOk) {

                    case -2:
                        originBitmap = bitmap;
                        lookBitmap = scaleBitmap(bitmap, (float)(scaleCalImage * 1.3), (float)(scaleCalImage * 1.3));
                        Utils.bitmapToMat(scaleBitmap(bitmap, (float)scaleCalImage, (float)scaleCalImage), src1);
                        photosOk = -1;
                        okButton.setVisibility(View.VISIBLE);
                        openButton.setVisibility(View.GONE);
                        clearButton.setVisibility(View.VISIBLE);
                        textSaveView.setVisibility(View.VISIBLE);
                        takePhotoButton.setVisibility(View.INVISIBLE);
                        Log.i("test-clear", "yes");
                        break;
                    case 0:
                        /**
                        builder.setTitle("method");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setSingleChoiceItems(single_list, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    paintColor = Color.RED;
                                    setNew();
                                    photosOk = 1;
                                } else if (which == 1) {
                                    paintColor = Color.GREEN;
                                    setOld();
                                    photosOk = 1;
                                } else {
                                    paintColor = -1;
                                    photosOk = 5;
                                }
                                dialog.dismiss();
                                // startRecord();
                            }
                        });
                         */
                        paintColor = Color.RED;
                        setNew();
                        photosOk = 1;

                        showPhotosId = 0;
                        if (paintColor == Color.RED)
                            folderName = System.currentTimeMillis() + "-kp";
                        else if (paintColor == Color.GREEN)
                            folderName = System.currentTimeMillis() + "-old";
                        else
                            folderName = System.currentTimeMillis() + "-ab";

                        /**
                        AlertDialog dialog = builder.create();
                        dialog.show();
                         */
                        takePhotoButton.setBackgroundResource(R.drawable.snap_button_stop);
                        textInfoView.setVisibility(View.VISIBLE);
                        clearButton.setVisibility(View.GONE);
                        okButton.setVisibility(View.GONE);
                        st = System.currentTimeMillis();
                        //takePhotoButton.setText("Stop");
                        break;
                    case 1:
                        nowScore = bestScore;
                        clearTracker();
                        calculateLinesFrom(scaleBitmap(bestBitmap, (float) (scaleCalImage), (float) (scaleCalImage)));
                        nowBitmap = bestBitmap;
                        okButton.setVisibility(View.VISIBLE);
                        clearButton.setVisibility(View.VISIBLE);
                        takePhotoButton.setVisibility(View.INVISIBLE);
                        takePhotoButton.setBackgroundResource(R.drawable.snap_button);
                        //bestBitmap = bitmap;
                        textInfoView.setVisibility(View.INVISIBLE);
                        //takePhotoButton.setVisibility(View.INVISIBLE);
                        //okButton.setText("SAVE");
                        ed = System.currentTimeMillis();
                        //stopRecord();

                        /**don't have 3
                         textImageView.setVisibility(View.VISIBLE);
                         textImageView.setText("Now Image");
                         textImageView.setTextColor(Color.RED);
                         */
                        photosOk = 3;
                        break;
                    case 2:
                        nowScore = bestScore;
                        clearTracker();
                        calculateLinesFrom(scaleBitmap(bestBitmap, (float) (scaleCalImage), (float) (scaleCalImage)));
                        okButton.setVisibility(View.VISIBLE);
                        nowBitmap = bestBitmap;
                        //bestBitmap = bitmap;
                        takePhotoButton.setVisibility(View.INVISIBLE);
                        textInfoView.setVisibility(View.INVISIBLE);
                        ed = System.currentTimeMillis();
                        //stopRecord();
                        //takePhotoButton.setVisibility(View.INVISIBLE);
                        //okButton.setText("SAVE");

                        /**don't have 3
                         textImageView.setVisibility(View.VISIBLE);
                         textImageView.setText("Now Image");

                         textImageView.setTextColor(Color.RED);
                         */
                        photosOk = 3;
                        break;
                    case 5:
                        nowBitmap = bitmap;
                        calculateLinesFrom(scaleBitmap(bitmap, (float) (scaleCalImage), (float) (scaleCalImage)));
                        clearButton.setVisibility(View.VISIBLE);
                        okButton.setVisibility(View.VISIBLE);
                        takePhotoButton.setVisibility(View.INVISIBLE);
                        takePhotoButton.setBackgroundResource(R.drawable.snap_button);
                        bestBitmap = bitmap;
                        photosOk = 3;
                        ed = System.currentTimeMillis();
                    default:
                        Log.i("Error photosOk", "Error Num");
                        break;
                }
            }
        }
    };

    private Camera.PreviewCallback mCameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            /**
            drawView.getCanvas();
            drawView.clearDraw();
            testDraw();
            drawView.update();
             */
            if (photosOk == -2) {
                drawView.getCanvas();
                drawView.clearDraw();
                drawView.drawBack(0.f, (float)previewSize.height / 5 * 4, (float)previewSize.width, (float)previewSize.height);
                drawView.update();
            } else if (photosOk == -1) {
                drawView.getCanvas();
                drawView.clearDraw();
                drawView.drawBitmap(originBitmap);
                drawView.update();
            } else if (photosOk == 0) {
                drawView.getCanvas();
                drawView.clearDraw();
                drawView.drawInfoTest(info);
                drawView.drawBitmap(lookBitmap);
                drawView.drawBack(0.f, (float)previewSize.height / 5 * 4, (float)previewSize.width, (float)previewSize.height);
                drawView.update();
            } else if (photosOk > 0){
                if (photosOk < 3 || photosOk == 5) {
                    Camera.Size size = mCamera.getParameters().getPreviewSize();
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
                                size.height, null);
                        if (image != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                                    100, stream);
                            Bitmap inputImage = rotateBitmap(BitmapFactory.decodeByteArray(
                                    stream.toByteArray(), 0, stream.size()), 90);
                            stream.close();
                            nowBitmap = inputImage;
                            inputImage = scaleBitmap(inputImage, (float) (scaleCalImage), (float) (scaleCalImage));
                            long start = System.currentTimeMillis();
                            calculateLinesFrom(inputImage);

                            long stop = System.currentTimeMillis();
                            Log.i("info", "Time:" + (stop - start));
                            Log.i("type", src1.type() + " " + src2.type());
                            ed = System.currentTimeMillis();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /**控制停止开始*/
                    //photosOk = 3;
                }

                drawView.getCanvas();
                drawView.clearDraw();
                if (paintColor == -1) {
                    if (photosOk == 5) {
                        Log.i("info", alphaDisplay.getWidth() + "===== " + nowBitmap.getWidth());
                        drawView.drawBitmap(nowBitmap);
                        drawView.drawBitmap(alphaDisplay);
                    } else if (photosOk == 3 || photosOk == 4){
                        drawView.drawBitmap(nowBitmap);
                        if (photosOk != 4) {
                            drawView.drawBitmap(alphaDisplay);
                        }
                    }
                } else {
                    if (photosOk == 3 || photosOk == 4) {
                        drawView.drawBitmap(nowBitmap);
                    }
                    if (photosOk != 4) {
                        drawView.drawInfoTest(info);
                        drawView.drawBitmap(lookBitmap);
                        drawView.drawLocateInfo(originLocate, Color.parseColor("#E020D0EF"));
                        drawView.drawLocateInfo(nowLocate, Color.parseColor("#E0E05050"));
                        //drawView.drawLocateInfo(testLocate, Color.GREEN);
                    }
                }
                if (photosOk == -2 || photosOk == 0 || photosOk == 1)
                drawView.drawBack(0.f, (float)previewSize.height / 5 * 4, (float)previewSize.width, (float)previewSize.height);
                if (photosOk == 1 || photosOk == 2)
                testDraw();
                drawView.update();
               /*
                */
            }
        }
    };

    private void getTextInfo() {
        if (photosOk >= 1) {
            DecimalFormat df = new DecimalFormat("#.00");
            Log.i("info", (ed - st) + "ASD" + ed + " ASD" + st);
            textInfo = "Time: " + (ed - st) / 1000 + "s\n";
            textInfo += "Score: " + df.format(nowScore) + "\n";
        } else {
            textInfo = "Time: " + MyUtility.okInfo + "\n";
            textInfo += "Score: " + MyUtility.okInfo + "\n";
        }
        textInfo += info[0] + " " + info[1] + " " + info[2] + "\n" + info[3] + " " + info[4] + " " + info[5];
        /**
         */
    }

    private void changePictureSize(Size size) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureSize((int)size.width, (int)size.height);
        mCamera.setParameters(parameters);
    }

    /**手动聚焦触摸事件*/
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (photosOk == 4) {
                showPhotosId ^= 1;
                if (showPhotosId == 1) {
                    nowBitmap = originBitmap;
                    textImageView.setText("Origin Image");
                    textImageView.setTextColor(Color.BLUE);
                } else {
                    nowBitmap = bestBitmap;
                    textImageView.setText("Now Image");
                    textImageView.setTextColor(Color.RED);
                }
            } else {
                //handleFocus(event);
            }
        }
        return true;
    }

    private void initCamera() {
        mCamera = getCameraInstance();
        /**旋转90度显示*/
        mCamera.setDisplayOrientation(90);

        Camera.Parameters parameters = mCamera.getParameters();

        /**设置照片大小*/
        Camera.Size bestPictureSize = getBestPictureSize();
        printSupportedSize();
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
        parameters.setPreviewSize(bestPictureSize.width, bestPictureSize.height);
        Log.i("Init Camera", parameters.getPreviewSize().width + "," + parameters.getPreviewSize().height);

        previewSize = new Size((double)parameters.getPreviewSize().height, (double)parameters.getPreviewSize().width);
        initLines();

        mCamera.setParameters(parameters);
    }

    private void destroyCamera() {
        if (mCamera == null) {
            return ;
        }

        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private void startCameraPreview() {
        if (mCamera == null) {
            return ;
        }

        try {
            Camera.Parameters params = mCamera.getParameters();
//*EDIT*//params.setFocusMode("continuous-picture");
//It is better to use defined constraints as opposed to String, thanks to AbdelHady
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(mCameraPreviewCallback);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopCameraPreview() {

        if (mCamera == null) {
            return ;
        }

        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**得到一个camera的实例*/
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**计算聚焦区域*/
    private static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        Log.i("-----", x / previewSize.width + " " + y/previewSize.height);
        int centerX = (int) (x / previewSize.width * 2000) - 1000;
        int centerY = (int) (y / previewSize.height * 2000) - 1000;

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000-300);


        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void testDraw() {
        /**测试绘制 */
        PointF center = new PointF((float)200, (float)previewSize.height / 10 * 9);
        drawView.drawPhone(center, 0.8f);
        drawView.drawSphere(center, 0.8f);
        if (info[1] == "向左转")
            drawView.drawLeftArrow(new PointF(center.x, center.y), 0.8f);
        else if (info[1] == "向右转")
            drawView.drawRightArrow(new PointF(center.x, center.y), 0.8f);

        if (info[0] == "向上转")
            drawView.drawUpArrow(new PointF(center.x, center.y), 0.8f);
        else if (info[0] == "向下转")
            drawView.drawDownArrow(new PointF(center.x, center.y), 0.8f);

        if (info[2] == "向左摆")
            drawView.drawZNArrow(new PointF(center.x, center.y), 0.8f);
        else if (info[2] == "向右摆")
            drawView.drawZSArrow(new PointF(center.x, center.y), 0.8f);
    }

    /**手动聚焦***/
    private void handleFocus(MotionEvent event) {



        Camera.Parameters params = mCamera.getParameters();
        Log.i("----", params.toString());
        Camera.Size previewSize = params.getPreviewSize();
        Log.i("---", event.getX() + " " + event.getY());
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, previewSize);

        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            Log.i("----", focusRect + "");
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i("handle", "focus areas not supported");
        }

        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        mCamera.setParameters(params);

        /*
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mCamera.cancelAutoFocus();
                Camera.Parameters params = camera.getParameters();
                //if (params.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
                //}
            }
        });
        */
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {

            }
        });
    }

    class CameraView extends SurfaceView implements SurfaceHolder.Callback {

        public CameraView(Context context) {
            super(context);
            Log.i("CameraView", "INIT");
            initCamera();
            mHolder = this.getHolder();
            mHolder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("CameraView", "Created");
            startCameraPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mHolder.getSurface() == null) {
                return ;
            }

            stopCameraPreview();

            /**
             * 修改分辨率
             */

            startCameraPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("CameraView", "destroyed");
            stopCameraPreview();
        }
    }

    public Camera.Size getBestPictureSize() {
        Camera.Size size = mCamera.new Size(1920, 1080);
        return size;
    }

    public void printSupportedSize() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        for (Camera.Size size : sizes) {
            Log.i("Size", size.width + "," + size.height);
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float scaleWidth, float scaleHeight) {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void saveImage(Bitmap bitmap) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // 创建MediaRecorder
        }
        if (mCamera != null) {
            //mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
        }
        try {
            // 设置音频采集方式
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //设置文件的输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//aac_adif， aac_adts， output_format_rtp_avp， output_format_mpeg2ts ，webm
            //设置video的编码格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
            mRecorder.setVideoSize(1920, 1080);
            //设置录制的视频编码比特率
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            String path = Environment.getExternalStorageDirectory().getPath();
            Log.i("ASD", path);
            if (path != null) {
                File dir = new File(path + "/videos");
                if (!dir.exists()) {
                    dir.mkdir();
                    Log.i("ASD", "asdadsadasdsadsad");
                }
                long now = System.currentTimeMillis();
                if (paintColor == Color.RED)
                    path = dir + "/" + now + "-kp.mp4";
                else if (paintColor == Color.GREEN)
                    path = dir + "/" + now + "-old.mp4";
                else
                    path = dir + "/" + now + "-ab.mp4";
                Uri uri = Uri.fromFile(dir);
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                //设置输出文件的路径
                mRecorder.setOutputFile(path);
                //准备录制
                mRecorder.prepare();
                //开始录制
                mRecorder.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        try {
            //停止录制
            mRecorder.stop();
            //重置
            mRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native double MatchPhotoRANSAC(long src1, long src2, long result);
    public native void initTrack(long src);
    public native void setNew();
    public native void setOld();
    public native void alphaBlend(long src1, long src2, long result);
    public native void clearTracker();


    public native double MatchPhotoRANSACD(long src1, long src2, long src3, long result);
    public native void testTime();
    public native void MatchPhotoLMEDS(long src1, long src2, long result);
    public native void matMul(long mat1, long mat2, long result);

}
