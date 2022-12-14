package com.github.aursu.souvenirs;

import javax.json.JsonObject;
import java.util.Map;

public class ManufacturersTable extends DataTable {
    public ManufacturersTable(DataSource datasource) {
        super("manufacturers", datasource);
    }

    private ManufacturersTable(DataTable table, Map<String, JsonObject> rows) {
        super(table, rows);
    }

    protected DataTable DataTableFactory(Map<String, JsonObject> rows) {
        return new ManufacturersTable(this, rows);
    }

    public void display(boolean hideIds) {
        Map<String, JsonObject> data = selectAll();

        if (data.isEmpty())
            System.out.printf("Table %s is empty!\n", getName());
        else
            for (String key : data.keySet()) {
                Manufacturer man = Manufacturer.of(data.get(key));

                System.out.println(man.toJson(hideIds));
            }
    }
}
