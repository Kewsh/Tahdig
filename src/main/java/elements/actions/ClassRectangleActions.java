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

public class ClassRectangleActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private VBox attributesDialogContent, methodsDialogContent;
    private StackPane stack, actionsStack, connectionsStack, attributesStack, methodsStack, deleteStack;
    private JFXPopup actionsPopup, connectionsPopup;
    private JFXButton connectionsButton, methodsButton, attributesButton, deleteButton;
    private JFXButton compositionButton, inheritanceButton, implementationButton, containmentButton;
    private JFXButton attributesCloseButton, methodsCloseButton, addAttributeButton, addMethodButton;
    private JFXButton deleteDialogConfirmButton, deleteDialogCancelButton;
    private JFXDialog attributesDialog, methodsDialog, deleteDialog;
    private JFXDialogLayout attributesDialogLayout, methodsDialogLayout, deleteDialogLayout;
    private JFXTreeTableView attributeTreeView, methodTreeView;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement edit for all remaining shapes
    //TODO: implement edit for the entire class, including class name attributes and methods
    //for attributes, possibly open the same dialog but with the correct buttons checked, and just let the user change them?

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
        deleteButton.setDisableVisualFocus(true);

        setButtonStyles(connectionsButton, "connectionsButton", 50, 70);
        setButtonStyles(methodsButton, "classMethodsButton", 50, 70);
        setButtonStyles(attributesButton, "attributesButton", 50, 70);

        compositionButton = new JFXButton("Composition");
        inheritanceButton = new JFXButton("Inheritance");
        implementationButton = new JFXButton("Implementation");
        containmentButton = new JFXButton("Containment");

        setButtonStyles(compositionButton, "compositionButton", 135, 50);
        setButtonStyles(inheritanceButton, "classInheritanceButton", 135, 50);
        setButtonStyles(implementationButton, "implementationButton", 135, 50);
        setButtonStyles(containmentButton, "containmentButton", 135, 50);

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
                                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                for (int i = 0; i < classes.size(); i++){
                                    ObjectNode temp_class = (ObjectNode) classes.get(i);
                                    ObjectNode info = (ObjectNode) temp_class.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y)
                                        classes.remove(i);
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

                StackPane methodGenerateStack = new StackPane();
                methodGenerateStack.setLayoutX(x + 130);
                methodGenerateStack.setLayoutY(y);

                root.getChildren().add(methodGenerateStack);
                methodGenerateDialog.show(methodGenerateStack);

                Label accessNotSpecified = new Label("*no access type specified");
                Label typeNotSpecified = new Label("*no return type specified");
                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false, false, false};               //specifies whether each error label is set

                accessNotSpecified.setStyle("-fx-text-fill: red;");
                typeNotSpecified.setStyle("-fx-text-fill: red;");
                nameNotGiven.setStyle("-fx-text-fill: red;");
                nameAlreadyExists.setStyle("-fx-text-fill: red;");

                methodGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                    @Override
                    public void handle(ActionEvent event) {

                        for (int i = 0; i < 4; i++)
                            errorFlags[i] = false;

                        if (methodsVBox.getChildren().contains(nameAlreadyExists))
                            methodsVBox.getChildren().remove(nameAlreadyExists);
                        if (methodsVBox.getChildren().contains(accessNotSpecified))
                            methodsVBox.getChildren().remove(accessNotSpecified);
                        if (methodsVBox.getChildren().contains(typeNotSpecified))
                            methodsVBox.getChildren().remove(typeNotSpecified);
                        if (methodsVBox.getChildren().contains(nameNotGiven))
                            methodsVBox.getChildren().remove(nameNotGiven);

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
                        }
                        else{
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = null;
                            ArrayNode attributes;
                            ArrayNode methods = null;

                            try {
                                rootNode = objectMapper.readTree(CanvasContents);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ArrayNode classes = (ArrayNode) rootNode.get("classes");
                            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                            for (JsonNode interf_iter : interfaces){
                                if (interf_iter.get("name").textValue().equals(inputName)){
                                    methodsVBox.getChildren().add(nameAlreadyExists);
                                    errorFlags[3] = true;
                                    break;
                                }
                            }
                            if (!errorFlags[3]) {
                                for (JsonNode class_iter : classes) {
                                    if (class_iter.get("name").textValue().equals(inputName)) {
                                        methodsVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[3] = true;
                                        break;
                                    }
                                    ObjectNode info = (ObjectNode) class_iter.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                        attributes = (ArrayNode) info.get("attributes");
                                        methods = (ArrayNode) info.get("methods");
                                        for (JsonNode attribute : attributes) {
                                            if (attribute.get("name").textValue().equals(inputName)) {
                                                methodsVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[3] = true;
                                                break;
                                            }
                                        }
                                        if (!errorFlags[3]) {
                                            for (JsonNode method : methods) {
                                                if (method.get("name").textValue().equals(inputName)) {
                                                    methodsVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[3] = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (errorFlags[3]) break;
                                    }
                                }
                            }
                            if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2] && !errorFlags[3]){

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

                StackPane attributeGenerateStack = new StackPane();
                attributeGenerateStack.setLayoutX(x + 130);
                attributeGenerateStack.setLayoutY(y);

                root.getChildren().add(attributeGenerateStack);
                attributeGenerateDialog.show(attributeGenerateStack);

                Label accessNotSpecified = new Label("*no access type specified");
                Label typeNotSpecified = new Label("*no data type specified");
                Label nameNotGiven = new Label("*name field must not be empty");
                Label nameAlreadyExists = new Label("*this name has already been used");

                boolean[] errorFlags = {false, false, false, false};               //specifies whether each error label is set

                accessNotSpecified.setStyle("-fx-text-fill: red;");
                typeNotSpecified.setStyle("-fx-text-fill: red;");
                nameNotGiven.setStyle("-fx-text-fill: red;");
                nameAlreadyExists.setStyle("-fx-text-fill: red;");

                attributeGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                    @Override
                    public void handle(ActionEvent event) {

                        for (int i = 0; i < 4; i++)
                            errorFlags[i] = false;

                        if (attributesVBox.getChildren().contains(nameAlreadyExists))
                            attributesVBox.getChildren().remove(nameAlreadyExists);
                        if (attributesVBox.getChildren().contains(accessNotSpecified))
                            attributesVBox.getChildren().remove(accessNotSpecified);
                        if (attributesVBox.getChildren().contains(typeNotSpecified))
                            attributesVBox.getChildren().remove(typeNotSpecified);
                        if (attributesVBox.getChildren().contains(nameNotGiven))
                            attributesVBox.getChildren().remove(nameNotGiven);

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
                        }
                        else{
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = null;
                            ArrayNode attributes = null;
                            ArrayNode methods;

                            try {
                                rootNode = objectMapper.readTree(CanvasContents);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ArrayNode classes = (ArrayNode) rootNode.get("classes");
                            ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                            for (JsonNode interf_iter : interfaces){
                                if (interf_iter.get("name").textValue().equals(inputName)){
                                    attributesVBox.getChildren().add(nameAlreadyExists);
                                    errorFlags[3] = true;
                                    break;
                                }
                            }
                            if (!errorFlags[3]) {
                                for (JsonNode class_iter : classes) {
                                    if (class_iter.get("name").textValue().equals(inputName)) {
                                        attributesVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[3] = true;
                                        break;
                                    }
                                    ObjectNode info = (ObjectNode) class_iter.get("info");
                                    if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                                        attributes = (ArrayNode) info.get("attributes");
                                        methods = (ArrayNode) info.get("methods");
                                        for (JsonNode attribute : attributes) {
                                            if (attribute.get("name").textValue().equals(inputName)) {
                                                attributesVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[3] = true;
                                                break;
                                            }
                                        }
                                        if (!errorFlags[3]) {
                                            for (JsonNode method : methods) {
                                                if (method.get("name").textValue().equals(inputName)) {
                                                    attributesVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[3] = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (errorFlags[3]) break;
                                    }
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
                                updateAttributesTreeView(attributeTreeView, selectedAccess.getText(), extraTypesString,
                                        selectedType.getText(), inputName);
                                attributeGenerateDialog.close();
                            }
                            try {
                                objectMapper.writeValue(CanvasContents, rootNode);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //TODO: handle duplicate dialogs here (basically when two different "add attribute" dialogs are shown)

                //


            }
        });

        attributesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                attributesStack = new StackPane();
                attributesStack.setLayoutX(x + 130);
                attributesStack.setLayoutY(y);
                root.getChildren().add(attributesStack);

                attributeTreeView = getAttributes();
                attributesDialogContent = new VBox(attributeTreeView, addAttributeButton);
                attributesDialogContent.setSpacing(15);
                attributesDialogLayout.setBody(attributesDialogContent);
                attributesDialog.show(attributesStack);
            }
        });

        // connections button

        connectionsStack = new StackPane();
        connectionsStack.setLayoutX(x - 125);
        connectionsStack.setLayoutY(y - 5);
        connectionsStack.setMinHeight(100);
        root.getChildren().add(connectionsStack);

        connectionsPopup = new JFXPopup();
        connectionsPopup.setPopupContent(new VBox(compositionButton, inheritanceButton, implementationButton, containmentButton));
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

        // composition button

        compositionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXPopup compositionPopup = new JFXPopup();
                StackPane compositionStack = new StackPane();
                root.getChildren().add(compositionStack);

                compositionStack.setLayoutX(x + 15);
                compositionStack.setLayoutY(y - 5);
                compositionPopup.setAutoHide(true);
                compositionPopup.setHideOnEscape(true);
                JFXComboBox compositionComboBox = new JFXComboBox();
                compositionComboBox.setPromptText("Composition target");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                for (JsonNode class_iter : classes){
                    if (class_iter.get("name").textValue().equals(getName()))
                        continue;

                    //TODO: handle duplicates here (i.e. classes that already are connected with composition with this class)

                    compositionComboBox.getItems().add(class_iter.get("name").textValue());
                }
                try {
                    objectMapper.writeValue(CanvasContents, rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                compositionPopup.setPopupContent(compositionComboBox);
                compositionPopup.show(compositionStack);

                //TODO: handle duplicates

                compositionComboBox.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = null;
                        ObjectNode targetClass = null;
                        try {
                            rootNode = objectMapper.readTree(CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ArrayNode classes = (ArrayNode) rootNode.get("classes");
                        for (JsonNode class_iter : classes){
                            if (class_iter.get("name").textValue().equals(compositionComboBox.getValue().toString())){
                                targetClass = (ObjectNode) class_iter;
                                break;
                            }
                        }
                        try {
                            objectMapper.writeValue(CanvasContents, rootNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ObjectNode targetInfo = (ObjectNode) targetClass.get("info");
                        drawConnectionLine(root, CanvasContents, 'c', 'c', "composition", x, y, targetInfo.get("x").doubleValue(), targetInfo.get("y").doubleValue());
                        compositionPopup.hide();
                        connectionsPopup.hide();
                        actionsPopup.hide();
                    }
                });
            }
        });

        // inheritance button

        inheritanceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXPopup inheritancePopup = new JFXPopup();
                StackPane inheritanceStack = new StackPane();
                root.getChildren().add(inheritanceStack);

                inheritanceStack.setLayoutX(x + 15);
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
                ArrayNode classes = (ArrayNode) rootNode.get("classes");
                for (JsonNode class_iter : classes){
                    if (class_iter.get("name").textValue().equals(getName()))
                        continue;

                    //TODO: handle duplicates here (i.e. classes that already are connected with inheritance with this class)

                    inheritanceComboBox.getItems().add(class_iter.get("name").textValue());
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
                        ObjectNode targetClass = null;
                        try {
                            rootNode = objectMapper.readTree(CanvasContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ArrayNode classes = (ArrayNode) rootNode.get("classes");
                        for (JsonNode class_iter : classes){
                            if (class_iter.get("name").textValue().equals(inheritanceComboBox.getValue().toString())){
                                targetClass = (ObjectNode) class_iter;
                                break;
                            }
                        }
                        try {
                            objectMapper.writeValue(CanvasContents, rootNode);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ObjectNode targetInfo = (ObjectNode) targetClass.get("info");
                        drawConnectionLine(root, CanvasContents, 'c', 'c', "inheritance", x, y, targetInfo.get("x").doubleValue(), targetInfo.get("y").doubleValue());
                        inheritancePopup.hide();
                        connectionsPopup.hide();
                        actionsPopup.hide();
                    }
                });
            }
        });

        // implementation button

        implementationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                JFXPopup implementationPopup = new JFXPopup();
                StackPane implementationStack = new StackPane();
                root.getChildren().add(implementationStack);

                implementationStack.setLayoutX(x + 15);
                implementationStack.setLayoutY(y - 5);
                implementationPopup.setAutoHide(true);
                implementationPopup.setHideOnEscape(true);
                JFXComboBox implementationComboBox = new JFXComboBox();
                implementationComboBox.setPromptText("Implementation target");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(CanvasContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                for (JsonNode interf_iter : interfaces)

                    //TODO: handle duplicates here (i.e. interfaces that already are connected with implementation with this class)

                    implementationComboBox.getItems().add(interf_iter.get("name").textValue());
                try {
                    objectMapper.writeValue(CanvasContents, rootNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                implementationPopup.setPopupContent(implementationComboBox);
                implementationPopup.show(implementationStack);

                //TODO: handle duplicates

                implementationComboBox.setOnAction(new EventHandler<ActionEvent>(){
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
                            if (interf_iter.get("name").textValue().equals(implementationComboBox.getValue().toString())){
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
                        drawConnectionLine(root, CanvasContents, 'c', 'i', "implementation", x, y, targetInfo.get("x").doubleValue(), targetInfo.get("y").doubleValue());
                        implementationPopup.hide();
                        connectionsPopup.hide();
                        actionsPopup.hide();
                    }
                });
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

    private String getName(){
        return this.name;
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

    private VBox createMethodExtraButtonsVBox(List<JFXCheckBox> extraButtons){

        extraButtons.add(new JFXCheckBox("virtual"));
        extraButtons.add(new JFXCheckBox("static"));

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
            rootNode = objectMapper.readTree(CanvasContents);
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
            objectMapper.writeValue(CanvasContents, rootNode);
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
            objectMapper.writeValue(CanvasContents, rootNode);
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
