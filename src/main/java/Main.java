import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import elements.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

//TODO: set validations on method/variable/function/... names (handle spaces, signs such as $#%^...)
//TODO: do not allow inheritance/implementation for elements that are already inherited/implemented by the target class
//TODO: implement drag and drop for object that are already on the canvas
//TODO: place all important and big dialogs on base stack (i.e. line 37, stackPane)
//TODO: for the connections button, we move its action to a new dialog that pops up on the base stack
//  this dialog will have an hbox that consists of 2 combo boxes(left one is the connection type and right one is target)
//TODO: from this, we conclude that the only popup that we have that isn't shown on the base stack, is the actions popup itself
//  which can be easily passed to different methods and they can hide it if its being shown. this should handle duplicate dialogs
//TODO: for every dialog that is displayed on base stack, close all other popups and disable them while this dialog is open
//TODO: edit the style of all these dialogs, (i.e. better looking buttons, proper labels and etc)
//TODO: change all fonts to some beautiful font either from defaults or from google fonts
//  e.g. @import url('https://fonts.googleapis.com/css?family=Muli&display=swap');
//TODO: set all dialogs to overLayClose false and put a close a button for them

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        VBox vBox = new VBox();
        StackPane stackPane = new StackPane(vBox);
        Scene scene = new Scene(stackPane, 720, 720);
        scene.getStylesheets().add("styles.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle("Tahdig");
        primaryStage.setMaximized(true);

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
        JFXButton generateJavaCodeButton = new JFXButton("Java");
        generateJavaCodeButton.setId("butt1");
        generateJavaCodeButton.setDisableVisualFocus(true);                         // this removes the default focus on the buttons
        generateJavaCodeButton.setMinSize(236, 100);
        JFXButton generateCppCodeButton = new JFXButton("C++");
        generateCppCodeButton.setId("butt2");
        generateCppCodeButton.setDisableVisualFocus(true);
        generateCppCodeButton.setMinSize(236, 100);
        hBox3.getChildren().add(generateJavaCodeButton);
        hBox3.getChildren().add(generateCppCodeButton);
        hBox3.setPadding(new Insets(30, 0, 0, 8));
        hBox3.setSpacing(15);

        // generateCppCode button

        generateCppCodeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                CppEngine cppEngine = CppEngine.getInstance();
                if (!cppEngine.isReady()) {

                    JFXButton okButton = new JFXButton("OK");
                    Text text = new Text("Canvas is empty");
                    text.setFont(Font.font(20));
                    StackPane textStack = new StackPane(text);
                    textStack.setPadding(new Insets(20, 0, 0, 0));
                    JFXDialog canvasEmpty = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            false);
                    okButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            canvasEmpty.close();
                        }
                    });
                    JFXDialogLayout canvasEmptyLayout = new JFXDialogLayout();

                    canvasEmptyLayout.setMinSize(500, 100);
                    canvasEmptyLayout.setBody(textStack);
                    canvasEmptyLayout.setActions(okButton);
                    canvasEmpty.setContent(canvasEmptyLayout);

                    canvasEmpty.show(stackPane);
                    return;
                }
                if (!cppEngine.isPossible()){

                    JFXButton yesButton = new JFXButton("Yes");
                    JFXButton cancelButton = new JFXButton("Cancel");
                    yesButton.setStyle("-fx-font-size: 20px;");
                    cancelButton.setStyle("-fx-font-size: 20px;");
                    Text text = new Text("due to your specific design, a standard java code output is impossible, but Tahdig can take"
                            + " specific actions to make it possible.\nconsidering that the result might not be identical to your design,"
                            + " Do you wish to continue?");
                    text.setFont(Font.font(18));
                    StackPane textStack = new StackPane(text);
                    textStack.setPadding(new Insets(20, 0, 0, 0));
                    JFXDialog codeImpossible = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            false);
                    yesButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            codeImpossible.close();
                            try {
                                cppEngine.generateCode();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    cancelButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            codeImpossible.close();
                        }
                    });
                    JFXDialogLayout codeImpossibleLayout = new JFXDialogLayout();

                    codeImpossibleLayout.setMinSize(500, 100);
                    codeImpossibleLayout.setBody(textStack);
                    codeImpossibleLayout.setActions(cancelButton, yesButton);
                    codeImpossible.setContent(codeImpossibleLayout);

                    codeImpossible.show(stackPane);
                    return;
                }
                try {
                    cppEngine.generateCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //

        // generateJavaCode button

        generateJavaCodeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                JavaEngine javaEngine = JavaEngine.getInstance();
                if (!javaEngine.isReady()) {

                    JFXButton okButton = new JFXButton("OK");
                    Text text = new Text("Canvas is empty");
                    text.setFont(Font.font(20));
                    StackPane textStack = new StackPane(text);
                    textStack.setPadding(new Insets(20, 0, 0, 0));
                    JFXDialog canvasEmpty = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            false);
                    okButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            canvasEmpty.close();
                        }
                    });
                    JFXDialogLayout canvasEmptyLayout = new JFXDialogLayout();

                    canvasEmptyLayout.setMinSize(500, 100);
                    canvasEmptyLayout.setBody(textStack);
                    canvasEmptyLayout.setActions(okButton);
                    canvasEmpty.setContent(canvasEmptyLayout);

                    canvasEmpty.show(stackPane);
                    return;
                }
                if (!javaEngine.isPossible()){

                    JFXButton yesButton = new JFXButton("Yes");
                    JFXButton cancelButton = new JFXButton("Cancel");
                    yesButton.setStyle("-fx-font-size: 20px;");
                    cancelButton.setStyle("-fx-font-size: 20px;");
                    Text text = new Text("due to your specific design, a standard java code output is impossible, but Tahdig can take"
                                    + " specific actions to make it possible.\nconsidering that the result might not be identical to your design,"
                                    + " Do you wish to continue?");
                    text.setFont(Font.font(18));
                    StackPane textStack = new StackPane(text);
                    textStack.setPadding(new Insets(20, 0, 0, 0));
                    JFXDialog codeImpossible = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            false);
                    yesButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            codeImpossible.close();
                            try {
                                javaEngine.generateCode();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    cancelButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {
                            codeImpossible.close();
                        }
                    });
                    JFXDialogLayout codeImpossibleLayout = new JFXDialogLayout();

                    codeImpossibleLayout.setMinSize(500, 100);
                    codeImpossibleLayout.setBody(textStack);
                    codeImpossibleLayout.setActions(cancelButton, yesButton);
                    codeImpossible.setContent(codeImpossibleLayout);

                    codeImpossible.show(stackPane);
                    return;
                }
                try {
                    javaEngine.generateCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //

        VBox leftControl  = new VBox(label, separator3, hBox2, hBox4);
        Label label3 = new Label("Generate Code");
        label3.setId("1111");
        label3.setPadding(new Insets(450, 0, 0, 0));
        Separator sep5 = new Separator(Orientation.HORIZONTAL);
        leftControl.getChildren().addAll(label3, sep5, hBox3);
        leftControl.setMinWidth(500);
        leftControl.setId("tools");


        Separator separator = new Separator(Orientation.VERTICAL);

        HBox hBox =  new HBox(leftControl, separator, new DrawingPane(scene, stackPane).getPane());

        Menu menu1 = new Menu("Menu 1");
        Menu menu2 = new Menu("Menu 2");
        MenuBar menuBar = new MenuBar();
        menuBar.setBackground(new Background(new BackgroundFill(Paint.valueOf("#393e46"), new CornerRadii(0), new Insets(0, 0, 0, 0))));
        menuBar.getMenus().add(menu1);
        menuBar.getMenus().add(menu2);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        vBox.getChildren().addAll(borderPane, separator2, hBox);

        primaryStage.show();

    }
}