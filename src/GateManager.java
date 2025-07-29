import java.util.ArrayList;
import java.util.List;

public class GateManager {
    private static GateManager instance= null;
    private final List<Way> gateways;
    private final String airportName;
    private GateManager(String airportName, int number){
        this.gateways = new ArrayList<>(number);
        this.airportName= airportName;
    }
    public static GateManager getInstance(String airportName, int number){
        if  (instance == null)
            instance = new GateManager(airportName, number);
        return instance;
    }
    public static GateManager getInstance(){
        return instance;
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
