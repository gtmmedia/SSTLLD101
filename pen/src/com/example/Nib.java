package com.example;

public class Nib {
    private double sizeInMm;
    private String material;
    private NibType nibType;

    public enum NibType {
        BALLPOINT, GEL, FOUNTAIN
    }

    public Nib(double sizeInMm, String material, NibType nibType) {
        this.sizeInMm = sizeInMm;
        this.material = material;
        this.nibType = nibType;
    }

    public double getSizeInMm() {
        return sizeInMm;
    }

    public String getMaterial() {
        return material;
    }

    public NibType getNibType() {
        return nibType;
    }

    public void write() {
        System.out.println("Writing with " + sizeInMm + "mm " + nibType + " nib");
    }

    @Override
    public String toString() {
        return "Nib{" +
                "sizeInMm=" + sizeInMm +
                ", material='" + material + '\'' +
                ", nibType=" + nibType +
                '}';
    }
}
