package com.tahdig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class tools {

    private tools(){}       // tools is a utility class and can not be instantiated

    public static void reCreateCanvas(DrawingPane drawingPane) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        placeShapesOnCanvas((ArrayNode) rootNode.get("classes"), "rectangle", drawingPane, 1);
        placeShapesOnCanvas((ArrayNode) rootNode.get("interfaces"), "diamond", drawingPane, 2);
        placeShapesOnCanvas((ArrayNode) rootNode.get("functions"), "circle", drawingPane, 0);
        placeShapesOnCanvas((ArrayNode) rootNode.get("headers"), "ellipse", drawingPane, 4);
        placeShapesOnCanvas((ArrayNode) rootNode.get("packages"), "hexagon", drawingPane, 3);

        for (JsonNode line : rootNode.get("lines")){

            double srcX = line.get("startX").doubleValue();
            double srcY = line.get("startY").doubleValue();
            double destX = line.get("endX").doubleValue();
            double destY = line.get("endY").doubleValue();

            char objectType1 = findTargetObject(srcX, srcY, rootNode);
            char objectType2 = findTargetObject(destX, destY, rootNode);
            drawConnectionLine(drawingPane.getRoot(), objectType1, objectType2, line.get("type").toString(), srcX, srcY, destX, destY);
        }
        try {
            objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char findTargetObject(double x, double y, JsonNode rootNode){

        for (JsonNode object : rootNode.get("classes")){
            if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                return 'c';
        }
        for (JsonNode object : rootNode.get("interfaces")){
            if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                return 'i';
        }
        for (JsonNode object : rootNode.get("packages")){
            if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                return 'p';
        }
        return 0;
    }

    private static void placeShapesOnCanvas(ArrayNode array, String shapeType, DrawingPane drawingPane, int defaultIdArrayIndex) throws IOException{
        for (JsonNode object : array){
            double x = object.get("info").get("x").doubleValue();
            double y = object.get("info").get("y").doubleValue();
            drawingPane.checkPositionAndResize(x, y);
            drawingPane.placeShapeOnCanvas(x, y, shapeType, object.get("name").textValue());
        }
        drawingPane.setDefaultId(defaultIdArrayIndex, array.size()+1);
    }

    public static void drawConnectionLine(Group root, char type1, char type2, String connectionType, double srcX, double srcY, double destX, double destY){

        Line line;
        Point[] srcPoints;
        Point[] destPoints;

        if (type1 == 'c')                   // class
            srcPoints = new Point[]{new Point(srcX, srcY+60), new Point(srcX+50, srcY), new Point(srcX+100, srcY+60), new Point(srcX+50, srcY+120)};
        else if (type1 == 'i') {            // interface
            srcY += 60;
            srcPoints = new Point[]{new Point(srcX, srcY), new Point(srcX + 75, srcY - 60), new Point(srcX + 150, srcY), new Point(srcX + 75, srcY + 60)};
        }
        else {                              // package
            srcY += 50;
            srcPoints = new Point[]{new Point(srcX, srcY), new Point(srcX + 75, srcY - 50), new Point(srcX + 150, srcY), new Point(srcX + 75, srcY + 50)};
        }
        if (type2 == 'c')
            destPoints = new Point[]{new Point(destX, destY+60), new Point(destX+50, destY), new Point(destX+100, destY+60), new Point(destX+50, destY+120)};
        else {                              // interface
            destY += 60;
            destPoints = new Point[]{new Point(destX, destY), new Point(destX+75, destY-60), new Point(destX+150, destY), new Point(destX+75, destY+60)};
        }

        if (destY < srcY)
            line = new Line(srcPoints[1].x, srcPoints[1].y, destPoints[3].x, destPoints[3].y);
        else if (destY > srcY+120)
            line = new Line(srcPoints[3].x, srcPoints[3].y, destPoints[1].x, destPoints[1].y);
        else if (destX >= srcX)
            line = new Line(srcPoints[2].x, srcPoints[2].y, destPoints[0].x, destPoints[0].y);
        else
            line = new Line(srcPoints[0].x, srcPoints[0].y, destPoints[2].x, destPoints[2].y);

        line.setStrokeWidth(2.2);
        line.setStroke(Paint.valueOf("white"));
        root.getChildren().add(line);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayNode lines = (ArrayNode) rootNode.get("lines");
        ObjectNode targetLine = objectMapper.createObjectNode();

        targetLine.put("type", connectionType);
        targetLine.put("startX", srcX);
        targetLine.put("startY", type1 == 'i' ? srcY-60 : type1 == 'p' ? srcY-50 : srcY);
        targetLine.put("endX", destX);
        targetLine.put("endY", type2 == 'i' ? destY-60 : destY);

        lines.add(targetLine);
        try {
            objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: insert the connectionType string somewhere on the line
        //TODO: work more on the connections algorithm

    }

    public static void setOnDragDetected(StackPane stack, String type) {

        Dragboard db = stack.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(type);
        String path = "";

        switch(type) {
            case "circle":
                path = new File("src/main/resources/icons/FunctionCircle.png").getAbsolutePath();
                break;
            case "rectangle":
                path = new File("src/main/resources/icons/ClassRectangle.png").getAbsolutePath();
                break;
            case "diamond":
                path = new File("src/main/resources/icons/InterfaceDiamond.png").getAbsolutePath();
                break;
            case "hexagon":
                path = new File("src/main/resources/icons/PackageHexagon.png").getAbsolutePath();
                break;
            default:                // ellipse
                path = new File("src/main/resources/icons/HeaderFileEllipse.png").getAbsolutePath();
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Image image = new Image(input);
        ImageView imageView = new ImageView(image);
        db.setDragView(imageView.getImage());
        db.setContent(content);
    }

    private static StackPane putShapeOnStackPane(javafx.scene.shape.Shape shape, String text){

        Text shapeText = new Text(text);
        shapeText.setFont(new Font("monospace", 20));
        StackPane shapeStack = new StackPane();
        shapeStack.getChildren().addAll(shape, shapeText);
        return shapeStack;
    }

    public static StackPane drawEllipse(String text){

        Ellipse ellipse = new Ellipse(0.0, 0.0, 100.0, 50.0);
        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(2);
        ellipse.setFill(Paint.valueOf("#6593f5"));

        return putShapeOnStackPane(ellipse, text);
    }

    public static StackPane drawHexagon(String text){

        Polygon hexagon = new Polygon();
        hexagon.getPoints().addAll(0.0, 50.0,
                50.0, 0.0,
                100.0, 0.0,
                150.0, 50.0,
                100.0, 100.0,
                50.0, 100.0);
        hexagon.setStroke(Color.BLACK);
        hexagon.setStrokeWidth(2);
        hexagon.setFill(Paint.valueOf("#6593f5"));

        return putShapeOnStackPane(hexagon, text);
    }

    public static StackPane drawDiamond(String text){

        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(300.0, 60.0,
                375.0, 0.0,
                450.0, 60.0,
                375.0, 120.0);
        diamond.setStroke(Color.BLACK);
        diamond.setStrokeWidth(2);
        diamond.setFill(Paint.valueOf("#6593f5"));

        return putShapeOnStackPane(diamond, text);
    }

    public static StackPane drawCircle(String text){

        Circle circle = new Circle();
        circle.setRadius(60);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setFill(Paint.valueOf("#6593f5"));

        return putShapeOnStackPane(circle, text);
    }

    public static StackPane drawRectangle(String text){

        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(100);
        rectangle.setHeight(120);
        rectangle.setArcWidth(30.0);
        rectangle.setArcHeight(20.0);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(2);
        rectangle.setFill(Paint.valueOf("#6593f5"));

        return putShapeOnStackPane(rectangle, text);
    }

    public static DrawingPane generateNewDrawingPane(Stage primaryStage, HBox hBox, Scene scene, StackPane stackPane, File file){

        hBox.getChildren().remove(2);
        File temp = new File("out/Untitled.tahdig");
        if (temp.exists()) temp.delete();
        if (file == null)
            DrawingPane.CanvasContents = new File("out/Untitled.tahdig");
        else DrawingPane.CanvasContents = file;
        DrawingPane drawingPane = new DrawingPane(scene, stackPane);
        primaryStage.setTitle(cutExtension(DrawingPane.CanvasContents.getName()) + " - Tahdig");
        hBox.getChildren().add(drawingPane.getPane());
        return drawingPane;
    }

    public static String cutExtension(String string){
        return string.split(".tahdig$")[0];
    }

    public static boolean isCanvasEmpty(JsonNode rootNode){

        if (rootNode.get("classes").size() == 0 && rootNode.get("interfaces").size() == 0 && rootNode.get("functions").size() == 0 &&
                rootNode.get("headers").size() == 0 && rootNode.get("packages").size() == 0)
            return true;
        else return false;
    }

    public static JFXDialog generateNoticeDialog(String text, String heading, String iconPath){

        Text dialogText = new Text(text);
        dialogText.setFont(Font.font("Muli", 20));
        StackPane textStack = new StackPane(dialogText);
        textStack.setPadding(new Insets(20, 0, 0, 0));

        JFXDialog noticeDialog = new JFXDialog(new StackPane(),
                new Region(),
                JFXDialog.DialogTransition.CENTER,
                true);
        JFXDialogLayout noticeDialogLayout = new JFXDialogLayout();

        ImageView noticeIcon = null;
        try {
            noticeIcon = new ImageView(new Image(new FileInputStream(new File(iconPath).getAbsolutePath())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StackPane iconStack = new StackPane(noticeIcon);
        iconStack.setPadding(new Insets(20, 0, 0, 0));

        HBox hBox = new HBox(iconStack, textStack);
        hBox.setSpacing(30);
        noticeDialogLayout.setBody(hBox);
        noticeDialogLayout.setHeading(new Label(heading));
        noticeDialog.setContent(noticeDialogLayout);

        return noticeDialog;
    }

    public static final class Point{
        public double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
