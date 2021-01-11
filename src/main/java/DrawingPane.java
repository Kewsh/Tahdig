import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class DrawingPane {

    private ScrollPane scrollPane;
    private Canvas canvas;
    private GraphicsContext gc;
    private Group root;
    private int defaultClassId;
    private int defaultFunctionId;
    private int defaultInterfaceId;
    private int defaultPackageId;
    private int defaultHeaderFileId;
    private int flag = 0;

    public DrawingPane(){

        defaultClassId = 1;
        defaultPackageId = 1;
        defaultInterfaceId = 1;
        defaultFunctionId = 1;
        defaultHeaderFileId = 1;

        scrollPane  = new ScrollPane();
        canvas = new Canvas();
        canvas.setHeight(2500);
        canvas.setWidth(2500);
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

                double x = event.getSceneX() + horizontalBar.valueProperty().getValue()*1100 - 500;
                double y = event.getSceneY() + verticalBar.valueProperty().getValue()*1550 - 30;

                boolean isLocationOk = checkPosition(x, y);
                if (isLocationOk) {
                    placeShapeOnCanvas(x, y, db.getString());
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

    private boolean checkPosition(double x, double y){
        if (x >= 2400 || y >= 2380)
            return false;
        return true;
    }

    private void placeShapeOnCanvas(double x, double y, String shape){

        Text text;
        StackPane stack;
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
                circle.setRadius(55);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(2);
                circle.setFill(Color.YELLOW);

                text = new Text("Function" + defaultFunctionId);
                defaultFunctionId += 1;
                text.setFont(new Font("monospace", 20));
                stack = new StackPane();
                stack.getChildren().addAll(circle, text);
                stack.setLayoutX(x);
                stack.setLayoutY(y);

                root.getChildren().add(stack);


                flag += 1;


                break;

            case "rectangle":
                System.out.println("two");
                break;
            case "diamond":
                System.out.println("three");
                break;
            case "hexagon":
                System.out.println("four");
                break;
            default:            //ellipse
                System.out.println("five");
        }
    }

    public ScrollPane getPane(){
        return scrollPane;
    }
}
