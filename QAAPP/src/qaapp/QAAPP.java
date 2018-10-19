/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qaapp;

import java.io.IOException;
import static java.lang.System.loadLibrary;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static org.opencv.core.Core.NATIVE_LIBRARY_NAME;

/**
 *
 * @author shaesler
 */
public class QAAPP extends Application {

    private Stage stage;

    private VBox rootLayout;

    @Override
    public void init() {
        loadLibrary(NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initRootPane();

        this.stage.setMinHeight(900.0);
        this.stage.setMinWidth(1200.0);
        //this.stage.setTitle("Hyperthermie Patient Datenbank");
        //this.stage.getIcons().add(new Image(this.getClass().getResourceAsStream("view/images/favicon.png")));
        this.stage.show();

    }

    private void initRootPane() {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/mainView.fxml"));
            rootLayout = (VBox) loader.load();
            Scene scene = new Scene(rootLayout);
            stage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(QAAPP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
