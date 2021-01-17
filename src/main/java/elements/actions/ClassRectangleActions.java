package elements.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassRectangleActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private VBox attributesDialogContent;
    private StackPane stack, actionsStack, connectionsStack, attributesStack;
    private JFXPopup actionsPopup, connectionsPopup;
    private JFXButton connectionsButton, methodsButton, attributesButton, deleteButton;
    private JFXButton compositionButton, generalizationButton, implementationButton, containmentButton;
    private JFXButton attributesCloseButton, addAttributeButton;
    private JFXDialog attributesDialog;
    private JFXDialogLayout attributesDialogLayout;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement edit for the entire class, including class name attributes and methods

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
        attributesDialogLayout.setHeading(new Label("Attributes"));
        attributesDialogLayout.setActions(attributesCloseButton);
        attributesDialog.setContent(attributesDialogLayout);

        addAttributeButton = new JFXButton("Add attribute");
        addAttributeButton.setId("addAttributeButton");
        addAttributeButton.setMinWidth(900);

        //

        addAttributeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

                List<JFXRadioButton> accessButtons = new ArrayList<JFXRadioButton>();
                List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                List<JFXCheckBox> extraButtons = new ArrayList<JFXCheckBox>();

                ToggleGroup accessGroup = new ToggleGroup();
                ToggleGroup typeGroup = new ToggleGroup();

                VBox accessButtonsVBox = createAccessButtonsVBox(accessButtons, accessGroup);
                VBox typeButtonsVBox = createAttributeTypeButtonsVBox(typeButtons, typeGroup);
                VBox extraButtonsVBox = createAttributeExtraButtonsVBox(extraButtons);

                //

                JFXTextField nameField = new JFXTextField();
                nameField.setPromptText("name");

                JFXButton clickMe = new JFXButton("click me");

                //

                //test

                JFXDialog testDialog = new JFXDialog(new StackPane(),
                        new Region(),
                        JFXDialog.DialogTransition.CENTER,
                        true);

                JFXDialogLayout testLayout = new JFXDialogLayout();
                HBox testHBox = new HBox(accessButtonsVBox, extraButtonsVBox, typeButtonsVBox, nameField, clickMe);
                testHBox.setSpacing(50);
                testHBox.setMinWidth(800);
                VBox testVBox = new VBox(testHBox);
                testLayout.setBody(testVBox);
                testDialog.setContent(testLayout);
                StackPane testStack = new StackPane();
                testStack.setLayoutX(x + 130);
                testStack.setLayoutY(y);
                root.getChildren().add(testStack);
                testDialog.show(testStack);

                //test

                Label accessNotSpecified = new Label("*no access type specified");
                Label typeNotSpecified = new Label("*no data type specified");
                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameAlreadyExists = new Label("*an attribute with this name already exists");

                boolean[] errorFlags = {false, false, false, false};               //specifies whether each error label is set

                accessNotSpecified.setStyle("-fx-text-fill: red;");
                typeNotSpecified.setStyle("-fx-text-fill: red;");
                nameNotGiven.setStyle("-fx-text-fill: red;");
                nameAlreadyExists.setStyle("-fx-text-fill: red;");

                clickMe.setOnAction(new EventHandler<ActionEvent>(){

                    @Override
                    public void handle(ActionEvent event) {

                        for (int i = 0; i < 4; i++)
                            errorFlags[i] = false;

                        if (testVBox.getChildren().contains(nameAlreadyExists))
                            testVBox.getChildren().remove(nameAlreadyExists);
                        if (testVBox.getChildren().contains(accessNotSpecified))
                            testVBox.getChildren().remove(accessNotSpecified);
                        if (testVBox.getChildren().contains(typeNotSpecified))
                            testVBox.getChildren().remove(typeNotSpecified);
                        if (testVBox.getChildren().contains(nameNotGiven))
                            testVBox.getChildren().remove(nameNotGiven);

                        JFXRadioButton selectedAccess = (JFXRadioButton) accessGroup.getSelectedToggle();
                        if (selectedAccess == null) {
                            testVBox.getChildren().add(accessNotSpecified);
                            errorFlags[0] = true;
                        }

                        JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                        if (selectedType == null) {
                            testVBox.getChildren().add(typeNotSpecified);
                            errorFlags[1] = true;
                        }

                        String inputName = nameField.getText();
                        if (inputName.equals("")) {
                            testVBox.getChildren().add(nameNotGiven);
                            errorFlags[2] = true;
                        }
                        else{
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = null;
                            ArrayNode attributes = null;

                            try {
                                rootNode = objectMapper.readTree(CanvasContents);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ArrayNode classes = (ArrayNode) rootNode.get("classes");
                            for (JsonNode class_iter : classes){
                                ObjectNode info = (ObjectNode) class_iter.get("info");
                                if (info.get("x").intValue() == x && info.get("y").intValue() == y){
                                    attributes = (ArrayNode) info.get("attributes");
                                    for (JsonNode attribute : attributes){
                                        if (attribute.get("name").textValue().equals(inputName)) {
                                            testVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[3] = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3]){

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
                                testDialog.close();
                            }
                            try {
                                objectMapper.writeValue(CanvasContents, rootNode);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //TODO: refresh the attributes dialog everytime a new attribute is added

                //handle duplicate dialogs here

                //


            }
        });

        //

        attributesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                attributesStack = new StackPane();
                attributesStack.setLayoutX(x + 130);
                attributesStack.setLayoutY(y);
                root.getChildren().add(attributesStack);

                attributesDialogContent = new VBox(getAttributes(), addAttributeButton);
                attributesDialogContent.setSpacing(15);
                attributesDialogLayout.setBody(attributesDialogContent);
                attributesDialog.show(attributesStack);
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

    private VBox createAccessButtonsVBox(List<JFXRadioButton> accessButtons, ToggleGroup accessGroup){

        accessButtons.add(new JFXRadioButton("public"));
        accessButtons.add(new JFXRadioButton("private"));
        accessButtons.add(new JFXRadioButton("protected"));
        accessButtons.add(new JFXRadioButton("package-private"));

        for (int i = 0; i < 4; i++)
            accessButtons.get(i).setToggleGroup(accessGroup);
        return new VBox(accessButtons.get(0), accessButtons.get(1), accessButtons.get(2), accessButtons.get(3));
    }

    private VBox createAttributeExtraButtonsVBox(List<JFXCheckBox> extraButtons){

        extraButtons.add(new JFXCheckBox("final"));
        extraButtons.add(new JFXCheckBox("static"));
        extraButtons.add(new JFXCheckBox("transient"));
        extraButtons.add(new JFXCheckBox("volatile"));               // final and volatile don't go together

        extraButtons.get(0).selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                extraButtons.get(3).setDisable(newValue);
            }
        });
        extraButtons.get(3).selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                extraButtons.get(0).setDisable(newValue);
            }
        });

        return new VBox(extraButtons.get(0), extraButtons.get(1), extraButtons.get(2), extraButtons.get(3));
    }

    private VBox createAttributeTypeButtonsVBox(List<JFXRadioButton> typeButtons, ToggleGroup typeGroup){

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

        JFXTreeTableColumn<AttributeTreeItem, String> accessColumn = new JFXTreeTableColumn<>("Access");
        accessColumn.setPrefWidth(150);
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
                return param.getValue().getValue().extraSpecificer;
            } else {
                return extrasColumn.getComputedValue(param);
            }
        });

        JFXTreeTableColumn<AttributeTreeItem, String> typeColumn = new JFXTreeTableColumn<>("Type");
        typeColumn.setPrefWidth(150);
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
            treeRows.add(new AttributeTreeItem(new SimpleStringProperty(attribute.get("access").textValue()), new SimpleStringProperty(attribute.get("extra").textValue()),
                    new SimpleStringProperty(attribute.get("type").textValue()), new SimpleStringProperty(attribute.get("name").textValue())));

        final TreeItem<AttributeTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

        JFXTreeTableView<AttributeTreeItem> treeView = new JFXTreeTableView<>(root);
        treeView.setMinWidth(900);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.getColumns().setAll(accessColumn, extrasColumn, typeColumn, nameColumn);

        //TODO: try to separate the style for scrollbars on treeView and the scrollbars on drawing pane
        //dont want the scrollbars on the treeview to look so weird

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

    final class AttributeTreeItem extends RecursiveTreeObject<AttributeTreeItem> {

        final StringProperty accessSpecifier;
        final StringProperty extraSpecificer;
        final StringProperty dataType;
        final StringProperty attributeName;

        public AttributeTreeItem(StringProperty accessSpecifier, StringProperty extraSpecificer, StringProperty dataType, StringProperty attributeName) {
            this.accessSpecifier = accessSpecifier;
            this.extraSpecificer = extraSpecificer;
            this.dataType = dataType;
            this.attributeName = attributeName;
        }
    }
}
