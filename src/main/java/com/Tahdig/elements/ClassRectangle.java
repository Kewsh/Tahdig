package com.Tahdig.elements;

import com.Tahdig.DrawingPane;
import com.Tahdig.buttons.Element;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassRectangle {

    private final StackPane stack;

    public ClassRectangle(){

        stack = com.Tahdig.tools.ShapeDrawer.drawRectangle("Class");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            com.Tahdig.tools.DragDetector.setOnDragDetected(stack, "rectangle");
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
        private VBox attributesDialogContent, methodsDialogContent;
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXButton connectionsButton, methodsButton, attributesButton, deleteButton;
        private JFXButton attributesCloseButton, methodsCloseButton, addAttributeButton, addMethodButton, editButton;
        private JFXDialog attributesDialog, methodsDialog;
        private JFXDialogLayout attributesDialogLayout, methodsDialogLayout;
        private JFXTreeTableView attributeTreeView, methodTreeView;

        //TODO: implement edit for all remaining shapes
        //TODO: implement edit for the entire class, including class name attributes and methods
        //  for attributes, possibly open the same dialog but with the correct com.Kazemi.buttons checked, and just let the user change them?

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack) throws FileNotFoundException {

            this.x = x;
            this.y = y;
            this.name = name;
            this.root = root;
            this.stack = stack;
            addClassToCanvasContents();

            methodsButton = new JFXButton("Methods");
            attributesButton = new JFXButton("Attributes");

            editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

            setButtonStyles(methodsButton, "classMethodsButton", 50, 70);
            setButtonStyles(attributesButton, "attributesButton", 50, 70);

            // edit button

            editButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    JFXTextField nameField = new JFXTextField();
                    nameField.setText(getName());
                    nameField.setPadding(new Insets(40, 0, 0, 0));
                    nameField.setStyle("-fx-font-size: 18px;");

                    JFXButton applyChangesButton = new JFXButton("Apply");
                    applyChangesButton.setFont(new Font(18));
                    JFXDialog classEditDialog = new JFXDialog(new StackPane(),
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
                    VBox editVbox = new VBox(editHbox);
                    JFXDialogLayout classEditLayout = new JFXDialogLayout();
                    classEditLayout.setBody(editVbox);
                    classEditLayout.setActions(applyChangesButton);
                    classEditLayout.setHeading(new Label("Edit"));
                    classEditDialog.setContent(classEditLayout);

                    actionsPopup.hide();
                    classEditDialog.show(baseStack);

                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameNotValid = new Label("*name contains illegal characters");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false};                  //specifies whether each error label is set

                    nameNotGiven.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                    nameNotValid.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");
                    nameAlreadyExists.setStyle("-fx-text-fill: red; -fx-padding: 20 0 0 0");

                    applyChangesButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 3; i++)
                                errorFlags[i] = false;

                            if (editVbox.getChildren().contains(nameAlreadyExists))
                                editVbox.getChildren().remove(nameAlreadyExists);
                            if (editVbox.getChildren().contains(nameNotGiven))
                                editVbox.getChildren().remove(nameNotGiven);
                            if (editVbox.getChildren().contains(nameNotValid))
                                editVbox.getChildren().remove(nameNotValid);

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                editVbox.getChildren().add(nameNotGiven);
                                errorFlags[0] = true;
                            } else if (!checkNameValidity(inputName)){
                                editVbox.getChildren().add(nameNotValid);
                                errorFlags[1] = true;
                            } else{
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
                                            editVbox.getChildren().add(nameAlreadyExists);
                                            errorFlags[2] = true;
                                            break;
                                        }
                                    }
                                    if (!errorFlags[2]){
                                        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                        for (JsonNode interf_iter : interfaces){
                                            if (interf_iter.get("name").textValue().equals(inputName)){
                                                editVbox.getChildren().add(nameAlreadyExists);
                                                errorFlags[2] = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

                                    ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                    ObjectNode targetClass = null;
                                    int index = 0;
                                    for (int i = 0; i < classes.size(); i++){
                                        ObjectNode info = (ObjectNode) classes.get(i).get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            targetClass = (ObjectNode) classes.get(i);
                                            index = i;
                                            break;
                                        }
                                    }
                                    ObjectNode targetInfo = (ObjectNode) targetClass.get("info");

                                    targetClass.put("name", inputName);
                                    targetClass.put("info", targetInfo);
                                    classes.remove(index);
                                    classes.add(targetClass);

                                    setName(inputName);

                                    for (Node node : root.getChildren()){
                                        if (node == stack){
                                            Text text = (Text) stack.getChildren().get(1);
                                            text.setText(inputName);            // updating the name on the shape
                                            break;
                                        }
                                    }

                                    classEditDialog.close();
                                }
                                try {
                                    objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });

            // methods button

            methodsDialog = new JFXDialog(new StackPane(),
                    new Region(),
                    JFXDialog.DialogTransition.CENTER,
                    false);

            methodsCloseButton = new JFXButton("Close");
            methodsCloseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    methodsDialog.close();
                }
            });

            methodsDialogLayout = new JFXDialogLayout();
            methodsDialogLayout.setActions(methodsCloseButton);
            methodsDialog.setContent(methodsDialogLayout);

            addMethodButton = new JFXButton("Add method");
            addMethodButton.setId("addMethodButton");
            addMethodButton.setMinWidth(1000);

            addMethodButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> accessButtons = new ArrayList<JFXRadioButton>();
                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    List<JFXCheckBox> extraButtons = new ArrayList<JFXCheckBox>();

                    ToggleGroup accessGroup = new ToggleGroup();
                    ToggleGroup typeGroup = new ToggleGroup();

                    VBox accessButtonsVBox = createAccessButtonsVBox(accessButtons, accessGroup);
                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup, true);
                    VBox extraButtonsVBox = createMethodExtraButtonsVBox(extraButtons);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton methodGenerateButton = new JFXButton("Generate");
                    JFXDialog methodGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox methodsHBox = new HBox(accessButtonsVBox, extraButtonsVBox, typeButtonsVBox, nameField, methodGenerateButton);
                    methodsHBox.setSpacing(50);
                    methodsHBox.setMinWidth(800);
                    VBox methodsVBox = new VBox(methodsHBox);
                    JFXDialogLayout methodGenerateLayout = new JFXDialogLayout();
                    methodGenerateLayout.setBody(methodsVBox);
                    methodGenerateDialog.setContent(methodGenerateLayout);

                    actionsPopup.hide();
                    methodGenerateDialog.show(baseStack);

                    Label accessNotSpecified = new Label("*no access type specified");
                    Label typeNotSpecified = new Label("*no return type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameNotValid = new Label("*name contains illegal characters");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false, false, false};               //specifies whether each error label is set

                    accessNotSpecified.setStyle("-fx-text-fill: red;");
                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameNotValid.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    methodGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 5; i++)
                                errorFlags[i] = false;

                            if (methodsVBox.getChildren().contains(nameAlreadyExists))
                                methodsVBox.getChildren().remove(nameAlreadyExists);
                            if (methodsVBox.getChildren().contains(accessNotSpecified))
                                methodsVBox.getChildren().remove(accessNotSpecified);
                            if (methodsVBox.getChildren().contains(typeNotSpecified))
                                methodsVBox.getChildren().remove(typeNotSpecified);
                            if (methodsVBox.getChildren().contains(nameNotGiven))
                                methodsVBox.getChildren().remove(nameNotGiven);
                            if (methodsVBox.getChildren().contains(nameNotValid))
                                methodsVBox.getChildren().remove(nameNotValid);

                            JFXRadioButton selectedAccess = (JFXRadioButton) accessGroup.getSelectedToggle();
                            if (selectedAccess == null) {
                                methodsVBox.getChildren().add(accessNotSpecified);
                                errorFlags[0] = true;
                            }

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                methodsVBox.getChildren().add(typeNotSpecified);
                                errorFlags[1] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                methodsVBox.getChildren().add(nameNotGiven);
                                errorFlags[2] = true;
                            } else if (!checkNameValidity(inputName)){
                                methodsVBox.getChildren().add(nameNotValid);
                                errorFlags[3] = true;
                            } else{
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode attributes;
                                ArrayNode methods = null;

                                try {
                                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                for (JsonNode interf_iter : interfaces){
                                    if (interf_iter.get("name").textValue().equals(inputName)){
                                        methodsVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[4] = true;
                                        break;
                                    }
                                }
                                if (!errorFlags[4]) {
                                    for (JsonNode class_iter : classes) {
                                        if (class_iter.get("name").textValue().equals(inputName)) {
                                            methodsVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[4] = true;
                                            break;
                                        }
                                        ObjectNode info = (ObjectNode) class_iter.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                            attributes = (ArrayNode) info.get("attributes");
                                            methods = (ArrayNode) info.get("methods");
                                            for (JsonNode attribute : attributes) {
                                                if (attribute.get("name").textValue().equals(inputName)) {
                                                    methodsVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[4] = true;
                                                    break;
                                                }
                                            }
                                            if (!errorFlags[4]) {
                                                for (JsonNode method : methods) {
                                                    if (method.get("name").textValue().equals(inputName)) {
                                                        methodsVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[4] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (errorFlags[4]) break;
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3] && !errorFlags[4]){

                                    ObjectNode targetMethod = objectMapper.createObjectNode();
                                    StringBuilder extraTypes = new StringBuilder();

                                    for (int i = 0; i < extraButtons.size(); i++)
                                        if (extraButtons.get(i).isSelected()) extraTypes.append(extraButtons.get(i).getText() + " ");
                                    if (extraTypes.length() != 0) extraTypes.deleteCharAt(extraTypes.length()-1);
                                    String extraTypesString = extraTypes.toString();

                                    targetMethod.put("extra", extraTypesString);
                                    targetMethod.put("name", inputName);
                                    targetMethod.put("access", selectedAccess.getText());
                                    targetMethod.put("return", selectedType.getText());


                                    methods.add(targetMethod);
                                    updateMethodsTreeView(methodTreeView, selectedAccess.getText(), extraTypesString,
                                            selectedType.getText(), inputName);
                                    methodGenerateDialog.close();
                                }
                                try {
                                    objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });

            methodsButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    methodTreeView = getMethods();
                    methodsDialogContent = new VBox(methodTreeView, addMethodButton);
                    methodsDialogContent.setSpacing(15);
                    methodsDialogLayout.setBody(methodsDialogContent);
                    methodsDialogLayout.setHeading(new Label(getName() + "'s Methods"));        // update heading everytime

                    actionsPopup.hide();
                    methodsDialog.show(baseStack);
                }
            });

            // attributes button

            attributesDialog = new JFXDialog(new StackPane(),
                    new Region(),
                    JFXDialog.DialogTransition.CENTER,
                    false);

            attributesCloseButton = new JFXButton("Close");
            attributesCloseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    attributesDialog.close();
                }
            });

            attributesDialogLayout = new JFXDialogLayout();
            attributesDialogLayout.setActions(attributesCloseButton);
            attributesDialog.setContent(attributesDialogLayout);

            addAttributeButton = new JFXButton("Add attribute");
            addAttributeButton.setId("addAttributeButton");
            addAttributeButton.setMinWidth(1000);

            addAttributeButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> accessButtons = new ArrayList<JFXRadioButton>();
                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    List<JFXCheckBox> extraButtons = new ArrayList<JFXCheckBox>();

                    ToggleGroup accessGroup = new ToggleGroup();
                    ToggleGroup typeGroup = new ToggleGroup();

                    VBox accessButtonsVBox = createAccessButtonsVBox(accessButtons, accessGroup);
                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup, false);
                    VBox extraButtonsVBox = createAttributeExtraButtonsVBox(extraButtons);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton attributeGenerateButton = new JFXButton("Generate");
                    JFXDialog attributeGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox attributesHBox = new HBox(accessButtonsVBox, extraButtonsVBox, typeButtonsVBox, nameField, attributeGenerateButton);
                    attributesHBox.setSpacing(50);
                    attributesHBox.setMinWidth(800);
                    VBox attributesVBox = new VBox(attributesHBox);
                    JFXDialogLayout attributeGenerateLayout = new JFXDialogLayout();
                    attributeGenerateLayout.setBody(attributesVBox);
                    attributeGenerateDialog.setContent(attributeGenerateLayout);

                    actionsPopup.hide();
                    attributeGenerateDialog.show(baseStack);

                    Label accessNotSpecified = new Label("*no access type specified");
                    Label typeNotSpecified = new Label("*no data type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameNotValid = new Label("*name contains illegal characters");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false, false, false};               //specifies whether each error label is set

                    accessNotSpecified.setStyle("-fx-text-fill: red;");
                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameNotValid.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    attributeGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 5; i++)
                                errorFlags[i] = false;

                            if (attributesVBox.getChildren().contains(nameAlreadyExists))
                                attributesVBox.getChildren().remove(nameAlreadyExists);
                            if (attributesVBox.getChildren().contains(accessNotSpecified))
                                attributesVBox.getChildren().remove(accessNotSpecified);
                            if (attributesVBox.getChildren().contains(typeNotSpecified))
                                attributesVBox.getChildren().remove(typeNotSpecified);
                            if (attributesVBox.getChildren().contains(nameNotGiven))
                                attributesVBox.getChildren().remove(nameNotGiven);
                            if (attributesVBox.getChildren().contains(nameNotValid))
                                attributesVBox.getChildren().remove(nameNotValid);

                            JFXRadioButton selectedAccess = (JFXRadioButton) accessGroup.getSelectedToggle();
                            if (selectedAccess == null) {
                                attributesVBox.getChildren().add(accessNotSpecified);
                                errorFlags[0] = true;
                            }

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                attributesVBox.getChildren().add(typeNotSpecified);
                                errorFlags[1] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                attributesVBox.getChildren().add(nameNotGiven);
                                errorFlags[2] = true;
                            } else if (!checkNameValidity(inputName)){
                                attributesVBox.getChildren().add(nameNotValid);
                                errorFlags[3] = true;
                            }
                            else{
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode attributes = null;
                                ArrayNode methods;

                                try {
                                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                for (JsonNode interf_iter : interfaces){
                                    if (interf_iter.get("name").textValue().equals(inputName)){
                                        attributesVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[4] = true;
                                        break;
                                    }
                                }
                                if (!errorFlags[4]) {
                                    for (JsonNode class_iter : classes) {
                                        if (class_iter.get("name").textValue().equals(inputName)) {
                                            attributesVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[4] = true;
                                            break;
                                        }
                                        ObjectNode info = (ObjectNode) class_iter.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                            attributes = (ArrayNode) info.get("attributes");
                                            methods = (ArrayNode) info.get("methods");
                                            for (JsonNode attribute : attributes) {
                                                if (attribute.get("name").textValue().equals(inputName)) {
                                                    attributesVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[4] = true;
                                                    break;
                                                }
                                            }
                                            if (!errorFlags[4]) {
                                                for (JsonNode method : methods) {
                                                    if (method.get("name").textValue().equals(inputName)) {
                                                        attributesVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[4] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (errorFlags[4]) break;
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3] && !errorFlags[4]){

                                    ObjectNode targetAttribute = objectMapper.createObjectNode();
                                    StringBuilder extraTypes = new StringBuilder();

                                    for (int i = 0; i < extraButtons.size(); i++)
                                        if (extraButtons.get(i).isSelected()) extraTypes.append(extraButtons.get(i).getText() + " ");
                                    if (extraTypes.length() != 0) extraTypes.deleteCharAt(extraTypes.length()-1);
                                    String extraTypesString = extraTypes.toString();

                                    targetAttribute.put("extra", extraTypesString);
                                    targetAttribute.put("name", inputName);
                                    targetAttribute.put("access", selectedAccess.getText());
                                    targetAttribute.put("type", selectedType.getText());

                                    attributes.add(targetAttribute);
                                    updateAttributesTreeView(attributeTreeView, selectedAccess.getText(), extraTypesString,
                                            selectedType.getText(), inputName);
                                    attributeGenerateDialog.close();
                                }
                                try {
                                    objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });

            attributesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    attributeTreeView = getAttributes();
                    attributesDialogContent = new VBox(attributeTreeView, addAttributeButton);
                    attributesDialogContent.setSpacing(15);
                    attributesDialogLayout.setBody(attributesDialogContent);
                    attributesDialogLayout.setHeading(new Label(getName() + "'s Attributes"));

                    actionsPopup.hide();
                    attributesDialog.show(baseStack);
                }
            });

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-165);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            connectionsButton = (new com.Tahdig.buttons.ConnectionButton(x, y, actionsPopup, baseStack, root, Element.RECTANGLE).getButton());
            try {
                deleteButton = (new com.Tahdig.buttons.DeleteButton(x, y, name, root, stack, baseStack, actionsPopup,
                        Element.RECTANGLE)).getButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, connectionsButton, attributesButton, methodsButton, deleteButton));
            actionsPopup.setAutoHide(true);
            actionsPopup.setHideOnEscape(true);

            stack.setOnMouseClicked(new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                    actionsPopup.show(actionsStack);
                }
            });
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

        private VBox createAccessButtonsVBox(List<JFXRadioButton> accessButtons, ToggleGroup accessGroup){

            accessButtons.add(new JFXRadioButton("public"));
            accessButtons.add(new JFXRadioButton("private"));
            accessButtons.add(new JFXRadioButton("protected"));
            accessButtons.add(new JFXRadioButton("default"));

            for (int i = 0; i < 4; i++) {
                //for some reason, after changing the styles, the text on these com.Kazemi.buttons became invisible
                //so we have to reset it to black. we're probably changing this whole button thing though, so no worries
                accessButtons.get(i).setStyle("-fx-text-fill: black;");
                accessButtons.get(i).setToggleGroup(accessGroup);
            }
            return new VBox(accessButtons.get(0), accessButtons.get(1), accessButtons.get(2), accessButtons.get(3));
        }

        private VBox createTypeButtonsVBox(List<JFXRadioButton> typeButtons, ToggleGroup typeGroup, boolean isMethod){

            if (isMethod) typeButtons.add(new JFXRadioButton("void"));
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
                typeButtonsVBox.getChildren().add(typeButtons.get(i));
            }
            return typeButtonsVBox;
        }

        private VBox createMethodExtraButtonsVBox(List<JFXCheckBox> extraButtons){

            extraButtons.add(new JFXCheckBox("virtual"));
            extraButtons.add(new JFXCheckBox("static"));

            extraButtons.get(0).setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    extraButtons.get(1).setSelected(false);             // can be either static or virtual
                }
            });

            extraButtons.get(1).setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    extraButtons.get(0).setSelected(false);
                }
            });

            //TODO: implement other keywords for methods? such as synchronized

            return new VBox(extraButtons.get(0), extraButtons.get(1));
        }

        private VBox createAttributeExtraButtonsVBox(List<JFXCheckBox> extraButtons){

            extraButtons.add(new JFXCheckBox("constant"));
            extraButtons.add(new JFXCheckBox("static"));

            return new VBox(extraButtons.get(0), extraButtons.get(1));
        }

        private void addClassToCanvasContents(){

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
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
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setButtonStyles(JFXButton button, String buttonName, double width, double length){
            button.setMinSize(width, length);
            button.setId(buttonName);
            button.setDisableVisualFocus(true);
        }

        private void updateMethodsTreeView(JFXTreeTableView methodTreeView, String accessType, String extraType, String returnType, String inputName) {

            methodTreeView.getRoot().getChildren().add(new TreeItem(new MethodTreeItem(new SimpleStringProperty(accessType),
                    new SimpleStringProperty(extraType), new SimpleStringProperty(returnType),
                    new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getMethods(){

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode methods = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode classes = (ArrayNode) rootNode.get("classes");
            for (JsonNode class_iter : classes){
                info = (ObjectNode) class_iter.get("info");
                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                    methods = (ArrayNode) info.get("methods");
                    break;
                }
            }

            JFXTreeTableColumn<MethodTreeItem, String> accessColumn = new JFXTreeTableColumn<>("Access");
            accessColumn.setPrefWidth(200);
            accessColumn.setEditable(false);
            accessColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<MethodTreeItem, String> param) -> {
                if (accessColumn.validateValue(param)) {
                    return param.getValue().getValue().accessSpecifier;
                } else {
                    return accessColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<MethodTreeItem, String> extrasColumn = new JFXTreeTableColumn<>("Extra");
            extrasColumn.setPrefWidth(300);
            extrasColumn.setEditable(false);
            extrasColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<MethodTreeItem, String> param) -> {
                if (extrasColumn.validateValue(param)) {
                    return param.getValue().getValue().extraSpecifier;
                } else {
                    return extrasColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<MethodTreeItem, String> typeColumn = new JFXTreeTableColumn<>("Return");
            typeColumn.setPrefWidth(200);
            typeColumn.setEditable(false);
            typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<MethodTreeItem, String> param) -> {
                if (typeColumn.validateValue(param)) {
                    return param.getValue().getValue().returnType;
                } else {
                    return typeColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<MethodTreeItem, String> nameColumn = new JFXTreeTableColumn<>("Name");
            nameColumn.setPrefWidth(300);
            nameColumn.setEditable(false);
            nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<MethodTreeItem, String> param) -> {
                if (nameColumn.validateValue(param)) {
                    return param.getValue().getValue().methodName;
                } else {
                    return nameColumn.getComputedValue(param);
                }
            });

            typeColumn.setCellFactory((TreeTableColumn<MethodTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            extrasColumn.setCellFactory((TreeTableColumn<MethodTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            accessColumn.setCellFactory((TreeTableColumn<MethodTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            nameColumn.setCellFactory((TreeTableColumn<MethodTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<MethodTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode attribute : methods)
                treeRows.add(new MethodTreeItem(new SimpleStringProperty(attribute.get("access").textValue()),
                        new SimpleStringProperty(attribute.get("extra").textValue()), new SimpleStringProperty(attribute.get("return").textValue()),
                        new SimpleStringProperty(attribute.get("name").textValue())));

            final TreeItem<MethodTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<MethodTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(1000);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(accessColumn, extrasColumn, typeColumn, nameColumn);

            //TODO: try to separate the style for scrollbars on treeView and the scrollbars on drawing pane
            //don't want the scrollbars on the treeView to look so weird

            FlowPane main = new FlowPane();
            main.setPadding(new Insets(10));
            main.getChildren().add(treeView);

            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return treeView;
        }

        private void updateAttributesTreeView(JFXTreeTableView attributeTreeView, String accessType, String extraType, String dataType, String inputName) {

            attributeTreeView.getRoot().getChildren().add(new TreeItem(new AttributeTreeItem(new SimpleStringProperty(accessType),
                    new SimpleStringProperty(extraType), new SimpleStringProperty(dataType),
                    new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getAttributes(){

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode attributes = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
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

            JFXTreeTableColumn<AttributeTreeItem, String> accessColumn = new JFXTreeTableColumn<>("Access");
            accessColumn.setPrefWidth(200);
            accessColumn.setEditable(false);
            accessColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AttributeTreeItem, String> param) -> {
                if (accessColumn.validateValue(param)) {
                    return param.getValue().getValue().accessSpecifier;
                } else {
                    return accessColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<AttributeTreeItem, String> extrasColumn = new JFXTreeTableColumn<>("Extra");
            extrasColumn.setPrefWidth(300);
            extrasColumn.setEditable(false);
            extrasColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AttributeTreeItem, String> param) -> {
                if (extrasColumn.validateValue(param)) {
                    return param.getValue().getValue().extraSpecifier;
                } else {
                    return extrasColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<AttributeTreeItem, String> typeColumn = new JFXTreeTableColumn<>("Type");
            typeColumn.setPrefWidth(200);
            typeColumn.setEditable(false);
            typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AttributeTreeItem, String> param) -> {
                if (typeColumn.validateValue(param)) {
                    return param.getValue().getValue().dataType;
                } else {
                    return typeColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<AttributeTreeItem, String> nameColumn = new JFXTreeTableColumn<>("Name");
            nameColumn.setPrefWidth(300);
            nameColumn.setEditable(false);
            nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AttributeTreeItem, String> param) -> {
                if (nameColumn.validateValue(param)) {
                    return param.getValue().getValue().attributeName;
                } else {
                    return nameColumn.getComputedValue(param);
                }
            });

            typeColumn.setCellFactory((TreeTableColumn<AttributeTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            extrasColumn.setCellFactory((TreeTableColumn<AttributeTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            accessColumn.setCellFactory((TreeTableColumn<AttributeTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            nameColumn.setCellFactory((TreeTableColumn<AttributeTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<AttributeTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode attribute : attributes)
                treeRows.add(new AttributeTreeItem(new SimpleStringProperty(attribute.get("access").textValue()),
                        new SimpleStringProperty(attribute.get("extra").textValue()), new SimpleStringProperty(attribute.get("type").textValue()),
                        new SimpleStringProperty(attribute.get("name").textValue())));

            final TreeItem<AttributeTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<AttributeTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(1000);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(accessColumn, extrasColumn, typeColumn, nameColumn);

            //TODO: try to separate the style for scrollbars on treeView and the scrollbars on drawing pane
            //dont want the scrollbars on the treeview to look so weird

            FlowPane main = new FlowPane();
            main.setPadding(new Insets(10));
            main.getChildren().add(treeView);

            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return treeView;
        }

        private final class MethodTreeItem extends RecursiveTreeObject<MethodTreeItem> {

            final StringProperty accessSpecifier;
            final StringProperty extraSpecifier;
            final StringProperty returnType;
            final StringProperty methodName;

            public MethodTreeItem(StringProperty accessSpecifier, StringProperty extraSpecifier, StringProperty returnType, StringProperty methodName) {
                this.accessSpecifier = accessSpecifier;
                this.extraSpecifier = extraSpecifier;
                this.returnType = returnType;
                this.methodName = methodName;
            }
        }

        private final class AttributeTreeItem extends RecursiveTreeObject<AttributeTreeItem> {

            final StringProperty accessSpecifier;
            final StringProperty extraSpecifier;
            final StringProperty dataType;
            final StringProperty attributeName;

            public AttributeTreeItem(StringProperty accessSpecifier, StringProperty extraSpecifier, StringProperty dataType, StringProperty attributeName) {
                this.accessSpecifier = accessSpecifier;
                this.extraSpecifier = extraSpecifier;
                this.dataType = dataType;
                this.attributeName = attributeName;
            }
        }
    }
}
