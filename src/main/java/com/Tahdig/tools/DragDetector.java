package com.Tahdig.tools;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public abstract class DragDetector {

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
}
