import com.jfoenix.controls.JFXButton;
import elements.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        Label label = new Label("Elements");
        label.setId("11");
        Separator separator3 = new Separator(Orientation.HORIZONTAL);

        HBox hBox2 = new HBox(new FunctionCircle().getElement(), new ClassRectangle().getElement(), new InterfaceDiamond().getElement());
        hBox2.setPadding(new Insets(50, 30, 0, 30));
        hBox2.setSpacing(40);

        HBox hBox4 = new HBox(new PackageHexagon().getElement(), new HeaderFileEllipse().getElement());
        hBox4.setPadding(new Insets(50, 50, 0, 50));
        hBox4.setSpacing(50);

        Label label2 = new Label("Generate code");
        label2.setId("111");

        HBox hBox3 = new HBox();
        JFXButton b1 = new JFXButton("Java");
        b1.setId("butt1");
        b1.setMinSize(236, 100);
        JFXButton b2 = new JFXButton("C++");
        b2.setId("butt2");
        b2.setMinSize(236, 100);
        hBox3.getChildren().add(b1);
        hBox3.getChildren().add(b2);
        hBox3.setPadding(new Insets(30, 0, 0, 8));
        hBox3.setSpacing(15);

        VBox leftControl  = new VBox(label, separator3, hBox2, hBox4);
        Label label3 = new Label("Generate Code");
        label3.setId("1111");
        label3.setPadding(new Insets(450, 0, 0, 0));
        Separator sep5 = new Separator(Orientation.HORIZONTAL);
        //sep5.setPadding(new Insets(450, 0, 0, 0));
        leftControl.getChildren().addAll(label3, sep5, hBox3);
        leftControl.setMinWidth(500);
        leftControl.setId("tools");

        ScrollPane rightControl  = new ScrollPane();

        Canvas canvas = new Canvas();
        canvas.setHeight(2500);
        canvas.setWidth(2500);

        //

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

                for (Node node : rightControl.lookupAll(".scroll-bar")) {
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
                System.out.println(x);
                System.out.println(y);
                System.out.println("Dropped " + db.getString());

                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        canvas.localToSceneTransformProperty().addListener( ( observable, oldValue, newValue ) -> {
            final Bounds boundsOnScene = canvas.localToScene( canvas.getBoundsInLocal() );
        } );

        //

        rightControl.setContent(canvas);
        rightControl.setId("drawForm");



//        // draw to canvas
//
          GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.setFill(Color.GREEN);
//        gc.setStroke(Color.BLUE);
//        gc.setLineWidth(3);
          gc.strokeLine(2300, 2320, 2390, 2320);
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
//        //

        Separator separator = new Separator(Orientation.VERTICAL);

        HBox hBox =  new HBox(leftControl, separator, rightControl);

        Menu menu1 = new Menu("Menu 1");
        Menu menu2 = new Menu("Menu 2");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu1);
        menuBar.getMenus().add(menu2);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);

        VBox vBox = new VBox(borderPane, separator2, hBox);

        Scene scene = new Scene(vBox, 720, 720);
        scene.getStylesheets().add("styles.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX App");
        primaryStage.setMaximized(true);
        primaryStage.show();

    }
}