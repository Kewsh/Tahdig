package elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class InterfaceDiamond {

    private Polygon diamond;
    private final Text text;
    private final StackPane stack;

    public InterfaceDiamond(){

        diamond = new Polygon();
        diamond.getPoints().addAll(300.0, 60.0,
                375.0, 0.0,
                450.0, 60.0,
                375.0, 120.0);
        diamond.setStroke(Color.BLUE);
        diamond.setStrokeWidth(2);
        diamond.setFill(Color.YELLOW);

        text = new Text("Interface");
        text.setFont(new Font("monospace", 20));
        stack = new StackPane();
        stack.getChildren().addAll(diamond, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            System.out.println("diamond drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("diamond");

            String path = new File("src/main/resources/InterfaceDiamond.png").getAbsolutePath();

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

        diamond.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
