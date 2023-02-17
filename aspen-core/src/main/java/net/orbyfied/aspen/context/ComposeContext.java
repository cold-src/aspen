package net.orbyfied.aspen.context;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.Context;
import net.orbyfied.aspen.Schema;
import net.orbyfied.aspen.util.Throwables;
import net.orbyfied.aspen.util.ThrowingConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComposeContext extends Context {

    // the tasks to run after composing
    final List<ThrowingConsumer<ComposeContext>> postTasks = new ArrayList<>();

    public ComposeContext(ConfigurationProvider provider, Operation operation, Schema schema) {
        super(provider, operation, schema);
    }

    public ComposeContext schedulePost(ThrowingConsumer<ComposeContext> consumer) {
        postTasks.add(consumer);
        return this;
    }

    // run post-compose tasks
    // and then clear them
    public void runPost() {
        try {
            for (ThrowingConsumer<ComposeContext> consumer : postTasks)
                if (consumer != null)
                    consumer.accept(this);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }

        postTasks.clear();
    }

}
