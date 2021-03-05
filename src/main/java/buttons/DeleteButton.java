package buttons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXPopup;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DeleteButton {

    private StackPane deleteStack;
    private JFXButton deleteButton, deleteDialogConfirmButton, deleteDialogCancelButton;
    private JFXDialog deleteDialog;
    private JFXDialogLayout deleteDialogLayout;

    public enum Element{
        CIRCLE, RECTANGLE, DIAMOND, HEXAGON, ELLIPSE;
    }

    public DeleteButton(double x, double y, double offsetX, double offsetY, String name, Group root, StackPane stack,
                        JFXPopup actionsPopup, File CanvasContents, Element element) throws IOException {

        deleteButton = new JFXButton();
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));
        deleteButton.setDisableVisualFocus(true);

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
                                ArrayNode array = null;
                                switch(element){
                                    case CIRCLE:
                                        array = (ArrayNode) rootNode.get("functions");
                                        break;
                                    case RECTANGLE:
                                        array = (ArrayNode) rootNode.get("classes");
                                        break;
                                    case DIAMOND:
                                        array = (ArrayNode) rootNode.get("interfaces");
                                        break;
                                    case HEXAGON:
                                        array = (ArrayNode) rootNode.get("packages");
                                        break;
                                    case ELLIPSE:
                                        array = (ArrayNode) rootNode.get("headers");
                                }
                                //TODO: change all default statements in switch cases to what they point to. default is not necessary

                                for (int i = 0; i < array.size(); i++){
                                    ObjectNode object = (ObjectNode) array.get(i);
                                    if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                                        array.remove(i);
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
                        deleteStack.setLayoutX(x + offsetX);
                        deleteStack.setLayoutY(y + offsetY);
                        root.getChildren().add(deleteStack);
                        deleteDialog.show(deleteStack);

                        break;
                    }
                }
            }
        });
    }

    public JFXButton getButton(){
        return deleteButton;
    }
}
