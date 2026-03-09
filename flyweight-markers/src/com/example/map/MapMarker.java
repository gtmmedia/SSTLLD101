package com.example.map;

/**
 * CURRENT STATE (BROKEN ON PURPOSE):
 * Each marker owns a private MarkerStyle created via 'new'.
 * This means identical styles are duplicated across thousands of markers.
 *
 * TODO (student):
 * - Store intrinsic state as a shared MarkerStyle obtained from MarkerStyleFactory.
 * - Keep only extrinsic state here: lat, lng, label.
 */
public class MapMarker {

    private final double lat;
    private final double lng;
    private final String label;

    // BROKEN: style is created per marker; should be shared
    private final MarkerStyle style;

    public MapMarker(double lat, double lng, String label,
                     String shape, String color, int size, boolean filled) {
        this.lat = lat;
        this.lng = lng;
        this.label = label;

        // BROKEN: per-marker allocation
        this.style = new MarkerStyle(shape, color, size, filled);
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getLabel() { return label; }
    public MarkerStyle getStyle() { return style; }
}
