/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.view.controller;

import DICOM.DICOM;
import Threads.ImageProcess;
import Threads.LoadImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.controlsfx.control.RangeSlider;

/**
 *
 * @author shaesler
 */
public class MainViewController implements Initializable {

    @FXML
    private Button loadImages;

    @FXML
    private ListView<DICOM> listImages;

    @FXML
    private ImageView viewImage;

    @FXML
    private RangeSlider windowing;

    boolean displayImage = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listImages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            try {
                windowing.setDisable(false);
                displayImage = false;
                DICOM temp = listImages.getSelectionModel().getSelectedItem();
                double minValue = temp.getWindowCenter() - (temp.getWindowWidth() / 2);
                double maxValue = temp.getWindowCenter() + (temp.getWindowWidth() / 2);
                windowing.setMin(temp.getMinHU());
                windowing.setMax(temp.getMaxHU());
                windowing.setLowValue(minValue);
                windowing.setHighValue(maxValue);
                viewImage.setImage(SwingFXUtils.toFXImage(temp.getBufferedImage(), null));
                displayImage = true;
                ImageProcess process = new ImageProcess(temp.getPixelData());
                process.start();
            } catch (IOException ex) {
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        windowing.lowValueProperty().addListener((Observable o) -> {
            try {

                if (displayImage) {
                    double windowWidth = windowing.getLowValue() - windowing.getHighValue();
                    double windowCenter = windowWidth / 2;
                    DICOM temp = listImages.getSelectionModel().getSelectedItem();
                    temp.setWindowWidth(windowWidth);
                    temp.setWindowCenter(windowCenter);
                    viewImage.setImage(SwingFXUtils.toFXImage(temp.getBufferedImage(), null));
                }
            } catch (IOException ex) {
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        windowing.highValueProperty().addListener(o -> {
            try {
                if (displayImage) {
                    double windowWidth = windowing.getLowValue() - windowing.getHighValue();
                    double windowCenter = windowWidth / 2;
                    DICOM temp = listImages.getSelectionModel().getSelectedItem();
                    temp.setWindowWidth(windowWidth);
                    temp.setWindowCenter(windowCenter);
                    viewImage.setImage(SwingFXUtils.toFXImage(temp.getBufferedImage(), null));
                }
            } catch (IOException ex) {
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    public void loadImage(ActionEvent event) {
        Node node = (Node) event.getSource();
        FileChooser loadImage = new FileChooser();
        List<File> files = loadImage.showOpenMultipleDialog(node.getScene().getWindow());
        if (!files.isEmpty()) {
            ObservableList filess = FXCollections.observableArrayList();
            filess.addAll(files);
            LoadImage loadTask = new LoadImage(files);
            loadTask.setOnSucceeded((WorkerStateEvent event1) -> {
                listImages.setItems(loadTask.getValue());
            });
            loadTask.start();
        }
    }

}
