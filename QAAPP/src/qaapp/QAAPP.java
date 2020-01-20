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
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static org.opencv.core.Core.NATIVE_LIBRARY_NAME;
import qaapp.Data.DataBean;
import qaapp.view.controller.MainViewController;
import qaapp.view.controller.ShiftViewController;

/**
 *
 * @author shaesler
 */
public class QAAPP extends Application {

    private Stage stage;

    private VBox rootLayout;

    private DataBean bean = new DataBean();

    @Override
    public void init() {
        loadLibrary(NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        bean.setPrimaryStage(this.stage);
        initRootPane();

        this.stage.setMinHeight(900.0);
        this.stage.setMinWidth(1200.0);
        this.stage.setTitle("QA Application");
        //this.stage.getIcons().add(new Image(this.getClass().getResourceAsStream("view/images/favicon.png")));
        this.stage.show();

    }

    private void initRootPane() {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(c -> {
                return new MainViewController(bean, loadTab());
            });
            loader.setLocation(this.getClass().getResource("view/mainview.fxml"));

            rootLayout = (VBox) loader.load();
            Scene scene = new Scene(rootLayout);
            stage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(QAAPP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Tab loadTab() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(c -> {
                return new ShiftViewController(bean);
            });
            loader.setLocation(this.getClass().getResource("view/shiftView.fxml"));
            Tab temp = new Tab("Shift");
            temp.setContent(loader.load());
            return temp;
        } catch (IOException ex) {
            Logger.getLogger(QAAPP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
