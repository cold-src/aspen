package net.orbyfied.aspen;

import net.orbyfied.aspen.context.ComposeContext;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
            public boolean matches(ComposeContext context) {
                return true;
            }

            @Override
            public void compose(ComposeContext context) throws Throwable {
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.get(i).compose(context);
                }
            }
        };
    }

    static SchemaComposer allExtend(Class<?> superType, Consumer<ComposeContext> consumer) {
        return new SchemaComposer() {
            @Override
            public int exactness() {
                return 20;
            }

            @Override
            public boolean matches(ComposeContext context) {
                return superType.isAssignableFrom(context.schema().klass);
            }

            @Override
            public void compose(ComposeContext context) throws Throwable {
                consumer.accept(context);
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
     * @param context The context.
     * @return True/false.
     */
    boolean matches(ComposeContext context);

    /**
     * Composes the schema.
     *
     * @param context The context.
     */
    void compose(ComposeContext context) throws Throwable;

}
