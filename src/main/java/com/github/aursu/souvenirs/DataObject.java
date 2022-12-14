package com.github.aursu.souvenirs;

import javax.json.JsonObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public abstract class DataObject {
    // id could be stored inside object, but it is driven by DataSource
    private int id;

    public DataObject() {
        id = 0;
    }

    public int getId() {
        return id;
    }
    public  void setId(int id) {
        this.id = id;
    }

    public abstract JsonObject toJson();

    public JsonObject toJsonExtended() {
        return toJson();
    }

    public abstract void loadJson(JsonObject obj);

    public void display() {
        System.out.println(toJson());
    }

    public void displayExtended() {
        System.out.println(toJsonExtended());
    }

    public static String jsonDateTime(LocalDate dt) {
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate loadDateTime(String jsonDateTime)  {
        try {
            return LocalDate.parse(jsonDateTime, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        catch (DateTimeParseException e) {
            return null;
        }
    }
}
