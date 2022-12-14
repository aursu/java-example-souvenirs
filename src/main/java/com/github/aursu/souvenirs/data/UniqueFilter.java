package com.github.aursu.souvenirs.data;

import com.github.aursu.souvenirs.DataObject;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class UniqueFilter<T> implements JSONFilter {

    private List<T> values = new ArrayList<>();
    BiFunction<JsonObject, String, T> conv;

    public UniqueFilter(BiFunction<JsonObject, String, T> conv) {
        this.conv = conv;
    }

    public static JSONFilter stringFilter() {
        return new UniqueFilter<String>((obj, column) -> obj.getString(column));
    }

    public static JSONFilter yearFilter() {
        return new UniqueFilter<Integer>((obj, column) -> DataObject.loadDateTime(obj.getString(column)).getYear());
    }

    @Override
    public boolean apply(JsonObject obj, String column) {
        T value = conv.apply(obj, column);

        if (values.contains(value)) return false;

        values.add(value);

        return true;
    }
}
