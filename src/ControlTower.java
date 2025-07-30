import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlTower {
    private static ControlTower instance;
    private final List<Way> runaways, taxiwaysTakeoff, taxiwaysLanding;
    private final String airportName;
    private PriorityQueue<Plane> landingQueue=  new PriorityQueue<>(Comparator.comparing(Plane::isEmergency).reversed().thenComparingDouble(Plane::getFuel));
    private ControlTower(String airportName, int nrRunaways, int nrTaxiways){
        this.runaways= IntStream.range(1, nrRunaways).mapToObj(Way::new).collect(Collectors.toList());
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

    public synchronized int acquire(int type, int id){
        int idWay= -1;
        List<Way> ways= switch (type){
            case 0-> this.runaways;
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

    public synchronized void release(int type, int id){
        List<Way> ways= switch (type){
            case 0-> this.runaways;
            case 1-> this.taxiwaysTakeoff;
            case 2-> this.taxiwaysLanding;
            default-> null;
        };
        if (ways == null)
            return;
        Way way= ways.get(id);
        way.release();
    }

    public synchronized LandingClearance landingRequest(Plane plane) throws InterruptedException {
        int gateId, runwayId, taxiwayId;
        GateManager gateManager= GateManager.getInstance();
        this.landingQueue.remove(plane);
        this.landingQueue.add(plane);
        if(!landingQueue.isEmpty()&& landingQueue.peek()!=plane)
            return null;
        gateId = gateManager.acquire(plane.getId());
        runwayId = acquire(0, plane.getId());
        taxiwayId = acquire(1, plane.getId());
        if(gateId == -1 || runwayId == -1 || taxiwayId == -1)
            return null;
        return new LandingClearance(runwayId, taxiwayId, gateId);
    }

    public synchronized LandingClearance takeoffClearance(Plane plane) throws InterruptedException {
        return null;
    }

    public String getAirportName(){
        return this.airportName;
    }
}
