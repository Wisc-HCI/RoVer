package controller;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import model.Group;


public class GroupProperties extends VBox {

    private Group group;
    private boolean refreshed;

    private Label nameLabel;
    private TextField nameTextField;
    private HBox nameHBox;

    public GroupProperties(Group group) {

        this.group = group;

        this.setPadding(new Insets(10));
        this.setSpacing(8);

        nameLabel = new Label("Name:");
        nameTextField = new TextField();
        nameTextField.setText(group.getName());
        nameHBox = new HBox();
        nameHBox.getChildren().addAll(nameLabel, nameTextField);
        nameHBox.setSpacing(10);

        this.getChildren().addAll(nameHBox);
    }

    public void run() {

        nameTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {

                // parse name
                String txt = nameTextField.getText();
                txt = txt.replaceAll("\\s+","_");
                if (txt.length() > 20)
                    txt = txt.substring(0, 16);
                group.setName(txt);

            } else if (e.getCode() == KeyCode.ESCAPE)
                nameTextField.setText(group.getName());

        });

    }
}
