package com.example;

public class FountainPen extends Pen {
    private final double tipWidth;
    private final String tipTaper;
    private boolean isInked;

    public FountainPen(String brand, String model, Refill refill, double tipWidth, String tipTaper) {
        super(brand, model, refill);
        this.tipWidth = tipWidth;
        this.tipTaper = tipTaper;
        this.isInked = true;
    }

    public double getTipWidth() {
        return tipWidth;
    }

    public String getTipTaper() {
        return tipTaper;
    }

    public boolean isInked() {
        return isInked;
    }

    public void dip(Ink ink) {
        System.out.println("Dipping fountain pen in ink...");
        refill.replaceInk(ink);
        isInked = true;
    }

    @Override
    public void write(String text) {
        if (!isOpen) {
            System.out.println("Open the pen first!");
            return;
        }

        if (!hasInk()) {
            System.out.println("Fountain pen needs to be inked!");
            return;
        }

        System.out.println("FountainPen " + brand + " writing elegantly: " + text);
        System.out.println("Tip width: " + tipWidth + "mm, Taper: " + tipTaper);
        refill.getInk().depleteInk(0.05);
    }

    @Override
    public String getType() {
        return "Fountain";
    }

    @Override
    public String toString() {
        return "FountainPen{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", tipWidth=" + tipWidth +
                ", tipTaper='" + tipTaper + '\'' +
                ", isInked=" + isInked +
                ", refill=" + refill +
                '}';
    }
}
