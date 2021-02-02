package elements.actions;

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
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static elements.actions.ConnectionBuilder.drawConnectionLine;

public class InterfaceDiamondActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private VBox methodsDialogContent;
    private StackPane stack, actionsStack, methodsStack, deleteStack, connectionsStack;
    private JFXPopup actionsPopup, connectionsPopup;
    private JFXDialog methodsDialog, deleteDialog;
    private JFXButton methodsCloseButton, addMethodButton, deleteButton, methodsButton;
    private JFXButton inheritanceButton, connectionsButton;
    private JFXButton deleteDialogConfirmButton, deleteDialogCancelButton;
    private JFXDialogLayout methodsDialogLayout, deleteDialogLayout;
    private JFXTreeTableView methodTreeView;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement rename

    public InterfaceDiamondActions(double x, double y, String name, Group root, StackPane stack, File CanvasContents) throws FileNotFoundException {

        this.x = x;
        this.y = y;
        this.name = name;
        this.root = root;
        this.stack = stack;
        this.CanvasContents = CanvasContents;
        addInterfaceToCanvasContents();

        methodsButton = new JFXButton("Methods");
        connectionsButton = new JFXButton("Connect");
        inheritanceButton = new JFXButton("Inheritance");

        setButtonStyles(methodsButton, "interfaceMethodsButton", 50, 70);
        setButtonStyles(connectionsButton, "connectionsButton", 50, 70);
        setButtonStyles(inheritanceButton, "interfaceInheritanceButton", 40, 50);

        deleteButton = new JFXButton();
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
        deleteButton.setDisableVisualFocus(true);

        // delete button

        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (Node node : root.getChildren()) {
                    if (node == stack) {

                        deleteDialog = new JFXDialog(new StackPane(),
                                new Region(),
                                JFXDialog.DialogTransition.CENTER,
                                false);
                        deleteDialogConfirmButton = new JFXButton("Yes");
                        deleteDialogCancelButton = new JFXButton("Cancel");

                        deleteDialogConfirmButton.setOnAction(new EventHandler<ActionEvent>() {
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
                                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                for (int i = 0; i < interfaces.size(); i++){
                                    ObjectNode temp_interface = (ObjectNode) interfaces.get(i);
                                    ObjectNode info = (ObjectNode) temp_interface.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y)
                                        interfaces.remove(i);
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
                        deleteDialogCancelButton.setOnAction(new EventHandler<ActionEvent>() {
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
                        deleteStack.setLayoutX(x + 150);
                        deleteStack.setLayoutY(y);
                        root.getChildren().add(deleteStack);
                        deleteDialog.show(deleteStack);

                        break;
                    }
                }
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

                StackPane methodGenerateStack = new StackPane();
                methodGenerateStack.setLayoutX(x + 130);
                methodGenerateStack.setLayoutY(y);

                root.getChildren().add(methodGenerateStack);
                methodGenerateDialog.show(methodGenerateStack);

                Label typeNotSpecified = new Label("*no return type specified");
                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false, false};               //specifies whether each error label is set

                typeNotSpecified.setStyle("-fx-text-fill: red;");
                nameNotGiven.setStyle("-fx-text-fill: red;");
                nameAlreadyExists.setStyle("-fx-text-fill: red;");

                methodGenerateButton.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {

                        for (int i = 0; i < 3; i++)
                            errorFlags[i] = false;

                        if (methodsVBox.getChildren().contains(nameAlreadyExists))
                            methodsVBox.getChildren().remove(nameAlreadyExists);
                        if (methodsVBox.getChildren().contains(typeNotSpecified))
                            methodsVBox.getChildren().remove(typeNotSpecified);
                        if (methodsVBox.getChildren().contains(nameNotGiven))
                            methodsVBox.getChildren().remove(nameNotGiven);

                        JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                        if (selectedType == null) {
                            methodsVBox.getChildren().add(typeNotSpecified);
                            errorFlags[0] = true;
                        }

                        String inputName = nameField.getText();
                        if (inputName.equals("")) {
                            methodsVBox.getChildren().add(nameNotGiven);
                            errorFlags[1] = true;
                        } else {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = null;
                            ArrayNode methods = null;

                            try {
                                rootNode = objectMapper.readTree(CanvasContents);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ArrayNode classes = (ArrayNode) rootNode.get("classes");
                            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                            for (JsonNode interf_iter : interfaces) {
                                if (interf_iter.get("name").textValue().equals(inputName)) {
                                    methodsVBox.getChildren().add(nameAlreadyExists);
                                    errorFlags[2] = true;
                                    break;
                                }
                                ObjectNode info = (ObjectNode) interf_iter.get("info");
                                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                    methods = (ArrayNode) info.get("methods");
                                    for (JsonNode method : methods) {
                                        if (method.get("name").textValue().equals(inputName)) {
                                            methodsVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[2] = true;
                                            break;
                                        }
                                    }
                                    if (errorFlags[2]) break;
                                }
                            }
                            if (!errorFlags[2]) {
                                for (JsonNode class_iter : classes) {
                                    if (class_iter.get("name").textValue().equals(inputName)) {
                                        methodsVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[2] = true;
                                        break;
                                    }
                                }
                            }
                            if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]) {

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
                                objectMapper.writeValue(CanvasContents, rootNode);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //TODO: handle duplicate dialogs here (basically when two different "add method" dialogs are shown)

                //


            }
        });

        methodsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                methodsStack = new StackPane();
                methodsStack.setLayoutX(x + 130);
                methodsStack.setLayoutY(y);
                root.getChildren().add(methodsStack);

                methodTreeView = getMethods();
                methodsDialogContent = new VBox(methodTreeView, addMethodButton);
                methodsDialogContent.setSpacing(15);
                methodsDialogLayout.setBody(methodsDialogContent);
                methodsDialog.show(methodsStack);
            }
        });

        // connections button

        connectionsStack = new StackPane();
        connectionsStack.setLayoutX(x - 50);
        connectionsStack.setLayoutY(y - 5);
        connectionsStack.setMinHeight(100);
        root.getChildren().add(connectionsStack);

        connectionsPopup = new JFXPopup();
        connectionsPopup.setPopupContent(new VBox(inheritanceButton));
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

        // inheritance button

        inheritanceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXPopup inheritancePopup = new JFXPopup();
                StackPane inheritanceStack = new StackPane();
                root.getChildren().add(inheritanceStack);

                inheritanceStack.setLayoutX(x + 55);
                inheritanceStack.setLayoutY(y - 5);
                inheritancePopup.setAutoHide(true);
                inheritancePopup.setHideOnEscape(true);
                JFXComboBox inheritanceComboBox = new JFXComboBox();
                inheritanceComboBox.setPromptText("Inheritance target");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                for (JsonNode interf_iter : interfaces){
                    if (interf_iter.get("name").textValue().equals(getName()))
                        continue;

                    //TODO: handle duplicates here (i.e. interfaces that already are connected with inheritance with this interface)

                    inheritanceComboBox.getItems().add(interf_iter.get("name").textValue());
                }
                try {
                    objectMapper.writeValue(CanvasContents, rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inheritancePopup.setPopupContent(inheritanceComboBox);
                inheritancePopup.show(inheritanceStack);

                //TODO: handle duplicates

                inheritanceComboBox.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = null;
                        ObjectNode targetInterface = null;
                        try {
                            rootNode = objectMapper.readTree(CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                        for (JsonNode interf_iter : interfaces){
                            if (interf_iter.get("name").textValue().equals(inheritanceComboBox.getValue().toString())){
                                targetInterface = (ObjectNode) interf_iter;
                                break;
                            }
                        }
                        try {
                            objectMapper.writeValue(CanvasContents, rootNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ObjectNode targetInfo = (ObjectNode) targetInterface.get("info");
                        drawConnectionLine(root, CanvasContents, 'i', 'i', "inheritance", x, y, targetInfo.get("x").doubleValue(), targetInfo.get("y").doubleValue());
                        inheritancePopup.hide();
                        connectionsPopup.hide();
                        actionsPopup.hide();
                    }
                });
            }
        });

        // actions button

        actionsStack = new StackPane();
        actionsStack.setLayoutX(x - 50);
        actionsStack.setLayoutY(y - 85);
        actionsStack.setMinHeight(100);
        root.getChildren().add(actionsStack);

        actionsPopup = new JFXPopup();
        actionsPopup.setPopupContent(new HBox(connectionsButton, methodsButton, deleteButton));
        actionsPopup.setAutoHide(true);
        actionsPopup.setHideOnEscape(true);

        stack.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                actionsPopup.show(actionsStack);
            }
        });

        actionsPopup.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (flag[0])
                    flag[1] = true;
            }
        });
    }

    private String getName(){
        return this.name;
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
            rootNode = objectMapper.readTree(CanvasContents);
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
            objectMapper.writeValue(CanvasContents, rootNode);
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
            rootNode = objectMapper.readTree(CanvasContents);
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
            objectMapper.writeValue(CanvasContents, rootNode);
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
            rootNode = objectMapper.readTree(CanvasContents);
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
            objectMapper.writeValue(CanvasContents, rootNode);
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
