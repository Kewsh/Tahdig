package elements;

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

public class HeaderFileEllipse {

    private final StackPane stack;

    public HeaderFileEllipse(){

        stack = tools.ShapeDrawer.drawEllipse("Header-File");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            tools.DragDetector.setOnDragDetected(stack, "ellipse");
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
        private File CanvasContents;
        private Group root;
        private VBox variablesDialogContent, functionsDialogContent, classesDialogContent;
        private StackPane stack, actionsStack, variablesStack, functionsStack, classesStack;
        private JFXPopup actionsPopup;
        private JFXButton deleteButton;
        private JFXButton functionsButton, variablesButton, classesButton;
        private JFXButton variablesCloseButton, addVariableButton, addFunctionButton, functionsCloseButton;
        private JFXButton addClassButton, classesCloseButton, editButton;
        private JFXDialog variablesDialog, functionsDialog, classesDialog;
        private JFXDialogLayout variablesDialogLayout, functionsDialogLayout, classesDialogLayout;
        private JFXTreeTableView variableTreeView, functionTreeView, classTreeView;

        private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack, File CanvasContents) throws FileNotFoundException {

            this.x = x;
            this.y = y;
            this.name = name;
            this.root = root;
            this.stack = stack;
            this.CanvasContents = CanvasContents;
            addHeaderToCanvasContents();

            editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

            functionsButton = new JFXButton("Functions");
            variablesButton = new JFXButton("Variables");
            classesButton = new JFXButton("Classes");

            setButtonStyles(functionsButton, "functionsButton", 50, 70);
            setButtonStyles(variablesButton, "variablesButton", 50, 70);
            setButtonStyles(classesButton, "classesButton", 50, 70);

            // edit button

            editButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    JFXTextField nameField = new JFXTextField();
                    nameField.setText(getName());

                    JFXButton applyChangesButton = new JFXButton("Apply");
                    JFXDialog headerEditDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    VBox editVBox = new VBox(nameField, applyChangesButton);
                    editVBox.setSpacing(30);
                    editVBox.setMaxHeight(200);

                    JFXDialogLayout headerEditLayout = new JFXDialogLayout();

                    headerEditLayout.setBody(editVBox);
                    headerEditDialog.setContent(headerEditLayout);
                    headerEditDialog.show(baseStack);

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
                                    ArrayNode headers = (ArrayNode) rootNode.get("headers");
                                    for (JsonNode header_iter : headers) {
                                        if (header_iter.get("name").textValue().equals(inputName)) {
                                            editVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[1] = true;
                                            break;
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1]){

                                    ArrayNode headers = (ArrayNode) rootNode.get("headers");
                                    ObjectNode targetHeader = null;
                                    int index = 0;
                                    for (int i = 0; i < headers.size(); i++){
                                        ObjectNode info = (ObjectNode) headers.get(i).get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            targetHeader = (ObjectNode) headers.get(i);
                                            index = i;
                                            break;
                                        }
                                    }
                                    ObjectNode targetInfo = (ObjectNode) targetHeader.get("info");

                                    targetHeader.put("name", inputName);
                                    targetHeader.put("info", targetInfo);
                                    headers.remove(index);
                                    headers.add(targetHeader);

                                    setName(inputName);

                                    for (Node node : root.getChildren()){
                                        if (node == stack){
                                            Text text = (Text) stack.getChildren().get(1);
                                            text.setText(inputName);                            // updating the name on the shape
                                            break;
                                        }
                                    }

                                    headerEditDialog.close();
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

            // classes button

            classesDialog = new JFXDialog(new StackPane(),
                    new Region(),
                    JFXDialog.DialogTransition.CENTER,
                    false);

            classesCloseButton = new JFXButton("Close");
            classesCloseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    classesDialog.close();
                }
            });

            classesDialogLayout = new JFXDialogLayout();
            classesDialogLayout.setHeading(new Label("Classes"));
            classesDialogLayout.setActions(classesCloseButton);
            classesDialog.setContent(classesDialogLayout);

            addClassButton = new JFXButton("Add class");
            addClassButton.setId("addClassButton");
            addClassButton.setMinWidth(450);

            addClassButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton classGenerateButton = new JFXButton("Generate");
                    JFXDialog classGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox classesHBox = new HBox(nameField, classGenerateButton);
                    classesHBox.setSpacing(50);
                    classesHBox.setMinWidth(450);

                    VBox classesVBox = new VBox(classesHBox);
                    JFXDialogLayout classGenerateLayout = new JFXDialogLayout();

                    classGenerateLayout.setBody(classesVBox);
                    classGenerateDialog.setContent(classGenerateLayout);
                    classGenerateDialog.show(baseStack);

                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false};                  //specifies whether each error label is set

                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    classGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 2; i++)
                                errorFlags[i] = false;

                            if (classesVBox.getChildren().contains(nameAlreadyExists))
                                classesVBox.getChildren().remove(nameAlreadyExists);
                            if (classesVBox.getChildren().contains(nameNotGiven))
                                classesVBox.getChildren().remove(nameNotGiven);

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                classesVBox.getChildren().add(nameNotGiven);
                                errorFlags[0] = true;
                            }
                            else{
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode classes;
                                ArrayNode headers;

                                try {
                                    rootNode = objectMapper.readTree(CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                classes = (ArrayNode) rootNode.get("classes");
                                for (JsonNode class_iter : classes){
                                    if (class_iter.get("name").textValue().equals(inputName)){
                                        classesVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[1] = true;
                                        break;
                                    }
                                }
                                if (!errorFlags[1]){
                                    headers = (ArrayNode) rootNode.get("headers");
                                    for (JsonNode header_iter : headers){
                                        ObjectNode info = (ObjectNode) header_iter.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            ArrayNode variables = (ArrayNode) info.get("variables");
                                            for (JsonNode variable : variables){
                                                if (variable.get("name").textValue().equals(inputName)){
                                                    classesVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[1] = true;
                                                    break;
                                                }
                                            }
                                            if (!errorFlags[1]){
                                                ArrayNode functions = (ArrayNode) info.get("functions");
                                                for (JsonNode function : functions){
                                                    if (function.get("name").textValue().equals(inputName)){
                                                        classesVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[1] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!errorFlags[1]){
                                                classes = (ArrayNode) info.get("classes");
                                                for (JsonNode class_iter : classes){
                                                    if (class_iter.get("name").textValue().equals(inputName)){
                                                        classesVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[1] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1]){

                                    ArrayNode classList = null;
                                    ObjectNode targetClass = objectMapper.createObjectNode();
                                    headers = (ArrayNode) rootNode.get("headers");

                                    for (JsonNode header : headers){
                                        ObjectNode info = (ObjectNode) header.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            classList = (ArrayNode) info.get("classes");
                                            break;
                                        }
                                    }
                                    targetClass.put("name", inputName);

                                    classList.add(targetClass);
                                    updateClassesTreeView(classTreeView, inputName);
                                    classGenerateDialog.close();
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

            classesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    classTreeView = getClasses();
                    classesDialogContent = new VBox(classTreeView, addClassButton);
                    classesDialogContent.setSpacing(15);
                    classesDialogLayout.setBody(classesDialogContent);
                    classesDialog.show(baseStack);
                }
            });

            // functions button

            functionsDialog = new JFXDialog(new StackPane(),
                    new Region(),
                    JFXDialog.DialogTransition.CENTER,
                    false);

            functionsCloseButton = new JFXButton("Close");
            functionsCloseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    functionsDialog.close();
                }
            });

            functionsDialogLayout = new JFXDialogLayout();
            functionsDialogLayout.setHeading(new Label("Functions"));
            functionsDialogLayout.setActions(functionsCloseButton);
            functionsDialog.setContent(functionsDialogLayout);

            addFunctionButton = new JFXButton("Add function");
            addFunctionButton.setId("addFunctionButton");
            addFunctionButton.setMinWidth(500);

            addFunctionButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    ToggleGroup typeGroup = new ToggleGroup();
                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup, true);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton functionGenerateButton = new JFXButton("Generate");
                    JFXDialog functionGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox functionsHBox = new HBox(typeButtonsVBox, nameField, functionGenerateButton);
                    functionsHBox.setSpacing(50);
                    functionsHBox.setMinWidth(450);

                    VBox functionsVBox = new VBox(functionsHBox);
                    JFXDialogLayout functionGenerateLayout = new JFXDialogLayout();

                    functionGenerateLayout.setBody(functionsVBox);
                    functionGenerateDialog.setContent(functionGenerateLayout);
                    functionGenerateDialog.show(baseStack);

                    Label typeNotSpecified = new Label("*no return type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false};               //specifies whether each error label is set

                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    functionGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 3; i++)
                                errorFlags[i] = false;

                            if (functionsVBox.getChildren().contains(nameAlreadyExists))
                                functionsVBox.getChildren().remove(nameAlreadyExists);
                            if (functionsVBox.getChildren().contains(typeNotSpecified))
                                functionsVBox.getChildren().remove(typeNotSpecified);
                            if (functionsVBox.getChildren().contains(nameNotGiven))
                                functionsVBox.getChildren().remove(nameNotGiven);

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                functionsVBox.getChildren().add(typeNotSpecified);
                                errorFlags[0] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                functionsVBox.getChildren().add(nameNotGiven);
                                errorFlags[1] = true;
                            }
                            else{
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode classes;
                                ArrayNode headers;

                                try {
                                    rootNode = objectMapper.readTree(CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                classes = (ArrayNode) rootNode.get("classes");
                                for (JsonNode class_iter : classes){
                                    if (class_iter.get("name").textValue().equals(inputName)){
                                        functionsVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[2] = true;
                                        break;
                                    }
                                }
                                if (!errorFlags[2]){
                                    headers = (ArrayNode) rootNode.get("headers");
                                    for (JsonNode header_iter : headers){
                                        ObjectNode info = (ObjectNode) header_iter.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            ArrayNode variables = (ArrayNode) info.get("variables");
                                            for (JsonNode variable : variables){
                                                if (variable.get("name").textValue().equals(inputName)){
                                                    functionsVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[2] = true;
                                                    break;
                                                }
                                            }
                                            if (!errorFlags[2]){
                                                ArrayNode functions = (ArrayNode) info.get("functions");
                                                for (JsonNode function : functions){
                                                    if (function.get("name").textValue().equals(inputName)){
                                                        functionsVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[2] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!errorFlags[2]){
                                                classes = (ArrayNode) info.get("classes");
                                                for (JsonNode class_iter : classes){
                                                    if (class_iter.get("name").textValue().equals(inputName)){
                                                        functionsVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[2] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

                                    ArrayNode functionList = null;
                                    ObjectNode targetFunction = objectMapper.createObjectNode();
                                    headers = (ArrayNode) rootNode.get("headers");

                                    for (JsonNode header : headers){
                                        ObjectNode info = (ObjectNode) header.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            functionList = (ArrayNode) info.get("functions");
                                            break;
                                        }
                                    }
                                    targetFunction.put("return", selectedType.getText());
                                    targetFunction.put("name", inputName);

                                    functionList.add(targetFunction);
                                    updateFunctionsTreeView(functionTreeView, selectedType.getText(), inputName);
                                    functionGenerateDialog.close();
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

            functionsButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    functionTreeView = getFunctions();
                    functionsDialogContent = new VBox(functionTreeView, addFunctionButton);
                    functionsDialogContent.setSpacing(15);
                    functionsDialogLayout.setBody(functionsDialogContent);
                    functionsDialog.show(baseStack);
                }
            });

            // variables button

            variablesDialog = new JFXDialog(new StackPane(),
                    new Region(),
                    JFXDialog.DialogTransition.CENTER,
                    false);

            variablesCloseButton = new JFXButton("Close");
            variablesCloseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    variablesDialog.close();
                }
            });

            variablesDialogLayout = new JFXDialogLayout();
            variablesDialogLayout.setHeading(new Label("Attributes"));
            variablesDialogLayout.setActions(variablesCloseButton);
            variablesDialog.setContent(variablesDialogLayout);

            addVariableButton = new JFXButton("Add variable");
            addVariableButton.setId("addVariableButton");
            addVariableButton.setMinWidth(500);

            addVariableButton.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    ToggleGroup typeGroup = new ToggleGroup();
                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup, false);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setPromptText("name");

                    JFXButton variableGenerateButton = new JFXButton("Generate");
                    JFXDialog variableGenerateDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox variablesHBox = new HBox(typeButtonsVBox, nameField, variableGenerateButton);
                    variablesHBox.setSpacing(50);
                    variablesHBox.setMinWidth(450);

                    VBox variablesVBox = new VBox(variablesHBox);
                    JFXDialogLayout variableGenerateLayout = new JFXDialogLayout();

                    variableGenerateLayout.setBody(variablesVBox);
                    variableGenerateDialog.setContent(variableGenerateLayout);
                    variableGenerateDialog.show(baseStack);

                    Label typeNotSpecified = new Label("*no data type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false};               //specifies whether each error label is set

                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    variableGenerateButton.setOnAction(new EventHandler<ActionEvent>(){

                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 3; i++)
                                errorFlags[i] = false;

                            if (variablesVBox.getChildren().contains(nameAlreadyExists))
                                variablesVBox.getChildren().remove(nameAlreadyExists);
                            if (variablesVBox.getChildren().contains(typeNotSpecified))
                                variablesVBox.getChildren().remove(typeNotSpecified);
                            if (variablesVBox.getChildren().contains(nameNotGiven))
                                variablesVBox.getChildren().remove(nameNotGiven);

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                variablesVBox.getChildren().add(typeNotSpecified);
                                errorFlags[0] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                variablesVBox.getChildren().add(nameNotGiven);
                                errorFlags[1] = true;
                            }
                            else{
                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode rootNode = null;
                                ArrayNode classes;
                                ArrayNode headers;

                                try {
                                    rootNode = objectMapper.readTree(CanvasContents);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                classes = (ArrayNode) rootNode.get("classes");
                                for (JsonNode class_iter : classes){
                                    if (class_iter.get("name").textValue().equals(inputName)){
                                        variablesVBox.getChildren().add(nameAlreadyExists);
                                        errorFlags[2] = true;
                                        break;
                                    }
                                }
                                if (!errorFlags[2]){
                                    headers = (ArrayNode) rootNode.get("headers");
                                    for (JsonNode header_iter : headers){
                                        ObjectNode info = (ObjectNode) header_iter.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            ArrayNode variables = (ArrayNode) info.get("variables");
                                            for (JsonNode variable : variables){
                                                if (variable.get("name").textValue().equals(inputName)){
                                                    variablesVBox.getChildren().add(nameAlreadyExists);
                                                    errorFlags[2] = true;
                                                    break;
                                                }
                                            }
                                            if (!errorFlags[2]){
                                                ArrayNode functions = (ArrayNode) info.get("functions");
                                                for (JsonNode function : functions){
                                                    if (function.get("name").textValue().equals(inputName)){
                                                        variablesVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[2] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!errorFlags[2]){
                                                classes = (ArrayNode) info.get("classes");
                                                for (JsonNode class_iter : classes){
                                                    if (class_iter.get("name").textValue().equals(inputName)){
                                                        variablesVBox.getChildren().add(nameAlreadyExists);
                                                        errorFlags[2] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

                                    ArrayNode variableList = null;
                                    ObjectNode targetVariable = objectMapper.createObjectNode();
                                    headers = (ArrayNode) rootNode.get("headers");

                                    for (JsonNode header : headers){
                                        ObjectNode info = (ObjectNode) header.get("info");
                                        if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y){
                                            variableList = (ArrayNode) info.get("variables");
                                            break;
                                        }
                                    }
                                    targetVariable.put("type", selectedType.getText());
                                    targetVariable.put("name", inputName);

                                    variableList.add(targetVariable);
                                    updateVariablesTreeView(variableTreeView, selectedType.getText(), inputName);
                                    variableGenerateDialog.close();
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

            variablesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    variableTreeView = getVariables();
                    variablesDialogContent = new VBox(variableTreeView, addVariableButton);
                    variablesDialogContent.setSpacing(15);
                    variablesDialogLayout.setBody(variablesDialogContent);
                    variablesDialog.show(baseStack);
                }
            });

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-120);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            try {
                deleteButton = (new buttons.DeleteButton(x, y, 210, 0, name, root, stack, baseStack, actionsPopup,
                        CanvasContents, buttons.DeleteButton.Element.ELLIPSE)).getButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, classesButton, functionsButton, variablesButton, deleteButton));
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

        private VBox createTypeButtonsVBox(List<JFXRadioButton> typeButtons, ToggleGroup typeGroup, boolean isFunction){

            if (isFunction) typeButtons.add(new JFXRadioButton("void"));
            typeButtons.add(new JFXRadioButton("boolean"));
            typeButtons.add(new JFXRadioButton("byte"));
            typeButtons.add(new JFXRadioButton("char"));
            typeButtons.add(new JFXRadioButton("short"));
            typeButtons.add(new JFXRadioButton("int"));
            typeButtons.add(new JFXRadioButton("long"));
            typeButtons.add(new JFXRadioButton("float"));
            typeButtons.add(new JFXRadioButton("double"));

            VBox typeButtonsVBox = new VBox();
            for (int i = 0; i < typeButtons.size(); i++) {
                typeButtons.get(i).setToggleGroup(typeGroup);
                typeButtonsVBox.getChildren().add(typeButtons.get(i));
            }
            return typeButtonsVBox;
        }

        private void addHeaderToCanvasContents(){

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayNode headers = (ArrayNode) rootNode.get("headers");
            ObjectNode info = objectMapper.createObjectNode();
            ObjectNode thisHeader = objectMapper.createObjectNode();

            info.put("x", x);
            info.put("y", y);
            info.put("classes", objectMapper.createArrayNode());
            info.put("functions", objectMapper.createArrayNode());
            info.put("variables", objectMapper.createArrayNode());

            thisHeader.put("name", name);
            thisHeader.put("info", info);

            headers.add(thisHeader);
            try {
                objectMapper.writeValue(CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void updateVariablesTreeView(JFXTreeTableView variableTreeView, String dataType, String inputName) {

            variableTreeView.getRoot().getChildren().add(new TreeItem(new VariableTreeItem(new SimpleStringProperty(dataType),
                    new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getVariables(){

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode variables = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode headers = (ArrayNode) rootNode.get("headers");
            for (JsonNode header : headers){
                info = (ObjectNode) header.get("info");
                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                    variables = (ArrayNode) info.get("variables");
                    break;
                }
            }

            JFXTreeTableColumn<VariableTreeItem, String> typeColumn = new JFXTreeTableColumn<>("Type");
            typeColumn.setPrefWidth(200);
            typeColumn.setEditable(false);
            typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<VariableTreeItem, String> param) -> {
                if (typeColumn.validateValue(param)) {
                    return param.getValue().getValue().dataType;
                } else {
                    return typeColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<VariableTreeItem, String> nameColumn = new JFXTreeTableColumn<>("Name");
            nameColumn.setPrefWidth(300);
            nameColumn.setEditable(false);
            nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<VariableTreeItem, String> param) -> {
                if (nameColumn.validateValue(param)) {
                    return param.getValue().getValue().variableName;
                } else {
                    return nameColumn.getComputedValue(param);
                }
            });

            typeColumn.setCellFactory((TreeTableColumn<VariableTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            nameColumn.setCellFactory((TreeTableColumn<VariableTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<VariableTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode variable : variables)
                treeRows.add(new VariableTreeItem(new SimpleStringProperty(variable.get("type").textValue()),
                        new SimpleStringProperty(variable.get("name").textValue())));

            final TreeItem<VariableTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<VariableTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(500);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(typeColumn, nameColumn);

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

        private void updateFunctionsTreeView(JFXTreeTableView functionTreeView, String returnType, String inputName) {

            functionTreeView.getRoot().getChildren().add(new TreeItem(new FunctionTreeItem(new SimpleStringProperty(returnType),
                    new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getFunctions(){

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode functions = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode headers = (ArrayNode) rootNode.get("headers");
            for (JsonNode header : headers){
                info = (ObjectNode) header.get("info");
                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                    functions = (ArrayNode) info.get("functions");
                    break;
                }
            }

            JFXTreeTableColumn<FunctionTreeItem, String> typeColumn = new JFXTreeTableColumn<>("Type");
            typeColumn.setPrefWidth(200);
            typeColumn.setEditable(false);
            typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FunctionTreeItem, String> param) -> {
                if (typeColumn.validateValue(param)) {
                    return param.getValue().getValue().returnType;
                } else {
                    return typeColumn.getComputedValue(param);
                }
            });

            JFXTreeTableColumn<FunctionTreeItem, String> nameColumn = new JFXTreeTableColumn<>("Name");
            nameColumn.setPrefWidth(300);
            nameColumn.setEditable(false);
            nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FunctionTreeItem, String> param) -> {
                if (nameColumn.validateValue(param)) {
                    return param.getValue().getValue().functionName;
                } else {
                    return nameColumn.getComputedValue(param);
                }
            });

            typeColumn.setCellFactory((TreeTableColumn<FunctionTreeItem, String> param) -> new TextFieldTreeTableCell<>());
            nameColumn.setCellFactory((TreeTableColumn<FunctionTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<FunctionTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode function : functions)
                treeRows.add(new FunctionTreeItem(new SimpleStringProperty(function.get("return").textValue()),
                        new SimpleStringProperty(function.get("name").textValue())));

            final TreeItem<FunctionTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<FunctionTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(500);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(typeColumn, nameColumn);

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

        private void updateClassesTreeView(JFXTreeTableView classTreeView, String inputName) {

            classTreeView.getRoot().getChildren().add(new TreeItem(new ClassTreeItem(new SimpleStringProperty(inputName))));
        }

        private JFXTreeTableView getClasses(){

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode info;
            ArrayNode classes = null;
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayNode headers = (ArrayNode) rootNode.get("headers");
            for (JsonNode header : headers){
                info = (ObjectNode) header.get("info");
                if (info.get("x").doubleValue() == x && info.get("y").doubleValue() == y) {
                    classes = (ArrayNode) info.get("classes");
                    break;
                }
            }

            JFXTreeTableColumn<ClassTreeItem, String> nameColumn = new JFXTreeTableColumn<>("Name");
            nameColumn.setPrefWidth(450);
            nameColumn.setEditable(false);
            nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ClassTreeItem, String> param) -> {
                if (nameColumn.validateValue(param)) {
                    return param.getValue().getValue().className;
                } else {
                    return nameColumn.getComputedValue(param);
                }
            });

            nameColumn.setCellFactory((TreeTableColumn<ClassTreeItem, String> param) -> new TextFieldTreeTableCell<>());

            ObservableList<ClassTreeItem> treeRows = FXCollections.observableArrayList();
            for (JsonNode class_iter : classes)
                treeRows.add(new ClassTreeItem(new SimpleStringProperty(class_iter.get("name").textValue())));

            final TreeItem<ClassTreeItem> root = new RecursiveTreeItem<>(treeRows, RecursiveTreeObject::getChildren);

            JFXTreeTableView<ClassTreeItem> treeView = new JFXTreeTableView<>(root);
            treeView.setMinWidth(450);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.getColumns().setAll(nameColumn);

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

        private final class ClassTreeItem extends RecursiveTreeObject<ClassTreeItem> {

            final StringProperty className;

            public ClassTreeItem(StringProperty className) {
                this.className = className;
            }
        }

        private final class FunctionTreeItem extends RecursiveTreeObject<FunctionTreeItem> {

            final StringProperty returnType;
            final StringProperty functionName;

            public FunctionTreeItem(StringProperty returnType, StringProperty functionName) {
                this.returnType = returnType;
                this.functionName = functionName;
            }
        }

        private final class VariableTreeItem extends RecursiveTreeObject<VariableTreeItem> {

            final StringProperty dataType;
            final StringProperty variableName;

            public VariableTreeItem(StringProperty dataType, StringProperty variableName) {
                this.dataType = dataType;
                this.variableName = variableName;
            }
        }
    }

}
