import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CppEngine {

    private static CppEngine engine = null;
    private File CanvasContents;
    private JsonNode rootNode;
    private ObjectMapper objectMapper;

    public static CppEngine getInstance(){                              // singleton
        if (engine == null)
            engine = new CppEngine();
        return engine;
    }

    public boolean isPossible(){
        boolean state = true;
        openJsonFile();

        if (rootNode.get("interfaces").size() != 0)
            state = false;

        //TODO: answer this: are packages also considered java-only?
        return state;
    }

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

    private CppEngine(){
        CanvasContents = new File("out/canvas_contents.json");
    }

    private void handlePackages() throws IOException {

        //TODO:  handle packages here
        //      ignoring packages for now ---> is it even possible in cpp?
        //      like, can it be done without having header-files for every class?
    }

    private void generateHeaderCode() throws IOException {

        for (JsonNode header : rootNode.get("headers")){
            File headerFile = new File("out/code/" + header.get("name").textValue() + ".hpp");
            headerFile.createNewFile();
            String fileContents = "#pragma once\n\n";

            for (JsonNode variable : header.get("info").get("variables"))
                fileContents += variable.get("type").textValue() + " " + variable.get("name").textValue() + "\n\n";
            for (JsonNode function : header.get("info").get("functions"))
                fileContents += function.get("return").textValue() + " " + function.get("name").textValue() + "();\n\n";
            for (JsonNode class_iter : header.get("info").get("classes"))
                fileContents += "class " + class_iter.get("name").textValue() + ";\n\n";

            writeToFile(headerFile, fileContents);
        }
    }

    private void generateFunctionCode() throws IOException {

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
            writeToFile(functionsFile, fileContents);
        }
    }

    private void generateInterfaceCode() throws IOException {

        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        for (JsonNode interf_iter : interfaces){

            File interfaceFile = new File("out/code/" + interf_iter.get("name").textValue() + "_absCLASS" + ".cpp");
            if (!interfaceFile.exists())
                interfaceFile.createNewFile();
            String fileContents = "class " + interf_iter.get("name").textValue() + "_absCLASS" + " ";

            double x = interf_iter.get("info").get("x").doubleValue();
            double y = interf_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("type").textValue().equals("inheritance") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){

                    ObjectNode targetInterface = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), interfaces);
                    File tempFile = new File("out/code/" + targetInterface.get("name").textValue() + "_absCLASS" + ".cpp");
                    tempFile.createNewFile();
                    String tempFileContents = "class " + targetInterface.get("name").textValue() + "_absCLASS" + " {\n\n";
                    tempFileContents += "\tprivate: void dummy()=0;\n\n";          // this pure virtual dummy method makes the class abstract

                    for (JsonNode method : targetInterface.get("info").get("methods")){
                        tempFileContents += "\tpublic: ";
                        if (method.get("extra").textValue().contains("static")) tempFileContents += "static ";
                        tempFileContents += method.get("return").textValue() + " ";
                        tempFileContents += method.get("name").textValue() + "();\n\n";

                        //TODO: handle parameters here
                    }
                    tempFileContents += "};\n";
                    writeToFile(tempFile, tempFileContents);

                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetInterface.get("name").textValue() + "_absCLASS ";
                    } else{
                        fileContents += ", " + targetInterface.get("name").textValue() + "_absCLASS ";
                    }
                }
            }

            fileContents += "{\n\n\tprivate: void dummy()=0;\n\n";
            for (JsonNode method : interf_iter.get("info").get("methods")){
                fileContents += "\tpublic: ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";

                //TODO: handle parameters here
            }
            fileContents += "};\n";
            writeToFile(interfaceFile, fileContents);
        }
    }

    private void generateClassCode() throws IOException {

        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes) {
            File classFile = new File("out/code/" + class_iter.get("name").textValue() + ".cpp");
            classFile.createNewFile();
            String fileContents = "class " + class_iter.get("name").textValue() + " ";

            double x = class_iter.get("info").get("x").doubleValue();
            double y = class_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("type").textValue().equals("inheritance") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    ObjectNode targetClass = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), classes);

                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetClass.get("name").textValue() + " ";
                    } else{
                        fileContents += ", " + targetClass.get("name").textValue() + " ";
                    }
                }
            }

            for (JsonNode line : rootNode.get("lines")){
                if (line.get("type").textValue().equals("implementation") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    ObjectNode targetInterface = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), (ArrayNode) rootNode.get("interfaces"));

                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += ": public " + targetInterface.get("name").textValue() + "_absCLASS ";
                    } else{
                        fileContents += ", " + targetInterface.get("name").textValue() + "_absCLASS ";
                    }
                }
            }

            fileContents += "{\n\n";
            for (JsonNode attribute : class_iter.get("info").get("attributes")){
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
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("type").textValue().equals("composition") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    ObjectNode targetClass = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), classes);
                    fileContents += "\tprivate " + targetClass.get("name").textValue() + " composition" + compositionId++ + ";\n\n";
                }
            }

            for (JsonNode method : class_iter.get("info").get("methods")){
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
            writeToFile(classFile, fileContents);
        }
    }

    private void updateJsonFile(){
        try {
            objectMapper.writeValue(CanvasContents, rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openJsonFile(){
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

    private char[] readFromFile(File file) throws IOException {
        final int MAX_FILE_SIZE = 4096;
        char[] buffer = new char[MAX_FILE_SIZE];
        FileReader myReader = new FileReader(file.getAbsolutePath());
        myReader.read(buffer);
        myReader.close();
        return buffer;
    }

    private void writeToFile(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file.getAbsolutePath());
        writer.write(text);
        writer.close();
    }

    private ObjectNode findTargetObject(double x, double y, ArrayNode array){
        for (JsonNode target_iter : array){
            if (target_iter.get("info").get("x").doubleValue() == x && target_iter.get("info").get("y").doubleValue() == y)
                return (ObjectNode) target_iter;
        }
        return null;
    }
}
