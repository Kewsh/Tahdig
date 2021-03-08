package elements;

import buttons.Element;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PackageHexagon {

    private final StackPane stack;

    public PackageHexagon(){

        stack = tools.ShapeDrawer.drawHexagon("Package");
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        stack.setOnDragDetected((MouseEvent event) -> {
            tools.DragDetector.setOnDragDetected(stack, "hexagon");
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
        private StackPane stack, actionsStack;
        private JFXPopup actionsPopup;
        private JFXButton deleteButton;
        private JFXButton connectionsButton, editButton;

        //TODO: implement rename

        public Actions(double x, double y, String name, Group root, StackPane stack, StackPane baseStack, File CanvasContents) throws FileNotFoundException {
            this.x = x;
            this.y = y;
            this.name = name;
            this.root = root;
            this.stack = stack;
            this.CanvasContents = CanvasContents;
            addPackageToCanvasContents();

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

                    JFXButton applyChangesButton = new JFXButton("Apply");
                    JFXDialog packageEditDialog = new JFXDialog(new StackPane(),
                            new Region(),
                            JFXDialog.DialogTransition.CENTER,
                            true);

                    VBox editVBox = new VBox(nameField, applyChangesButton);
                    editVBox.setSpacing(30);
                    editVBox.setMaxHeight(200);
                    JFXDialogLayout packageEditLayout = new JFXDialogLayout();
                    packageEditLayout.setBody(editVBox);
                    packageEditDialog.setContent(packageEditLayout);

                    actionsPopup.hide();
                    packageEditDialog.show(baseStack);

                    Label nameNotGiven = new Label("*name field must not be empty");
                    Label nameNotValid = new Label("*name contains illegal characters");
                    Label nameAlreadyExists = new Label("*this name has already been used");

                    boolean[] errorFlags = {false, false, false};                  //specifies whether each error label is set

                    nameNotGiven.setStyle("-fx-text-fill: red;");
                    nameNotValid.setStyle("-fx-text-fill: red;");
                    nameAlreadyExists.setStyle("-fx-text-fill: red;");

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
                                    rootNode = objectMapper.readTree(CanvasContents);
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
                                    objectMapper.writeValue(CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });

            // actions button

            actionsPopup = new JFXPopup();
            actionsStack = new StackPane();
            actionsStack.setLayoutX(x-40);
            actionsStack.setLayoutY(y-85);
            actionsStack.setMinHeight(100);
            root.getChildren().add(actionsStack);

            connectionsButton = (new buttons.ConnectionButton(x, y, actionsPopup, baseStack, root, Element.HEXAGON, CanvasContents)).getButton();
            try {
                deleteButton = (new buttons.DeleteButton(x, y, name, root, stack, baseStack, actionsPopup,
                        CanvasContents, Element.HEXAGON)).getButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
            actionsPopup.setPopupContent(new HBox(editButton, connectionsButton, deleteButton));
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

        private void setButtonStyles(JFXButton button, String buttonName, double width, double length){
            button.setMinSize(width, length);
            button.setId(buttonName);
            button.setDisableVisualFocus(true);
        }

        private void addPackageToCanvasContents(){

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(CanvasContents);
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
                objectMapper.writeValue(CanvasContents, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
