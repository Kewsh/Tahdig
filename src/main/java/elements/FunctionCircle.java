package elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class FunctionCircle {

    private final StackPane stack;

    public FunctionCircle(){

        stack = tools.ShapeDrawer.drawCircle("Function");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            tools.DragDetector.setOnDragDetected(stack, "circle");
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
        private File CanvasContents;
        private Group root;
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXButton editButton, deleteButton;

        private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, File CanvasContents) throws FileNotFoundException {

            this.x = x;
            this.y = y;
            this.name = name;
            this.returnType = "void";
            this.root = root;
            this.stack = stack;
            this.CanvasContents = CanvasContents;
            addFunctionToCanvasContents();

            // edit button

            editButton = new JFXButton();
            String path = new File("src/main/resources/icons/EditPencil.png").getAbsolutePath();
            editButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
            editButton.setMinSize(68,70);
            editButton.setDisableVisualFocus(true);

            editButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    List<JFXRadioButton> typeButtons = new ArrayList<JFXRadioButton>();
                    ToggleGroup typeGroup = new ToggleGroup();
                    VBox typeButtonsVBox = createTypeButtonsVBox(typeButtons, typeGroup);

                    JFXTextField nameField = new JFXTextField();
                    nameField.setText(getName());

                    JFXButton applyChangesButton = new JFXButton("Apply");
                    JFXDialog functionEditDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    HBox editHBox = new HBox(typeButtonsVBox, nameField, applyChangesButton);
                    editHBox.setSpacing(50);
                    editHBox.setMinWidth(450);

                    VBox editVBox = new VBox(editHBox);
                    JFXDialogLayout functionEditLayout = new JFXDialogLayout();

                    functionEditLayout.setBody(editVBox);
                    functionEditDialog.setContent(functionEditLayout);

                    StackPane functionEditStack = new StackPane();
                    functionEditStack.setLayoutX(x + 130);
                    functionEditStack.setLayoutY(y);

                    root.getChildren().add(functionEditStack);
                    functionEditDialog.show(functionEditStack);

                    Label typeNotSpecified = new Label("*no return type specified");
                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false};               //specifies whether each error label is set

                    typeNotSpecified.setStyle("-fx-text-fill: red;");
                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

                    applyChangesButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override
                        public void handle(ActionEvent event) {

                            for (int i = 0; i < 3; i++)
                                errorFlags[i] = false;

                            if (editVBox.getChildren().contains(nameAlreadyExists))
                                editVBox.getChildren().remove(nameAlreadyExists);
                            if (editVBox.getChildren().contains(typeNotSpecified))
                                editVBox.getChildren().remove(typeNotSpecified);
                            if (editVBox.getChildren().contains(nameNotGiven))
                                editVBox.getChildren().remove(nameNotGiven);

                            JFXRadioButton selectedType = (JFXRadioButton) typeGroup.getSelectedToggle();
                            if (selectedType == null) {
                                editVBox.getChildren().add(typeNotSpecified);
                                errorFlags[0] = true;
                            }

                            String inputName = nameField.getText();
                            if (inputName.equals("")) {
                                editVBox.getChildren().add(nameNotGiven);
                                errorFlags[1] = true;
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
                                    ArrayNode classes = (ArrayNode) rootNode.get("classes");
                                    for (JsonNode class_iter : classes) {
                                        if (class_iter.get("name").textValue().equals(inputName)) {
                                            editVBox.getChildren().add(nameAlreadyExists);
                                            errorFlags[2] = true;
                                            break;
                                        }
                                    }
                                    if (!errorFlags[2]) {
                                        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
                                        for (JsonNode interf_iter : interfaces) {
                                            if (interf_iter.get("name").textValue().equals(inputName)) {
                                                editVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[2] = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!errorFlags[2]) {
                                        ArrayNode functions = (ArrayNode) rootNode.get("functions");
                                        for (JsonNode func_iter : functions) {
                                            if (func_iter.get("name").textValue().equals(inputName)) {
                                                editVBox.getChildren().add(nameAlreadyExists);
                                                errorFlags[2] = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!errorFlags[0] && !errorFlags[1] && !errorFlags[2]){

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

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-10);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            try {
                deleteButton = (new buttons.DeleteButton(x, y, 150, 0, name, root, stack, actionsPopup,
                        CanvasContents, buttons.DeleteButton.Element.CIRCLE)).getButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, deleteButton));
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

        private void setName(String name){
            this.name = name;
        }

        private void setReturnType(String returnType){
            this.returnType = returnType;
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
                rootNode = objectMapper.readTree(CanvasContents);
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
                objectMapper.writeValue(CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
