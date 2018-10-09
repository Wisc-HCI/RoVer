package misc;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

//TODO Delete this class, was being used to test different UI elements and how they worked. 

public class test extends Application {

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 350, 300);
        primaryStage.setTitle("Dots");
        primaryStage.setScene(scene);

        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if(me.getButton().equals(MouseButton.PRIMARY)) {
                Circle circle = new Circle(me.getX(), me.getY(), 10, Color.BLUE);
                addEventHandler(root, circle);
                root.getChildren().add(circle);
            }
        });

        primaryStage.show();
    }

    public void addEventHandler(Group parent, Node node) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if(me.getButton().equals(MouseButton.SECONDARY)) {
                parent.getChildren().remove(node);
            }
        });
    }
}
