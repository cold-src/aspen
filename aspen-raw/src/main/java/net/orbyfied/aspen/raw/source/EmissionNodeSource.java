package net.orbyfied.aspen.raw.source;

import net.orbyfied.aspen.raw.Representable;

public class EmissionNodeSource implements NodeSource {

    // the representable source
    Representable<?> representable;

    public EmissionNodeSource(Representable<?> representable) {
        this.representable = representable;
    }

    public Representable<?> getRepresentable() {
        return representable;
    }

    @Override
    public String toPrettyString() {
        return "emitted(" + representable + ")";
    }

}
