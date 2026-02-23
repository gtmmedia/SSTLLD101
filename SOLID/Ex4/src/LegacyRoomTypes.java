public class LegacyRoomTypes {
    public static final int SINGLE = 1;
    public static final int DOUBLE = 2;
    public static final int TRIPLE = 3;
    public static final int DELUXE = 4;

    public static String nameOf(int t) {
        return switch (t) {
            case SINGLE -> "SINGLE";
            case DOUBLE -> "DOUBLE";
            case TRIPLE -> "TRIPLE";
            default -> "DELUXE";
        };
    }
}
