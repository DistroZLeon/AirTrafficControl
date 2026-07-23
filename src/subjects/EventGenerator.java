package subjects;
import observers.Observer;
import states.event.EventState;
import states.event.EventType;
import states.plane.AircraftInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EventGenerator extends Subject implements Runnable {
    private final Random random = new Random();
    private EventGenerator(){}

    private static class Holder{
        private static final EventGenerator INSTANCE = new EventGenerator();
    }

    public static EventGenerator getInstance(){
        return Holder.INSTANCE;
    }

    public void triggerEvent(EventState state){
        notifyObservers(state);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(10000);
                if(random.nextDouble()>= 0.85){
                    List<AircraftInterface> activePlanes = new ArrayList<>();
                    for(Observer o : observers){
                        if(o instanceof AircraftInterface plane){
                            activePlanes.add(plane);
                        }
                    }

                    if(!activePlanes.isEmpty()){
                        AircraftInterface plane= activePlanes.get(random.nextInt(activePlanes.size()));

                        EventType[] types= EventType.values();
                        EventType type= types[random.nextInt(types.length)];
                        double drainRate= 0.0;
                        if(type== EventType.FUEL_LEAK){
                            drainRate= random.nextDouble();
                        }

                        EventState state= new EventState(plane.getId(), type, drainRate,
                                "Sudden issue - "+ type.name()+ " - detected in flight "+ plane.getId());

                        System.out.println("+ Emergency detected on flight "+ plane.getId());
                        triggerEvent(state);
                    }

                }
            }
        } catch (InterruptedException e) {
            System.out.println("EventGenerator interrupted");
            Thread.currentThread().interrupt();
        }
    }
}