import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JavaEngine {

    private static JavaEngine engine = null;
    private File CanvasContents;
    private JsonNode rootNode;
    private ObjectMapper objectMapper;

    public static JavaEngine getInstance(){                             // singleton
        if (engine == null)
            engine = new JavaEngine();
        return engine;
    }

    public boolean isPossible(){
        boolean state = true;
        openJsonFile();

        if (rootNode.get("headers").size() != 0)
            state = false;
        for (JsonNode class_iter : rootNode.get("classes")){
            double x = class_iter.get("info").get("x").doubleValue();
            double y = class_iter.get("info").get("y").doubleValue();
            int count = 0;
            for (JsonNode line : rootNode.get("lines")){
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

    private JavaEngine(){
        CanvasContents = new File("out/canvas_contents.json");
    }

    private void handlePackages() throws IOException {

        //TODO: consider implementing multi-level packages (sub-packages)

        for (JsonNode package_iter : rootNode.get("packages")){

            double x = package_iter.get("info").get("x").doubleValue();
            double y = package_iter.get("info").get("y").doubleValue();
            File packageFolder = new File("out/code/" + package_iter.get("name").textValue());
            packageFolder.mkdir();

            for (JsonNode line : rootNode.get("lines")){
                if (line.get("type").textValue().equals("containment") &&
                        line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y){
                    double targetX = line.get("endX").doubleValue();
                    double targetY = line.get("endY").doubleValue();
                    String name = null;
                    for (JsonNode interf_iter : rootNode.get("interfaces")){
                        if (interf_iter.get("info").get("x").doubleValue() == targetX && interf_iter.get("info").get("y").doubleValue() == targetY){
                            name = interf_iter.get("name").textValue();
                            break;
                        }
                    }
                    if (name == null){
                        for (JsonNode class_iter : rootNode.get("classes")){
                            if (class_iter.get("info").get("x").doubleValue() == targetX && class_iter.get("info").get("y").doubleValue() == targetY){
                                name = class_iter.get("name").textValue();
                                break;
                            }
                        }
                    }
                    moveFile(name, packageFolder.getAbsolutePath());
                }
            }

            File codeDirectory = new File("out/code/");
            File packageDirectory = new File("out/code/" + package_iter.get("name").textValue());

            for (File file : codeDirectory.listFiles()){
                if (file.getName().equals(package_iter.get("name").textValue())) continue;
                if (file.isDirectory()){
                    for (File subDirectoryFile : file.listFiles()){
                        for (File packageFile : packageDirectory.listFiles()) {
                            char[] buffer = readFromFile(subDirectoryFile);
                            editOccurrencesInFile(buffer, packageFile.getName().substring(0, packageFile.getName().indexOf(".")),
                                    package_iter.get("name").textValue(), subDirectoryFile);
                        }
                    }
                } else{
                    for (File packageFile : packageDirectory.listFiles()) {
                        char[] buffer = readFromFile(file);
                        try {
                            editOccurrencesInFile(buffer, packageFile.getName().substring(0, packageFile.getName().indexOf(".")),
                                    package_iter.get("name").textValue(), file);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void generateHeaderCode() throws IOException {

        for (JsonNode header : rootNode.get("headers")){
            File headerClassFile = new File("out/code/" + header.get("name").textValue() + "_class" + ".java");
            headerClassFile.createNewFile();
            String fileContents = "public class " + header.get("name").textValue() + "_class {\n\n";

            for (JsonNode variable : header.get("info").get("variables"))
                fileContents += "\tpublic " + variable.get("type").textValue() + " " + variable.get("name").textValue() + ";\n\n";
            for (JsonNode function : header.get("info").get("functions"))
                fileContents += "\tpublic " + function.get("return").textValue() + " " + function.get("name").textValue() + "();\n\n";
            for (JsonNode class_iter : header.get("info").get("classes"))
                fileContents += "\tpublic class " + class_iter.get("name").textValue() + " {}\n\n";

            fileContents += "}\n";
            writeToFile(headerClassFile, fileContents);
        }
    }

    private void generateFunctionCode() throws IOException {

        ArrayNode functions = (ArrayNode) rootNode.get("functions");
        if (functions.size() != 0) {
            File functionsFile = new File("out/code/GlobalFunctions.java");                     //TODO: protect this name
            functionsFile.createNewFile();
            String fileContents = "public abstract class GlobalFunctions {\n\n";

            for (JsonNode function : functions) {
                fileContents += "\tpublic static " + function.get("info").get("return").textValue() + " ";
                fileContents += function.get("name").textValue() + "();\n\n";
                //TODO: handle parameters here
            }
            fileContents += "}\n";
            writeToFile(functionsFile, fileContents);
        }
    }

    private void generateInterfaceCode() throws IOException {

        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        for (JsonNode interf_iter : interfaces){
            File interfaceFile = new File("out/code/" + interf_iter.get("name").textValue() + ".java");
            interfaceFile.createNewFile();
            String fileContents = "public interface " + interf_iter.get("name").textValue() + " ";

            double x = interf_iter.get("info").get("x").doubleValue();
            double y = interf_iter.get("info").get("y").doubleValue();

            boolean inheritanceFlag = false;
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("inheritance")){
                    ObjectNode targetInterface = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), interfaces);
                    if (!inheritanceFlag){
                        inheritanceFlag = true;
                        fileContents += "extends " + targetInterface.get("name").textValue();
                    }
                    else{
                        fileContents += " , " + targetInterface.get("name").textValue() + " ";
                    }
                }
            }

            fileContents += "{\n\n";
            for (JsonNode method : interf_iter.get("info").get("methods")){
                fileContents += "\t";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";

                //TODO: handle parameters here

            }
            fileContents += "}\n";
            writeToFile(interfaceFile, fileContents);
        }
    }

    private void generateClassCode() throws IOException {

        ArrayNode classes = (ArrayNode) rootNode.get("classes");
        for (JsonNode class_iter : classes) {
            double x = class_iter.get("info").get("x").doubleValue();
            double y = class_iter.get("info").get("y").doubleValue();

            File classFile = new File("out/code/" + class_iter.get("name").textValue() + ".java");
            classFile.createNewFile();
            String fileContents = "public class " + class_iter.get("name").textValue() + " ";

            boolean inheritanceFlag = false;
            boolean implementationFlag = false;
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("inheritance")){
                    ObjectNode targetClass = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), classes);
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
                            fileContents += " , " + targetClass.get("name").textValue() + "_interface" + " ";
                        }
                        File tempFile = new File("out/code/" + targetClass.get("name").textValue() + "_interface" + ".java");
                        tempFile.createNewFile();
                        String tempFileContents = "public interface " + targetClass.get("name").textValue() + "_interface {\n\n";
                        ArrayNode tempClassMethods = (ArrayNode) targetClass.get("info").get("methods");

                        for (JsonNode method : tempClassMethods){
                            tempFileContents += "\tpublic " + method.get("return").textValue() + " " + method.get("name").textValue() + "();\n\n";
                        }
                        tempFileContents += "}\n";
                        writeToFile(tempFile, tempFileContents);
                    }
                }
            }

            for (JsonNode line : rootNode.get("lines")){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("implementation")){
                    ObjectNode targetInterface = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), (ArrayNode) rootNode.get("interfaces"));
                    if (!implementationFlag){
                        implementationFlag = true;
                        fileContents += "implements " + targetInterface.get("name").textValue();
                    }
                    else{
                        fileContents += " , " + targetInterface.get("name").textValue() + " ";
                    }
                }
            }

            fileContents += "{\n\n";
            for (JsonNode attribute : class_iter.get("info").get("attributes")){
                fileContents += "\t";
                if (!attribute.get("access").textValue().equals("default"))
                    fileContents += attribute.get("access").textValue() + " ";
                if (attribute.get("extra").textValue().contains("static")) fileContents += "static ";
                if (attribute.get("extra").textValue().contains("constant")) fileContents += "final ";
                fileContents += attribute.get("type").textValue() + " ";
                fileContents += attribute.get("name").textValue() + ";\n\n";
            }

            int compositionId = 1;
            for (JsonNode line : rootNode.get("lines")){
                if (line.get("startX").doubleValue() == x && line.get("startY").doubleValue() == y && line.get("type").textValue().equals("composition")){
                    ObjectNode targetClass = findTargetObject(line.get("endX").doubleValue(), line.get("endY").doubleValue(), classes);
                    fileContents += "\tprivate " + targetClass.get("name").textValue() + " composition" + compositionId++ + ";\n\n";
                }
            }

            for (JsonNode method : class_iter.get("info").get("methods")){
                fileContents += "\t";
                if (!method.get("access").textValue().equals("default"))
                    fileContents += method.get("access").textValue() + " ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                if (!method.get("extra").textValue().contains("virtual")) fileContents += "final ";

                //TODO: implement other keywords parallel to ClassRectangleActions method generator

                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";
            }

            fileContents += "}\n";
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

    private void editOccurrencesInFile(char[] haystack, String needle, String packageName, File file) throws IOException {

        //TODO: make sure that the needles found are actual types, not variable names or etc

        String editedString = "";
        for (int i = 0; haystack[i] != '\0'; i++){
            if (haystack[i] == needle.charAt(0)){
                boolean flag = false;
                int j;
                for (j = 0; j < needle.length(); j++) {
                    if (haystack[i + j] != needle.charAt(j)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag){                         //occurrence found
                    i += j-1;
                    editedString += packageName + "." + needle;
                    continue;
                }
            }
            editedString += haystack[i];
        }
        writeToFile(file, editedString);
    }

    private void moveFile(String name, String path){
        File codeDirectory = new File("out/code/");
        File targetFile = null;
        for (File file : codeDirectory.listFiles()){
            if (file.getAbsolutePath().contains("\\" + name + ".java")) {
                targetFile = file;
                break;
            }
        }
        targetFile.renameTo(new File(path + "\\" + name + ".java"));

        //TODO: move [name]_interface.java as well?
    }
}
