package com.github.aursu.souvenirs.data;

import javax.json.JsonObject;

public interface JSONFilter {
    boolean apply(JsonObject obj, String column);
}
