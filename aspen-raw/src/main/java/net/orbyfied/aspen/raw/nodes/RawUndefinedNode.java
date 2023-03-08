package net.orbyfied.aspen.raw.nodes;

import net.orbyfied.aspen.raw.format.SpecialObjects;

@RawNodeTypeDesc(typeName = "undefined")
public class RawUndefinedNode extends RawValueNode<Object> {

    public static RawUndefinedNode undefined() {
        return new RawUndefinedNode();
    }

    //////////////////////////////////////////

    RawUndefinedNode() { super(); }

    @Override
    protected Object toValue0() {
        return SpecialObjects.UNDEFINED;
    }

    @Override
    public Object toValue() {
        return SpecialObjects.UNDEFINED;
    }

}
