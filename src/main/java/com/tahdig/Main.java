package com.tahdig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.tahdig.elements.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.exit;

//TODO: clean the code :) like for real.. clean the entirety of the code
//TODO: implement settings and help
//TODO: implement drag and drop for object that are already on the canvas
//TODO: set some dialogs to overLayClose false and put a close a button for them
//  for the rest, if you see fit, set them to overLayClose true
//TODO: show the code output somewhere on the main stage?!
//TODO: delete code folder before second output? (for the same language?)

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage){

        AtomicBoolean exitFlag = new AtomicBoolean(false);
        AtomicBoolean openFileFlag = new AtomicBoolean(false);
        AtomicBoolean newFileFlag = new AtomicBoolean(false);

        VBox vBox = new VBox();
        StackPane stackPane = new StackPane(vBox);
        Scene scene = new Scene(stackPane, 720, 720);
        scene.getStylesheets().add("styles.css");

        primaryStage.setScene(scene);
        primaryStage.setTitle(tools.cutExtension(DrawingPane.CanvasContents.getName()) + " - Tahdig");
        primaryStage.setMaximized(true);

        Label elementShelfLabel = new Label("Elements");
        elementShelfLabel.setStyle("-fx-text-fill: black;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: cursive;" +
                "-fx-padding: 25 0 0 190;");

        HBox elementShelfFirstRow = new HBox(new FunctionCircle().getElement(), new ClassRectangle().getElement(), new InterfaceDiamond().getElement());
        elementShelfFirstRow.setPadding(new Insets(50, 30, 0, 30));
        elementShelfFirstRow.setSpacing(40);
        HBox elementShelfSecondRow = new HBox(new PackageHexagon().getElement(), new HeaderFileEllipse().getElement());
        elementShelfSecondRow.setPadding(new Insets(50, 50, 0, 50));
        elementShelfSecondRow.setSpacing(50);

        HBox codeButtonsHbox = generateCodeButtonsHbox();
        JFXButton generateJavaCodeButton = (JFXButton) codeButtonsHbox.getChildren().get(0);
        JFXButton generateCppCodeButton = (JFXButton) codeButtonsHbox.getChildren().get(1);

        generateJavaCodeButton.setOnAction(event -> {
            JavaEngine javaEngine = JavaEngine.getInstance();
            if (!javaEngine.isReady()) {
                showCanvasEmptyDialog(stackPane);
                return;
            }
            if (!javaEngine.isPossible()){
                showCodeWarningDialog(javaEngine, stackPane);
                return;
            }
            try {
                javaEngine.generateCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        generateCppCodeButton.setOnAction(event -> {
            CppEngine cppEngine = CppEngine.getInstance();
            if (!cppEngine.isReady()) {
                showCanvasEmptyDialog(stackPane);
                return;
            }
            if (!cppEngine.isPossible()){
                showCodeWarningDialog(cppEngine, stackPane);
                return;
            }
            try {
                cppEngine.generateCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        VBox toolsPane  = new VBox(elementShelfLabel, new Separator(Orientation.HORIZONTAL), elementShelfFirstRow, elementShelfSecondRow);

        Label generateCodeLabel = new Label("Generate Code");
        generateCodeLabel.setStyle("-fx-alignment: center;" +
                "-fx-text-fill: black;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: cursive;" +
                "-fx-padding: 400 0 0 165;");

        toolsPane.getChildren().addAll(generateCodeLabel, new Separator(Orientation.HORIZONTAL), codeButtonsHbox);
        toolsPane.setMinWidth(500);
        toolsPane.setStyle("-fx-background-color: #4b5d67;");

        HBox mainHbox =  new HBox(toolsPane, new Separator(Orientation.VERTICAL), (new DrawingPane(scene, stackPane)).getPane());
        JFXDialog savePromptDialog = generateSavePromptDialog(primaryStage, scene, mainHbox, stackPane, exitFlag, openFileFlag, newFileFlag);
        MenuButton fileMenuButton = generateFileMenuButton(primaryStage, scene, mainHbox, stackPane, savePromptDialog, exitFlag, openFileFlag, newFileFlag);

        BorderPane borderPane = new BorderPane();
        borderPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("#393e46"), new CornerRadii(0), new Insets(0, 0, 0, 0))));
        borderPane.setTop(new HBox(fileMenuButton));

        vBox.getChildren().addAll(borderPane, new Separator(Orientation.HORIZONTAL), mainHbox);
        primaryStage.show();
    }

    private HBox generateCodeButtonsHbox(){

        String defaultButtonStyle = "-fx-background-color: #6593f5;" +
                "-fx-text-fill: black;" +
                "-fx-font-size: 40px;" +
                "-fx-background-radius: 15px;" +
                "-fx-border-radius: 15px;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 2px;" +
                "-fx-font-family: Arial;";
        HBox mainHbox = new HBox();

        JFXButton generateJavaCodeButton = new JFXButton("Java");
        generateJavaCodeButton.setStyle(defaultButtonStyle);
        generateJavaCodeButton.setDisableVisualFocus(true);
        generateJavaCodeButton.setMinSize(236, 100);

        JFXButton generateCppCodeButton = new JFXButton("C++");
        generateCppCodeButton.setStyle(defaultButtonStyle);
        generateCppCodeButton.setDisableVisualFocus(true);
        generateCppCodeButton.setMinSize(236, 100);

        mainHbox.getChildren().addAll(generateJavaCodeButton, generateCppCodeButton);
        mainHbox.setPadding(new Insets(30, 0, 0, 8));
        mainHbox.setSpacing(15);
        return mainHbox;
    }

    private MenuButton generateFileMenuButton(Stage primaryStage, Scene scene, HBox mainHbox, StackPane stackPane, JFXDialog savePromptDialog,
                                              AtomicBoolean exitFlag, AtomicBoolean openFileFlag, AtomicBoolean newFileFlag){

        MenuItem menuItem0 = null, menuItem1 = null, menuItem2 = null, menuItem3 = null, menuItem4 = null, menuItem5 = null;
        try {
            menuItem0 = setUpMenuItem("New      ", "src/main/resources/icons/New.png", "Ctrl+N");
            menuItem1 = setUpMenuItem("Open     ", "src/main/resources/icons/Open.png", "Ctrl+O");
            menuItem2 = setUpMenuItem("Save     ", "src/main/resources/icons/Save16.png", "Ctrl+S");
            menuItem3 = setUpMenuItem("Save As      ", "src/main/resources/icons/Save16.png", "Ctrl+Shift+S");
            menuItem4 = setUpMenuItem("Settings     ", "src/main/resources/icons/Settings.png", "");
            menuItem5 = setUpMenuItem("Exit     ", "src/main/resources/icons/Exit.png", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        menuItem0.setOnAction(event -> {
            if (DrawingPane.CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") && DrawingPane.CanvasContents.exists()){

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!tools.isCanvasEmpty(rootNode)) {
                    newFileFlag.set(true);
                    savePromptDialog.show(stackPane);
                } else tools.generateNewDrawingPane(primaryStage, mainHbox, scene, stackPane, null);
            }
            else tools.generateNewDrawingPane(primaryStage, mainHbox, scene, stackPane, null);
        });
        menuItem1.setOnAction(event -> {
            if (DrawingPane.CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") && DrawingPane.CanvasContents.exists()){

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!tools.isCanvasEmpty(rootNode)) {
                    openFileFlag.set(true);
                    savePromptDialog.show(stackPane);
                } else openTahdigFile(primaryStage, mainHbox, scene, stackPane);
            }
            else openTahdigFile(primaryStage, mainHbox, scene, stackPane);
        });
        menuItem2.setOnAction(event -> {
            try {
                saveTahdigFile(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        menuItem3.setOnAction(event -> {
            try {
                saveAsTahdigFile(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        menuItem5.setOnAction(event -> {
            if (DrawingPane.CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") && DrawingPane.CanvasContents.exists()){

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!tools.isCanvasEmpty(rootNode)) {
                    exitFlag.set(true);
                    savePromptDialog.show(stackPane);
                } else {
                    Platform.exit();
                    exit(0);
                }
            }
            else {
                Platform.exit();
                exit(0);
            }
        });

        MenuItem finalMenuItem = menuItem5;
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            finalMenuItem.fire();
        });

        MenuButton fileMenuButton = new MenuButton("File", null, menuItem0, menuItem1, menuItem2, menuItem3, menuItem4, menuItem5);
        fileMenuButton.setStyle("-fx-text-fill: white;");
        fileMenuButton.setBackground(new Background(new BackgroundFill(Paint.valueOf("#393e46"), new CornerRadii(0), new Insets(0, 0, 0, 0))));

        return fileMenuButton;
    }

    private MenuItem setUpMenuItem(String text, String iconPath, String accelerator) throws IOException {

        MenuItem menuItem = new MenuItem(text);
        menuItem.setGraphic(new ImageView(new Image(new FileInputStream(new File(iconPath).getAbsolutePath()))));
        if (accelerator != "")
            menuItem.setAccelerator(KeyCombination.keyCombination(accelerator));
        return menuItem;
    }

    private JFXDialog generateSavePromptDialog(Stage primaryStage, Scene scene, HBox mainHbox, StackPane stackPane,
                                               AtomicBoolean exitFlag, AtomicBoolean openFileFlag, AtomicBoolean newFileFlag){

        JFXDialog savePromptDialog = tools.generateNoticeDialog("Do you want to save change to " + DrawingPane.CanvasContents.getName() + "?",
                "Save", "src/main/resources/icons/Save.png");
        savePromptDialog.setOverlayClose(false);

        JFXButton yesButton = new JFXButton("Yes");
        yesButton.setFont(new Font(20));
        yesButton.setOnAction(event -> {
            savePromptDialog.close();
            try {
                saveAsTahdigFile(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exitFlag.get()) {
                Platform.exit();
                exit(0);
            }
            if (openFileFlag.get()) {
                openTahdigFile(primaryStage, mainHbox, scene, stackPane);
                openFileFlag.set(false);
            } else if (newFileFlag.get()) {
                newFileFlag.set(false);
                tools.generateNewDrawingPane(primaryStage, mainHbox, scene, stackPane, null);
            }
        });
        JFXButton noButton = new JFXButton("No");
        noButton.setFont(new Font(20));
        noButton.setOnAction(event -> {
            savePromptDialog.close();
            if (exitFlag.get()) {
                Platform.exit();
                exit(0);
            }
            if (openFileFlag.get()) {
                openTahdigFile(primaryStage, mainHbox, scene, stackPane);
                openFileFlag.set(false);
            } else if (newFileFlag.get()) {
                newFileFlag.set(false);
                tools.generateNewDrawingPane(primaryStage, mainHbox, scene, stackPane, null);
            }
        });
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setFont(new Font(20));
        cancelButton.setOnAction(event -> {
            savePromptDialog.close();
            exitFlag.set(false);
        });

        ((JFXDialogLayout) savePromptDialog.getContent()).setActions(yesButton, noButton, cancelButton);
        return savePromptDialog;
    }

    private void showCanvasEmptyDialog(StackPane stackPane){

        JFXDialog canvasEmpty = tools.generateNoticeDialog("\tCanvas is empty!", "Error", "src/main/resources/icons/Error.png");

        JFXButton okButton = new JFXButton("OK");
        okButton.setFont(new Font(20));
        okButton.setOnAction(event1 -> canvasEmpty.close());
        ((JFXDialogLayout) canvasEmpty.getContent()).setActions(okButton);

        canvasEmpty.show(stackPane);
    }

    private void showCodeWarningDialog(Engine engine, StackPane stackPane){

        String warningText = "Due to your specific design, a standard code output is impossible, but Tahdig\ncan take"
                + " specific actions to make it possible. Considering that the result might\nnot be identical to your design,"
                + " do you wish to continue?";
        JFXDialog codeImpossible = tools.generateNoticeDialog(warningText, "Warning", "src/main/resources/icons/Warning.png");

        JFXButton yesButton = new JFXButton("Yes");
        yesButton.setFont(new Font(20));
        yesButton.setOnAction(event15 -> {
            codeImpossible.close();
            try {
                engine.generateCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        JFXButton cancelButton = new JFXButton("Cancel");
        cancelButton.setFont(new Font(20));
        cancelButton.setOnAction(event1 -> codeImpossible.close());

        ((JFXDialogLayout) codeImpossible.getContent()).setActions(cancelButton, yesButton);
        codeImpossible.show(stackPane);
    }

    private void saveTahdigFile(Stage primaryStage) throws IOException {

        if (!DrawingPane.CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") && DrawingPane.CanvasContents.exists())
            return;
        saveAsTahdigFile(primaryStage);
    }

    private void saveAsTahdigFile(Stage primaryStage) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tahdig file", "*.tahdig"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file == null)
            return;
        if (DrawingPane.CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") && DrawingPane.CanvasContents.exists())
            DrawingPane.CanvasContents.renameTo(file);
        else {
            FileReader myReader = new FileReader(DrawingPane.CanvasContents.getAbsolutePath());
            FileWriter myWriter = new FileWriter(file.getAbsolutePath());
            final int MAXIMUM_FILE_SIZE = 50000;
            char[] buffer = new char[MAXIMUM_FILE_SIZE];
            myReader.read(buffer);
            myWriter.write(buffer);
            myWriter.close();
            myReader.close();
        }
        DrawingPane.CanvasContents = file;
        primaryStage.setTitle(tools.cutExtension(DrawingPane.CanvasContents.getName()) + " - Tahdig");
    }

    private void openTahdigFile(Stage primaryStage, HBox mainHbox, Scene scene, StackPane stackPane){

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tahdig file", "*.tahdig"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null){
            DrawingPane.CanvasContents = file;
            primaryStage.setTitle(tools.cutExtension(DrawingPane.CanvasContents.getName()) + " - Tahdig");
            DrawingPane drawingPane = tools.generateNewDrawingPane(primaryStage, mainHbox, scene, stackPane, file);
            try {
                tools.reCreateCanvas(drawingPane);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}