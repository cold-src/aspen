package net.orbyfied.aspen;

import net.orbyfied.aspen.context.OptionComposeContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A class which describes processing of
 * annotations on specific types of option
 * fields.
 */
public interface OptionComposer {

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
            public boolean matches(OptionComposeContext context) {
                return true;
            }

            @Override
            public boolean open(OptionComposeContext context) throws Exception {
                final int l = list.size();
                for (int i = 0; i < l; i++)
                    if (list.get(i).open(context))
                        return true;
                return false;
            }

            @Override
            public void configure(OptionComposeContext context) throws Exception {
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.get(i).configure(context);
                }
            }
        };
    }

    static <A extends Annotation> OptionComposer configureAllAnnotated(Class<A> annotation,
                                                                       BiConsumer<OptionComposeContext, A> consumer) {
        return new OptionComposer() {
            @Override
            public int exactness() {
                return 10;
            }

            @Override
            public boolean matches(OptionComposeContext context) {
                return context.element().isAnnotationPresent(annotation);
            }

            @Override
            public boolean open(OptionComposeContext context) throws Exception {
                return false;
            }

            @Override
            public void configure(OptionComposeContext context) throws Exception {
                consumer.accept(context, context.element().getAnnotation(annotation));
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
     * @param context The context.
     * @return True/false.
     */
    boolean matches(OptionComposeContext context);

    /**
     * Creates a new property builder for
     * the given type and name in the provided
     * context.
     *
     * The builder should be set through
     * {@link OptionComposeContext#builder(Property.Builder)}
     *
     * This is only called on the most exact
     * composer out of a pipeline.
     *
     * @param context The context.
     * @throws Exception Any exceptions that might occur.
     * @return If the open succeeded, if false is returned in a pipeline,
     *         the pipeline will continue to the next composer to open it.
     */
    boolean open(OptionComposeContext context) throws Exception;

    /**
     * Configures the property.
     *
     * @param context The context.
     * @throws Exception Any exceptions that might occur.
     */
    void configure(OptionComposeContext context) throws Exception;

}
