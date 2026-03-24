package com.example;

public class Ink {
    private Color color;
    private double quantityInMl;
    private String type;
    private double viscosity;

    public Ink(Color color, double quantityInMl, String type, double viscosity) {
        this.color = color;
        this.quantityInMl = Math.round(quantityInMl * 100.0) / 100.0; // Round to 2 decimal places
        this.type = type;
        this.viscosity = viscosity;
    }

    // Helper method to round to 2 decimal places
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getQuantityInMl() {
        return quantityInMl;
    }

    public void depleteInk(double amount) {
        if (quantityInMl >= amount) {
            quantityInMl = roundToTwoDecimals(quantityInMl - amount);
        } else {
            System.out.println("Not enough ink!");
            quantityInMl = 0;
        }
    }

    public void refill(double amount) {
        quantityInMl = roundToTwoDecimals(quantityInMl + amount);
    }

    public boolean isEmpty() {
        return quantityInMl <= 0;
    }

    public String getType() {
        return type;
    }

    public double getViscosity() {
        return viscosity;
    }

    @Override
    public String toString() {
        return "Ink{" +
                "color=" + color +
                ", quantityInMl=" + quantityInMl +
                ", type='" + type + '\'' +
                ", viscosity=" + viscosity +
                '}';
    }
}
