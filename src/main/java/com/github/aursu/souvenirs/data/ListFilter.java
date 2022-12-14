package com.github.aursu.souvenirs.data;

import javax.json.JsonObject;
import java.util.List;
import java.util.function.BiFunction;

public class ListFilter<T> implements JSONFilter {
    private final List<T> value;
    BiFunction<JsonObject, String, T> conv;

    public ListFilter(List<T> value, BiFunction<JsonObject, String, T> conv) {
        this.value = value;
        this.conv = conv;
    }

    @Override
    public boolean apply(JsonObject obj, String column) {
        return value.contains(conv.apply(obj, column));
    }

    public static JSONFilter integerFilter(List<Integer> filterValues) {
        return new ListFilter<Integer>(filterValues, (obj, column) -> obj.getInt(column));
    }
}
