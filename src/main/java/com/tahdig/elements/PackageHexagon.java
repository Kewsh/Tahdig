package com.tahdig.elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.tahdig.DrawingPane;
import com.tahdig.buttons.*;
import com.tahdig.tools;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PackageHexagon {

    private final StackPane stack;

    public PackageHexagon(){

        stack = com.tahdig.tools.drawHexagon("Package");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            com.tahdig.tools.setOnDragDetected(stack, "hexagon");
        });
        stack.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });
    }

    public StackPane getElement(){
        return stack;
    }

    public static class Actions {

        private double x, y;
        private String name;
        private Group root;
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXButton deleteButton;
        private JFXButton connectionsButton, editButton;

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack) throws FileNotFoundException {

            this.x = x;
            this.y = y;
            this.name = name;
            this.root = root;
            this.stack = stack;
            addPackageToCanvasContents();

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-40);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            try {
                connectionsButton = (new ConnectionButton(x, y, actionsPopup, baseStack, root, tools.Element.HEXAGON)).getButton();
                deleteButton = (new DeleteButton(x, y, name, root, stack, baseStack, actionsPopup,
                        tools.Element.HEXAGON)).getButton();
                editButton = setUpEditButton(baseStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, connectionsButton, deleteButton));
            actionsPopup.setAutoHide(true);
            actionsPopup.setHideOnEscape(true);

            stack.setOnMouseClicked(event -> actionsPopup.show(actionsStack));
        }

        private JFXButton setUpEditButton(StackPane baseStack) throws IOException{

            JFXButton editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

            editButton.setOnAction(event -> {

                JFXTextField nameField = new JFXTextField();
                nameField.setText(getName());
                nameField.setPadding(new Insets(40, 0, 0, 0));
                nameField.setStyle("-fx-font-size: 18px;");

                JFXButton applyChangesButton = new JFXButton("Apply");
                applyChangesButton.setFont(new Font(18));
                JFXDialog packageEditDialog = new JFXDialog(new StackPane(),
                        new Region(),
                        JFXDialog.DialogTransition.CENTER,
                        true);

                ImageView editIcon = null;
                try {
                    editIcon = new ImageView(new Image(new FileInputStream(new File("src/main/resources/icons/Edit64.png").getAbsolutePath())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                StackPane editIconStack = new StackPane(editIcon);
                editIconStack.setPadding(new Insets(20, 0, 0, 0));

                HBox editHbox = new HBox(editIconStack, nameField);
                editHbox.setSpacing(30);
                VBox editVBox = new VBox(editHbox);
                JFXDialogLayout packageEditLayout = new JFXDialogLayout();
                packageEditLayout.setBody(editVBox);
                packageEditLayout.setActions(applyChangesButton);
                packageEditLayout.setHeading(new Label("Edit"));
                packageEditDialog.setContent(packageEditLayout);

                actionsPopup.hide();
                packageEditDialog.show(baseStack);

                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameNotValid = new Label("*name contains illegal characters");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false, false};                  //specifies whether each error label is set

                nameNotGiven.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                nameNotValid.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                nameAlreadyExists.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");

                applyChangesButton.setOnAction(event1 -> {

                    for (int i = 0; i < 3; i++)
                        errorFlags[i] = false;

                    if (editVBox.getChildren().contains(nameAlreadyExists))
                        editVBox.getChildren().remove(nameAlreadyExists);
                    if (editVBox.getChildren().contains(nameNotGiven))
                        editVBox.getChildren().remove(nameNotGiven);
                    if (editVBox.getChildren().contains(nameNotValid))
                        editVBox.getChildren().remove(nameNotValid);

                    String inputName = nameField.getText();
                    if (inputName.equals("")) {
                        editVBox.getChildren().add(nameNotGiven);
                        errorFlags[0] = true;
                    } else if (!checkNameValidity(inputName)){
                        editVBox.getChildren().add(nameNotValid);
                        errorFlags[1] = true;
                    }
                    else{
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = null;

                        try {
                            rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!getName().equals(inputName)) {
                            ArrayNode packages = (ArrayNode) rootNode.get("packages");
                            for (JsonNode package_iter : packages) {
                                if (package_iter.get("name").textValue().equals(inputName)) {
                                    editVBox.getChildren().add(nameAlreadyExists);
                                    errorFlags[2] = true;
                                    break;
                                }
                            }
                        }
                        if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

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
                            objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
            return editButton;
        }

        private void setName(String name){
            this.name = name;
        }

        private String getName(){
            return this.name;
        }

        private boolean checkNameValidity(String name){

            if (!Character.isAlphabetic(name.charAt(0)) && name.charAt(0) != '_')
                return false;
            boolean state = true;
            for (int i = 0; i < name.length(); i++){
                char c = name.charAt(i);
                if (!Character.isDigit(c) && !Character.isAlphabetic(c) && c != '_'){
                    state = false;
                    break;
                }
            }
            return state;
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
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayNode packages = (ArrayNode) rootNode.get("packages");
            ObjectNode info = objectMapper.createObjectNode();
            ObjectNode thisPack = objectMapper.createObjectNode();

            info.put("x", x);
            info.put("y", y);
            thisPack.put("name", name);
            thisPack.put("info", info);

            packages.add(thisPack);
            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
