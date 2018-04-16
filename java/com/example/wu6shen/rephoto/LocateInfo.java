package com.example.wu6shen.rephoto;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;


/**
 * Created by wu6shen on 17-5-5.
 */

public class LocateInfo {
    public Point[] frame = new Point[4];
    public Point[] cross = new Point[4];
    public Point center = new Point();
    double[] errors = new double[6];

    LocateInfo(double width, double height, double scale) {
        center = new Point(width / 2, height / 2);
        frame[0] = new Point(center.x - width / 2 * scale, center.y - height / 2 * scale);
        frame[1] = new Point(center.x + width / 2 * scale, center.y - height / 2 * scale);
        frame[2] = new Point(center.x + width / 2 * scale, center.y + height / 2 * scale);
        frame[3] = new Point(center.x - width / 2 * scale, center.y + height / 2 * scale);

        cross[0] = new Point(center.x - height / 6 * scale, center.y);
        cross[1] = new Point(center.x + height / 6 * scale, center.y);
        cross[2] = new Point(center.x, center.y - height / 6 * scale);
        cross[3] = new Point(center.x, center.y + height / 6 * scale);
    }

    LocateInfo(LocateInfo origin, Mat homography, double scale) {
        double[] dataHomography = new double[9];
        homography.get(0, 0, dataHomography);

        Point temp;

        for (int i = 0; i < 4; i++) {
            temp = new Point(origin.frame[i].x * scale, origin.frame[i].y * scale);
            frame[i] = getNewPoint(temp, dataHomography);
            frame[i].x /= scale; frame[i].y /= scale;

            temp = new Point(origin.cross[i].x * scale, origin.cross[i].y * scale);
            cross[i] = getNewPoint(temp, dataHomography);
            cross[i].x /= scale; cross[i].y /= scale;
        }

        temp = new Point(origin.center.x * scale, origin.center.y * scale);
        center = getNewPoint(temp, dataHomography);
        center.x /= scale; center.y /= scale;
    }

    public Point getNewPoint(Point point, double[] dataHomgraphy) {
        double dataPoint[] = {point.x, point.y, 1};
        double dataNewPoint[] = new double[3];
        for (int i = 0; i < 3; i++) {
            dataNewPoint[i] = 0;
            for (int j = 0; j < 3; j++) {
                dataNewPoint[i] += dataHomgraphy[i * 3 + j] * dataPoint[j];
            }
        }
        //Log.i("Point", "(" + point.x + ", " + point.y + ")" + "-->(" + (dataNewPoint[0] / dataNewPoint[2]) + ", " + (dataNewPoint[1] / dataNewPoint[2]) + ")");
        return new Point(dataNewPoint[0] / dataNewPoint[2], dataNewPoint[1] / dataNewPoint[2]);
    }

    /**返回一个6位三进制数 代表正负或零 依次为
     * 仰俯 左右转 左右摆 左右移 上下移 前后移*/
    public double[] getErrors() {
        return errors;
    }
    public String[] getInfo(LocateInfo origin) {
        String[] info = new String[6];

        /**case0 仰俯*/
        errors[0] = (frame[2].x - frame[3].x) - (frame[1].x - frame[0].x);
        errors[0] /= 2;
        Log.i("INFOGET", "Case 0:" + errors[0]);
        if (errors[0] > MyUtility.THRES) {
            //info[0] = "Rot D";
            info[0] = "向上转";
            //info[0] = 1;
            /**1*/
        } else if (errors[0] < -MyUtility.THRES) {
            //errors[0] = -errors[0];
            //info[0] = "Rot U";
            info[0] = "向下转";
            //info[0] = -1;
            /**-1*/
        } else {
            info[0] = MyUtility.okInfo;
            //info[0] = 0;
            //errors[0] = 0;
            /**0*/
        }

        /**case1 左右转*/
        errors[1] = (frame[2].y - frame[1].y) - (frame[3].y - frame[0].y);
        errors[1] /= 2;
        //Log.i("INFOGET", "Case 1:" + errors[1]);
        if (errors[1] > MyUtility.THRES) {
            //info[1] = "Rot L";
            info[1] = "向左转";
            //info[1] = 1;
            /**1*/
        } else if (errors[1] < -MyUtility.THRES) {
            //errors[1] = -errors[1];
            //info[1] = "Rot R";
            info[1] = "向右转";
            //info[1] = -1;
            /**-1*/
        } else {
            errors[1] = 0;
            info[1] = MyUtility.okInfo;
            //info[1] = 0;
            /**0*/
        }

        /**case2 左右摆*/
        errors[2] = cross[1].y - cross[0].y;
        //Log.i("INFOGET", "Case 2:" + errors[2]);
        if (errors[2] > MyUtility.THRES) {
            //info[2] = "Rot CCW";
            info[2] = "向左摆";
            //info[2] = 1;
            /**1*/
        } else if (errors[2] < -MyUtility.THRES) {
            //errors[2] = -errors[2];
            //info[2] = "Rot CW ";
            info[2] = "向右摆";
            //info[2] = -1;
            /**-1*/
        } else {
            errors[2] = 0;
            info[2] = MyUtility.okInfo;
            //info[2] = 0;
            /**0*/
        }

        /**case3 左右移*/
        errors[3] = center.y - origin.center.y;
        //Log.i("INFOGET", "Case 3:" + errors[3]);
        if (errors[3] > MyUtility.THRES) {
            //info[3] = "Mov L";
            info[3] = "向右移";
            //info[3] = 1;
            /**1*/
        } else if (errors[3] < -MyUtility.THRES) {
            //errors[3] = -errors[3];
            //info[3] = "Mov R";
            info[3] = "向左移";
            //info[3] = -1;
            /**-1*/
        } else {
            errors[3] = 0;
            info[3] = MyUtility.okInfo;
            //info[3] = 0;
            /**0*/
        }

        errors[4] = center.x - origin.center.x;
        /**case4 上下移动*/
        //Log.i("INFOGET", "Case 4:" + errors[4]);
        if (errors[4] > MyUtility.THRES) {
            //info[4] = 1;
            //info[4] = "Mov D";
            info[4] = "向下移";
            /**1*/
        } else if (errors[4] < -MyUtility.THRES) {
            //errors[4] = -errors[4];
            //info[4] = "Mov U";
            info[4] = "向上移";
            //info[4] = -1;
            /**-1*/
        } else {
            errors[4] = 0;
            info[4] = MyUtility.okInfo;
            //info[4] = 0;
            /**0*/
        }

        /**case5 前后移*/
        double nowArea = MyUtility.getPolygonArea(frame);
        double originArea = MyUtility.getPolygonArea(origin.frame);
        double areaThres = MyUtility.THRES * MyUtility.THRES * 200;
        errors[5] = nowArea - originArea;
        //Log.i("INFOGET", "Case 5:" + nowArea + " " + originArea);
        if (errors[5] > areaThres) {
            //info[5] = 1;
            //info[5] = "Mov FW ";
            info[5] = "向前移";
            /**1*/
        } else if (errors[5] < -areaThres) {
            //errors[5] = -errors[5];
            //info[5] = "Mov BW ";
            info[5] = "向后移";
            //info[5] = 1;
            /**-1*/
        } else {
            errors[5] = 0;
            info[5] = MyUtility.okInfo;
            //info[5] = 0;
            /**0*/
        }
        if (errors[5] != 0) errors[5] = Math.sqrt(errors[5] / 200);

        for (int i = 0; i < 6; i++) {
            int id = i;
            for (int j = i + 1; j < 6; j++) {
                if (errors[j] > errors[id]) id = j;
            }
            //MyUtility.swap(errors, i, id);
            //MyUtility.swap(info, i, id);
            //Log.i("Sort", errors[i] + "");
        }

        return info;
    }
}