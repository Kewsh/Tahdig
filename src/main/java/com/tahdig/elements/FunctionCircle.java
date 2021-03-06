package com.tahdig.elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.tahdig.DrawingPane;
import com.tahdig.tools;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FunctionCircle {

    private final StackPane stack;

    public FunctionCircle(){

        stack = com.tahdig.tools.drawCircle("Function");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            com.tahdig.tools.setOnDragDetected(stack, "circle");
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
        private String name, returnType;
        private Group root;
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXButton editButton, deleteButton;

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack) throws FileNotFoundException {

            this.x = x;
            this.y = y;
            this.name = name;
            this.returnType = "void";
            this.root = root;
            this.stack = stack;
            addFunctionToCanvasContents();

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-10);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            try {
                deleteButton = (new com.tahdig.buttons.DeleteButton(x, y, name, root, stack, baseStack, actionsPopup,
                        tools.Element.CIRCLE)).getButton();
                editButton = setUpEditButton(baseStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, deleteButton));
            actionsPopup.setAutoHide(true);
            actionsPopup.setHideOnEscape(true);

            stack.setOnMouseClicked(event -> actionsPopup.show(actionsStack));
        }

        private JFXButton setUpEditButton(StackPane baseStack) throws IOException {

            JFXButton editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

            editButton.setOnAction(event -> {

                List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                ToggleGroup typeGroup = new ToggleGroup();
                VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup);
                typeButtonsVBox.setPadding(new Insets(40, 0, 0, 0));
                typeButtonsVBox.setStyle("-fx-font-size: 18px;");
                typeButtonsVBox.setMinWidth(100);

                JFXTextField nameField = new JFXTextField();
                nameField.setText(getName());
                nameField.setPadding(new Insets(100, 0, 0, 0));
                //TODO: these insets are inaccurate, however, it does not matter since we're about to change the whole
                //  design of these boxes, (i.e. not gonna be using radio buttons)
                nameField.setStyle("-fx-font-size: 18px;");

                JFXButton applyChangesButton = new JFXButton("Apply");
                applyChangesButton.setFont(new Font(18));
                JFXDialog functionEditDialog = new JFXDialog(new StackPane(),
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

                HBox editHBox = new HBox(editIconStack, typeButtonsVBox, nameField);
                editHBox.setSpacing(30);
                VBox editVBox = new VBox(editHBox);
                JFXDialogLayout functionEditLayout = new JFXDialogLayout();
                functionEditLayout.setBody(editVBox);
                functionEditLayout.setActions(applyChangesButton);
                functionEditLayout.setHeading(new Label("Edit"));
                functionEditDialog.setContent(functionEditLayout);

                actionsPopup.hide();
                functionEditDialog.show(baseStack);

                Label typeNotSpecified = new Label("*no return type specified");
                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameNotValid = new Label("*name contains illegal characters");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false, false, false};               //specifies whether each error label is set

                typeNotSpecified.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                nameNotGiven.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                nameNotValid.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                nameAlreadyExists.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");

                applyChangesButton.setOnAction(event1 -> {

                    for (int i = 0; i < 4; i++)
                        errorFlags[i] = false;

                    if (editVBox.getChildren().contains(nameAlreadyExists))
                        editVBox.getChildren().remove(nameAlreadyExists);
                    if (editVBox.getChildren().contains(typeNotSpecified))
                        editVBox.getChildren().remove(typeNotSpecified);
                    if (editVBox.getChildren().contains(nameNotGiven))
                        editVBox.getChildren().remove(nameNotGiven);
                    if (editVBox.getChildren().contains(nameNotValid))
                        editVBox.getChildren().remove(nameNotValid);

                    JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                    if (selectedType == null) {
                        editVBox.getChildren().add(typeNotSpecified);
                        errorFlags[0] = true;
                    }

                    String inputName = nameField.getText();
                    if (inputName.equals("")) {
                        editVBox.getChildren().add(nameNotGiven);
                        errorFlags[1] = true;
                    } else if (!checkNameValidity(inputName)){
                        editVBox.getChildren().add(nameNotValid);
                        errorFlags[2] = true;
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = null;

                        try {
                            rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!getName().equals(inputName)) {
                            ArrayNode classes = (ArrayNode) rootNode.get("classes");
                            for (JsonNode class_iter : classes) {
                                if (class_iter.get("name").textValue().equals(inputName)) {
                                    editVBox.getChildren().add(nameAlreadyExists);
                                    errorFlags[3] = true;
                                    break;
                                }
                            }
                            if (!errorFlags[3]) {
                                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                for (JsonNode interf_iter : interfaces) {
                                    if (interf_iter.get("name").textValue().equals(inputName)) {
                                        editVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[3] = true;
                                        break;
                                    }
                                }
                            }
                            if (!errorFlags[3]) {
                                ArrayNode functions = (ArrayNode) rootNode.get("functions");
                                for (JsonNode func_iter : functions) {
                                    if (func_iter.get("name").textValue().equals(inputName)) {
                                        editVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[3] = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3]){

                            ArrayNode functions = (ArrayNode) rootNode.get("functions");
                            ObjectNode function = null;
                            int index = 0;
                            for (int i = 0; i < functions.size(); i++){
                                ObjectNode info = (ObjectNode) functions.get(i).get("info");
                                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                    function = (ObjectNode) functions.get(i);
                                    index = i;
                                    break;
                                }
                            }
                            ObjectNode targetInfo = (ObjectNode) function.get("info");

                            targetInfo.put("return", selectedType.getText());
                            function.put("name", inputName);
                            function.put("info", targetInfo);
                            functions.remove(index);
                            functions.add(function);

                            setReturnType(selectedType.getText());
                            setName(inputName);

                            for (Node node : root.getChildren()){
                                if (node == stack){
                                    Text text = (Text) stack.getChildren().get(1);
                                    text.setText(inputName);                            // updating the name on the shape
                                    break;
                                }
                            }

                            functionEditDialog.close();
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

        private String getName(){
            return this.name;
        }

        private void setName(String name){
            this.name = name;
        }

        private void setReturnType(String returnType){
            this.returnType = returnType;
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

        private VBox createTypeButtonsVBox(List<JFXRadioButton> typeButtons, ToggleGroup typeGroup){

            typeButtons.add(new JFXRadioButton("void"));
            typeButtons.add(new JFXRadioButton("boolean"));
            typeButtons.add(new JFXRadioButton("byte"));
            typeButtons.add(new JFXRadioButton("char"));
            typeButtons.add(new JFXRadioButton("short"));
            typeButtons.add(new JFXRadioButton("int"));
            typeButtons.add(new JFXRadioButton("long"));
            typeButtons.add(new JFXRadioButton("float"));
            typeButtons.add(new JFXRadioButton("double"));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode classes = (ArrayNode) rootNode.get("classes");
            for (JsonNode class_iter : classes)
                typeButtons.add(new JFXRadioButton(class_iter.get("name").textValue()));
            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
            for (JsonNode interf_iter : interfaces)
                typeButtons.add(new JFXRadioButton(interf_iter.get("name").textValue()));

            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            VBox typeButtonsVBox = new VBox();
            for (int i = 0; i < typeButtons.size(); i++) {
                typeButtons.get(i).setToggleGroup(typeGroup);
                if (typeButtons.get(i).getText().equals(returnType))
                    typeGroup.selectToggle(typeButtons.get(i));                     // default selected type
                typeButtonsVBox.getChildren().add(typeButtons.get(i));
            }
            return typeButtonsVBox;
        }

        private void addFunctionToCanvasContents(){

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayNode functions = (ArrayNode) rootNode.get("functions");
            ObjectNode info = objectMapper.createObjectNode();
            ObjectNode thisFunc = objectMapper.createObjectNode();

            info.put("x", x);
            info.put("y", y);
            info.put("return", "void");
            info.put("params", objectMapper.createArrayNode());

            thisFunc.put("name", name);
            thisFunc.put("info", info);

            functions.add(thisFunc);
            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
