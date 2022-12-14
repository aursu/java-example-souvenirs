package com.github.aursu.souvenirs;

import com.github.aursu.souvenirs.data.JSONFilter;
import com.github.aursu.souvenirs.data.StaticFilter;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataTable {
    private final String name;
    private final DataSource source;
    private final File table;

    // table structure
    // primary key is mandatory - it is file name
    private final String key = "id";

    // all available rows
    private Map<String, JsonObject> rows;

    public DataTable(String name, DataSource source) {
        this.name = name;
        this.source = source;
        this.table = new File(source.getSource(), name);
        sync();
    }

    // create virtual table based on partial dataset
    protected DataTable(DataTable table, Map<String, JsonObject> rows) {
        this.name = table.name;
        this.source = table.source;
        this.table = table.table;
        this.rows = rows;
    }

    /**
     * Factory to create new data table
     * Supposed to be overriden in child classes to return proper object
     *
     * @param rows Set of rows to use in new object
     * @return New Data Table based on provided set of rows
     */
    protected DataTable DataTableFactory(Map<String, JsonObject> rows) {
        return new DataTable(this, rows);
    }

    public int size() {
        return rows.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public String getName() {
        return this.name;
    }

    protected DataSource getSource() {
        return this.source;
    }

    private int getId(JsonObject object) {
        JsonValue keyValue = object.get(key);

        // if key field does not exist  return 0
        if (keyValue == null) return 0;

        return object.getInt(key);
    }

    private JsonObject setId(JsonObject object) {
        return setId(object, nextKey());
    }

    private JsonObject setId(JsonObject object, int id) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder job = factory.createObjectBuilder(object);

        return job.add(key, id).build();
    }

    private String getFileName(JsonObject object) {
        int id = getId(object);
        if (id == 0) return null;
        return getFileName(id);
    }

    private String getFileName(int id) {
        return String.join(".",String.valueOf(id), "json");
    }

    public void create() {
        if ( ! exists()) table.mkdir();
    }

    public boolean exists() {
        return table.exists() && table.isDirectory();
    }

    public Stream<JsonObject> stream() {
        return selectAll().entrySet().stream().map(e -> e.getValue());
    }

    public Stream<JsonObject> stream(String column, JSONFilter filter) {
        return stream().filter(e -> filter.apply(e, column));
    }

    public int nextKey() {
        try {
            return stream().map(e -> e.getInt(key))
                    .mapToInt(Integer::intValue)
                    .max().orElse(0) + 1;
        }
        catch (Exception e) {
            return 1;
        }
    }

    /**
     * Insert JSON object into Data store
     *
     * Object can contain ID or not.
     * If there is no ID, then new one will be generated
     * File name is automatically generated based on object's ID
     *
     * @param object JSON object to insert into DataStore
     */
    public void insertRow(JsonObject object) {
        // primary key must be set before write
        if (getId(object) == 0)
            object = setId(object);

        String rowFileName = getFileName(object);

        insertRow(rowFileName, object);
    }

    /**
     * Insert JSON object into Data store using provided file name
     *
     * @param rowFileName File name to use for object store
     * @param object JSON object to insert into DataStore
     */
    public void insertRow(String rowFileName, JsonObject object) {
        try (FileOutputStream fos = new FileOutputStream(new File(table, rowFileName));
             JsonWriter jw = Json.createWriter(fos)) {

            jw.writeObject(object);
            jw.close();

            sync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Insert JSON object into Data store with provided id
     *
     * @param id Use provided id for the object
     * @param object JSON object to insert into DataStore
     */
    public void insertRow(int id, JsonObject object) {
        String rowFileName = getFileName(id);

        if (getId(object) == 0)
            object = setId(object, id);

        insertRow(rowFileName, object);
    }

    public Map<String, JsonObject> findRow(String column, JSONFilter filter) {
        return selectAll().entrySet().stream()
                .filter(e -> filter.apply(e.getValue(), column))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    public DataTable selection(String column, JSONFilter filter) {
        Map<String, JsonObject> rows = findRow(column, filter);
        return DataTableFactory(rows);
    }

    public void deleteRows(String column, JSONFilter filter) {
        for(Map.Entry<String, JsonObject> entry: findRow(column, filter).entrySet()) {
            new File(table, entry.getKey()).delete();
        }
        sync();
    }

    public JsonObject getRow(int id) {
        return  findRow(key, StaticFilter.of(id)).get(getFileName(id));
    }

    public void deleteRows(int value) {
        deleteRows(key, StaticFilter.of(value));
    }

    public void deleteRows() {
        for(Map.Entry<String, JsonObject> entry: selectAll().entrySet()) {
            new File(table, entry.getKey()).delete();
        }
        sync();
    }

    /**
     * @return List of all JSON files inside table directory
     */
    private List<String> selectAllFiles() {
        return Arrays.stream(table.listFiles())
                .filter(File::isFile)
                .map(File::getName).filter(name -> name.endsWith(".json")).toList();
    }

    /**
     * unset property `rows` to read them from file system again
     */
    public void sync() {
        rows = null;
    }

    /**
     * @return List of all JSON objects from file system
     */
    public Map<String, JsonObject> selectAll() {
        if (rows == null) rows = new HashMap<>();
        else return rows;

        for (String rowFileName: selectAllFiles())  {
            try(FileInputStream fis = new FileInputStream(new File(table, rowFileName));
                JsonReader jr = Json.createReader(fis)) {

                JsonObject row = jr.readObject();
                jr.close();

                rows.put(rowFileName, row);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return rows;
    }

    public void display() {
        display(false);
    }

    public void display(boolean hideIds) {
        Map<String, JsonObject> data = selectAll();

        if (data.isEmpty())
            System.out.printf("Table %s is empty!\n", getName());
        else
            for (String key : data.keySet()) {
                JsonObject obj = data.get(key);

                System.out.println(obj);
            }
    }
}
