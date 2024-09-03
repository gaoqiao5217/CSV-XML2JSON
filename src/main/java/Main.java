import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String CSV_FILE_PATH = "data.csv";
    private static final String XML_FILE_PATH = "data.xml";
    private static final String JSON_FILE_PATH = "data.json";
    private static final String JSON2_FILE_PATH = "data2.json";

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> employees_csv = parseCSV(columnMapping, CSV_FILE_PATH);
        String json = listToJson(employees_csv);
        writeString(json, JSON_FILE_PATH);

        List<Employee> employees_xml = parseXML(XML_FILE_PATH);
        String json1 = listToJson(employees_xml);
        writeString(json1, JSON2_FILE_PATH);

        try {
            String json2 = readString(JSON2_FILE_PATH);
            List<Employee> list = jsonToList(json2);
            list.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> employees = new ArrayList<>();

        try {
            File file = new File(fileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("employee");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());

                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    employees.add(employee);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            return new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build()
                    .parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> String listToJson(List<T> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type listType = new TypeToken<List<T>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static void writeString(String jsonString, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readString(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> employees = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            JSONArray jsonArray = (JSONArray) parser.parse(json);
            for (Object obj : jsonArray) {
                Employee employee = gson.fromJson(obj.toString(), Employee.class);
                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }
}