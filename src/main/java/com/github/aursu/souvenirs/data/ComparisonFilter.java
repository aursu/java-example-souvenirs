package com.github.aursu.souvenirs.data;

import com.github.aursu.souvenirs.DataObject;

import javax.json.JsonObject;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ComparisonFilter<T extends Number> implements JSONFilter {
    private BiFunction<JsonObject, String, T> conv;
    private Predicate<T> predicate;

    public ComparisonFilter(BiFunction<JsonObject, String, T> conv, Predicate<T> predicate) {
        this.conv = conv;
        this.predicate = predicate;
    }

    @Override
    public boolean apply(JsonObject obj, String column) {
        return predicate.test(conv.apply(obj, column));
    }

    public static JSONFilter lessThen(double value) {
        return new ComparisonFilter<Double>(
                (obj, column) -> obj.getJsonNumber(column).doubleValue(),
                (e) -> e < value);
    }

    public static JSONFilter sameYear(int value) {
        return new ComparisonFilter<Integer>(
                (obj, column) -> DataObject.loadDateTime(obj.getString(column)).getYear(),
                (e) -> e == value);
    }
}
