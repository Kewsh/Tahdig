package elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FunctionCircle {

    private Circle circle;
    private final Text text;
    private final StackPane stack;

    public FunctionCircle(){

        circle = new Circle();
        circle.setRadius(55);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(2);
        circle.setFill(Color.YELLOW);

        text = new Text ("Function");
        text.setFont(new Font("monospace", 20));
        stack = new StackPane();
        stack.getChildren().addAll(circle, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            System.out.println("drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(stack.getChildren().get(0).toString());

            FileInputStream input = null;
            try {
                input = new FileInputStream("C:/Users/m-pc/Desktop/image3.png");             // size matters :))
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Image image = new Image(input);
            ImageView imageView = new ImageView(image);
            db.setDragView(imageView.getImage());
            db.setContent(content);
        });

        circle.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
