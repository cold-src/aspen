package net.orbyfied.aspen;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * A class which describes processing of
 * annotations on specific types of option
 * fields.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface OptionComposer<P extends Property, B extends Property.Builder> {

    static OptionComposer orderedPipeline(List<OptionComposer> list) {
        list.sort(Comparator.comparingInt(OptionComposer::exactness));
        // always add base processor to the end
        // of the list (least exact)
        list.add(ConfigurationProvider.BASE_OPTION_COMPOSER);

        return new OptionComposer() {
            @Override
            public int exactness() {
                return -1;
            }

            @Override
            public boolean matches(ConfigurationProvider provider, Schema schema, Class type) {
                return true;
            }

            @Override
            public Property.Builder open(ConfigurationProvider provider, Schema schema, String name, Class type, AnnotatedElement field) throws Exception {
                return list.get(0).open(
                        provider, schema, name, type, field);
            }

            @Override
            public void configure(ConfigurationProvider provider, Schema schema, AnnotatedElement field, Property.Builder builder) throws Exception {
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.get(i)
                            .configure(
                                    provider, schema, field, builder);
                }
            }
        };
    }

    // process the given annotation
    // if it is present on the element
    static <A extends Annotation> void processIfPresent(AnnotatedElement element,
                                                        Class<A> aClass,
                                                        Consumer<A> consumer) {
        A an = element.getAnnotation(aClass);
        if (an == null)
            return;
        consumer.accept(an);
    }

    //////////////////////////////////////

    /**
     * Get the exactness of this composer.
     *
     * This means how many types this will
     * match compared to others.
     *
     * Lower is better.
     */
    int exactness();

    /**
     * If this composer is applicable to
     * the given type in the given context.
     *
     * @param provider The provider.
     * @param schema The schema.
     * @param type The type.
     * @return True/false.
     */
    boolean matches(ConfigurationProvider provider,
                    Schema schema,
                    Class<?> type);

    /**
     * Creates a new property builder for
     * the given type and name in the provided
     * context.
     *
     * This is only called on the most exact
     * composer out of a pipeline.
     *
     * @param provider The provider.
     * @param schema The schema.
     * @param name The name of the property.
     * @param type The type of the property.
     * @param field The option field.
     * @return The builder.
     * @throws Exception Any exceptions that might occur.
     */
    B open(ConfigurationProvider provider,
           Schema schema,
           String name,
           Class<?> type,
           AnnotatedElement field) throws Exception;

    /**
     * Configures the property.
     *
     * @param provider The provider.
     * @param schema The schema.
     * @param field The option field.
     * @param builder The property builder.
     * @throws Exception Any exceptions that might occur.
     */
    void configure(ConfigurationProvider provider,
                   Schema schema,
                   AnnotatedElement field,
                   B builder) throws Exception;

}
