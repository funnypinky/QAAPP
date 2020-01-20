/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.Data;

import DICOM.DICOM;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 *
 * @author shaesler
 */
public class DataBean {

    private ObservableList<DICOM> images = FXCollections.emptyObservableList();

    private int selectedIndex = -1;
    
    private PropertyChangeSupport changes = new PropertyChangeSupport(images);
    
    private PropertyChangeSupport valueIndex = new PropertyChangeSupport(selectedIndex);
    
    private Stage primaryStage;

    public DataBean() {

    }

    public ObservableList<DICOM> getImages() {
        return images;
    }

    public void setImages(ObservableList<DICOM> images) {
        ObservableList oldValue = this.images;
        this.images = images;
        changes.firePropertyChange("images", oldValue, images);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
    
    public void addSelectedChangeListener (PropertyChangeListener l) {
        valueIndex.addPropertyChangeListener(l);
    }
    
    public void removeSelectedChangeListener(PropertyChangeListener l) {
        valueIndex.removePropertyChangeListener(l);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        int oldValue = this.selectedIndex;
        this.selectedIndex = selectedIndex;
        valueIndex.firePropertyChange("selectedIndex", oldValue, selectedIndex);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
}
