/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.Threads.Analyse.EPIDShift;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import modelling.Result;
import org.opencv.core.Point;

/**
 *
 * @author shaesler
 */
public class EpidShiftResult extends Result {

    public Point leftUpper = new Point();
    public Point rightUpper = new Point();
    public Point leftBottom = new Point();
    public Point rightBottom = new Point();

    private final int imageWidth;
    private final int imageHeight;
    private Point center;
    private double centerDev;
    private final double sid;

    private double[] pixelSize;

    private Image cannyImage;

    private Image thresholdImage;

    private Image original;

    private EpidShiftResult reference;

    public EpidShiftResult(int imageWidth, int imageHeight, double sid) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.sid = sid;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getFieldWidth() {
        double a1 = Math.abs(rightUpper.x - leftUpper.x);
        double a2 = Math.abs(rightBottom.x - leftBottom.x);
        return (a1 + a2) / 2 * this.pixelSize[0];
    }

    public double getFieldHeight() {
        double a1 = Math.abs(leftBottom.y - leftUpper.y);
        double a2 = Math.abs(rightBottom.y - rightUpper.y);
        return (a1 + a2) / 2 * this.pixelSize[1];
    }

    public double calculateSID() {
        if (reference == null) {
            return -1;
        }
        double a1 = this.getFieldWidth() / reference.getFieldWidth();
        double a2 = this.getFieldHeight() / reference.getFieldHeight();
        return (a1 + a2) / 2 * 1000;
    }

    public Point getCenter() {
        return center;
    }

    public double getCenterDev() {
        return centerDev;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public Image cornerImage() {
        BufferedImage bImage = SwingFXUtils.fromFXImage(this.original, null);

        Graphics2D g2d = (Graphics2D) bImage.getGraphics();
        g2d.setColor(Color.YELLOW);
        GeneralPath path = new GeneralPath();
        path.moveTo(leftUpper.x, leftUpper.y);
        path.lineTo(rightUpper.x, rightUpper.y);
        path.lineTo(rightBottom.x, rightBottom.y);
        path.lineTo(leftBottom.x, leftBottom.y);
        path.lineTo(leftUpper.x, leftUpper.y);
        path.closePath();
        g2d.draw(path);
        return SwingFXUtils.toFXImage(bImage, null);
    }

    public Image getCannyImage() {
        return cannyImage;
    }

    public void setCannyImage(Image cannyImage) {
        this.cannyImage = cannyImage;
    }

    public Image getThresholdImage() {
        return thresholdImage;
    }

    public void setThresholdImage(Image thresholdImage) {
        this.thresholdImage = thresholdImage;
    }

    public Image getOriginal() {
        return original;
    }

    public void setOriginal(Image original) {
        this.original = original;
    }

    public Point getLeftUpper() {
        return leftUpper;
    }

    public void setLeftUpper(Point leftUpper) {
        this.leftUpper = leftUpper;
    }

    public Point getRightUpper() {
        return rightUpper;
    }

    public void setRightUpper(Point rightUpper) {
        this.rightUpper = rightUpper;
    }

    public Point getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(Point leftBottom) {
        this.leftBottom = leftBottom;
    }

    public Point getRightBottom() {
        return rightBottom;
    }

    public void setRightBottom(Point rightBottom) {
        this.rightBottom = rightBottom;
    }

    public double[] getPixelSize() {
        return pixelSize;
    }

    public void setPixelSize(double[] pixelSize) {
        this.pixelSize = pixelSize;
    }

    public double getSid() {
        return sid;
    }

    public EpidShiftResult getReference() {
        return reference;
    }

    public void setReference(EpidShiftResult reference) {
        this.reference = reference;
    }

}
