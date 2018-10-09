package model_ctrl;

import java.util.ArrayList;
import java.util.HashMap;

import controller.MainController;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import model.Variable;
import javafx.scene.input.KeyEvent;

public class MicroParameterizer {

    private HashMap<Variable, Node> params;

    @SuppressWarnings({"restriction", "unchecked"})
    public MicroParameterizer(ArrayList<Variable> vars, MainController mc) {
        params = new HashMap<>();
        for (Variable glob : vars) {
            if (glob.isParameterizable()) {
                ComboBox temp = new ComboBox();
                ToggleGroup toggleGroup = new ToggleGroup();
                TextArea textTemp = new TextArea();

                RadioButton rb1 = new RadioButton("True");
                rb1.setToggleGroup(toggleGroup);
                rb1.setUserData("true");
                RadioButton rb2 = new RadioButton("False");
                rb2.setToggleGroup(toggleGroup);
                rb2.setUserData("false");

                HBox toggleHBox = new HBox();
                toggleHBox.getChildren().addAll(rb1, rb2);
                toggleHBox.setSpacing(5);

                toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
                    public void changed(ObservableValue<? extends Toggle> ov,
                                        Toggle old_toggle, Toggle new_toggle) {
                        if (toggleGroup.getSelectedToggle() != null)
                            glob.setValue(toggleGroup.getSelectedToggle().getUserData().toString());
                        if (mc != null)
                            mc.verify();
                    }
                });

                temp.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String t, String t1) {
                        System.out.println("glob setting value in Handle");
                        if (!(t == null))
                            glob.setValue(t1);
                        if (mc != null)
                            mc.verify();
                    }
                });

                textTemp.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        String txt = keyEvent.getText();
                        char ch = (txt.length() > 0) ? txt.charAt(0) : ' ';
                        glob.setValue(textTemp.getText() + ((ch >= 32 & ch <= 126) ? (ch) : ""));

                    }
                });

                if (glob.getType().equals("int")) {
                    String bound = glob.getBound();
                    int lb = Integer.parseInt(bound.substring(0, bound.indexOf('.')));
                    bound = bound.substring(bound.indexOf('.') + 2, bound.length());
                    int ub = Integer.parseInt(bound);

                    for (int i = lb; i <= ub; i++)
                        temp.getItems().add(i + "");

                    if (glob.getRequired())
                        temp.setValue("");
                    else
                        temp.setValue(glob.getValue());

                    params.put(glob, temp);
                } else if (glob.getType().equals("bool")) {
                    if (glob.getRequired()) {
                        rb1.setSelected(false);
                        rb2.setSelected(false);
                    }
                    else
                        rb1.setSelected(glob.getValue().equalsIgnoreCase("true"));
                    params.put(glob, toggleHBox);
                } else if (glob.getType().equals("nominal")) {
                    for (String str : glob.getNominalValues()) {
                        temp.getItems().add(str);
                    }
                    temp.setValue("");
                    params.put(glob, temp);
                } else if (glob.getType().equals("str")) {
                    textTemp.setText(glob.getValue());
                    params.put(glob, textTemp);
                } else if (glob.getType().equals("array")) {
                    ListView tempField = new ListView();
                    if (glob.getArrayVals().size() > 0)
                        tempField.getItems().addAll(glob.arrayToTextArea());
                    params.put(glob, tempField);
                }

                //}
            }
        }
    }

    public HashMap<Variable, Node> getParams() {
        return params;
    }

}
