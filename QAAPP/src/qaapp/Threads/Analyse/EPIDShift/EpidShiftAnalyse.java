/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.Threads.Analyse.EPIDShift;

import DICOM.DICOM;
import ImageHelper.Helper;
import RawDCMLibary.DICOM.DICOMFile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import modelling.AnalyseModul;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author shaesler
 */
public class EpidShiftAnalyse extends AnalyseModul {

    private Mat srcMat = null;
    private final Mat thresholdMat = new Mat();
    private final int imageWidth;
    private final int imageHeight;
    private final DICOM dicom;
    private final int threshold = (int) (256 * .8);
    private final double minDistance = 100;
    private final EpidShiftResult returnResult;

    private final double thresholdCanny1 = 255 * .125;
    private final double thresholdCanny2 = 255 * .5;

    public EpidShiftAnalyse(DICOM dicom) {
        this.dicom = dicom;
        this.srcMat = Helper.image2Mat(this.dicom.getImage());
        this.imageWidth = this.dicom.getImageWidth();
        this.imageHeight = this.dicom.getImageHeight();
        returnResult = new EpidShiftResult(this.imageWidth, this.imageHeight, this.dicom.getSID());
        this.returnResult.getDetails().put("SID", this.dicom.getTagTable().get("3002,0026").getValue(true));
        String[] pixelSize = this.dicom.getTagTable().get("3002,0011").getValue(true).toString().split("\\u005C");
        double[] pixel = {Double.parseDouble(pixelSize[0]), Double.parseDouble(pixelSize[1])};
        this.returnResult.setPixelSize(pixel);
    }

    @Override
    public EpidShiftResult call() {
        Mat mat = this.srcMat.clone();

        /// Parameters for Shi-Tomasi algorithm
        int maxCorners = Math.max(23, 1);
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.001;

        returnResult.setOriginal(this.dicom.getImage());

        double minDistance = 10;

        /// Copy the source image
        Mat copy = this.srcMat.clone();
        Mat cannyMat = new Mat();

        ///Prepare image for edge detection
        Imgproc.GaussianBlur(copy, copy, new Size(3, 3), 2, 2);
        Imgproc.threshold(copy, thresholdMat, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        returnResult.setThresholdImage(Helper.mat2Image(thresholdMat));
        Imgproc.Canny(thresholdMat, cannyMat, thresholdCanny1, thresholdCanny2);
        returnResult.setCannyImage(Helper.mat2Image(cannyMat));

        /// Apply corner detection
        Imgproc.goodFeaturesToTrack(cannyMat, corners, maxCorners, qualityLevel, minDistance);

        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);

        MatOfPoint2f matCorners = new MatOfPoint2f();
        matCorners.fromList(corners.toList());

        /// Set the needed parameters to find the refined corners
        Size winSize = new Size(15, 15);
        Size zeroZone = new Size(-1, -1);
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 40, 0.001);

        /// Calculate the refined corner locations        
        Imgproc.cornerSubPix(cannyMat, matCorners, winSize, zeroZone, criteria);
        List<Point> points = matCorners.toList();

        List<MatOfPoint> rectAngle = new ArrayList<>();

        Imgproc.findContours(cannyMat, rectAngle, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_TC89_KCOS);

        double maxArea = 0;
        int maxContur = 0;
        for (int i = 0; i < rectAngle.size(); i++) {
            if (maxArea < Imgproc.contourArea(rectAngle.get(i))) {
                maxArea = Imgproc.contourArea(rectAngle.get(i));
                maxContur = i;
            }
        }
        Rect rect = Imgproc.boundingRect(rectAngle.get(maxContur));

        double x1 = rect.tl().x;
        double y1 = rect.tl().y;
        double x2 = rect.br().x;
        double y2 = rect.br().y;

        Point leftUpper = null;
        Point leftBottom = null;
        Point rightUpper = null;
        Point rightBottom = null;

        double minDistLeftUpper = Double.MAX_VALUE;
        double minDistRightBottom = Double.MAX_VALUE;
        double minDistRightUpper = Double.MAX_VALUE;
        double minDistLeftBottom = Double.MAX_VALUE;

        for (Point point : points) {
            if (minDistLeftUpper > calDis(new Point(x1, y1), point)) {
                minDistLeftUpper = calDis(new Point(x1, y1), point);
                leftUpper = point;
            }
            if (minDistRightBottom > calDis(new Point(x2, y2), point)) {
                minDistRightBottom = calDis(new Point(x2, y2), point);
                rightBottom = point;
            }
            if (minDistRightUpper > calDis(new Point(x2, y1), point)) {
                minDistRightUpper = calDis(new Point(x2, y1), point);
                rightUpper = point;
            }
            if (minDistLeftBottom > calDis(new Point(x1, y2), point)) {
                minDistLeftBottom = calDis(new Point(x1, y2), point);
                leftBottom = point;
            }
        }

        returnResult.setLeftUpper(leftUpper);
        returnResult.setRightBottom(rightBottom);
        returnResult.setLeftBottom(leftBottom);
        returnResult.setRightUpper(rightUpper);
        this.returnResult.getDetails().put("fieldWidth", this.returnResult.getFieldWidth());
        this.returnResult.getDetails().put("fieldHeight", this.returnResult.getFieldHeight());
        return returnResult;
    }

    public static boolean isFoundCorner(Point point, Point corner, int tolerance) {
        return isFoundCorner(point.x, point.y, corner, tolerance);
    }

    public static boolean isFoundCorner(double x, double y, Point corner, int tolerance) {
        boolean returnValue = false;

        double xCorner = corner.x;
        double yCorner = corner.y;
        returnValue = (xCorner - tolerance < x && x < xCorner + tolerance) && (yCorner - tolerance < y && y < yCorner + tolerance);
        return returnValue;
    }

    public static double calDis(Point p1, Point p2) {
        return calDis(p1.x, p1.y, p2.x, p2.y);
    }

    public static double calDis(double x1, double y1, double x2, double y2) {
        return (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
    }

    public static BufferedImage mat2BufferedImage(Mat in) {
        BufferedImage out;
        int cols = in.cols();
        int rows = in.rows();
        byte[] data = new byte[cols * rows * (int) in.elemSize()];
        int type;
        in.get(0, 0, data);

        if (in.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        out = new BufferedImage(cols, rows, type);

        out.getRaster().setDataElements(0, 0, cols, rows, data);
        return out;
    }
}
