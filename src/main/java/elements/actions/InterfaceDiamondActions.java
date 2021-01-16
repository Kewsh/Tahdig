package elements.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class InterfaceDiamondActions {

    private double x, y;
    private String name;
    private File CanvasContents;
    private Group root;
    private StackPane stack, actionsStack;
    private JFXPopup actionsPopup;
    private JFXButton deleteButton;

    private final boolean flag[] = {false, false};          // used to make sure popups are shown and hidden properly

    //TODO: implement rename

    public InterfaceDiamondActions(double x, double y, String name, Group root, StackPane stack, File CanvasContents) throws FileNotFoundException {

        this.x = x;
        this.y = y;
        this.name = name;
        this.root = root;
        this.stack = stack;
        this.CanvasContents = CanvasContents;
        addClassToCanvasContents();

        deleteButton = new JFXButton();
        String path = new File("src/main/resources/icons/TrashCan.png").getAbsolutePath();
        deleteButton.setGraphic(new ImageView(new Image(new FileInputStream(path))));

        // actions button

        actionsStack = new StackPane();
        actionsStack.setLayoutX(x-125);
        actionsStack.setLayoutY(y-85);
        actionsStack.setMinHeight(100);
        root.getChildren().add(actionsStack);

        actionsPopup = new JFXPopup();
        actionsPopup.setPopupContent(new HBox(deleteButton));
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
}
