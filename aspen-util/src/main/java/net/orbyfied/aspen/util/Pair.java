package net.orbyfied.aspen.util;

public class Pair<A, B> {

    public A first;
    public B second;

    public Pair() { }
    public Pair(A a, B b) {
        this.first = a;
        this.second = b;
    }

    public A getFirst() {
        return first;
    }

    public Pair<A, B> setFirst(A first) {
        this.first = first;
        return this;
    }

}
