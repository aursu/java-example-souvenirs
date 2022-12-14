package com.github.aursu.souvenirs.data;

import javax.json.JsonObject;
import java.util.regex.Pattern;

public class PatternFilter implements JSONFilter {
    private final Pattern value;

    public PatternFilter(Pattern value) {
        this.value = value;
    }

    @Override
    public boolean apply(JsonObject obj, String column) {
        return value.matcher(obj.getString(column)).find();
    }
}
