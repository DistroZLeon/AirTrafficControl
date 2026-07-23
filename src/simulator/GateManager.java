package simulator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GateManager{
    private final List<Way> gateways;
    private final String airportName;

    private GateManager(){
        int number= 8;
        this.gateways = IntStream.range(0, number).mapToObj(Way::new).collect(Collectors.toList());
        this.airportName= "Otopeni";
    }

    private static class Holder{
        private static final GateManager INSTANCE = new GateManager();
    }

    public static GateManager getInstance(){
        return Holder.INSTANCE;
    }

    public int getGateId(int planeId){
        for(Way gateway: gateways){
            if(gateway.getOccupantId() == planeId)
                return gateway.getId();
        }
        return -1;
    }

    public int acquire( int id) {
        int idWay = -1;

        for (Way w : this.gateways) {
            idWay = w.acquire(id);
            if (idWay != -1)
                break;
        }
        return idWay;
    }

    public void release(int id){
        Way way= this.gateways.get(id);
        way.release();
    }
    public String getAirportName(){
        return this.airportName;
    }
}
