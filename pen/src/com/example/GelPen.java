package com.example;

public class GelPen extends Pen {
    private final double gelDensity;
    private final boolean isPressurized;

    public GelPen(String brand, String model, Refill refill, double gelDensity, boolean isPressurized) {
        super(brand, model, refill);
        this.gelDensity = gelDensity;
        this.isPressurized = isPressurized;
    }

    public double getGelDensity() {
        return gelDensity;
    }

    public boolean isPressurized() {
        return isPressurized;
    }

    @Override
    public void write(String text) {
        if (!isOpen) {
            System.out.println("Open the pen first!");
            return;
        }

        if (!hasInk()) {
            System.out.println("No ink available!");
            return;
        }

        System.out.println("GelPen " + brand + " writing smoothly: " + text);
        if (isPressurized) {
            System.out.println("Writing with pressure-based flow");
        }
        refill.getInk().depleteInk(0.15);
    }

    @Override
    public String getType() {
        return "Gel";
    }

    @Override
    public String toString() {
        return "GelPen{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", gelDensity=" + gelDensity +
                ", isPressurized=" + isPressurized +
                ", refill=" + refill +
                '}';
    }
}
