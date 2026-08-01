package states.plane.stateMachine;
import simulator.plane.Plane;
import java.util.Map;

public interface PlaneState {
    void execute(Plane plane) throws InterruptedException;

    Map<String, Object> getStateDetails(Plane plane);
}
