import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

public class Main extends Application {

// splitpane -> l: whatever, r: scrollpane
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws FileNotFoundException {


        VBox leftControl  = new VBox(new Label("Left Control"));
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
        primaryStage.setFullScreen(true);
        primaryStage.show();

    }
}