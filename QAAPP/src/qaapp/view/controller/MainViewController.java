/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.view.controller;

import DICOM.DICOM;
import ImageHelper.ImageProcessing;
import java.beans.PropertyChangeEvent;
import qaapp.Threads.LoadImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import qaapp.Data.DataBean;

/**
 *
 * @author shaesler
 */
public class MainViewController implements Initializable {

    @FXML
    private MenuItem openFolder;

    @FXML
    private ListView<DICOM> listImages;

    @FXML
    private Canvas dicomView;

    @FXML
    private ListView<String> listDetails;

    @FXML
    private Slider windowCenter;

    @FXML
    private Slider windowWidth;

    @FXML
    private Button openBt;

    @FXML
    private TabPane tabPane;

    private GraphicsContext gc;

    private boolean displayImage = false;

    private DataBean bean;

    private Tab tab;

    private final Preferences prefs = Preferences.userRoot();
    private final String LAST_OPEN_PATH = "lastOpenPath";

    private final String propertyValue = prefs.get(LAST_OPEN_PATH, null);

    public MainViewController(DataBean bean, Tab tab) {
        this.bean = bean;
        this.tab = tab;
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        System.exit(0); //normally Exit  
    }

    @FXML
    public void openFolder(ActionEvent event) {
        FileChooser loadImage = new FileChooser();
        if (propertyValue != null) {
            loadImage.setInitialDirectory(new File(propertyValue));
        }
        List<File> files = loadImage.showOpenMultipleDialog(null);
        if (files != null && !files.isEmpty()) {
            prefs.put(LAST_OPEN_PATH, files.get(0).getAbsoluteFile().getParent());
            ObservableList filess = FXCollections.observableArrayList();
            filess.addAll(files);
            LoadImage loadTask = new LoadImage(files);
            loadTask.setOnSucceeded((WorkerStateEvent event1) -> {
                bean.setImages(loadTask.getValue());
            });
            loadTask.start();
        }
        stackImages(this.gc);

    }

    public void stackImages(GraphicsContext gc) {
        gc.clearRect(0, 0, dicomView.getWidth(), dicomView.getHeight());
        List<DICOM> temp = listImages.getSelectionModel().getSelectedItems();
        if (!temp.isEmpty()) {
            for (int i = 0; i < temp.size(); i++) {
                try {
                    gc.setGlobalAlpha(.9);
                    Image tempImage = ImageProcessing.makeTransparent(SwingFXUtils.toFXImage(temp.get(i).getDefaultBufferedImage(), null));
                    gc.drawImage(tempImage, 0, 0);
                } catch (IOException ex) {
                    Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.tabPane.getTabs().add(this.tab);
        initListImage();
        initSliders();
    }

    private void initListImage() {
        this.gc = dicomView.getGraphicsContext2D();
        this.bean.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            listImages.setItems((ObservableList) evt.getNewValue());
        });
        listImages.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listImages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && listImages.getSelectionModel().getSelectedItems().size() == 1) {
                DICOM temp = listImages.getSelectionModel().getSelectedItem();
                windowCenter.setMin(temp.getMinHU());
                windowCenter.setMax(temp.getMaxHU());
                windowCenter.setValue(temp.getWindowCenter());
                windowWidth.setMin(1);
                windowWidth.setMax(Math.abs(temp.getMinHU()) + Math.abs(temp.getMaxHU()));
                windowWidth.setValue(temp.getWindowWidth());
                windowCenter.setVisible(true);
                windowWidth.setVisible(true);
                paintImage(temp);
                this.displayImage = true;
                this.listDetails.setItems(temp.getDetails());
                this.bean.setSelectedIndex(listImages.getSelectionModel().getSelectedIndex());
            } else if (listImages.getSelectionModel().getSelectedItems().size() > 1) {
                windowCenter.setVisible(false);
                windowWidth.setVisible(false);
                this.listDetails.getItems().clear();
                stackImages(gc);
            }
            if (newSelection == null) {
                windowCenter.setVisible(false);
                windowWidth.setVisible(false);
                this.displayImage = false;
                this.listDetails.getItems().clear();
            }
        });
    }

    private void initSliders() {
        windowCenter.valueProperty().addListener((Observable o) -> {
            if (this.displayImage) {
                DICOM temp = listImages.getSelectionModel().getSelectedItem();
                temp.setWindowCenter(windowCenter.getValue());
                paintImage(temp);
            }
        });
        windowWidth.valueProperty().addListener((Observable o) -> {
            if (this.displayImage) {
                DICOM temp = listImages.getSelectionModel().getSelectedItem();
                temp.setWindowWidth(windowWidth.getValue());
                paintImage(temp);
            }
        });
    }

    private void paintImage(DICOM dcmObj) {
        gc.clearRect(0, 0, dicomView.getWidth(), dicomView.getHeight());
        gc.drawImage(dcmObj.getImage(), 0, 0);
        gc.setGlobalAlpha(1);
    }
}
