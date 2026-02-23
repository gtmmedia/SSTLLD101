public class TripRequest {
    public final String studentId;
    public final GeoPoint from;
    public final GeoPoint to;

    public TripRequest(String studentId, GeoPoint from, GeoPoint to) {
        this.studentId = studentId; this.from = from; this.to = to;
    }
}
