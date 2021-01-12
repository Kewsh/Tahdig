import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Stack;

public class DrawingPane {

    private Stage stage;
    private Scene scene;
    private ScrollPane scrollPane;
    private Canvas canvas;
    private GraphicsContext gc;
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
        gc = canvas.getGraphicsContext2D();

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
                    try {
                        placeShapeOnCanvas(x, y, db.getString());
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



//        // draw on canvas
//
//        gc.setFill(Color.GREEN);
//        gc.setStroke(Color.BLUE);
//        gc.setLineWidth(3);
        gc.strokeLine(500, 500, 520, 500);
//        gc.fillOval(10, 60, 30, 30);
//        gc.strokeOval(60, 60, 30, 30);
//        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(3, 4, 90, 120, 10, 10);
        //gc.strokeRoundRect(13, 15, 90, 120, 10, 10);
//        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
//        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
//        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
//        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
//        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
//        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
//        gc.fillPolygon(new double[]{10, 40, 10, 40},
//                new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolygon(new double[]{60, 90, 60, 90},
//                new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolyline(new double[]{110, 140, 110, 140},
//                new double[]{210, 210, 240, 240}, 4);
//
//
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

    private void placeShapeOnCanvas(double x, double y, String shape) throws FileNotFoundException {

        Text text;
        StackPane stack;
        JFXButton connectionsButton = new JFXButton("Connect");
        JFXButton methodsButton = new JFXButton("Methods");
        JFXButton attributesButton = new JFXButton("Attributes");
        //JFXButton deleteButton = new JFXButton("Delete");
        JFXButton deleteButton = new JFXButton();

        //icon example, transparent background
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));

        connectionsButton.setMinSize(50, 70);
        connectionsButton.setStyle("-fx-font-size: 18px; -fx-font-family: Verdana;");
        methodsButton.setMinSize(50, 70);
        methodsButton.setStyle("-fx-font-size: 18px; -fx-font-family: Verdana;");
        attributesButton.setMinSize(50, 70);
        attributesButton.setStyle("-fx-font-size: 18px; -fx-font-family: Verdana;");
        //deleteButton.setMinSize(50, 50);

        // add trash can icon as 4th button (optional)

        connectionsButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                System.out.println("i got clicked. lol");
            }
        });

        switch(shape)
        {
            case "circle":

                //change the text in a previously created shape

//                StackPane stack2;
//                Text text2;
//                if (flag == 1) {
//                    stack2 = (StackPane) root.getChildren().get(1);
//                    text2 = (Text) stack2.getChildren().get(1);
//                    text2.setText("hi");
//                }

                Circle circle = new Circle();
                circle.setRadius(60);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(2);
                circle.setFill(Color.YELLOW);

                text = new Text("Function" + defaultIdArray[0]);
                defaultIdArray[0] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(circle, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                //testing popup

                StackPane stack2 = new StackPane();
                stack2.setLayoutX(x-125);
                stack2.setLayoutY(y-85);
                stack2.setMinHeight(100);
                root.getChildren().add(stack2);

                JFXPopup popup = new JFXPopup();
                popup.setPopupContent(new HBox(connectionsButton, attributesButton, methodsButton, deleteButton));
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);

                stack.setOnMouseClicked(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        popup.show(stack2);
                    }
                });


                //



                // testing dialog

//                StackPane stack2 = new StackPane();
//
//                JFXButton button = new JFXButton("Close");
//                JFXButton button2 = new JFXButton("line");
//                JFXButton button3 = new JFXButton("Mtd");
//
//                JFXDialog dialog2 = new JFXDialog(stack2, new Label("hello"), JFXDialog.DialogTransition.CENTER, false);
//                button.setOnAction(new EventHandler<ActionEvent>(){
//                    @Override
//                    public void handle(ActionEvent event){
//                        dialog2.close();
//                    }
//                });
//                JFXDialogLayout layout2 = new JFXDialogLayout();
//                layout2.setHeading(new Label("Actions"));
//                layout2.setActions(button, button2, button3);
//                dialog2.setContent(layout2);
//
//                stack2.setLayoutX(x-75);
//                stack2.setLayoutY(y-150);
//                root.getChildren().add(stack2);
//
//                stack.setOnMouseClicked(new EventHandler<MouseEvent>(){
//                    @Override
//                    public void handle(MouseEvent event) {
////                        //popup.show(stage);
//                        dialog2.show();
//                    }
//                });


                // -----------------

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

                text = new Text("Class" + defaultIdArray[1]);
                defaultIdArray[1] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(rectangle, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

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

                text = new Text("Interface" + defaultIdArray[2]);
                defaultIdArray[2] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(diamond, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

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

                text = new Text("Package" + defaultIdArray[3]);
                defaultIdArray[3] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(hexagon, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

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

                text = new Text("Header-File" + defaultIdArray[4]);
                defaultIdArray[4] += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(ellipse, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

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
