package com.aliibrahim.domparser.core.schema;

import org.jdom2.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/** Holds the known {@link RecordSchema}s and picks the right one for a loaded document. */
public final class SchemaRegistry {

    private final List<RecordSchema> schemas = new ArrayList<>();

    /** A registry containing every schema published through {@link ServiceLoader}. */
    public static SchemaRegistry discover() {
        SchemaRegistry registry = new SchemaRegistry();
        ServiceLoader.load(RecordSchema.class).forEach(registry::register);
        return registry;
    }

    public SchemaRegistry register(RecordSchema schema) {
        boolean duplicate = schemas.stream().anyMatch(s -> s.id().equals(schema.id()));
        if (!duplicate) {
            schemas.add(schema);
        }
        return this;
    }

    public List<RecordSchema> all() {
        return Collections.unmodifiableList(schemas);
    }

    public Optional<RecordSchema> detect(Document document) {
        return schemas.stream().filter(s -> s.supports(document)).findFirst();
    }

    public Optional<RecordSchema> byId(String id) {
        return schemas.stream().filter(s -> s.id().equals(id)).findFirst();
    }
}
