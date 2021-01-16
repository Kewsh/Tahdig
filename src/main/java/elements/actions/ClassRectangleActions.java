package elements.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
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
        addAttributeButton.setMinWidth(750);

        //

        addAttributeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

                List<JFXRadioButton> accessButtons = new ArrayList<JFXRadioButton>();
                List<JFXCheckBox> extraButtons = new ArrayList<JFXCheckBox>();
                List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();

                accessButtons.add(new JFXRadioButton("public"));
                accessButtons.add(new JFXRadioButton("private"));
                accessButtons.add(new JFXRadioButton("protected"));
                accessButtons.add(new JFXRadioButton("package-private"));

                ToggleGroup accessGroup = new ToggleGroup();
                accessButtons.get(0).setToggleGroup(accessGroup);
                accessButtons.get(1).setToggleGroup(accessGroup);
                accessButtons.get(2).setToggleGroup(accessGroup);
                accessButtons.get(3).setToggleGroup(accessGroup);

                VBox accessButtonsVBox = new VBox(accessButtons.get(0), accessButtons.get(1),
                        accessButtons.get(2), accessButtons.get(3));

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

                VBox extraButtonsVBox = new VBox(extraButtons.get(0), extraButtons.get(1),
                        extraButtons.get(2), extraButtons.get(3));

                //test

                JFXDialog testDialog = new JFXDialog(new StackPane(),
                        new Region(),
                        JFXDialog.DialogTransition.CENTER,
                        true);

                JFXDialogLayout testLayout = new JFXDialogLayout();
                testLayout.setBody(new HBox(accessButtonsVBox, extraButtonsVBox));
                testDialog.setContent(testLayout);
                StackPane testStack = new StackPane();
                testStack.setLayoutX(x + 130);
                testStack.setLayoutY(y);
                root.getChildren().add(testStack);
                testDialog.show(testStack);

                //handle duplicate dialogs here

                //

                //types


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
        extrasColumn.setPrefWidth(150);
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
        treeView.setMinWidth(750);
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
