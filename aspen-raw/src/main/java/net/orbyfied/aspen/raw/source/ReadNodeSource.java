package net.orbyfied.aspen.raw.source;

public class ReadNodeSource implements NodeSource {

    // the location in a file
    FileLocation location;

    public ReadNodeSource location(FileLocation location) {
        this.location = location;
        return this;
    }

    public FileLocation location() {
        return location;
    }

    @Override
    public String toPrettyString() {
        if (location == null)
            return "unknown read source";
        return "file(" + location.fileName() + ") line(" + location.line() + ") column(" + location.column() + ")";
    }

}
