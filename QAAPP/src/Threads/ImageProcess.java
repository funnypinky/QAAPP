/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import modelling.Result;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author shaesler
 */
public class ImageProcess extends Service<Result> {

    private final int[] pixelData;
    private final Mat srcMat;

    public ImageProcess(int[] pixelData) {
        this.pixelData = pixelData;
        srcMat = new Mat(new Size(1024, 768), CvType.CV_32SC1);

        srcMat.put(0, 0, this.pixelData);
    }


    @Override
    protected Task<Result> createTask() {
        int maxCorners = 10;

        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;

        /// Copy the source image
        Mat copy;
        Mat newMat = new Mat();
        srcMat.convertTo(newMat, CvType.CV_32FC1);
        copy = srcMat.clone();

        /// Apply corner detection
        Imgproc.goodFeaturesToTrack(newMat, corners, maxCorners, qualityLevel, minDistance, new Mat(), blockSize, useHarrisDetector, k);

        /// Draw corners detected
        System.out.println("** Number of corners detected: " + corners.size());
        int r = 4;
        Point[] cornerpoints = corners.toArray();

        for (Point point : cornerpoints){
            Imgproc.circle(copy, point, r, new Scalar(111), 1);
        }

        /// Show what you got
        
        

        /// Set the neeed parameters to find the refined corners
        Size winSize = new Size(5, 5);
        Size zeroZone = new Size(-1, -1);
        TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);

        /// Calculate the refined corner locations
        MatOfPoint2f cornerSub = new MatOfPoint2f(cornerpoints);
        Imgproc.cornerSubPix(newMat, cornerSub, winSize, zeroZone, term);
        Point[] subCorner = cornerSub.toArray();
        for (Point point : subCorner){
            Imgproc.circle(copy, point, r, new Scalar(333), 1);
        }
        Imgcodecs.imwrite("C:\\temp\\result.jpg",copy);
        return null;
    }

}
