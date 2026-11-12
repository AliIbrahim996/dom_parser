package com.aliibrahim.domparser.core.schema;

import java.util.List;

/** The original {@code <Users><User id=".."/></Users>} document format. */
public final class UserSchema implements RecordSchema {

    /** Created by {@link java.util.ServiceLoader}. */
    public UserSchema() {
    }

    private static final List<FieldDefinition> FIELDS = List.of(
            FieldDefinition.text("firstName", "First name"),
            FieldDefinition.text("lastName", "Last name"),
            FieldDefinition.integer("age", "Age"),
            FieldDefinition.choice("gender", "Gender", "Male", "Female"));

    /** Returns {@code "users"}. */
    @Override
    public String id() {
        return "users";
    }

    /** Returns {@code "Users"}. */
    @Override
    public String displayName() {
        return "Users";
    }

    /** Returns {@code "Users"}. */
    @Override
    public String rootElement() {
        return "Users";
    }

    /** Returns {@code "User"}. */
    @Override
    public String recordElement() {
        return "User";
    }

    /** First name, last name, age (whole number) and gender (Male or Female); all required. */
    @Override
    public List<FieldDefinition> fields() {
        return FIELDS;
    }
}
