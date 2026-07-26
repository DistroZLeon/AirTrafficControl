package simulator;

import config.AirportConfig;
import config.Registry;
import observers.Observer;
import states.plane.AircraftInterface;
import states.plane.LandingClearance;
import states.weather.WeatherState;
import subjects.Subject;
import subjects.WeatherEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlTower implements Observer {
    private record WeatherLock(Way way, int type) {}
    private final List<WeatherLock> weatherClosedWays;
    private final Random random = new Random();
    private final List<Way> runways, taxiwaysTakeoff, taxiwaysLanding;
    private final String airportName;

    private static int extractGravity(AircraftInterface aircraft) {
        if(aircraft.getEmergency()==null){
            return -1;
        }

        return aircraft.getEmergency().type().getGravity().getValue();
    }

    private final PriorityBlockingQueue<AircraftInterface> takeoffQueue= new PriorityBlockingQueue<>(5, Comparator.comparingDouble((AircraftInterface p)-> p.getFlyingPlan().getTimeOfDeparture()).thenComparing(Comparator.comparingDouble((AircraftInterface p) -> p.getFlyingPlan().getTimeOfArrival()).reversed()));
    private final PriorityBlockingQueue<AircraftInterface> landingQueue=  new PriorityBlockingQueue<>(11,Comparator.comparingInt(ControlTower::extractGravity).reversed().thenComparing(AircraftInterface::getRemainingTimeFlight));

    private ControlTower(){
        AirportConfig airportConfig= Registry.activeConfiguration.airportConfig();
        this.airportName = airportConfig.airportName();
        int nrRunaways = airportConfig.numberOfRunways();
        int nrTakeOffTaxiways = airportConfig.numberOfTakeOffTaxiways();
        int nrLandingTaxiways = airportConfig.numberOfLandingTaxiways();

        this.runways= IntStream.range(0, nrRunaways).mapToObj(Way::new).collect(Collectors.toList());
        this.taxiwaysTakeoff= IntStream.range(0, nrTakeOffTaxiways).mapToObj(Way::new).collect(Collectors.toList());
        this.taxiwaysLanding= IntStream.range(0, nrLandingTaxiways).mapToObj(Way::new).collect(Collectors.toList());
        this.weatherClosedWays= new ArrayList<>();
    }

    private static class Holder{
        private static final ControlTower INSTANCE = new ControlTower();
    }

    public static ControlTower getInstance(){
        return Holder.INSTANCE;
    }

    private synchronized int acquire(int type, int id){
        int idWay= -1;
        List<Way> ways= switch (type){
            case 0-> this.runways;
            case 1-> this.taxiwaysTakeoff;
            default-> this.taxiwaysLanding;
        };
        if (ways == null)
            return -1;
        for (Way w: ways){
            idWay= w.acquire(id);
            if (idWay!= -1)
                break;
        }
        return idWay;
    }

    private synchronized void release(int type, int id){
        List<Way> ways= switch (type){
            case 0-> this.runways;
            case 1-> this.taxiwaysTakeoff;
            default-> this.taxiwaysLanding;
        };
        if (ways == null)
            return;
        Way way= ways.get(id);
        way.release();
    }

    public synchronized void updateLandingQueue(AircraftInterface plane){
        this.landingQueue.add(plane);
        dispatch();
    }

    public synchronized void updateTakeoffQueue(AircraftInterface plane){
        this.takeoffQueue.add(plane);
        dispatch();
    }

    public synchronized void changePriority(AircraftInterface plane){
        if(this.landingQueue.contains(plane)) {
            this.landingQueue.remove(plane);
            this.landingQueue.add(plane);
        }

        if(this.takeoffQueue.contains(plane)) {
            this.takeoffQueue.remove(plane);
            this.takeoffQueue.add(plane);
        }

        dispatch();
    }

    public synchronized void removeFromLandingQueue(AircraftInterface plane){
        this.landingQueue.remove(plane);
    }

    private synchronized void dispatch(){
        GateManager gateManager= GateManager.getInstance();
        boolean resourceAssigned= true;

        while (resourceAssigned){
            resourceAssigned= false;
            AircraftInterface arrivingPlane= this.landingQueue.peek();
            if(arrivingPlane != null){
                int gateId= gateManager.acquire(arrivingPlane.getId());
                int runwayId= acquire(0, arrivingPlane.getId());
                int taxiwayId= acquire(2, arrivingPlane.getId());

                if(runwayId != -1 && taxiwayId != -1&& gateId != -1){
                    landingQueue.poll();
                    arrivingPlane.grantClearance(new LandingClearance(runwayId, taxiwayId, gateId));
                    resourceAssigned= true;
                }
                else{
                    if(taxiwayId != -1) release(2, taxiwayId);
                    if(runwayId != -1) release(0, runwayId);
                    if(gateId != -1) gateManager.release(gateId);
                }
            }

            AircraftInterface leavingPlane= this.takeoffQueue.peek();
            if(leavingPlane != null){
                int gateId= gateManager.getGateId(leavingPlane.getId());
                if(gateId != -1){
                    int runwayId= acquire(0, leavingPlane.getId());
                    int taxiwayId= acquire(1, leavingPlane.getId());

                    if(runwayId != -1 && taxiwayId != -1){
                        takeoffQueue.poll();
                        leavingPlane.grantClearance(new LandingClearance(runwayId, taxiwayId, gateId));
                        resourceAssigned= true;
                    }
                    else{
                        if(taxiwayId != -1) release(1, taxiwayId);
                        if(runwayId != -1) release(0, runwayId);
                    }
                }
            }
        }

    }

    public synchronized void finishedLanding(int runwayId, int taxiwayId){
        release(0, runwayId);
        release(2, taxiwayId);
        dispatch();
    }

    public synchronized void finishedTakeoff(int runwayId, int taxiwayId, int gateId){
        GateManager gateManager= GateManager.getInstance();
        release(0, runwayId);
        release(1, taxiwayId);
        gateManager.release(gateId);
        dispatch();
    }

    public String getAirportName(){
        return this.airportName;
    }

    @Override
    public void update(Subject source, Object arg) {
        if(source instanceof WeatherEngine){
            if(arg instanceof WeatherState state){
                final int weatherId= -999;

                int attempts=0;
                while(this.weatherClosedWays.size()< state.lanesClose()&& attempts<40) {
                    attempts++;
                    int choice = this.random.nextInt(3);

                    int closedWayId = acquire(choice, weatherId);
                    if (closedWayId != -1) {
                        String message = switch (choice) {
                            case 0 -> "Runway-";
                            case 1 -> "Takeoff Taxiway-";
                            default -> "Landing Taxiway-";
                        };
                        this.weatherClosedWays.add(new WeatherLock(getWayById(choice, closedWayId), choice));
                        System.out.println(message + closedWayId + " closed due to weather!");
                    }
                }

                while(this.weatherClosedWays.size()> state.lanesClose()) {
                    int index = this.random.nextInt(this.weatherClosedWays.size());
                    WeatherLock weatherLock= this.weatherClosedWays.remove(index);

                    release(weatherLock.type(), weatherLock.way().getId());

                    String message = switch (weatherLock.type()) {
                        case 0 -> "Runway-";
                        case 1 -> "Takeoff Taxiway-";
                        default -> "Landing Taxiway-";
                    };
                    System.out.println(message + weatherLock.way().getId() + " opened due to weather!");
                }

                dispatch();
            }
        }
    }

    private Way getWayById(int type, int id){
        List<Way> ways= switch (type){
            case 0->this.runways;
            case 1->this.taxiwaysTakeoff;
            default->this.taxiwaysLanding;
        };

        for(Way way: ways)
            if(way.getId() == id)
                return way;

        return null;
    }
}
