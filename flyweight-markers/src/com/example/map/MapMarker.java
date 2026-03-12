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

    // FIXED: now stores shared intrinsic state
    private final MarkerStyle style;

    public MapMarker(double lat, double lng, String label, MarkerStyle style) {
        this.lat = lat;
        this.lng = lng;
        this.label = label;
        this.style = style;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getLabel() { return label; }
    public MarkerStyle getStyle() { return style; }
}
