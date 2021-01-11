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

public class PackageHexagon {

    private Polygon hexagon;
    private final Text text;
    private final StackPane stack;

    public PackageHexagon(){

        hexagon = new Polygon();
        hexagon.getPoints().addAll(0.0, 50.0,
                50.0, 0.0,
                100.0, 0.0,
                150.0, 50.0,
                100.0, 100.0,
                50.0, 100.0);
        hexagon.setStroke(Color.BLUE);
        hexagon.setStrokeWidth(2);
        hexagon.setFill(Color.YELLOW);

        text = new Text("Package");
        text.setFont(new Font("monospace", 20));
        stack = new StackPane();
        stack.getChildren().addAll(hexagon, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            System.out.println("hexagon drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(stack.getChildren().get(0).toString());

            String path = new File("src/main/resources/PackageHexagon.png").getAbsolutePath();

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

        hexagon.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
