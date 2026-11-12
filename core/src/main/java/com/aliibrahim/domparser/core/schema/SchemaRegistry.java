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

    /** Creates an empty registry; see {@link #discover()} for one filled from the class path. */
    public SchemaRegistry() {
    }

    /**
     * Creates a registry containing every schema published through {@link ServiceLoader}.
     *
     * @return the populated registry
     */
    public static SchemaRegistry discover() {
        SchemaRegistry registry = new SchemaRegistry();
        ServiceLoader.load(RecordSchema.class).forEach(registry::register);
        return registry;
    }

    /**
     * Adds a schema unless one with the same {@link RecordSchema#id()} is already registered.
     *
     * @param schema the schema to add
     * @return this registry, for chaining
     */
    public SchemaRegistry register(RecordSchema schema) {
        boolean duplicate = schemas.stream().anyMatch(s -> s.id().equals(schema.id()));
        if (!duplicate) {
            schemas.add(schema);
        }
        return this;
    }

    /**
     * Every registered schema.
     *
     * @return an unmodifiable list in registration order
     */
    public List<RecordSchema> all() {
        return Collections.unmodifiableList(schemas);
    }

    /**
     * Finds the first schema that {@linkplain RecordSchema#supports(Document) supports} a document.
     *
     * @param document the loaded document
     * @return the matching schema, or empty for generic XML
     */
    public Optional<RecordSchema> detect(Document document) {
        return schemas.stream().filter(s -> s.supports(document)).findFirst();
    }

    /**
     * Finds a schema by its id.
     *
     * @param id the schema id, e.g. {@code "users"}
     * @return the schema, or empty if none is registered with that id
     */
    public Optional<RecordSchema> byId(String id) {
        return schemas.stream().filter(s -> s.id().equals(id)).findFirst();
    }
}
