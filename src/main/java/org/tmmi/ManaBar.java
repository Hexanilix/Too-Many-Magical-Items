package org.tmmi;

public class ManaBar {
    public float limit;
    public float amount;
    public ManaBar(float limit, float amount) {
        this.limit = limit;
        this.amount = amount;
    }

    public void add(float amount) {
        this.amount += Math.min(this.limit - this.amount, amount);
    }
    public void sub(float amount) {
        this.amount -= Math.min(amount, this.amount);
    }
}
