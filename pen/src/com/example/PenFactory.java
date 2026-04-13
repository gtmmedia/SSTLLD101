package com.example;

public class PenFactory {
    public static final double DEFAULT_BALLPOINT_SIZE = 1.0;
    public static final double DEFAULT_GEL_SIZE = 0.7;
    public static final double DEFAULT_FOUNTAIN_SIZE = 1.5;

    public static Pen createBallpointPen(String brand, String model, Color inkColor) {
        Ink ink = new Ink(inkColor, 10.0, "Oil-based", 0.8);
        Nib nib = new Nib(DEFAULT_BALLPOINT_SIZE, "Steel", Nib.NibType.BALLPOINT);
        Refill refill = new Refill(ink, nib);
        return new BallpointPen(brand, model, refill, 0.7, "Tungsten Carbide");
    }

    public static Pen createGelPen(String brand, String model, Color inkColor) {
        Ink ink = new Ink(inkColor, 8.0, "Water-based gel", 0.5);
        Nib nib = new Nib(DEFAULT_GEL_SIZE, "Ceramic", Nib.NibType.GEL);
        Refill refill = new Refill(ink, nib);
        return new GelPen(brand, model, refill, 1.2, true);
    }

    public static Pen createFountainPen(String brand, String model, Color inkColor) {
        Ink ink = new Ink(inkColor, 15.0, "Water-based", 0.3);
        Nib nib = new Nib(DEFAULT_FOUNTAIN_SIZE, "Gold", Nib.NibType.FOUNTAIN);
        Refill refill = new Refill(ink, nib);
        return new FountainPen(brand, model, refill, DEFAULT_FOUNTAIN_SIZE, "Tapered");
    }

    public static Pen createCustomPen(String penType, String brand, String model, Color inkColor, double nibSize) {
        return switch (penType.toLowerCase()) {
            case "ballpoint" -> {
                Ink ballpointInk = new Ink(inkColor, 10.0, "Oil-based", 0.8);
                Nib ballpointNib = new Nib(nibSize, "Steel", Nib.NibType.BALLPOINT);
                Refill ballpointRefill = new Refill(ballpointInk, ballpointNib);
                yield new BallpointPen(brand, model, ballpointRefill, nibSize, "Tungsten Carbide");
            }
            case "gel" -> {
                Ink gelInk = new Ink(inkColor, 8.0, "Water-based gel", 0.5);
                Nib gelNib = new Nib(nibSize, "Ceramic", Nib.NibType.GEL);
                Refill gelRefill = new Refill(gelInk, gelNib);
                yield new GelPen(brand, model, gelRefill, 1.2, true);
            }
            case "fountain" -> {
                Ink fountainInk = new Ink(inkColor, 15.0, "Water-based", 0.3);
                Nib fountainNib = new Nib(nibSize, "Gold", Nib.NibType.FOUNTAIN);
                Refill fountainRefill = new Refill(fountainInk, fountainNib);
                yield new FountainPen(brand, model, fountainRefill, nibSize, "Tapered");
            }
            default -> throw new IllegalArgumentException("Unknown pen type: " + penType);
        };
    }
}
