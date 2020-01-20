/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp.view.controller;

import DICOM.DICOM;
import ImageHelper.ImageProcessing;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import qaapp.Data.DataBean;
import qaapp.Threads.Analyse.EPIDShift.EpidShiftAnalyse;
import qaapp.Threads.Analyse.EPIDShift.EpidShiftResult;

/**
 * FXML Controller class
 *
 * @author shaesler
 */
public class ShiftViewController implements Initializable {

    @FXML
    protected ListView listImages;

    @FXML
    private Canvas dicomView;

    @FXML
    private ListView listDetails;

    @FXML
    private Slider windowCenter;

    @FXML
    private Slider windowWidth;

    private Slider thresholdSlider = new Slider(0, 100, 80);

    private Label sliderValue = new Label();

    private DataBean bean;

    private HashMap<DICOM, EpidShiftResult> listOfResults = new HashMap<>();

    private final Stage dialog = new Stage();

    private double startPx = 0;

    @FXML
    private ToggleGroup viewMode;

    @FXML
    private ToggleButton orgin;

    @FXML
    private ToggleButton canny;

    @FXML
    private ToggleButton result;

    @FXML
    private ToggleButton thresholdBT;
    private EpidShiftResult reference;

    public ShiftViewController(DataBean bean) {
        this.bean = bean;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(null);
        initMouseEvent();
        initToggleButton();
        this.bean.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            listImages.setItems((ObservableList) evt.getNewValue());
            listImages.getSelectionModel().select(this.bean.getSelectedIndex());
            process();
        });
        this.thresholdSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            sliderValue.setText(String.format("%.2f", new_val));
        });
        this.viewMode.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                stackImages(dicomView.getGraphicsContext2D());
            }

        });
        listImages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            stackImages(dicomView.getGraphicsContext2D());
            writeDetails(listOfResults.get(newSelection));
        });
    }

    private void writeDetails(EpidShiftResult result) {
        this.listDetails.getItems().clear();
        this.listDetails.getItems().add("SID [mm]: " + result.getDetails().get("SID"));
        this.listDetails.getItems().add(String.format("gemessene x-Feldbreite [mm]: %1.3f", result.getDetails().get("fieldWidth")));
        this.listDetails.getItems().add(String.format("gemessene y-Feldbreite [mm]: %1.3f", result.getDetails().get("fieldHeight")));
        this.listDetails.getItems().add(String.format("gemessener Abstand [mm]: %1.3f", result.getDetails().get("calSID")));
    }

    public void stackImages(GraphicsContext gc) {
        DICOM item = (DICOM) this.listImages.getSelectionModel().getSelectedItem();
        gc.clearRect(0, 0, dicomView.getWidth(), dicomView.getHeight());
        Image tempImage = null;
        gc.setGlobalAlpha(.9);
        VIEWMODE switchCat = (VIEWMODE) viewMode.getSelectedToggle().getUserData();
        if (switchCat != null && item != null) {
            switch (switchCat) {
                case ORIGINAL:
                    try {
                    tempImage = SwingFXUtils.toFXImage(item.getDefaultBufferedImage(), null);
                } catch (IOException ex) {
                    Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
                case CANNY:
                    tempImage = listOfResults.get(item).getCannyImage();
                    break;
                case THRESHOLD:
                    tempImage = listOfResults.get(item).getThresholdImage();
                    break;
                case RESULT:
                    tempImage = listOfResults.get(item).cornerImage();
                    break;
            }
            gc.drawImage(tempImage, 0, 0);
        }
    }

    private boolean isButtonToggle(ToggleGroup group) {
        return group.getToggles().stream().anyMatch((toggle) -> (toggle.isSelected()));
    }

    private void process() {
        bean.getImages().forEach((item) -> {
            EpidShiftAnalyse analyseItem = new EpidShiftAnalyse(item);
            listOfResults.put(item, analyseItem.call());
        });
        listOfResults.keySet().forEach(dicom -> {
            if (dicom.toString().contains("Referenz")) {
                reference = listOfResults.get(dicom);
            }
        });
        listOfResults.values().forEach(tmp -> {
            tmp.setReference(reference);
            tmp.getDetails().put("calSID", tmp.calculateSID());
        });
    }

    private void initToggleButton() {
        this.orgin.setUserData(VIEWMODE.ORIGINAL);
        this.canny.setUserData(VIEWMODE.CANNY);
        this.result.setUserData(VIEWMODE.RESULT);
        this.thresholdBT.setUserData(VIEWMODE.THRESHOLD);
    }

    private void initMouseEvent() {
        dicomView.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                VBox dialogVbox = new VBox();
                dialogVbox.getChildren().addAll(thresholdSlider, this.sliderValue);
                Scene dialogScene = new Scene(dialogVbox);
                dialog.setScene(dialogScene);
                dialog.setX(event.getScreenX());
                dialog.setY(event.getScreenY());
                dialog.show();
                startPx = event.getX();
            }
            event.consume();
        });

        dicomView.addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                dialog.close();
            }
            event.consume();
        });

        dicomView.addEventFilter(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                thresholdSlider.setValue(thresholdSlider.getValue() + (event.getX() - startPx) * .3);
            }
            event.consume();
        });

    }

    public enum VIEWMODE {
        ORIGINAL, CANNY, RESULT, THRESHOLD;
    }
}
