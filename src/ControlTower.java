import java.util.ArrayList;
import java.util.List;

public class ControlTower {
    private static ControlTower instance;
    private final List<Way> runaways, taxiwaysTakeoff, taxiwaysLanding;
    private final String airportName;
    private ControlTower(String airportName, int nrRunaways, int nrTaxiways){
        this.runaways= new ArrayList<>(nrRunaways);
        this.taxiwaysTakeoff= new ArrayList<>(nrTaxiways);
        this.taxiwaysLanding= new ArrayList<>(nrTaxiways);
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
    public String getAirportName(){
        return this.airportName;
    }
}
