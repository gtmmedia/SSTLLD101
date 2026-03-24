package com.example;

public class Refill {
    private Ink ink;
    private Nib nib;

    public Refill(Ink ink, Nib nib) {
        this.ink = ink;
        this.nib = nib;
    }

    public Ink getInk() {
        return ink;
    }

    public Nib getNib() {
        return nib;
    }

    public void replaceInk(Ink newInk) {
        this.ink = newInk;
    }

    public void replaceNib(Nib newNib) {
        this.nib = newNib;
    }

    @Override
    public String toString() {
        return "Refill{" +
                "ink=" + ink +
                ", nib=" + nib +
                '}';
    }
}
