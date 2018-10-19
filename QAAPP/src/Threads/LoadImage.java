/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;


import DICOM.DICOM;
import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author shaesler
 */
public class LoadImage extends Service<ObservableList<DICOM>> {

    List<File> files;
    ObservableList<DICOM> dicoms = FXCollections.observableArrayList();

    public LoadImage(List<File> files) {
        this.files = files;
    }

    @Override
    protected Task<ObservableList<DICOM>> createTask() {
        return new Task<ObservableList<DICOM>>() {
           @Override
            protected ObservableList<DICOM> call() throws Exception {
                final int size = files.size();
                for (int i = 0; i < files.size(); i++) {
                    File selectedFile = files.get(i);
                    updateProgress(i, size);
                    DICOM dcmTemp = new DICOM();
                    dcmTemp.loadDicomObject(selectedFile);
                    dicoms.add(dcmTemp);
                }
                return dicoms;
            }
        };

    }
}
