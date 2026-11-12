package com.aliibrahim.domparser.core.schema;

import java.util.List;

/** The original {@code <Users><User id=".."/></Users>} document format. */
public final class UserSchema implements RecordSchema {

    private static final List<FieldDefinition> FIELDS = List.of(
            FieldDefinition.text("firstName", "First name"),
            FieldDefinition.text("lastName", "Last name"),
            FieldDefinition.integer("age", "Age"),
            FieldDefinition.choice("gender", "Gender", "Male", "Female"));

    @Override
    public String id() {
        return "users";
    }

    @Override
    public String displayName() {
        return "Users";
    }

    @Override
    public String rootElement() {
        return "Users";
    }

    @Override
    public String recordElement() {
        return "User";
    }

    @Override
    public List<FieldDefinition> fields() {
        return FIELDS;
    }
}
