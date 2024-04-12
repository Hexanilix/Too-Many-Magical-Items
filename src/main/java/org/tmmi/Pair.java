package org.tmmi;

public class Pair<P1, P2> {
    private P1 key;
    private P2 value;
    Pair(P1 obj1, P2 obj2) {
        this.key = obj1;
        this.value = obj2;
    }

    public P1 key() {
        return key;
    }

    public P2 value() {
        return value;
    }

    public void setKey(P1 k) {
        this.key = k;
    }
    public void setValue(P2 v) {
        this.value = v;
    }
}
