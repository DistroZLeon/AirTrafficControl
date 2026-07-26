package states.plane.stateMachine;
import simulator.plane.Plane;

public interface PlaneState {
    void execute(Plane plane) throws InterruptedException;
}
