package com.github.aursu.souvenirs.data;

import com.github.aursu.souvenirs.DataObject;

import javax.json.JsonObject;
import java.util.function.BiFunction;

public class StaticFilter<T> implements JSONFilter {

    private final T value;

    BiFunction<JsonObject, String, T> conv;

    public StaticFilter(T value, BiFunction<JsonObject, String, T> conv) {
        this.value = value;
        this.conv = conv;
    }

    public static JSONFilter of(String value) {
        return new StaticFilter<String>(value, (obj, column) -> obj.getString(column));
    }

    public static JSONFilter of(int value) {
        return new StaticFilter<Integer>(value, (obj, column) -> obj.getInt(column));
    }

    public static JSONFilter of(double value) {
        return new StaticFilter<Double>(value, (obj, column) -> obj.getJsonNumber(column).doubleValue());
    }

    public static JSONFilter ofYear(int value) {
        return new StaticFilter<Integer>(value, (obj, column) -> DataObject.loadDateTime(obj.getString(column)).getYear());
    }

    @Override
    public boolean apply(JsonObject obj, String column) {
        return conv.apply(obj, column).equals(value);
    }
}
