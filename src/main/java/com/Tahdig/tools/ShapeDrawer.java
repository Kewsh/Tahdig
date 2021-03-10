package com.Tahdig.tools;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class ShapeDrawer {

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
}
