import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
        circle.setRadius(35);
        circle.setFill(Color.YELLOW);

        final Text text = new Text ("circ");                        // circle with text in it
        text.setFont(new Font("monospace", 20));
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(circle, text);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(80);
        rectangle.setHeight(100);
        rectangle.setFill(Color.YELLOW);

        HBox hBox2 = new HBox(stack, rectangle);
        hBox2.setPadding(new Insets(75));
        hBox2.setSpacing(125);

        Label label2 = new Label("hi");
        label2.setId("111");

        HBox hBox3 = new HBox();
        JFXButton b1 = new JFXButton("button1");
        b1.setId("butt1");
        JFXButton b2 = new JFXButton("button2");
        b2.setId("butt2");
        hBox3.getChildren().add(b1);
        hBox3.getChildren().add(b2);
        hBox3.setPadding(new Insets(400, 25, 0, 25));
        hBox3.setSpacing(25);

        VBox leftControl  = new VBox(label, separator3, hBox2, new Separator(Orientation.HORIZONTAL), label2);
        leftControl.getChildren().add(new Separator(Orientation.HORIZONTAL));
        leftControl.getChildren().add(hBox3);
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
        Menu menu2 = new Menu("Menu 2");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu1);
        menuBar.getMenus().add(menu2);

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