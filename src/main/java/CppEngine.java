import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CppEngine {

    private static CppEngine engine = null;
    private File CanvasContents;

    private CppEngine(){
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

        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        for (JsonNode interf_iter : interfaces){

            File interfaceFile = new File("out/code/" + interf_iter.get("name").textValue() + "_absCLASS" + ".cpp");
            if (!interfaceFile.exists())
                interfaceFile.createNewFile();
            String fileContents = "class " + interf_iter.get("name").textValue() + "_absCLASS" + " ";

            double x = interf_iter.get("info").get("x").doubleValue();
            double y = interf_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            ArrayNode lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("type").textValue().equals("inheritance") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetInterface = null;
                    for (JsonNode target_interf_iter : interfaces) {
                        if (target_interf_iter.get("info").get("x").doubleValue() == targetX &&
                                target_interf_iter.get("info").get("y").doubleValue() == targetY) {
                            targetInterface = (ObjectNode) target_interf_iter;
                            break;
                        }
                    }
                    File tempFile = new File("out/code/" + targetInterface.get("name").textValue() + "_absCLASS" + ".cpp");
                    tempFile.createNewFile();

                    String tempFileContents = "class " + targetInterface.get("name").textValue() + "_absCLASS" + " {\n\n";
                    tempFileContents += "{\n\n\tprivate: void dummy()=0;\n\n";          // this pure virtual dummy method makes the class abstract
                    ArrayNode methods = (ArrayNode) targetInterface.get("info").get("methods");
                    for (JsonNode method : methods){
                        tempFileContents += "\tpublic: ";
                        if (method.get("extra").textValue().contains("static")) tempFileContents += "static ";
                        tempFileContents += method.get("return").textValue() + " ";
                        tempFileContents += method.get("name").textValue() + "();\n\n";

                        //TODO: handle parameters here
                    }
                    tempFileContents += "};\n";
                    FileWriter tempWriter = new FileWriter(tempFile.getAbsolutePath());
                    tempWriter.write(tempFileContents);
                    tempWriter.close();

                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetInterface.get("name").textValue() + "_absCLASS ";
                    } else{
                        fileContents += ", " + targetInterface.get("name").textValue() + "_absCLASS ";
                    }
                }
            }

            fileContents += "{\n\n\tprivate: void dummy()=0;\n\n";
            ArrayNode methods = (ArrayNode) interf_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\tpublic: ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";

                //TODO: handle parameters here
            }
            fileContents += "};\n";
            FileWriter myWriter = new FileWriter(interfaceFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }

        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes) {
            File classFile = new File("out/code/" + class_iter.get("name").textValue() + ".cpp");
            classFile.createNewFile();
            String fileContents = "class " + class_iter.get("name").textValue() + " ";

            double x = class_iter.get("info").get("x").doubleValue();
            double y = class_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            ArrayNode lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("type").textValue().equals("inheritance") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetClass = null;
                    for (JsonNode target_class_iter : classes){
                        if (target_class_iter.get("info").get("x").doubleValue() == targetX &&
                                target_class_iter.get("info").get("y").doubleValue() == targetY){
                            targetClass = (ObjectNode) target_class_iter;
                            break;
                        }
                    }
                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetClass.get("name").textValue() + " ";
                    } else{
                        fileContents += ", " + targetClass.get("name").textValue();
                    }
                }
            }

            lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("type").textValue().equals("implementation") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetInterface = null;
                    for (JsonNode target_interf_iter : rootNode.get("interfaces")){
                        if (target_interf_iter.get("info").get("x").doubleValue() == targetX &&
                                target_interf_iter.get("info").get("y").doubleValue() == targetY){
                            targetInterface = (ObjectNode) target_interf_iter;
                            break;
                        }
                    }
                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetInterface.get("name").textValue() + "_absCLASS ";
                    } else{
                        fileContents += ", " + targetInterface.get("name").textValue() + "_absCLASS ";
                    }
                }
            }

            fileContents += "{\n\n";
            ArrayNode attributes = (ArrayNode) class_iter.get("info").get("attributes");
            for (JsonNode attribute : attributes){
                fileContents += "\t";
                if (attribute.get("access").textValue().equals("default"))
                    fileContents += "private: ";
                else
                    fileContents += attribute.get("access").textValue() + ": ";
                if (attribute.get("extra").textValue().contains("static")) fileContents += "static ";
                if (attribute.get("extra").textValue().contains("constant")) fileContents += "const ";
                if (attribute.get("type").textValue().equals("byte"))
                    fileContents += "char ";
                else if (attribute.get("type").textValue().equals("boolean"))
                    fileContents += "bool ";
                else
                    fileContents += attribute.get("type").textValue() + " ";
                fileContents += attribute.get("name").textValue() + ";\n\n";
            }

            int compositionId = 1;
            lines = (ArrayNode) rootNode.get("lines");
            for (JsonNode line : lines){
                if (line.get("type").textValue().equals("composition") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    ObjectNode targetClass = null;
                    for (JsonNode target_class_iter : classes){
                        if (target_class_iter.get("info").get("x").doubleValue() == targetX &&
                                target_class_iter.get("info").get("y").doubleValue() == targetY){
                            targetClass = (ObjectNode) target_class_iter;
                            break;
                        }
                    }
                    fileContents += "\tprivate " + targetClass.get("name").textValue() + " composition" + compositionId++ + ";\n\n";
                }
            }

            ArrayNode methods = (ArrayNode) class_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\t";
                if (method.get("access").textValue().equals("default"))
                    fileContents += "private: ";
                else
                    fileContents += method.get("access").textValue() + ": ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                if (method.get("extra").textValue().contains("virtual")) fileContents += "virtual ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";
            }

            fileContents += "};\n";
            FileWriter myWriter = new FileWriter(classFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }

        ArrayNode functions = (ArrayNode) rootNode.get("functions");
        if (functions.size() != 0){
            File functionsFile = new File("out/code/GlobalFunctions.cpp");                     //TODO: protect this name
            functionsFile.createNewFile();
            String fileContents = "class GlobalFunctions {\n\n";
            for (JsonNode function : functions){
                fileContents += "\tpublic: static " + function.get("info").get("return").textValue() + " ";
                fileContents += function.get("name").textValue() + "();\n\n";
                //TODO: handle parameters here
            }
            fileContents += "};\n";
            FileWriter myWriter = new FileWriter(functionsFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }

        //TODO: handle header files and packages here



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
        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        if (interfaces.size() != 0)
            state = false;
        // are packages also considered java-only?
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

    public static CppEngine getInstance(){                              // singleton
        if (engine == null)
            engine = new CppEngine();
        return engine;
    }
}
