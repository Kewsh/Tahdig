package elements.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static elements.actions.ConnectionBuilder.drawConnectionLine;

public class PackageHexagonActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private StackPane stack, actionsStack, deleteStack, connectionsStack;
    private JFXPopup actionsPopup, connectionsPopup;
    private JFXButton deleteButton, deleteDialogConfirmButton, deleteDialogCancelButton;
    private JFXButton connectionsButton, containmentButton, editButton;
    private JFXDialog deleteDialog;
    private JFXDialogLayout deleteDialogLayout;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement rename

    public PackageHexagonActions(double x, double y, String name, Group root, StackPane stack, File CanvasContents) throws FileNotFoundException {

        this.x = x;
        this.y = y;
        this.name = name;
        this.root = root;
        this.stack = stack;
        this.CanvasContents = CanvasContents;
        addPackageToCanvasContents();

        connectionsButton = new JFXButton("Connect");
        containmentButton = new JFXButton("Containment");

        setButtonStyles(connectionsButton, "connectionsButton", 50, 70);
        setButtonStyles(containmentButton, "containmentButton", 40, 50);

        deleteButton = new JFXButton();
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
        deleteButton.setDisableVisualFocus(true);

        editButton = new JFXButton();
        path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
        editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
        editButton.setMinSize(68,70);
        editButton.setDisableVisualFocus(true);

        // edit button

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXTextField nameField = new JFXTextField();
                nameField.setText(getName());

                JFXButton applyChangesButton = new JFXButton("Apply");
                JFXDialog packageEditDialog = new JFXDialog(new StackPane(),
                        new Region(),
                        JFXDialog.DialogTransition.CENTER,
                        true);

                VBox editVBox = new VBox(nameField, applyChangesButton);
                editVBox.setSpacing(30);
                editVBox.setMaxHeight(200);

                JFXDialogLayout packageEditLayout = new JFXDialogLayout();

                packageEditLayout.setBody(editVBox);
                packageEditDialog.setContent(packageEditLayout);

                StackPane packageEditStack = new StackPane();
                packageEditStack.setLayoutX(x + 130);
                packageEditStack.setLayoutY(y);

                root.getChildren().add(packageEditStack);
                packageEditDialog.show(packageEditStack);

                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false};                  //specifies whether each error label is set

                nameNotGiven.setStyle("-fx-text-fill: red;");
                nameAlreadyExists.setStyle("-fx-text-fill: red;");

                applyChangesButton.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {

                        for (int i = 0; i < 2; i++)
                            errorFlags[i] = false;

                        if (editVBox.getChildren().contains(nameAlreadyExists))
                            editVBox.getChildren().remove(nameAlreadyExists);
                        if (editVBox.getChildren().contains(nameNotGiven))
                            editVBox.getChildren().remove(nameNotGiven);

                        String inputName = nameField.getText();
                        if (inputName.equals("")) {
                            editVBox.getChildren().add(nameNotGiven);
                            errorFlags[0] = true;
                        }
                        else{
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = null;

                            try {
                                rootNode = objectMapper.readTree(CanvasContents);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!getName().equals(inputName)) {
                                ArrayNode packages = (ArrayNode) rootNode.get("packages");
                                for (JsonNode package_iter : packages) {
                                    if (package_iter.get("name").textValue().equals(inputName)) {
                                        editVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[1] = true;
                                        break;
                                    }
                                }
                            }
                            if (!errorFlags[0] && !errorFlags[1]){

                                ArrayNode packages = (ArrayNode) rootNode.get("packages");
                                ObjectNode targetPackage = null;
                                int index = 0;
                                for (int i = 0; i < packages.size(); i++){
                                    ObjectNode info = (ObjectNode) packages.get(i).get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                        targetPackage = (ObjectNode) packages.get(i);
                                        index = i;
                                        break;
                                    }
                                }
                                ObjectNode targetInfo = (ObjectNode) targetPackage.get("info");

                                targetPackage.put("name", inputName);
                                targetPackage.put("info", targetInfo);
                                packages.remove(index);
                                packages.add(targetPackage);

                                setName(inputName);

                                for (Node node : root.getChildren()){
                                    if (node == stack){
                                        Text text = (Text) stack.getChildren().get(1);
                                        text.setText(inputName);                            // updating the name on the shape
                                        break;
                                    }
                                }

                                packageEditDialog.close();
                            }
                            try {
                                objectMapper.writeValue(CanvasContents, rootNode);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //TODO: handle duplicate dialogs here (basically when two different "edit" dialogs are shown)

                //

            }
        });

        // delete button

        deleteButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                for (Node node : root.getChildren()){
                    if (node == stack){

                        deleteDialog = new JFXDialog(new StackPane(),
                                new Region(),
                                JFXDialog.DialogTransition.CENTER,
                                false);
                        deleteDialogConfirmButton = new JFXButton("Yes");
                        deleteDialogCancelButton = new JFXButton("Cancel");

                        deleteDialogConfirmButton.setOnAction(new EventHandler<ActionEvent>(){
                            @Override
                            public void handle(ActionEvent event) {
                                root.getChildren().remove(node);
                                actionsPopup.hide();
                                deleteDialog.close();

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;

                                try {
                                    rootNode = objectMapper.readTree(CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ArrayNode packages = (ArrayNode) rootNode.get("packages");
                                for (int i = 0; i < packages.size(); i++){
                                    ObjectNode temp_package = (ObjectNode) packages.get(i);
                                    ObjectNode info = (ObjectNode) temp_package.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y)
                                        packages.remove(i);
                                }
                                try {
                                    objectMapper.writeValue(CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //TODO: hide and close all dialogs and popups related to this object here
                                //TODO: also handle all relations and dependencies
                            }
                        });
                        deleteDialogCancelButton.setOnAction(new EventHandler<ActionEvent>(){
                            @Override
                            public void handle(ActionEvent event) {
                                deleteDialog.close();
                            }
                        });

                        deleteDialogLayout = new JFXDialogLayout();
                        deleteDialogLayout.setBody(new Text("Are you sure you want to delete " + name + "?"));
                        deleteDialogLayout.setActions(deleteDialogCancelButton, deleteDialogConfirmButton);
                        deleteDialog.setContent(deleteDialogLayout);

                        deleteStack = new StackPane();
                        deleteStack.setLayoutX(x+150);
                        deleteStack.setLayoutY(y);
                        root.getChildren().add(deleteStack);
                        deleteDialog.show(deleteStack);

                        break;
                    }
                }
            }
        });

        // connections button

        connectionsStack = new StackPane();
        connectionsStack.setLayoutX(x - 5);
        connectionsStack.setLayoutY(y - 5);
        connectionsStack.setMinHeight(100);
        root.getChildren().add(connectionsStack);

        connectionsPopup = new JFXPopup();
        connectionsPopup.setPopupContent(new VBox(containmentButton));
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
                    connectionsPopup.show(connectionsStack);
                flag[0] = !flag[0];
            }
        });

        // containment button

        containmentButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXPopup containmentPopup = new JFXPopup();
                StackPane containmentStack = new StackPane();
                root.getChildren().add(containmentStack);

                containmentStack.setLayoutX(x + 115);
                containmentStack.setLayoutY(y - 5);
                containmentPopup.setAutoHide(true);
                containmentPopup.setHideOnEscape(true);
                JFXComboBox containmentComboBox = new JFXComboBox();
                containmentComboBox.setPromptText("Containment target");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                for (JsonNode interf_iter : interfaces)

                    //TODO: handle duplicates here (i.e. interfaces and classes that already are connected via containment with this package)

                    containmentComboBox.getItems().add(interf_iter.get("name").textValue());
                for (JsonNode class_iter : classes)
                    containmentComboBox.getItems().add(class_iter.get("name").textValue());
                try {
                    objectMapper.writeValue(CanvasContents, rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                containmentPopup.setPopupContent(containmentComboBox);
                containmentPopup.show(containmentStack);

                //TODO: handle duplicates

                containmentComboBox.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = null;
                        ObjectNode targetElement = null;
                        boolean isInterface = false;
                        try {
                            rootNode = objectMapper.readTree(CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                        for (JsonNode interf_iter : interfaces){
                            if (interf_iter.get("name").textValue().equals(containmentComboBox.getValue().toString())){
                                targetElement = (ObjectNode) interf_iter;
                                isInterface = true;
                                break;
                            }
                        }
                        if (targetElement == null){
                            for (JsonNode class_iter : classes){
                                if (class_iter.get("name").textValue().equals(containmentComboBox.getValue().toString())){
                                    targetElement = (ObjectNode) class_iter;
                                    break;
                                }
                            }
                        }
                        try {
                            objectMapper.writeValue(CanvasContents, rootNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ObjectNode targetInfo = (ObjectNode) targetElement.get("info");
                        drawConnectionLine(root, CanvasContents, 'p', isInterface ? 'i':'c', "containment", x, y, targetInfo.get("x").doubleValue(), targetInfo.get("y").doubleValue());
                        containmentPopup.hide();
                        connectionsPopup.hide();
                        actionsPopup.hide();
                    }
                });
            }
        });

        // actions button

        actionsStack = new StackPane();
        actionsStack.setLayoutX(x-35);
        actionsStack.setLayoutY(y-85);
        actionsStack.setMinHeight(100);
        root.getChildren().add(actionsStack);

        actionsPopup = new JFXPopup();
        actionsPopup.setPopupContent(new HBox(editButton, connectionsButton, deleteButton));
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

    private void setName(String name){
        this.name = name;
    }

    private String getName(){
        return this.name;
    }

    private void setButtonStyles(JFXButton button, String buttonName, double width, double length){
        button.setMinSize(width, length);
        button.setId(buttonName);
        button.setDisableVisualFocus(true);
    }

    private void addPackageToCanvasContents(){

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayNode packages = (ArrayNode) rootNode.get("packages");
        ObjectNode info = objectMapper.createObjectNode();
        ObjectNode thisPack = objectMapper.createObjectNode();

        info.put("x", x);
        info.put("y", y);
        info.put("classes", objectMapper.createArrayNode());
        info.put("interfaces", objectMapper.createArrayNode());

        thisPack.put("name", name);
        thisPack.put("info", info);

        packages.add(thisPack);
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
