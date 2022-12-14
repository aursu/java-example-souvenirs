package com.github.aursu.souvenirs;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

public class Manufacturer extends DataObject {
    private String name;
    private String country;

    public Manufacturer(String name, String country) {
        super();
        this.name = name;
        this.country = country;
    }

    private Manufacturer() {
        super();
        this.name = null;
        this.country = null;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Manufacturer of(JsonObject obj) {
        Manufacturer man = new Manufacturer();
        man.loadJson(obj);

        return man;
    }

    @Override
    public JsonObject toJson() {
        return toJson(false);
    }

    public JsonObject toJson(boolean hideIds) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();

        // set id only if we know id (otherwise it will be set by DataTable object)
        if (getId() > 0 && ! hideIds)
            builder.add("id",  getId());

        return builder.add("name", name)
                .add("country", country)
                .build();
    }

    @Override
    public void loadJson(JsonObject obj) {
        // store id inside object only if it exists inside JSON object
        if (obj.get("id") != null)
            setId(obj.getInt("id"));

        setName(obj.getString("name"));
        setCountry(obj.getString("country"));
    }
}
