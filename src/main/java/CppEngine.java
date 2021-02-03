import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

        //TODO: some types such as boolean and byte are different in cpp, handle this

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
            File classFile = new File("out/code/" + class_iter.get("name").textValue() + ".cpp");
            classFile.createNewFile();
            String fileContents = "class " + class_iter.get("name").textValue() + " ";

            //TODO: handle inheritance and implementations here

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
                fileContents += attribute.get("type").textValue() + " ";
                fileContents += attribute.get("name").textValue() + ";\n\n";
            }

            //TODO: handle compositions here

            ArrayNode methods = (ArrayNode) class_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\t";
                fileContents += "\t";
                if (method.get("access").textValue().equals("default"))
                    fileContents += "private: ";
                else
                    fileContents += method.get("access").textValue() + ": ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                if (method.get("extra").textValue().contains("virtual")) fileContents += "virtual ";

                //TODO: apparently methods cannot be static and virtual at the same time in cpp, handle this

                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "();\n\n";
            }

            fileContents += "};\n";
            FileWriter myWriter = new FileWriter(classFile.getAbsolutePath());
            myWriter.write(fileContents);
            myWriter.close();
        }
        ArrayNode interfaces = (ArrayNode) rootNode.get("interfaces");
        for (JsonNode interf_iter : interfaces){
            File interfaceFile = new File("out/code/" + interf_iter.get("name").textValue() + ".cpp");
            interfaceFile.createNewFile();
            String fileContents = "class " + interf_iter.get("name").textValue() + " ";

            //TODO: handle inheritance here

            fileContents += "{\n\n";
            ArrayNode methods = (ArrayNode) interf_iter.get("info").get("methods");
            for (JsonNode method : methods){
                fileContents += "\tpublic: ";
                if (method.get("extra").textValue().contains("static")) fileContents += "static ";
                fileContents += method.get("return").textValue() + " ";
                fileContents += method.get("name").textValue() + "()=0;\n\n";

                //TODO: Interface1.cpp:3:21: error: initializer specified for static member function 'static int Interface1::farda()'
                //    3 |  public: static int farda()=0; handle this
                //TODO: handle parameters here
            }
            fileContents += "};\n";
            FileWriter myWriter = new FileWriter(interfaceFile.getAbsolutePath());
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
