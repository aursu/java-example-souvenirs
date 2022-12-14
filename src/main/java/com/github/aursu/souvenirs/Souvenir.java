package com.github.aursu.souvenirs;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.LocalDate;

public class Souvenir extends DataObject {
    private String name;

    private Manufacturer manufacturer;
    private LocalDate release;
    private double price;

    private int id = 0;
    private int manufacturerId = 0;

    public Souvenir(String name, Manufacturer manufacturer) {
        this.name = name;

        this.setManufacturer(manufacturer);
    }

    public Manufacturer getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
        this.manufacturerId = manufacturer.getId();
    }

    public int getManufacturerId()  {
        return this.manufacturerId;
    }

    private Souvenir() {
        this.name = null;
        this.manufacturerId = 0;
        this.manufacturer = null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public JsonObject toJson() {
        return toJson(false);
    }

    public JsonObject toJson(boolean hideIds) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();

        if (id > 0 && ! hideIds)
            builder.add("id",  id);

        builder.add("name", name);

        if (! hideIds)
            builder.add("manufacturerId", manufacturerId);

        return builder.add("release", jsonDateTime(release))
                .add("price", price)
                .build();
    }

    public JsonObject toJsonExtended() {
        return toJsonExtended(false);
    }

    public JsonObject toJsonExtended(boolean hideIds) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();

        if (manufacturer == null)
            return toJson(hideIds);

        if (id > 0 && ! hideIds)
            builder.add("id",  id);

        return builder.add("name", name)
                .add("manufacturer", factory.createObjectBuilder(manufacturer.toJson(hideIds)))
                .add("release", jsonDateTime(release))
                .add("price", price)
                .build();
    }

    @Override
    public void loadJson(JsonObject obj) {
        // store id inside object only if it exists inside JSON object
        if (obj.get("id") != null)
            setId(obj.getInt("id"));

        // set manufacturer Id
        this.manufacturerId = obj.getInt("manufacturerId");

        setName(obj.getString("name"));
        setRelease(loadDateTime(obj.getString("release")));
        setPrice(obj.getJsonNumber("price").doubleValue());
    }

    public static Souvenir of(JsonObject obj) {
        Souvenir sov = new Souvenir();
        sov.loadJson(obj);

        return sov;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getRelease() {
        return release;
    }

    public void setRelease(LocalDate release) {
        this.release = release;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean equals(Souvenir cmp) {
        return (name.equals(cmp.name) && release.equals(cmp.release) && price == cmp.price);
    }
}
