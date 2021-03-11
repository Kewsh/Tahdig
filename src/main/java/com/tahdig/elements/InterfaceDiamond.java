package com.tahdig.elements;

import com.tahdig.DrawingPane;
import com.tahdig.buttons.Element;
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

public class InterfaceDiamond {

    private final StackPane stack;

    public InterfaceDiamond(){

        stack = com.tahdig.tools.drawDiamond("Interface");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            com.tahdig.tools.setOnDragDetected(stack, "diamond");
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
        private VBox methodsDialogContent;
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXDialog methodsDialog;
        private JFXButton methodsCloseButton, addMethodButton, deleteButton, methodsButton;
        private JFXButton connectionsButton, editButton;
        private JFXDialogLayout methodsDialogLayout;
        private JFXTreeTableView methodTreeView;

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack) throws FileNotFoundException {
            this.x = x;
            this.y = y;
            this.name = name;
            this.root = root;
            this.stack = stack;
            addInterfaceToCanvasContents();

            methodsButton = new JFXButton("Methods");
            setButtonStyles(methodsButton, "interfaceMethodsButton", 50, 70);

            editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

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
                    JFXDialog interfaceEditDialog = new JFXDialog(new StackPane(),
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
                    JFXDialogLayout interfaceEditLayout = new JFXDialogLayout();
                    interfaceEditLayout.setBody(editVBox);
                    interfaceEditLayout.setActions(applyChangesButton);
                    interfaceEditLayout.setHeading(new Label("Edit"));
                    interfaceEditDialog.setContent(interfaceEditLayout);

                    actionsPopup.hide();
                    interfaceEditDialog.show(baseStack);

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
                                    ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                    for (JsonNode class_iter : classes) {
                                        if (class_iter.get("name").textValue().equals(inputName)) {
                                            editVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[2] = true;
                                            break;
                                        }
                                    }
                                    if (!errorFlags[2]){
                                        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                        for (JsonNode interf_iter : interfaces){
                                            if (interf_iter.get("name").textValue().equals(inputName)){
                                                editVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[2] = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

                                    ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                    ObjectNode targetInterface = null;
                                    int index = 0;
                                    for (int i = 0; i < interfaces.size(); i++){
                                        ObjectNode info = (ObjectNode) interfaces.get(i).get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            targetInterface = (ObjectNode) interfaces.get(i);
                                            index = i;
                                            break;
                                        }
                                    }
                                    ObjectNode targetInfo = (ObjectNode) targetInterface.get("info");

                                    targetInterface.put("name", inputName);
                                    targetInterface.put("info", targetInfo);
                                    interfaces.remove(index);
                                    interfaces.add(targetInterface);

                                    setName(inputName);

                                    for (Node node : root.getChildren()){
                                        if (node == stack){
                                            Text text = (Text) stack.getChildren().get(1);
                                            text.setText(inputName);                            // updating the name on the shape
                                            break;
                                        }
                                    }

                                    interfaceEditDialog.close();
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
            methodsDialogLayout.setHeading(new Label("Methods"));
            methodsDialogLayout.setActions(methodsCloseButton);
            methodsDialog.setContent(methodsDialogLayout);

            addMethodButton = new JFXButton("Add method");
            addMethodButton.setId("addMethodButton");
            addMethodButton.setMinWidth(800);

            addMethodButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    List<JFXCheckBox> extraButtons = new ArrayList<JFXCheckBox>();

                    ToggleGroup typeGroup = new ToggleGroup();

                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup);
                    VBox extraButtonsVBox = createMethodExtraButtonsVBox(extraButtons);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton methodGenerateButton = new JFXButton("Generate");
                    JFXDialog methodGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox methodsHBox = new HBox(extraButtonsVBox, typeButtonsVBox, nameField, methodGenerateButton);
                    methodsHBox.setSpacing(50);
                    methodsHBox.setMinWidth(600);
                    VBox methodsVBox = new VBox(methodsHBox);
                    JFXDialogLayout methodGenerateLayout = new JFXDialogLayout();
                    methodGenerateLayout.setBody(methodsVBox);
                    methodGenerateDialog.setContent(methodGenerateLayout);

                    actionsPopup.hide();
                    methodGenerateDialog.show(baseStack);

                    Label typeNotSpecified = new Label("*no return type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameNotValid = new Label("*name contains illegal characters");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false, false};               //specifies whether each error label is set

                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameNotValid.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    methodGenerateButton.setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 4; i++)
                                errorFlags[i] = false;

                            if (methodsVBox.getChildren().contains(nameAlreadyExists))
                                methodsVBox.getChildren().remove(nameAlreadyExists);
                            if (methodsVBox.getChildren().contains(typeNotSpecified))
                                methodsVBox.getChildren().remove(typeNotSpecified);
                            if (methodsVBox.getChildren().contains(nameNotGiven))
                                methodsVBox.getChildren().remove(nameNotGiven);
                            if (methodsVBox.getChildren().contains(nameNotValid))
                                methodsVBox.getChildren().remove(nameNotValid);

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                methodsVBox.getChildren().add(typeNotSpecified);
                                errorFlags[0] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                methodsVBox.getChildren().add(nameNotGiven);
                                errorFlags[1] = true;
                            } else if (!checkNameValidity(inputName)){
                                methodsVBox.getChildren().add(nameNotValid);
                                errorFlags[2] = true;
                            }
                            else {
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode methods = null;

                                try {
                                    rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                for (JsonNode interf_iter : interfaces) {
                                    if (interf_iter.get("name").textValue().equals(inputName)) {
                                        methodsVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[3] = true;
                                        break;
                                    }
                                    ObjectNode info = (ObjectNode) interf_iter.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                        methods = (ArrayNode) info.get("methods");
                                        for (JsonNode method : methods) {
                                            if (method.get("name").textValue().equals(inputName)) {
                                                methodsVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[3] = true;
                                                break;
                                            }
                                        }
                                        if (errorFlags[3]) break;
                                    }
                                }
                                if (!errorFlags[3]) {
                                    for (JsonNode class_iter : classes) {
                                        if (class_iter.get("name").textValue().equals(inputName)) {
                                            methodsVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[3] = true;
                                            break;
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3]) {

                                    ObjectNode targetMethod = objectMapper.createObjectNode();
                                    StringBuilder extraTypes = new StringBuilder();

                                    for (int i = 0; i < extraButtons.size(); i++)
                                        if (extraButtons.get(i).isSelected())
                                            extraTypes.append(extraButtons.get(i).getText() + " ");
                                    if (extraTypes.length() != 0) extraTypes.deleteCharAt(extraTypes.length() - 1);
                                    String extraTypesString = extraTypes.toString();

                                    targetMethod.put("extra", extraTypesString);
                                    targetMethod.put("name", inputName);
                                    targetMethod.put("return", selectedType.getText());

                                    methods.add(targetMethod);
                                    updateMethodsTreeView(methodTreeView, extraTypesString,
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

                    actionsPopup.hide();
                    methodsDialog.show(baseStack);
                }
            });

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x - 90);
            actionsStack.setLayoutY(y - 85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            connectionsButton = (new com.tahdig.buttons.ConnectionButton(x, y, actionsPopup, baseStack, root, Element.DIAMOND)).getButton();
            try {
                deleteButton = (new com.tahdig.buttons.DeleteButton(x, y, name, root, stack, baseStack, actionsPopup,
                        Element.DIAMOND)).getButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, connectionsButton, methodsButton, deleteButton));
            actionsPopup.setAutoHide(true);
            actionsPopup.setHideOnEscape(true);

            stack.setOnMouseClicked(new EventHandler<MouseEvent>() {
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

        private void setButtonStyles(JFXButton button, String buttonName, double width, double length){
            button.setMinSize(width, length);
            button.setId(buttonName);
            button.setDisableVisualFocus(true);
        }

        private VBox createTypeButtonsVBox(List<JFXRadioButton> typeButtons, ToggleGroup typeGroup) {

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
                typeButtonsVBox.getChildren().add(typeButtons.get(i));
            }
            return typeButtonsVBox;
        }

        private VBox createMethodExtraButtonsVBox(List<JFXCheckBox> extraButtons) {

            extraButtons.add(new JFXCheckBox("static"));
            return new VBox(extraButtons.get(0));
        }

        private void addInterfaceToCanvasContents() {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
            ObjectNode info = objectMapper.createObjectNode();
            ObjectNode thisInterf = objectMapper.createObjectNode();

            info.put("x", x);
            info.put("y", y);
            info.put("methods", objectMapper.createArrayNode());

            thisInterf.put("name", name);
            thisInterf.put("info", info);

            interfaces.add(thisInterf);
            try {
                objectMapper.writeValue(DrawingPane.CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void updateMethodsTreeView(JFXTreeTableView methodTreeView, String extraType, String returnType, String inputName) {

            methodTreeView.getRoot().getChildren().add(new TreeItem(new MethodTreeItem(new SimpleStringProperty(extraType),
                    new SimpleStringProperty(returnType), new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getMethods() {

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode methods = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(DrawingPane.CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
            for (JsonNode interf_iter : interfaces) {
                info = (ObjectNode) interf_iter.get("info");
                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                    methods = (ArrayNode) info.get("methods");
                    break;
                }
            }

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
            nameColumn.setCellFactory((TreeTableColumn<MethodTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<MethodTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode attribute : methods)
                treeRows.add(new MethodTreeItem(new SimpleStringProperty(attribute.get("extra").textValue()),
                        new SimpleStringProperty(attribute.get("return").textValue()), new SimpleStringProperty(attribute.get("name").textValue())));

            final TreeItem<MethodTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<MethodTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(800);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(extrasColumn, typeColumn, nameColumn);

            //TODO: try to separate the style for scrollbars on treeView and the scrollbars on drawing pane
            //don't want the scrollbars on the treeview to look so weird

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

            final StringProperty extraSpecifier;
            final StringProperty returnType;
            final StringProperty methodName;

            public MethodTreeItem(StringProperty extraSpecifier, StringProperty returnType, StringProperty methodName) {
                this.extraSpecifier = extraSpecifier;
                this.returnType = returnType;
                this.methodName = methodName;
            }
        }
    }

}
