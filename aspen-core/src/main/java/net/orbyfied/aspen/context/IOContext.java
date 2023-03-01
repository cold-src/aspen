package net.orbyfied.aspen.context;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.Context;
import net.orbyfied.aspen.Schema;
import net.orbyfied.aspen.raw.RawIOContext;
import net.orbyfied.aspen.raw.RawProvider;

public class IOContext extends Context implements RawIOContext {

    // the file name
    final String fileName;

    public IOContext(ConfigurationProvider provider, Schema schema,
                     String fileName) {
        /*
            TODO: add ReadOperation and WriteOperation and allow them
             to be passed in here to distinguish between r/w
         */

        super(provider, null, schema);
        this.fileName = fileName;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public RawProvider<?> rawProvider() {
        return provider().rawProvider();
    }

}
