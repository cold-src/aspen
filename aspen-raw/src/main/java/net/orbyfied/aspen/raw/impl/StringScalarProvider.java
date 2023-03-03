package net.orbyfied.aspen.raw.impl;

import net.orbyfied.aspen.raw.RawIOContext;
import net.orbyfied.aspen.raw.RawProvider;
import net.orbyfied.aspen.raw.format.StringScalarFormat;

/**
 * An {@link RawProvider} which saves scalars as text
 * and there for provides standardized APIs for said
 * functionality.
 */
public interface StringScalarProvider<IC extends RawIOContext> extends RawProvider<IC> {

    StringScalarFormat stringScalarFormat();

}
