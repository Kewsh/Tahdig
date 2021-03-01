import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JavaEngine {

    private static JavaEngine engine = null;
    private File CanvasContents;

    private JavaEngine(){
        CanvasContents = new File("out/canvas_contents.json");
    }

    public void generateCode() throws IOException {

        (new File("out/code/")).mkdir();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes) {

            double x = class_iter.get("info").get("x").doubleValue();
            double y = class_iter.get("info").get("y").doubleValue();

            File classFile = new File("out/code/" + class_iter.get("name").textValue() + ".java");
            classFile.createNewFile();
            String fileContents = "public class " + class_iter.get("name").textValue() + " ";

            boolean inheritanceFlag = false;
            boolean implementationFlag = false;
            ArrayNode lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("inheritance")){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetClass = null;
                    for (JsonNode target_class_iter : classes) {
                        if (target_class_iter.get("info").get("x").doubleValue() == targetX && target_class_iter.get("info").get("y").doubleValue() == targetY) {
                            targetClass = (ObjectNode) target_class_iter;
                            break;
                        }
                    }
                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += "extends " + targetClass.get("name").textValue() + " ";
                    }
                    else{
                        if (!implementationFlag){
                            implementationFlag = true;
                            fileContents += "implements " + targetClass.get("name").textValue() + "_interface" + " ";
                        }
                        else{
                            fileContents += ", " + targetClass.get("name").textValue() + "_interface" + " ";
                        }
                        File tempFile = new File("out/code/" + targetClass.get("name").textValue() + "_interface" + ".java");
                        tempFile.createNewFile();
                        String tempFileContents = "public interface " + targetClass.get("name").textValue() + "_interface {\n\n";
                        ArrayNode tempClassMethods = (ArrayNode) targetClass.get("info").get("methods");

                        for (JsonNode method : tempClassMethods){
                            tempFileContents += "public " + method.get("return").textValue() + method.get("name") + "();\n\n";
                        }
                        tempFileContents += "}\n";
                        FileWriter tempWriter = new FileWriter(tempFile.getAbsolutePath());
                        tempWriter.write(tempFileContents);
                        tempWriter.close();
                    }
                }
            }

            lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("implementation")){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetInterface = null;
                    for (JsonNode target_interf_iter : (ArrayNode) rootNode.get("interfaces")) {
                        if (target_interf_iter.get("info").get("x").doubleValue() == targetX && target_interf_iter.get("info").get("y").doubleValue() == targetY) {
                            targetInterface = (ObjectNode) target_interf_iter;
                            break;
                        }
                    }
                    if (!implementationFlag){
                        implementationFlag = true;
                        fileContents += "implements " + targetInterface.get("name").textValue();
                    }
                    else{
                        fileContents += ", " + targetInterface.get("name").textValue() + " ";
                    }
                }
            }

            fileContents += "{\n\n";
            ArrayNode attributes = (ArrayNode) class_iter.get("info").get("attributes");
            for (JsonNode attribute : attributes){
                fileContents += "\t";
                if (!attribute.get("access").textValue().equals("default"))
                    fileContents += attribute.get("access").textValue() + " ";
                if (attribute.get("extra").textValue().contains("static")) fileContents += "static ";
                if (attribute.get("extra").textValue().contains("constant")) fileContents += "final ";
                fileContents += attribute.get("type").textValue() + " ";
                fileContents += attribute.get("name").textValue() + ";\n\n";
            }

            lines = (ArrayNode) rootNode.get("lines");
            int compositionId = 1;
            for (JsonNode line : lines){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("composition")){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetClass = null;
                    for (JsonNode target_class_iter : classes) {
                        if (target_class_iter.get("info").get("x").doubleValue() == targetX && target_class_iter.get("info").get("y").doubleValue() == targetY) {
                            targetClass = (ObjectNode) target_class_iter;
                            break;
                        }
                    }
                    fileContents += "private " + targetClass.get("name").textValue() + " composition" + compositionId + ";\n\n";
                }
            }

            ArrayNode methods = (ArrayNode) class_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\t";
                if (!method.get("access").textValue().equals("default"))
                    fileContents += method.get("access").textValue() + " ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                if (!method.get("extra").textValue().contains("virtual")) fileContents += "final ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";
            }

            fileContents += "}\n";
            FileWriter myWriter = new FileWriter(classFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }
        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        for (JsonNode interf_iter : interfaces){
            File interfaceFile = new File("out/code/" + interf_iter.get("name").textValue() + ".java");
            interfaceFile.createNewFile();
            String fileContents = "public interface " + interf_iter.get("name").textValue() + " ";

            double x = interf_iter.get("info").get("x").doubleValue();
            double y = interf_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            ArrayNode lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("inheritance")){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetInterface = null;
                    for (JsonNode target_interf_iter : interfaces) {
                        if (target_interf_iter.get("info").get("x").doubleValue() == targetX && target_interf_iter.get("info").get("y").doubleValue() == targetY) {
                            targetInterface = (ObjectNode) target_interf_iter;
                            break;
                        }
                    }
                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += "extends " + targetInterface.get("name").textValue();
                    }
                    else{
                        fileContents += ", " + targetInterface.get("name").textValue() + " ";
                    }
                }
            }

            fileContents += "{\n\n";
            ArrayNode methods = (ArrayNode) interf_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\t";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";
                //TODO: handle parameters here
            }
            fileContents += "}\n";
            FileWriter myWriter = new FileWriter(interfaceFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }
        ArrayNode functions = (ArrayNode) rootNode.get("functions");
        if (functions.size() != 0){
            File functionsFile = new File("out/code/GlobalFunctions.java");                     //TODO: protect this name
            functionsFile.createNewFile();
            String fileContents = "public abstract class GlobalFunctions {\n\n";
            for (JsonNode function : functions){
                fileContents += "\tpublic static " + function.get("info").get("return").textValue() + " ";
                fileContents += function.get("name").textValue() + "();\n\n";
                //TODO: handle parameters here
            }
            fileContents += "}\n";
            FileWriter myWriter = new FileWriter(functionsFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }

        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPossible(){
        boolean state = true;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayNode headers = (ArrayNode) rootNode.get("headers");
        if (headers.size() != 0)
            state = false;
        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes){
            ObjectNode info = (ObjectNode) class_iter.get("info");
            double x = info.get("x").doubleValue();
            double y = info.get("y").doubleValue();
            int count = 0;
            ArrayNode lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("inheritance"))
                    count++;
                if (count == 2)
                    break;
            }
            if (count == 2) {
                state = false;
                break;
            }
        }
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state;
    }

    public boolean isReady(){
        if (!CanvasContents.exists()) return false;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        boolean state = false;

        try {
            rootNode = objectMapper.readTree(CanvasContents);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static JavaEngine getInstance(){                             // singleton
        if (engine == null)
            engine = new JavaEngine();
        return engine;
    }
}
