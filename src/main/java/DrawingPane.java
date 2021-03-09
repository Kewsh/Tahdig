import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DrawingPane {

    private Scene scene;
    private StackPane baseStack;
    private ScrollPane scrollPane;
    private Canvas canvas;
    private Group root;
    private int[] defaultIdArray;               // IDs for default element names
    private int width;
    private int height;

    public DrawingPane(Scene scene, StackPane baseStack){

        /*
         * height and width of the canvas both start from 2000, and can continue to grow to up to 6000px.
         * now the maximum size of 6000 could be platform or GPU-dependant. for me it even goes to up to
         * 8000 pixels, but in order for the application to function properly, it is advised to try not
         * to widen or heighten the canvas too much.
         * and please note that if on any system, it were ever reported that the maximum size of 6000
         * causes the application to crash, do make sure to reconsider the maximum size for the sake
         * of cross-platform functionality.
         */

        height = 2000;
        width = 2000;
        this.scene = scene;
        this.baseStack = baseStack;
        defaultIdArray = new int[]{1, 1, 1, 1, 1};
        scrollPane  = new ScrollPane();
        canvas = new Canvas();
        root = new Group();

        canvas.setHeight(height);
        canvas.setWidth(width);
        root.getChildren().add(canvas);
        scrollPane.setContent(root);
        scrollPane.setId("drawForm");

        canvas.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != canvas && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });

        canvas.setOnDragDropped((DragEvent event) -> {

            Dragboard db = event.getDragboard();
            if (db.hasString()) {

                ScrollBar verticalBar = null;
                ScrollBar horizontalBar = null;

                for (Node node : scrollPane.lookupAll(".scroll-bar")) {
                    if (node instanceof ScrollBar) {
                        ScrollBar scrollBar = (ScrollBar) node;
                        if (scrollBar.getOrientation() == Orientation.HORIZONTAL)
                            horizontalBar = scrollBar;
                        if (scrollBar.getOrientation() == Orientation.VERTICAL)
                            verticalBar = scrollBar;
                    }
                    if (horizontalBar != null && verticalBar != null) break;
                }

                double x = event.getSceneX() + horizontalBar.valueProperty().getValue()*(width-1400) - 500;
                double y = event.getSceneY() + verticalBar.valueProperty().getValue()*(height-950) - 30;

                boolean isLocationOk = checkPositionAndResize(x, y);
                if (isLocationOk) {
                    File outDirectory = new File("out/");
                    File CanvasContents = new File("out/canvas_contents.json");
                    if (!CanvasContents.exists()) {
                        try {
                            outDirectory.mkdir();
                            CanvasContents.createNewFile();
                            FileWriter myWriter = new FileWriter(CanvasContents.getAbsolutePath());
                            myWriter.write("{\"classes\": [], \"functions\": [], " + "\"interfaces\": [], \"headers\": [], " +
                                                "\"packages\": [], \"lines\": []}");
                            myWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        placeShapeOnCanvas(x, y, db.getString(), CanvasContents);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    event.setDropCompleted(true);
                } else event.setDropCompleted(false);
            } else event.setDropCompleted(false);
            event.consume();
        });
    }

    private boolean checkPositionAndResize(double x, double y){

        if (width == 6000 && height == 6000)
            return false;
        if (width < 6000 && x >= width-100)
            width += 500;
        if (height < 6000 && y >= height-120)
            height += 500;

        canvas.setHeight(height);
        canvas.setWidth(width);
        return true;
    }

    private void placeShapeOnCanvas(double x, double y, String shape, File CanvasContents) throws FileNotFoundException {
        String name;
        StackPane stack;

        switch(shape) {

            case "circle":

                name = "Function" + defaultIdArray[0];
                defaultIdArray[0] += 1;
                stack = tools.ShapeDrawer.drawCircle(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new elements.FunctionCircle.Actions(x, y, name, root, stack, baseStack, CanvasContents);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "rectangle":

                name = "Class" + defaultIdArray[1];
                defaultIdArray[1] += 1;
                stack = tools.ShapeDrawer.drawRectangle(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new elements.ClassRectangle.Actions(x, y, name, root, stack, baseStack, CanvasContents);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "diamond":

                name = "Interface" + defaultIdArray[2];
                defaultIdArray[2] += 1;
                stack = tools.ShapeDrawer.drawDiamond(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new elements.InterfaceDiamond.Actions(x, y, name, root, stack, baseStack, CanvasContents);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "hexagon":

                name = "Package" + defaultIdArray[3];
                defaultIdArray[3] += 1;
                stack = tools.ShapeDrawer.drawHexagon(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new elements.PackageHexagon.Actions(x, y, name, root, stack, baseStack, CanvasContents);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "ellipse":

                name = "Header_File" + defaultIdArray[4];
                defaultIdArray[4] += 1;
                stack = tools.ShapeDrawer.drawEllipse(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new elements.HeaderFileEllipse.Actions(x, y, name, root, stack, baseStack, CanvasContents);
                setCursor(stack);
                root.getChildren().add(stack);
        }
    }

    private void setCursor(StackPane stack){
        stack.setOnMouseEntered(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.HAND);
            }
        });
        stack.setOnMouseExited(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                scene.setCursor(Cursor.DEFAULT);
            }
        });
    }

    public ScrollPane getPane(){
        return scrollPane;
    }
}
