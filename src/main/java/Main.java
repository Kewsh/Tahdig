import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        Label label = new Label("Shapes");
        label.setId("11");
        Separator separator3 = new Separator(Orientation.HORIZONTAL);

        Circle circle = new Circle();
        circle.setCenterX(300);
        circle.setCenterY(300);
        circle.setRadius(35);
        circle.setStroke(Color.valueOf("#ff00ff"));
        circle.setStrokeWidth(5);
        circle.setFill(Color.BLACK);

        Rectangle rectangle = new Rectangle();
        rectangle.setX(200);
        rectangle.setY(200);
        rectangle.setWidth(80);
        rectangle.setHeight(100);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.BLACK);

        HBox hBox2 = new HBox(circle, rectangle);
        hBox2.setPadding(new Insets(75));
        hBox2.setSpacing(125);

        Circle circle2 = new Circle();
        circle.setCenterX(300);
        circle.setCenterY(30129);
        circle.setRadius(35);
        circle.setStroke(Color.valueOf("#ff00ff"));
        circle.setStrokeWidth(5);
        circle.setFill(Color.BLACK);

        Rectangle rectangle2 = new Rectangle();
        rectangle.setX(200);
        rectangle.setY(200);
        rectangle.setWidth(80);
        rectangle.setHeight(100);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.BLACK);

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        Label label2 = new Label("hi");
        label2.setId("111");

        HBox hBox3 = new HBox(circle2, rectangle2);
        hBox3.setPadding(new Insets(75));
        hBox3.setSpacing(125);


        VBox leftControl  = new VBox(label, separator3, hBox2, separator4, label2);
        leftControl.setMinWidth(500);
        leftControl.setId("tools");

        ScrollPane rightControl  = new ScrollPane();

        Canvas canvas = new Canvas();
        canvas.setHeight(2500);
        canvas.setWidth(2500);

        rightControl.setContent(canvas);
        rightControl.setId("drawForm");

        Separator separator = new Separator(Orientation.VERTICAL);

        HBox hBox =  new HBox(leftControl, separator, rightControl);

        Menu menu1 = new Menu("Menu 1");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu1);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);

        VBox vBox = new VBox(borderPane, separator2, hBox);

        Scene scene = new Scene(vBox, 800, 800);
        scene.getStylesheets().add("styles.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX App");
        primaryStage.setMaximized(true);
        primaryStage.show();

    }
}