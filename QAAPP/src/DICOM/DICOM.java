/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DICOM;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import static java.lang.Double.NaN;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;

/**
 *
 * @author shaesler
 */
public class DICOM {

    private int frame = 1;
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private double windowCenter;
    private double windowDefaultCenter;
    private double windowWidth;
    private double windowDefaultWidth;
    private boolean autoWindowing = false;
    private Attributes prState;
    private final ImageReader imageReader
            = ImageIO.getImageReadersByFormatName("DICOM").next();
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;

    private File dicomFile;

    private Attributes attributes;

    public DICOM(File dicomFile) {
        this.dicomFile = dicomFile;
    }

    public DICOM() {

    }

    public Attributes loadDicomObject(File f) throws IOException {
        if (f != null) {
            this.dicomFile = f;
            DicomInputStream dis = new DicomInputStream(f);
            try {
                this.attributes = dis.readDataset(-1, -1);
                this.windowDefaultWidth = attributes.getDouble(Tag.WindowWidth, 2500);
                this.windowWidth = this.windowDefaultWidth;
                this.windowDefaultCenter = attributes.getDouble(Tag.WindowCenter, 1250);
                this.windowCenter = this.windowDefaultCenter;
                return this.attributes;
            } finally {
                SafeClose.close(dis);
            }
        } else {
            return null;
        }
    }

    public int[] getPixelData() {
        return this.attributes.getInts(Tag.PixelData);
    }
    public BufferedImage getBufferedImage() throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(this.dicomFile);
        BufferedImage bi = readImage(iis);
        bi = convert(bi);
        return bi;
    }

    private BufferedImage convert(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        return cm.getNumComponents() == 3 ? BufferedImageUtils.convertToIntRGB(bi) : bi;
    }

    private BufferedImage readImage(ImageInputStream iis) throws IOException {
        imageReader.setInput(iis);
        return imageReader.read(frame - 1, readParam());
    }

    private ImageReadParam readParam() {
        DicomImageReadParam param
                = (DicomImageReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter((float) (windowCenter != NaN ? windowCenter : windowDefaultCenter));
        param.setWindowWidth((float) (windowWidth != NaN ? windowWidth : windowDefaultWidth));
        param.setAutoWindowing(autoWindowing);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setPresentationState(prState);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        return param;
    }

    public double getWindowWidth() {
        return this.windowWidth;
    }

    public double getMaxWindowWidth() {
        return Math.pow(2, Float.parseFloat(this.attributes.getString(Tag.BitsStored)));
    }

    public void setWindowWidth(double windowWidth) {
        this.windowWidth = windowWidth;
    }

    @Override
    public String toString() {
        return this.attributes.getString(Tag.RTImageLabel);
    }

    /**
     * <p>
     * Returns the maximum value in an array.</p>
     *
     * @param array an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is
     * <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public int max(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    public int min(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * Return the pixelvalue of a point with HU.
     *
     * @param HU - Hounsfield unit
     * @return pixel
     */
    public double huToPx(double HU) {
        return (HU - this.attributes.getDouble(Tag.RescaleIntercept, 1)) / this.attributes.getDouble(Tag.RescaleSlope, 1);
    }

    /**
     * Return the HU of a point with pixelvalue.
     *
     * @param pixel
     * @return HU - Hounsfield unit
     */
    public double pxToHu(double pixel) {
        return (this.attributes.getDouble(Tag.RescaleSlope, 1) * pixel + this.attributes.getDouble(Tag.RescaleIntercept, 1));
    }
    
    public double getMinHU(){
        return pxToHu(min(this.attributes.getInts(Tag.PixelData)));
    }
    
    public double getMaxHU(){
        return pxToHu(max(this.attributes.getInts(Tag.PixelData)));
    }
    
    public double getWindowCenter() {
        return this.windowCenter;
    }

    public void setWindowCenter(double windowCenter) {
        this.windowCenter = windowCenter;
    }
}

