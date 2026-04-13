package com.example;

public class BallpointPen extends Pen {
    private final double ballDiameter;
    private final String ballMaterial;

    public BallpointPen(String brand, String model, Refill refill, double ballDiameter, String ballMaterial) {
        super(brand, model, refill);
        this.ballDiameter = ballDiameter;
        this.ballMaterial = ballMaterial;
    }

    public double getBallDiameter() {
        return ballDiameter;
    }

    public String getBallMaterial() {
        return ballMaterial;
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

        System.out.println("BallpointPen " + brand + " writing: " + text);
        refill.getInk().depleteInk(0.1);
    }

    @Override
    public String getType() {
        return "Ballpoint";
    }

    @Override
    public String toString() {
        return "BallpointPen{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", ballDiameter=" + ballDiameter +
                ", ballMaterial='" + ballMaterial + '\'' +
                ", refill=" + refill +
                '}';
    }
}
