import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlTower {
    private static ControlTower instance;
    private final List<Way> runways, taxiwaysTakeoff, taxiwaysLanding;
    private final String airportName;
    private final PriorityBlockingQueue<Plane> takeoffQueue= new PriorityBlockingQueue<>(5, Comparator.comparingDouble((Plane p)-> p.getFlyingPlan().getTimeOfDeparture()).thenComparing(Comparator.comparingDouble((Plane p)->p.getFlyingPlan().getTimeOfArrival()).reversed()));
    private final PriorityBlockingQueue<PlaneState> landingQueue=  new PriorityBlockingQueue<>(11,Comparator.comparing(PlaneState::emergency).reversed().thenComparingDouble(PlaneState::fuel));
    private ControlTower(String airportName, int nrRunaways, int nrTaxiways){
        this.runways= IntStream.range(1, nrRunaways).mapToObj(Way::new).collect(Collectors.toList());
        this.taxiwaysTakeoff= IntStream.range(1, nrTaxiways).mapToObj(Way::new).collect(Collectors.toList());
        this.taxiwaysLanding= IntStream.range(1, nrTaxiways).mapToObj(Way::new).collect(Collectors.toList());
        this.airportName= airportName;
    }
    public static ControlTower getInstance(String airportName,int nrRunaways, int nrTaxiways){
        if (instance== null)
            instance= new ControlTower(airportName, nrRunaways, nrTaxiways);
        return instance;
    }

    public static ControlTower getInstance(){
        return instance;
    }

    private synchronized int acquire(int type, int id){
        int idWay= -1;
        List<Way> ways= switch (type){
            case 0-> this.runways;
            case 1-> this.taxiwaysTakeoff;
            case 2-> this.taxiwaysLanding;
            default-> null;
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
            case 2-> this.taxiwaysLanding;
            default-> null;
        };
        if (ways == null)
            return;
        Way way= ways.get(id);
        way.release();
    }

    public synchronized void updateLandingQueue(Plane plane){
        PlaneState state= new PlaneState(plane.getId(), plane.getFuel(), plane.isEmergency());
        this.landingQueue.remove(state);
        this.landingQueue.add(state);
    }

    public synchronized void updateTakeoffQueue(Plane plane){
        this.takeoffQueue.remove(plane);
        this.takeoffQueue.add(plane);
    }

    public synchronized LandingClearance landingRequest(Plane plane) throws InterruptedException {
        int gateId, runwayId, taxiwayId;
        GateManager gateManager= GateManager.getInstance();
        PlaneState state= new PlaneState(plane.getId(), plane.getFuel(), plane.isEmergency());
        PlaneState currentState= this.landingQueue.peek();
        if(currentState== null|| !currentState.equals(state))
            return null;
        gateId = gateManager.acquire(plane.getId());
        runwayId = acquire(0, plane.getId());
        taxiwayId = acquire(1, plane.getId());
        if(gateId == -1 || runwayId == -1 || taxiwayId == -1) {
            if(runwayId == -1) release(0, runwayId);
            if(taxiwayId == -1) release(1, taxiwayId);
            if(gateId == -1) gateManager.release(gateId);
            return null;
        }
        landingQueue.poll();
        return new LandingClearance(runwayId, taxiwayId, gateId);
    }

    public synchronized LandingClearance takeoffClearance(Plane plane) throws InterruptedException {
        int gateId, runwayId, taxiwayId;
        GateManager gateManager= GateManager.getInstance();
        gateId= gateManager.getGateId(plane.getId());
        if(gateId == -1)
            return null;
        Plane currentTop= takeoffQueue.peek();
        if(currentTop == null|| !currentTop.equals(plane))
            return null;
        runwayId = acquire(0, plane.getId());
        taxiwayId = acquire(2, plane.getId());
        if(runwayId == -1 || taxiwayId == -1) {
            if(runwayId == -1) release(0, runwayId);
            if(taxiwayId == -1) release(2, taxiwayId);
            return null;
        }
        takeoffQueue.poll();
        return new LandingClearance(runwayId, taxiwayId, gateId);
    }

    public synchronized void finishedLanding(int runwayId, int taxiwayId){
        release(0, runwayId);
        release(1, taxiwayId);
    }

    public synchronized void finishedTakeoff(int runwayId, int taxiwayId, int gateId){
        GateManager gateManager= GateManager.getInstance();
        release(0, runwayId);
        release(2, taxiwayId);
        gateManager.release(gateId);
    }

    public String getAirportName(){
        return this.airportName;
    }
}
