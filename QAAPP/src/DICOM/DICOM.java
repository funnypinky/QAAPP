/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DICOM;

import RawDCMLibary.DICOM.DICOMFile;
import RawDCMLibary.model.FileDicomTagTable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author shaesler
 */
public class DICOM {

    private StringProperty name = new SimpleStringProperty();
    private int frame = 1;
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private double windowCenter;
    private double windowDefaultCenter;
    private double windowWidth;
    private double windowDefaultWidth;
    private boolean autoWindowing = false;
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;
    private ArrayList<String> details = new ArrayList();

    private File dicomFile;

    private DICOMFile rf;

    private FileDicomTagTable tagTable;
    private double rescaleInterecpt;
    private double rescaleSlope;

    public DICOM(File dicomFile) {
        this.dicomFile = dicomFile;
    }

    public DICOM() {

    }

    public FileDicomTagTable loadDicomObject(File f) throws IOException {
        if (f != null) {
            this.dicomFile = f;
            DICOMFile rf = new DICOMFile(f.getAbsolutePath());
            this.rf = rf;
            this.rf.readHeader();
            this.tagTable = this.rf.getTagTable();
            this.windowDefaultWidth = Short.valueOf(this.tagTable.getValue("0028,1051") != null ? this.tagTable.getValue("0028,1051").toString() : "0");
            this.windowWidth = this.windowDefaultWidth;
            this.windowDefaultCenter = Short.valueOf(this.tagTable.getValue("0028,1050") != null ? this.tagTable.getValue("0028,1050").toString() : "0");
            this.windowCenter = this.windowDefaultCenter;
            this.rescaleInterecpt = Double.parseDouble(this.tagTable.getValue("0028,1052").toString());
            this.rescaleSlope = Double.parseDouble(this.tagTable.getValue("0028,1053").toString());
            details.add("Bildname: " + this.tagTable.getValue("3002,0002"));
            details.add("SID: " + this.tagTable.getValue("3002,0026"));
            this.name.set(this.tagTable.getValue("3002,0002").toString());
            return this.tagTable;

        } else {
            return null;
        }
    }

    public int[] getPixelData() {
        return this.rf.getPixelData();
    }

    public Image getImage() {
        return SwingFXUtils.toFXImage(rf.getBufferedImage((int)this.windowCenter, (int)this.windowWidth),null);
    }

    public BufferedImage getDefaultBufferedImage() throws IOException {
        return rf.getDefaultBufferedImage();
    }

    public double getWindowWidth() {
        return this.windowWidth;
    }

    public double getMaxWindowWidth() {
        return Math.pow(2, Double.parseDouble(this.tagTable.getValue("0028,0101").toString()));
    }

    public void setWindowWidth(double windowWidth) {
        this.windowWidth = windowWidth;
    }

    public StringProperty nameProperty() {

        return this.name;
    }

    @Override
    public String toString() {
        return this.name.get();
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
        return HU - rescaleInterecpt / rescaleSlope;
    }

    /**
     * Return the HU of a point with pixelvalue.
     *
     * @param pixel
     * @return HU - Hounsfield unit
     */
    public double pxToHu(double pixel) {
        return (this.rescaleSlope * pixel + this.rescaleInterecpt);
    }

    public double getMinHU() {
        return pxToHu(min(this.getPixelData()));
    }

    public double getMaxHU() {
        return pxToHu(max(this.getPixelData()));
    }

    public double getWindowCenter() {
        return this.windowCenter;
    }

    public void setWindowCenter(double windowCenter) {
        this.windowCenter = windowCenter;
    }

    public int getImageHeight() {
        return Integer.parseInt(this.tagTable.getValue("0028,0010").toString());
    }

    public int getImageWidth() {
        return Integer.parseInt(this.tagTable.getValue("0028,0011").toString());
    }

    public ObservableList<String> getDetails() {
        return FXCollections.observableArrayList(details);
    }

    public FileDicomTagTable getTagTable() {
        return tagTable;
    }
    public double getSID(){
        return Double.parseDouble(this.tagTable.getValue("3002,0026").toString());
    }
}
