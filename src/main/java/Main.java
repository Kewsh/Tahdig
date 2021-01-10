import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        Label label = new Label("Elements");
        label.setId("11");
        Separator separator3 = new Separator(Orientation.HORIZONTAL);

        Circle circle = new Circle();
        circle.setRadius(55);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(2);
        circle.setFill(Color.YELLOW);

        final Text text = new Text ("Function");                        // circle with text in it
        text.setFont(new Font("monospace", 20));
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(circle, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        //

        stack.setOnDragDetected((MouseEvent event) -> {
            System.out.println("drag detected");

            Dragboard db = stack.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(stack.getChildren().get(0).toString());


            FileInputStream input = null;
            try {
                input = new FileInputStream("C:/Users/m-pc/Desktop/image3.png");             // size matters :))
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Image image = new Image(input);
            ImageView imageView = new ImageView(image);

            db.setDragView(imageView.getImage());
            db.setContent(content);
        });
        circle.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });

        //

        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(80);
        rectangle.setHeight(120);
        rectangle.setArcWidth(30.0);
        rectangle.setArcHeight(20.0);
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(2);
        rectangle.setFill(Color.YELLOW);

        final Text text2 = new Text ("Class");                        // circle with text in it
        text2.setFont(new Font("monospace", 20));
        final StackPane stack2 = new StackPane();
        stack2.getChildren().addAll(rectangle, text2);
        stack2.setLayoutX(30);
        stack2.setLayoutY(30);

        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(300.0, 60.0,
                375.0, 0.0,
                450.0, 60.0,
                375.0, 120.0);
        diamond.setStroke(Color.BLUE);
        diamond.setStrokeWidth(2);
        diamond.setFill(Color.YELLOW);

        final Text text3 = new Text ("Interface");                        // circle with text in it
        text3.setFont(new Font("monospace", 20));
        final StackPane stack3 = new StackPane();
        stack3.getChildren().addAll(diamond, text3);
        stack3.setLayoutX(30);
        stack3.setLayoutY(30);

        HBox hBox2 = new HBox(stack, stack2, stack3);
        hBox2.setPadding(new Insets(50, 40, 0, 40));
        hBox2.setSpacing(45);



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

        final Text text4 = new Text ("Package");                        // circle with text in it
        text4.setFont(new Font("monospace", 20));
        final StackPane stack4 = new StackPane();
        stack4.getChildren().addAll(hexagon, text4);
        stack4.setLayoutX(30);
        stack4.setLayoutY(30);

        Ellipse ellipse = new Ellipse(0.0, 0.0, 100.0, 50.0);
        ellipse.setStroke(Color.BLUE);
        ellipse.setStrokeWidth(2);
        ellipse.setFill(Color.YELLOW);

        final Text text5 = new Text ("Header-File");                        // circle with text in it
        text5.setFont(new Font("monospace", 20));
        final StackPane stack5 = new StackPane();
        stack5.getChildren().addAll(ellipse, text5);
        stack5.setLayoutX(30);
        stack5.setLayoutY(30);

        HBox hBox4 = new HBox(stack4, stack5);
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
                System.out.println("Dropped");
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

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
          gc.strokeRoundRect(2300, 2300, 90, 120, 10, 10);
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