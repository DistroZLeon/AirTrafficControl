import config.Config;
import config.Registry;
import config.parser.DeserializerV2;
import config.parser.JsonTokenizer;
import simulator.ControlTower;
import simulator.GateManager;
import simulator.plane.Plane;
import subjects.EventGenerator;
import subjects.WeatherEngine;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("-- Initialization --");

        Config config;
        try (BufferedReader reader = new BufferedReader(new FileReader("./src/config/config.json"))){
            JsonTokenizer tokenizer = new JsonTokenizer(reader);
            DeserializerV2 deserializer = new DeserializerV2(tokenizer);
            config= deserializer.parseConfig();
            Registry.activeConfiguration= config;

            System.out.println("Config loaded successfully! Number of planes: "+ config.planes().size());
        }catch(IOException e){
            System.err.println("- Failed to parse config file: " + e.getMessage());
            return;
        }

        ControlTower tower= ControlTower.getInstance();
        GateManager gateManager= GateManager.getInstance();
        WeatherEngine weatherEngine= WeatherEngine.getInstance();
        EventGenerator eventGenerator= EventGenerator.getInstance();

        weatherEngine.addObserver(tower);

        Thread eventThread= new Thread(eventGenerator, "EventThread");
        eventThread.setDaemon(true);
        eventThread.start();
        Thread weatherThread= new Thread(weatherEngine, "WeatherThread");
        weatherThread.setDaemon(true);
        weatherThread.start();

        List<Plane> planes= Registry.activeConfiguration.planes();
        List<Thread> activePlanes= new ArrayList<>();
        for(Plane plane: planes) {
            weatherEngine.addObserver(plane);
            eventGenerator.addObserver(plane);

            if(plane.getFlyingPlan().getStartingPoint().equalsIgnoreCase(tower.getAirportName())){
                int gateId = gateManager.acquire(plane.getId());
                if (gateId != -1) {
                    System.out.println("Plane " + plane.getId() + " at Gate " + gateId);
                } else {
                    System.out.println("- Airport full!");
                    continue;
                }
            }
            Thread t= new Thread(plane, "Plane-" + plane.getId());
            activePlanes.add(t);
            t.start();
        }

        System.out.println("-- Running --");

        for(Thread thread: activePlanes){
            thread.join();
        }

        System.out.println("-- All planes have finished their schedule. Closing the Simulator --");
    }
}
