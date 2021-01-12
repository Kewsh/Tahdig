package elements;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class HeaderFileEllipse {

    private Scene scene;
    private Ellipse ellipse;
    private final Text text;
    private final StackPane stack;

    public HeaderFileEllipse(Scene scene){

        this.scene = scene;
        ellipse = new Ellipse(0.0, 0.0, 100.0, 50.0);
        ellipse.setStroke(Color.BLUE);
        ellipse.setStrokeWidth(2);
        ellipse.setFill(Color.YELLOW);

        text = new Text("Header-File");
        text.setFont(new Font("monospace", 20));
        stack = new StackPane();
        stack.getChildren().addAll(ellipse, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            System.out.println("ellipse drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("ellipse");

            String path = new File("src/main/resources/icons/HeaderFileEllipse.png").getAbsolutePath();

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

        ellipse.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
