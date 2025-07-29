public class Clock {
    private final static long startTime;
    private final static double scale= 3.0;
    static {
        startTime = System.nanoTime();
    }
    public static long getStartTime() {
        return startTime;
    }
    public static double getCurrentTime(){
        return (System.nanoTime()-startTime)/ (scale * 1e9);
    }
    public static double getScale(){
        return scale;
    }
}
