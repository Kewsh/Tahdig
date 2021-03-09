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
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tools.ConnectionBuilder.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DeleteButton {

    private JFXButton deleteButton, deleteDialogConfirmButton, deleteDialogCancelButton;
    private JFXDialog deleteDialog;
    private JFXDialogLayout deleteDialogLayout;

    public DeleteButton(double x, double y, String name, Group root, StackPane stack,
                        StackPane baseStack, JFXPopup actionsPopup, File CanvasContents, Element element) throws IOException {

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
                        deleteDialogConfirmButton.setFont(new Font(20));
                        deleteDialogCancelButton = new JFXButton("Cancel");
                        deleteDialogCancelButton.setFont(new Font(20));

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
                                for (int i = 0; i < array.size(); i++){
                                    ObjectNode object = (ObjectNode) array.get(i);
                                    if (object.get("info").get("x").doubleValue() == x && object.get("info").get("y").doubleValue() == y)
                                        array.remove(i);
                                }
                                deleteConnections(x, y, root, (ArrayNode) rootNode.get("lines"));

                                //TODO: handle all relations and dependencies of this object (method paramters, return type, attribute types, etc)
                                try {
                                    objectMapper.writeValue(CanvasContents, rootNode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        deleteDialogCancelButton.setOnAction(new EventHandler<ActionEvent>(){
                            @Override
                            public void handle(ActionEvent event) {
                                deleteDialog.close();
                            }
                        });

                        deleteDialogLayout = new JFXDialogLayout();

                        Text text = new Text("Are you sure you want to delete " + name + "?");
                        text.setFont(Font.font("Muli", 18));
                        StackPane textStack = new StackPane(text);
                        textStack.setPadding(new Insets(20, 0, 0, 0));

                        ImageView questionIcon = null;
                        try {
                            questionIcon = new ImageView(new Image(new FileInputStream(new File("src/main/resources/icons/Question.png").getAbsolutePath())));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        StackPane questionStack = new StackPane(questionIcon);
                        questionStack.setPadding(new Insets(20, 0, 0, 0));

                        HBox deleteHbox = new HBox(questionStack, textStack);
                        deleteHbox.setSpacing(20);
                        deleteDialogLayout.setHeading(new Label("Delete"));
                        deleteDialogLayout.setBody(deleteHbox);

                        //TODO: fix: this name isn't updated when element name has changed

                        deleteDialogLayout.setActions(deleteDialogCancelButton, deleteDialogConfirmButton);
                        deleteDialog.setContent(deleteDialogLayout);

                        actionsPopup.hide();
                        deleteDialog.show(baseStack);
                        break;
                    }
                }
            }
        });
    }

    private void deleteConnections(double x, double y, Group root, ArrayNode lines){

        final int MAX_CONNECTIONS = 100;
        int[] deleteIndices = new int[MAX_CONNECTIONS];
        int index = 0;

        for (int i = 0; i < lines.size(); i++){
            ObjectNode line = (ObjectNode) lines.get(i);
            if ((line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y) ||
                    (line.get("endX").doubleValue() == x && line.get("endY").doubleValue() == y)){
                deleteIndices[index++] = i;

                double startX = line.get("startX").doubleValue();
                double startY = line.get("startY").doubleValue();
                double endX = line.get("endX").doubleValue();
                double endY = line.get("endY").doubleValue();
                Point[] endPoints;

                for (Node node : root.getChildren()){
                    if (node.getClass().toString().contains("Line")) {
                        Line line_iter = (Line) node;
                        endPoints = locateConnectionEnds(startX, startY, endX, endY, line_iter.getStartX(), line_iter.getStartY(),
                                    line_iter.getEndX(), line_iter.getEndY());
                        if (endPoints != null) {
                            root.getChildren().remove(node);
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < deleteIndices.length; i++)
            lines.remove(deleteIndices[i]);
    }

    private Point[] locateConnectionEnds(double x1, double y1, double x2, double y2, double tX1, double tY1, double tX2, double tY2){

        Point[] endPoints = new Point[2];
        Point[] possibleStates = new Point[]{new Point(x1, y1+60), new Point(x1+50, y1), new Point(x1+100, y1+60), new Point(x1+50, y1+120),
                                             new Point(x1, y1+60), new Point(x1+75, y1), new Point(x1+150, y1+60), new Point(x1+75, y1+120),
                                             new Point(x1, y1+50), new Point(x1+75, y1), new Point(x1+150, y1+50), new Point(x1+75, y1+100),
                                             new Point(x2, y2+60), new Point(x2+50, y2), new Point(x2+100, y2+60), new Point(x2+50, y2+120),
                                             new Point(x2, y2+60), new Point(x2+75, y2), new Point(x2+150, y2+60), new Point(x2+75, y2+120),
                                             new Point(x2, y2+50), new Point(x2+75, y2), new Point(x2+150, y2+50), new Point(x2+75, y2+100)};
        for (Point point1 : possibleStates){
            if (point1.x == tX1 && point1.y == tY1){
                endPoints[0] = point1;
                for (Point point2 : possibleStates){
                    if (point2.x == tX2 && point2.y == tY2){
                        endPoints[1] = point2;
                        return endPoints;
                    }
                }
            }
        }
        return null;
    }

    public JFXButton getButton(){
        return deleteButton;
    }
}
