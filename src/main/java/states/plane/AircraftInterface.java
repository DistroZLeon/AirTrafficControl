package states.plane;
import states.event.EventState;

public interface AircraftInterface {
    int getId();
    double getFuel();
    double getRemainingTimeFlight();
    EventState getEmergency();
    FlySchedule getFlyingPlan();
    void grantClearance(LandingClearance clearance);

}
