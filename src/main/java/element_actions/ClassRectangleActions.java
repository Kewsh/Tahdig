package element_actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ClassRectangleActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private StackPane stack, actionsStack, connectionsStack, attributesStack;
    private JFXPopup actionsPopup, connectionsPopup;
    private JFXButton connectionsButton, methodsButton, attributesButton, deleteButton;
    private JFXButton compositionButton, generalizationButton, implementationButton, containmentButton;
    private JFXDialog attributesDialog;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement rename

    public ClassRectangleActions(double x, double y, String name, Group root, StackPane stack, File CanvasContents) throws FileNotFoundException {

        this.x = x;
        this.y = y;
        this.name = name;
        this.root = root;
        this.stack = stack;
        this.CanvasContents = CanvasContents;
        addClassToCanvasContents();

        connectionsButton = new JFXButton("Connect");
        methodsButton = new JFXButton("Methods");
        attributesButton = new JFXButton("Attributes");

        deleteButton = new JFXButton();
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));

        setButtonStyles(connectionsButton, "connectionsButton", 50, 70);
        setButtonStyles(methodsButton, "methodsButton", 50, 70);
        setButtonStyles(attributesButton, "attributesButton", 50, 70);

        compositionButton = new JFXButton("Composition");
        generalizationButton = new JFXButton("Generalization");
        implementationButton = new JFXButton("Implementation");
        containmentButton = new JFXButton("Containment");

        setButtonStyles(compositionButton, "compositionButton", 135, 50);
        setButtonStyles(generalizationButton, "generalizationButton", 135, 50);
        setButtonStyles(implementationButton, "implementationButton", 135, 50);
        setButtonStyles(containmentButton, "containmentButton", 135, 50);

        // attributes button

        attributesStack = new StackPane();
        attributesStack.setLayoutX(x + 130);
        attributesStack.setLayoutY(y);
        root.getChildren().add(attributesStack);

        attributesDialog = new JFXDialog(attributesStack,
                new Region(),
                JFXDialog.DialogTransition.CENTER,
                false);

        setAttributesDialogLayout();

        attributesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                attributesDialog.show();

                //TODO: don't forget to handle popups and other dialogs that could be on top of this

            }
        });

        // connections button

        connectionsStack = new StackPane();
        connectionsStack.setLayoutX(x - 125);
        connectionsStack.setLayoutY(y - 15);
        connectionsStack.setMinHeight(100);
        root.getChildren().add(connectionsStack);

        connectionsPopup = new JFXPopup();
        connectionsPopup.setPopupContent(new VBox(compositionButton, generalizationButton, implementationButton, containmentButton));
        connectionsPopup.setAutoHide(true);
        connectionsPopup.setHideOnEscape(true);

        connectionsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (flag[0] && flag[1]) {
                    flag[0] = false;
                    flag[1] = false;
                }
                if (flag[0])
                    connectionsPopup.hide();
                else
                    connectionsPopup.show(stack);
                flag[0] = !flag[0];
            }
        });

        // actions button

        actionsStack = new StackPane();
        actionsStack.setLayoutX(x-125);
        actionsStack.setLayoutY(y-85);
        actionsStack.setMinHeight(100);
        root.getChildren().add(actionsStack);

        actionsPopup = new JFXPopup();
        actionsPopup.setPopupContent(new HBox(connectionsButton, attributesButton, methodsButton, deleteButton));
        actionsPopup.setAutoHide(true);
        actionsPopup.setHideOnEscape(true);

        stack.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                actionsPopup.show(actionsStack);
            }
        });

        actionsPopup.setOnHiding(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                if (flag[0])
                    flag[1] = true;
            }
        });
    }

    private void addClassToCanvasContents(){

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        ObjectNode info = objectMapper.createObjectNode();
        ObjectNode thisClass = objectMapper.createObjectNode();

        info.put("x", x);
        info.put("y", y);
        info.put("attributes", objectMapper.createArrayNode());
        info.put("methods", objectMapper.createArrayNode());

        thisClass.put("name", name);
        thisClass.put("info", info);

        classes.add(thisClass);
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setButtonStyles(JFXButton button, String buttonName, double width, double length){
        button.setMinSize(width, length);
        button.setId(buttonName);
    }

    private JFXTreeTableView getAttributes(){

        //TODO: design jfxtreetableview for output

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode info;
        ArrayNode attributes = null;
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes){
            info = (ObjectNode) class_iter.get("info");
            if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                attributes = (ArrayNode) info.get("attributes");
                break;
            }
        }
        for (JsonNode attribute : attributes)
            System.out.println(attribute.get("access").textValue() + " "
                    + (attribute.get("extra").textValue().equals("") ? "" : (attribute.get("extra").textValue() + " "))
                    + attribute.get("type").textValue() + " " + attribute.get("name").textValue());
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JFXTreeTableView();
    }

    private void setAttributesDialogLayout(){

        JFXDialogLayout attributesDialogLayout = new JFXDialogLayout();
        JFXButton attributesCloseButton = new JFXButton("Close");

        attributesCloseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                attributesDialog.close();
            }
        });
        attributesDialogLayout.setHeading(new Label("Attributes"));
        //attributesDialogLayout.setBody(getAttributes());
        attributesDialogLayout.setActions(attributesCloseButton);

        attributesDialog.setContent(attributesDialogLayout);
    }
}
