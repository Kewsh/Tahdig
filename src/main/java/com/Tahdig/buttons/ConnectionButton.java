package com.Tahdig.buttons;

import com.Tahdig.DrawingPane;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jfoenix.controls.*;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import com.Tahdig.tools.ConnectionBuilder.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConnectionButton {

    private ObjectMapper objectMapper;
    private JsonNode rootNode;
    private HBox connectionHbox;
    private Group root;
    private JFXButton connectionButton, formConnectionButton, closeButton;
    private JFXDialog connectionsDialog;
    private JFXDialogLayout connectionsDialogLayout;
    private JFXComboBox targetsComboBox, connectionsComboBox;

    public ConnectionButton(double x, double y, JFXPopup actionsPopup, StackPane baseStack, Group root, Element element){

        this.root = root;

        targetsComboBox = new JFXComboBox();
        targetsComboBox.setPromptText("Target");
        targetsComboBox.setPadding(new Insets(20, 0, 0, 0));
        targetsComboBox.setStyle("-fx-font-size: 18px;");
        connectionsComboBox = new JFXComboBox();
        connectionsComboBox.setPromptText("Connection");
        connectionsComboBox.setPadding(new Insets(20, 0, 0, 0));
        connectionsComboBox.setStyle("-fx-font-size: 18px;");

        switch(element){
            case RECTANGLE:
                connectionsComboBox.getItems().add("Inheritance");
                connectionsComboBox.getItems().add("Implementation");
                connectionsComboBox.getItems().add("Composition");
                break;
            case DIAMOND:
                connectionsComboBox.getItems().add("Inheritance");
                break;
            case HEXAGON:
                connectionsComboBox.getItems().add("Containment");
        }
        connectionsComboBox.setOnAction(event -> setTargets(x, y, element));

        connectionsDialog = new JFXDialog(new StackPane(),
                new Region(),
                JFXDialog.DialogTransition.CENTER,
                true);
        connectionsDialogLayout = new JFXDialogLayout();

        ImageView connectionIcon = null;
        try {
            connectionIcon = new ImageView(new Image(new FileInputStream(new File("src/main/resources/icons/Connect.png").getAbsolutePath())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StackPane connectionIconStack = new StackPane(connectionIcon);
        connectionIconStack.setPadding(new Insets(20, 0, 0, 0));

        connectionHbox = new HBox(connectionIconStack, connectionsComboBox, targetsComboBox);
        connectionHbox.setSpacing(25);
        connectionsDialogLayout.setBody(connectionHbox);

        closeButton = new JFXButton("Close");
        closeButton.setFont(new Font(18));
        closeButton.setOnAction(event -> connectionsDialog.close());
        formConnectionButton = new JFXButton("Connect");
        formConnectionButton.setFont(new Font(18));
        formConnectionButton.setOnAction(event -> formConnection(x, y, element));

        connectionsDialogLayout.setActions(closeButton, formConnectionButton);
        connectionsDialogLayout.setHeading(new Label("Connect"));
        connectionsDialogLayout.setMinSize(500, 100);
        connectionsDialog.setContent(connectionsDialogLayout);

        connectionButton = new JFXButton("Connect");
        connectionButton.setMinSize(50, 70);
        connectionButton.setId("connectionsButton");
        connectionButton.setDisableVisualFocus(true);

        connectionButton.setOnAction(event -> {
            actionsPopup.hide();
            connectionsDialog.show(baseStack);
        });
    }

    private void formConnection(double x, double y, Element element){

        if (targetsComboBox.getValue() == null)
            return;         //TODO: print some error message?
        Point targetPoint;
        switch(connectionsComboBox.getValue().toString()){
            case "Inheritance":
                switch(element){
                    case RECTANGLE:
                        targetPoint = findTarget((ArrayNode) rootNode.get("classes"), targetsComboBox.getValue().toString());
                        com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'c', 'c', "inheritance",
                                x, y, targetPoint.x, targetPoint.y);
                        break;
                    case DIAMOND:
                        targetPoint = findTarget((ArrayNode) rootNode.get("interfaces"), targetsComboBox.getValue().toString());
                        com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'i', 'i', "inheritance",
                                x, y, targetPoint.x, targetPoint.y);
                }
                break;
            case "Implementation":          // implementation can only be from class to interface
                targetPoint = findTarget((ArrayNode) rootNode.get("interfaces"), targetsComboBox.getValue().toString());
                com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'c', 'i', "implementation",
                        x, y, targetPoint.x, targetPoint.y);
                break;
            case "Composition":             // composition can only be from class to class
                targetPoint = findTarget((ArrayNode) rootNode.get("classes"), targetsComboBox.getValue().toString());
                com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'c', 'c', "composition",
                        x, y, targetPoint.x, targetPoint.y);
                break;
            case "Containment":
                targetPoint = findTarget((ArrayNode) rootNode.get("classes"), targetsComboBox.getValue().toString());
                if (targetPoint == null){
                    targetPoint = findTarget((ArrayNode) rootNode.get("interfaces"), targetsComboBox.getValue().toString());
                    com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'p', 'i', "containment",
                            x, y, targetPoint.x, targetPoint.y);
                } else com.Tahdig.tools.ConnectionBuilder.drawConnectionLine(root, 'p', 'c', "containment",
                        x, y, targetPoint.x, targetPoint.y);
                break;
        }
        //TODO: maybe print some confirmation message?
        setTargets(x, y, element);          // update the targetsComboBox to not include the element we just formed a connection with
    }

    private Point findTarget(ArrayNode array, String name){
        for (JsonNode object : array){
            if (object.get("name").textValue().equals(name))
                return new Point(object.get("info").get("x").doubleValue(), object.get("info").get("y").doubleValue());
        }
        return null;
    }

    private void setTargets(double x, double y, Element element){

        objectMapper = new ObjectMapper();
        rootNode = null;
        try {
            rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String connectionType = connectionsComboBox.getValue().toString();
        ArrayNode array = null, array2 = null;
        if (connectionType.equals("Inheritance")){
            switch(element){
                case RECTANGLE:
                    array = (ArrayNode) rootNode.get("classes");
                    break;
                case DIAMOND:
                    array = (ArrayNode) rootNode.get("interfaces");
                    break;
            }
        } else if (connectionType.equals("Implementation"))
            array = (ArrayNode) rootNode.get("interfaces");
        else if (connectionType.equals("Composition"))
            array = (ArrayNode) rootNode.get("classes");
        else{     // containment
            array = (ArrayNode) rootNode.get("classes");
            array2 = (ArrayNode) rootNode.get("interfaces");
        }
        connectionHbox.getChildren().remove(2);         // remove previous targetsComboBox
        targetsComboBox = new JFXComboBox();
        targetsComboBox.setPromptText("Target");
        targetsComboBox.setPadding(new Insets(20, 0, 0, 0));
        targetsComboBox.setStyle("-fx-font-size: 18px;");
        connectionHbox.getChildren().add(targetsComboBox);

        String sourceName = getSourceName(x, y, element);
        for (JsonNode object : array)
            setValidTargets(object, x, y, sourceName, connectionType.toLowerCase());
        if (array2 != null){
            for (JsonNode object : array2)
                setValidTargets(object, x, y, sourceName, connectionType.toLowerCase());
        }
    }

    private void setValidTargets(JsonNode object, double x, double y, String sourceName, String connectionType){

        if (sourceName.equals(object.get("name").textValue()))
            return;             // source cannot be a target
        if (connectionType.equals("composition")){
            targetsComboBox.getItems().add(object.get("name").textValue());
            return;
        }
        boolean flag = false;
        double targetX = object.get("info").get("x").doubleValue();
        double targetY = object.get("info").get("y").doubleValue();

        for (JsonNode line : rootNode.get("lines")){
            if (line.get("type").textValue().equals(connectionType) && line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y &&
                    line.get("endX").doubleValue() == targetX && line.get("endY").doubleValue() == targetY){
                flag = true;
                break;
            }
        }
        if (!flag){
            targetsComboBox.getItems().add(object.get("name").textValue());
        }
    }

    private String getSourceName(double x, double y, Element element){

        ArrayNode array = null;
        switch(element){
            case RECTANGLE:
                array = (ArrayNode) rootNode.get("classes");
                break;
            case DIAMOND:
                array = (ArrayNode) rootNode.get("interfaces");
                break;
            case HEXAGON:
                array = (ArrayNode) rootNode.get("packages");
        }
        for (JsonNode object : array){
            if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                return object.get("name").textValue();
        }
        return null;
    }

    public JFXButton getButton(){
        return connectionButton;
    }
}
