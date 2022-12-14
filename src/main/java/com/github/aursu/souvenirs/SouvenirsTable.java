package com.github.aursu.souvenirs;

import javax.json.JsonObject;
import java.util.Map;

public class SouvenirsTable extends DataTable {
    private DataTable manufacturers = null;

    public SouvenirsTable(DataSource datasource) {
        super("souvenirs", datasource);
    }

    private SouvenirsTable(DataTable table, Map<String, JsonObject> rows) {
        super(table, rows);
    }

    public void setManufacturersTable(DataTable manufacturers) {
        this.manufacturers = manufacturers;
    }

    protected DataTable DataTableFactory(Map<String, JsonObject> rows) {
        SouvenirsTable sov = new SouvenirsTable(this, rows);
        sov.setManufacturersTable(manufacturers);

        return sov;
    }

    public void display(boolean hideIds) {
        Map<String, JsonObject> data = selectAll();

        if (data.isEmpty())
            System.out.printf("Table %s is empty!\n", getName());
        else
            for (String key : data.keySet()) {
                Souvenir sov = Souvenir.of(data.get(key));

                if (manufacturers != null) {
                    JsonObject manObj = manufacturers.getRow(sov.getManufacturerId());
                    if (manObj != null) {
                        Manufacturer man = Manufacturer.of(manObj);
                        sov.setManufacturer(man);
                    }
                }

                System.out.println(sov.toJsonExtended(hideIds));
            }
    }
}
