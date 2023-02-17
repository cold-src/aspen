package net.orbyfied.aspen.context;

import net.orbyfied.aspen.OptionProfile;
import org.jetbrains.annotations.Nullable;

/**
 * An operation which is executed
 * on a specific {@link OptionProfile}.
 */
public abstract class ProfileOperation extends Operation {

    // the profile this is executed on
    // can be null for an anonymous operation
    final OptionProfile profile;

    public ProfileOperation(OptionProfile profile) {
        this.profile = profile;
    }
    
    @Nullable
    public OptionProfile profile() {
        return profile;
    }

}
