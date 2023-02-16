package net.orbyfied.aspen;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public interface SchemaComposer {

    static SchemaComposer orderedPipeline(List<SchemaComposer> list) {
        list.sort(Comparator.comparingInt(SchemaComposer::exactness));
        // add base composer
        list.add(ConfigurationProvider.BASE_SCHEMA_COMPOSER);

        return new SchemaComposer() {
            @Override
            public int exactness() {
                return -1;
            }

            @Override
            public boolean matches(ConfigurationProvider provider, Schema schema) {
                return true;
            }

            @Override
            public void compose(ConfigurationProvider provider, Schema schema) throws Throwable {
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.get(i).compose(provider, schema);
                }
            }
        };
    }

    static SchemaComposer allExtend(Class<?> superType, BiConsumer<ConfigurationProvider, Schema> consumer) {
        return new SchemaComposer() {
            @Override
            public int exactness() {
                return 20;
            }

            @Override
            public boolean matches(ConfigurationProvider provider, Schema schema) {
                return superType.isAssignableFrom(schema.klass);
            }

            @Override
            public void compose(ConfigurationProvider provider, Schema schema) throws Throwable {
                consumer.accept(provider, schema);
            }
        };
    }

    ///////////////////////////////

    /**
     * Get the exactness on this schema composer.
     * This essentially means how narrow the match
     * function is.
     *
     * Lower is more exact.
     *
     * @return The exactness value.
     */
    int exactness();

    /**
     * If this composer is applicable to the
     * given schema in the specified context.
     *
     * @param provider The provider.
     * @param schema The schema.
     * @return True/false.
     */
    boolean matches(ConfigurationProvider provider, Schema schema);

    /**
     * Composes the schema.
     *
     * @param provider The configuration provider.
     * @param schema The schema.
     */
    void compose(ConfigurationProvider provider, Schema schema) throws Throwable;

}
