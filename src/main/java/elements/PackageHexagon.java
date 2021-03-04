package elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PackageHexagon {

    private final StackPane stack;

    public PackageHexagon(){

        stack = tools.ShapeDrawer.drawHexagon("Package");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("hexagon");

            String path = new File("src/main/resources/icons/PackageHexagon.png").getAbsolutePath();

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

        stack.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }
}
