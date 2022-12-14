package com.github.aursu.souvenirs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class DataSource {
    private final String name;

    private final File sourceRoot;
    private File source = null;

    public DataSource(String root, String name) {
        this.sourceRoot = new File(root);
        this.name = name;
    }

    public void setup() throws FileNotFoundException {
        // data source root must exists
        if( ! sourceRoot.isDirectory()) {
            throw new FileNotFoundException(String.format("root path for DataSource (%s) does not exist", sourceRoot.getPath()));
        }

        source = new File(sourceRoot, name);

        // create data source path if not exists
        if ( ! source.exists()) source.mkdir();

        // check if it is a directory and raise exception if not
        if ( ! source.isDirectory()) throw new RuntimeException("data source path is not a directory");
    }

    public File getSource() {
        return source;
    }

    public List<String> showTables() {
        if (source == null)
            throw new NullPointerException("Data source is not set. Use setup() method");
        return Arrays.stream(source.listFiles())
                .filter(File::isDirectory)
                .map(File::getName)
                .toList();
    }

    public DataTable getTable(String tableName) {
        return new DataTable(tableName, this);
    }

    public void createTable(String tableName) {
        getTable(tableName).create();
    }
}
