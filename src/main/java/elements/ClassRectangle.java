package elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ClassRectangle {

    private Rectangle rectangle;
    private final Text text;
    private final StackPane stack;

    public ClassRectangle(){

        rectangle = new Rectangle();
        rectangle.setWidth(100);
        rectangle.setHeight(120);
        rectangle.setArcWidth(30.0);
        rectangle.setArcHeight(20.0);
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(2);
        rectangle.setFill(Color.YELLOW);

        text = new Text("Class");
        text.setFont(new Font("monospace", 20));
        stack = new StackPane();
        stack.getChildren().addAll(rectangle, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            System.out.println("rectangle drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("rectangle");

            String path = new File("src/main/resources/ClassRectangle.png").getAbsolutePath();

            FileInputStream input = null;
            try {
                input = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Image image = new Image(input);
            ImageView imageView = new ImageView(image);
            db.setDragView(imageView.getImage());
            db.setContent(content);
        });

        rectangle.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
