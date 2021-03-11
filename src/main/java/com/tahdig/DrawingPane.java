package com.tahdig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
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
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DrawingPane {

    public static File CanvasContents = new File("out/Untitled.tahdig");
    private Scene scene;
    private StackPane baseStack;
    private ScrollPane scrollPane;
    private Canvas canvas;
    private Group root;
    private int[] defaultIdArray;               // IDs for default element names
    private int width, height;

    public DrawingPane(Scene scene, StackPane baseStack){

        /*
         * height and width of the canvas both start from 2000, and can continue to grow to up to 6000px.
         * now the maximum size of 6000 could be platform or GPU-dependant. for me it even goes to up to
         * 8000 pixels, but in order for the application to function properly, it is advised to try not
         * to widen or heighten the canvas too much.
         * and please note that if on any system, it were ever reported that the maximum size of 6000
         * causes the application to crash, do make sure to reconsider the maximum size for the sake
         * of cross-platform functionality.
         */

        height = 2000;
        width = 2000;
        this.scene = scene;
        this.baseStack = baseStack;
        defaultIdArray = new int[]{1, 1, 1, 1, 1};
        scrollPane  = new ScrollPane();
        canvas = new Canvas();
        root = new Group();

        canvas.setHeight(height);
        canvas.setWidth(width);
        root.getChildren().add(canvas);
        scrollPane.setContent(root);
        scrollPane.setId("drawForm");

        File outDirectory = new File("out/");
        if (!CanvasContents.exists()) {
            try {
                outDirectory.mkdir();
                CanvasContents.createNewFile();
                FileWriter myWriter = new FileWriter(CanvasContents.getAbsolutePath());
                myWriter.write("{\"classes\": [], \"functions\": [], " + "\"interfaces\": [], \"headers\": [], " +
                        "\"packages\": [], \"lines\": []}");
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig") || !tools.isCanvasEmpty(rootNode)) {
                try {
                    tools.reCreateCanvas(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String text;
                if (CanvasContents.getAbsolutePath().endsWith("Tahdig\\out\\Untitled.tahdig"))
                    text = "Your previous unsaved session was reloaded";
                else
                    text = "Session Loaded Successfully";
                JFXDialog sessionLoaded = tools.generateNoticeDialog(text, "Reload", "src/main/resources/icons/Reload.png");

                JFXButton okButton = new JFXButton("OK");
                okButton.setFont(new Font(20));
                okButton.setOnAction(event -> sessionLoaded.close());
                ((JFXDialogLayout) sessionLoaded.getContent()).setActions(okButton);

                sessionLoaded.show(baseStack);
            }
        }

        canvas.setOnDragOver(event -> {
            if (event.getGestureSource() != canvas && event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
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
                    if (horizontalBar != null && verticalBar != null) break;
                }

                double x = event.getSceneX() + horizontalBar.valueProperty().getValue()*(width-1400) - 500;
                double y = event.getSceneY() + verticalBar.valueProperty().getValue()*(height-950) - 30;

                boolean isLocationOk = checkPositionAndResize(x, y);
                if (isLocationOk) {
                    try {
                        placeShapeOnCanvas(x, y, db.getString(), "");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    event.setDropCompleted(true);
                } else event.setDropCompleted(false);
            } else event.setDropCompleted(false);
            event.consume();
        });
    }

    public boolean checkPositionAndResize(double x, double y){

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

    public void placeShapeOnCanvas(double x, double y, String shape, String name) throws FileNotFoundException {

        StackPane stack;

        switch(shape) {

            case "circle":

                if (name == "") {
                    name = "Function" + defaultIdArray[0];
                    defaultIdArray[0] += 1;
                }
                stack = com.tahdig.tools.drawCircle(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new com.tahdig.elements.FunctionCircle.Actions(x, y, name, root, stack, baseStack);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "rectangle":

                if (name == "") {
                    name = "Class" + defaultIdArray[1];
                    defaultIdArray[1] += 1;
                }
                stack = com.tahdig.tools.drawRectangle(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new com.tahdig.elements.ClassRectangle.Actions(x, y, name, root, stack, baseStack);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "diamond":

                if (name == "") {
                    name = "Interface" + defaultIdArray[2];
                    defaultIdArray[2] += 1;
                }
                stack = com.tahdig.tools.drawDiamond(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new com.tahdig.elements.InterfaceDiamond.Actions(x, y, name, root, stack, baseStack);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "hexagon":

                if (name == "") {
                    name = "Package" + defaultIdArray[3];
                    defaultIdArray[3] += 1;
                }
                stack = com.tahdig.tools.drawHexagon(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new com.tahdig.elements.PackageHexagon.Actions(x, y, name, root, stack, baseStack);
                setCursor(stack);
                root.getChildren().add(stack);
                break;

            case "ellipse":

                if (name == "") {
                    name = "Header_File" + defaultIdArray[4];
                    defaultIdArray[4] += 1;
                }
                stack = com.tahdig.tools.drawEllipse(name);
                stack.setLayoutX(x);
                stack.setLayoutY(y);
                new com.tahdig.elements.HeaderFileEllipse.Actions(x, y, name, root, stack, baseStack);
                setCursor(stack);
                root.getChildren().add(stack);
        }
    }

    private void setCursor(StackPane stack){
        stack.setOnMouseEntered(event -> scene.setCursor(Cursor.HAND));
        stack.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT));
    }

    public void setDefaultId(int index, int id){
        defaultIdArray[index] = id;
    }

    public Group getRoot(){
        return root;
    }

    public ScrollPane getPane(){
        return scrollPane;
    }
}
