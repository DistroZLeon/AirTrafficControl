import config.Config;
import config.Registry;
import config.parser.DeserializerV1;
import config.parser.JsonTokenizer;
import simulator.ControlTower;
import simulator.GateManager;
import simulator.Plane;
import states.plane.FlySchedule;
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
            DeserializerV1 deserializer = new DeserializerV1(tokenizer);
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

        new Thread(eventGenerator, "EventThread").start();
        new Thread(weatherEngine, "WeatherThread").start();

        List<Plane> planes= Registry.activeConfiguration.planes();

        for(Plane plane: planes) {
            weatherEngine.addObserver(plane);
            eventGenerator.addObserver(plane);

            if(plane.getFlyingPlan().getStartingPoint().equalsIgnoreCase(tower.getAirportName())){
                int gateId = gateManager.acquire(plane.getId());
                if (gateId != -1) {
                    System.out.println("Plane " + plane.getId() + " at Gate " + gateId);
                } else
                    System.out.println("- Airport full!");
            }
            new Thread(plane, "Plane-" + plane.getId()).start();
        }

        System.out.println("-- Running --");
    }
}
