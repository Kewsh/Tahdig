import elements.actions.*;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DrawingPane {

    private Stage stage;
    private Scene scene;
    private ScrollPane scrollPane;
    private Canvas canvas;
    private Group root;
    private int[] defaultIdArray;               // IDs for default element names
    private int width;
    private int height;

    public DrawingPane(Stage stage, Scene scene){

        /*
         * height and width of the canvas both start from 2000, and can continue to grow to up to 6000px.
         * now the maximum size of 6000 could be platform or GPU-dependant. for me it even goes to up to
         * 8000 pixels, but in order for the application to function properly, it is advised to try not
         * to widen or heighten the canvas too much.
         * and please note that if on any system, it were ever reported that the maximum size of 6000
         * causes the application to crash, do make sure to reconsider the maximum size for the sake
         * of cross-platform functionality
         */
        height = 2000;
        width = 2000;

        this.scene = scene;
        this.stage = stage;
        defaultIdArray = new int[]{1, 1, 1, 1, 1};
        scrollPane  = new ScrollPane();
        canvas = new Canvas();
        canvas.setHeight(height);
        canvas.setWidth(width);

        root = new Group();
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
                    if (horizontalBar != null && verticalBar != null)
                        break;
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
                            myWriter.write("{\"classes\": [], \"functions\": [], " +
                                            "\"interfaces\": [], \"headers\": [], " +
                                            "\"packages\": []}");
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
                }
                else
                    event.setDropCompleted(false);
            } else {
                event.setDropCompleted(false);
            }
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
        Text text;
        StackPane stack;

        switch(shape)
        {
            case "circle":

                Circle circle = new Circle();
                circle.setRadius(60);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(2);
                circle.setFill(Color.YELLOW);

                name = "Function" + defaultIdArray[0];
                text = new Text(name);
                defaultIdArray[0] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(circle, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                new FunctionCircleActions(x, y, name, root, stack, CanvasContents);

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

                root.getChildren().add(stack);
                break;

            case "rectangle":

                Rectangle rectangle = new Rectangle();
                rectangle.setWidth(100);
                rectangle.setHeight(120);
                rectangle.setArcWidth(30.0);
                rectangle.setArcHeight(20.0);
                rectangle.setStroke(Color.BLUE);
                rectangle.setStrokeWidth(2);
                rectangle.setFill(Color.YELLOW);

                name = "Class" + defaultIdArray[1];
                text = new Text(name);
                defaultIdArray[1] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(rectangle, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                new ClassRectangleActions(x, y, name, root, stack, CanvasContents);

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

                root.getChildren().add(stack);
                break;

            case "diamond":

                Polygon diamond = new Polygon();
                diamond.getPoints().addAll(300.0, 60.0,
                        375.0, 0.0,
                        450.0, 60.0,
                        375.0, 120.0);
                diamond.setStroke(Color.BLUE);
                diamond.setStrokeWidth(2);
                diamond.setFill(Color.YELLOW);

                name = "Interface" + defaultIdArray[2];
                text = new Text(name);
                defaultIdArray[2] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(diamond, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                new InterfaceDiamondActions(x, y, name, root, stack, CanvasContents);

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

                root.getChildren().add(stack);
                break;

            case "hexagon":

                Polygon hexagon = new Polygon();
                hexagon.getPoints().addAll(0.0, 50.0,
                        50.0, 0.0,
                        100.0, 0.0,
                        150.0, 50.0,
                        100.0, 100.0,
                        50.0, 100.0);
                hexagon.setStroke(Color.BLUE);
                hexagon.setStrokeWidth(2);
                hexagon.setFill(Color.YELLOW);

                name = "Package" + defaultIdArray[3];
                text = new Text(name);
                defaultIdArray[3] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(hexagon, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                new PackageHexagonActions(x, y, name, root, stack, CanvasContents);

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

                root.getChildren().add(stack);
                break;

            default:            //ellipse

                Ellipse ellipse = new Ellipse(0.0, 0.0, 100.0, 50.0);
                ellipse.setStroke(Color.BLUE);
                ellipse.setStrokeWidth(2);
                ellipse.setFill(Color.YELLOW);

                name = "Header-File" + defaultIdArray[4];
                text = new Text(name);
                defaultIdArray[4] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(ellipse, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                new HeaderFileEllipseActions(x, y, name, root, stack, CanvasContents);

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

                root.getChildren().add(stack);
        }
    }

    public ScrollPane getPane(){
        return scrollPane;
    }
}
