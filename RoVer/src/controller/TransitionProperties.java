package controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import model.GroupTransition;
import study.BugTracker;

import java.io.File;
import java.io.FileInputStream;


public class TransitionProperties extends VBox {

    private GroupTransition groupTransition;
    private BugTracker bugTracker;

    private boolean refresh;

    private Image readyImage, busyImage, suspendedImage;
    private ImageView readyImageView, busyImageView, suspendedImageView;
    private CheckBox readyCheckBox, busyCheckBox, suspendedCheckBox;

    public TransitionProperties(GroupTransition groupTransition, BugTracker bugTracker) {

        this.groupTransition = groupTransition;
        this.bugTracker = bugTracker;

        this.setPadding(new Insets(10));
        this.setSpacing(8);

        String path = "Icons" + File.separator;

        refresh = false;

        readyCheckBox = new CheckBox();
        readyCheckBox.setSelected(false);
        busyCheckBox = new CheckBox();
        busyCheckBox.setSelected(false);
        suspendedCheckBox = new CheckBox();
        suspendedCheckBox.setSelected(false);

        try {
            // add the images
            readyImage = new Image(new FileInputStream(path + "Icon_HumReady.png"));
            readyImageView = new ImageView(readyImage);
            readyImageView.setPreserveRatio(true);
            readyImageView.setFitHeight(40);

            busyImage = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
            busyImageView = new ImageView(busyImage);
            busyImageView.setPreserveRatio(true);
            busyImageView.setFitHeight(40);

            suspendedImage = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
            suspendedImageView = new ImageView(suspendedImage);
            suspendedImageView.setPreserveRatio(true);
            suspendedImageView.setFitHeight(40);

        } catch (Exception e) {
            System.out.println("Error: starter indicator images did not load.");
        }

        HBox readyHBox = new HBox();
        readyHBox.getChildren().addAll(readyImageView, readyCheckBox);
        readyHBox.setSpacing(10);

        HBox busyHBox = new HBox();
        busyHBox.getChildren().addAll(busyImageView, busyCheckBox);
        busyHBox.setSpacing(10);

        HBox suspendedHBox = new HBox();
        suspendedHBox.getChildren().addAll(suspendedImageView, suspendedCheckBox);
        suspendedHBox.setSpacing(10);

        this.getChildren().addAll(readyHBox, busyHBox, suspendedHBox);

    }

    public boolean isChanged() {

        boolean[] conditionals = groupTransition.getHumanBranching();

        if (conditionals[0])
            readyCheckBox.setSelected(true);
        if (conditionals[1])
            busyCheckBox.setSelected(true);
        if (conditionals[2])
            suspendedCheckBox.setSelected(true);

        readyCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //readyCheckBox.setSelected(!newValue);
                refresh = true;
                conditionals[0] = readyCheckBox.isSelected();
            }
        });

        busyCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //busyCheckBox.setSelected(!newValue);
                refresh = true;
                conditionals[1] = busyCheckBox.isSelected();
            }
        });

        suspendedCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //suspendedCheckBox.setSelected(!newValue);
                refresh = true;
                conditionals[2] = suspendedCheckBox.isSelected();
            }
        });

        groupTransition.getConditions().update(conditionals);

        return refresh;
    }
}
