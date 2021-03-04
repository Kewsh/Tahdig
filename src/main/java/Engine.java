import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Engine {

    protected ObjectMapper objectMapper;
    protected JsonNode rootNode;
    protected File CanvasContents;

    protected Engine(){
        CanvasContents = new File("out/canvas_contents.json");
    }

    public abstract boolean isPossible();

    public boolean isReady(){

        if (!CanvasContents.exists()) return false;
        boolean state;
        openJsonFile();

        ArrayNode classes, functions, interfaces, headers, packages;
        classes = (ArrayNode) rootNode.get("classes");
        functions = (ArrayNode) rootNode.get("functions");
        interfaces = (ArrayNode) rootNode.get("interfaces");
        headers = (ArrayNode) rootNode.get("headers");
        packages = (ArrayNode) rootNode.get("packages");

        if (classes.size() == 0 && functions.size() == 0 && interfaces.size() == 0 && headers.size() == 0 && packages.size() == 0)
            state = false;
        else
            state = true;
        return state;
    }

    public void generateCode() throws IOException {

        (new File("out/code/")).mkdir();            // create code folder
        openJsonFile();
        try{
            generateClassCode();
            generateInterfaceCode();
            generateFunctionCode();
            generateHeaderCode();
            handlePackages();
        } catch (IOException e){
            e.printStackTrace();
        }
        updateJsonFile();
    }

    protected abstract void generateClassCode() throws IOException;
    protected abstract void generateInterfaceCode() throws IOException ;
    protected abstract void generateFunctionCode() throws IOException;
    protected abstract void generateHeaderCode() throws IOException;
    protected abstract void handlePackages() throws IOException;

    protected void updateJsonFile(){
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void openJsonFile(){
        if (rootNode == null) {
            objectMapper = new ObjectMapper();
            rootNode = null;
            try {
                rootNode = objectMapper.readTree(CanvasContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected char[] readFromFile(File file) throws IOException {
        final int MAX_FILE_SIZE = 4096;
        char[] buffer = new char[MAX_FILE_SIZE];
        FileReader myReader = new FileReader(file.getAbsolutePath());
        myReader.read(buffer);
        myReader.close();
        return buffer;
    }

    protected void writeToFile(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file.getAbsolutePath());
        writer.write(text);
        writer.close();
    }

    protected ObjectNode findTargetObject(double x, double y, ArrayNode array){
        for (JsonNode target_iter : array){
            if (target_iter.get("info").get("x").doubleValue() == x && target_iter.get("info").get("y").doubleValue() == y)
                return (ObjectNode) target_iter;
        }
        return null;
    }
}
