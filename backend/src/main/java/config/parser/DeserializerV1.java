package config.parser;
import config.AirportConfig;
import config.Config;
import config.GeneratorSettings;
import simulator.plane.Plane;
import simulator.plane.PlaneFactory;
import states.plane.FlySchedule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeserializerV1 {
    protected final JsonTokenizer tokenizer;
    protected Token currentToken;

    public DeserializerV1(JsonTokenizer tokenizer) throws IOException {
        this.tokenizer = tokenizer;
        this.currentToken= tokenizer.getNextToken();
    }

    protected void consume(JsonToken expected) throws IOException {
        if(currentToken.type()!= expected){
            throw new IOException("Expected "+expected+" but got "+currentToken);
        }

        currentToken = tokenizer.getNextToken();
    }

    protected String consumeString() throws IOException {
        String value= currentToken.value();
        consume(JsonToken.STRING);
        return value;
    }

    protected int consumeInt() throws IOException {
        int value= Integer.parseInt(currentToken.value());
        consume(JsonToken.NUMBER);
        return value;
    }

    protected double consumeDouble() throws IOException {
        double value= Double.parseDouble(currentToken.value());
        consume(JsonToken.NUMBER);
        return value;
    }

    public Config parseConfig() throws IOException {
        consume(JsonToken.START_OBJECT);

        AirportConfig airportConfig= null;
        GeneratorSettings generatorSettings = null;
        List<Plane> planes= new ArrayList<>();

        while(this.currentToken.type() != JsonToken.END_OBJECT){
            String key= consumeString();
            consume(JsonToken.COLON);

            switch (key) {
                case "airportConfiguration"-> airportConfig= parseAirportConfig();
                case "generatorsSettings"-> generatorSettings= parseGeneratorsSettings();
                case "planes"-> planes= parsePlanes();
                default -> throw new RuntimeException("Unknown key in JSON: " + key);
            }

            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);

        }

        consume(JsonToken.END_OBJECT);
        return new Config(airportConfig, generatorSettings, planes);
    }

    protected AirportConfig parseAirportConfig() throws IOException {
        consume(JsonToken.START_OBJECT);
        String name= "";
        int gates= 0, runways= 0, taxiwaysTakeOff= 0, taxiwaysLanding=0;

        while(this.currentToken.type() != JsonToken.END_OBJECT){
            String key= consumeString();
            consume(JsonToken.COLON);
            switch (key) {
                case "airportName"-> name= consumeString();
                case "numberOfGates"-> gates= consumeInt();
                case "numberOfRunways"-> runways= consumeInt();
                case "numberOfTakeOffTaxiways"-> taxiwaysTakeOff= consumeInt();
                case "numberOfLandingTaxiways"-> taxiwaysLanding= consumeInt();
                default -> throw new RuntimeException("Unknown key: " + key);
            }
            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);

        }

        consume(JsonToken.END_OBJECT);
        return new AirportConfig(name, gates, runways, taxiwaysTakeOff, taxiwaysLanding);
    }

    protected GeneratorSettings parseGeneratorsSettings() throws IOException {
        consume(JsonToken.START_OBJECT);
        int weatherIntervals= 0, emergencyIntervals=0;

        while(this.currentToken.type() != JsonToken.END_OBJECT){
            String key= consumeString();
            consume(JsonToken.COLON);

            switch (key) {
                case "weatherUpdateIntervalsSeconds"-> weatherIntervals= consumeInt();
                case "emergencyGenerationIntervalsSeconds"-> emergencyIntervals= consumeInt();
                default -> throw new RuntimeException("Unknown key: " + key);
            }

            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);

        }

        consume(JsonToken.END_OBJECT);
        return new GeneratorSettings(weatherIntervals, emergencyIntervals);
    }

    protected List<Plane> parsePlanes() throws IOException {
        consume(JsonToken.START_ARRAY);
        List<Plane> planes= new ArrayList<>();

        while(this.currentToken.type() != JsonToken.END_ARRAY){
            planes.add(parsePlane());
            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);
        }
        consume(JsonToken.END_ARRAY);
        return planes;
    }

    protected Plane parsePlane() throws IOException {
        consume(JsonToken.START_OBJECT);
        List<FlySchedule> schedules= new ArrayList<>();
        double weight= 0.0, consumptionRate= 0.0;

        while(this.currentToken.type() != JsonToken.END_OBJECT){
            String key= consumeString();
            consume(JsonToken.COLON);
            switch (key) {
                case "schedules"-> schedules= parseSchedules();
                case "weight"-> weight= consumeDouble();
                case "consumptionRate"-> consumptionRate= consumeDouble();
                default -> throw new RuntimeException("Unknown key: " + key);
            }

            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);
        }

        consume(JsonToken.END_OBJECT);
        return PlaneFactory.createPlane("Passenger", schedules, weight, consumptionRate);
    }

    protected List<FlySchedule> parseSchedules() throws IOException {
        consume(JsonToken.START_ARRAY);
        List<FlySchedule> schedules= new ArrayList<>();

        while(this.currentToken.type() != JsonToken.END_ARRAY){
            consume(JsonToken.START_OBJECT);
            String destination= "", startingPoint= "";
            double timeOfArrival= 0.0, timeOfDeparture= 0.0, cargoWeight= 0.0;
            int numberOfPassengers= 0;

            while(this.currentToken.type() != JsonToken.END_OBJECT){
                String key= consumeString();
                consume(JsonToken.COLON);

                switch (key) {
                    case "destination"-> destination= consumeString();
                    case "startingPoint"-> startingPoint= consumeString();
                    case "timeOfArrival"-> timeOfArrival= consumeDouble();
                    case "timeOfDeparture"-> timeOfDeparture= consumeDouble();
                    case "numberOfPassengers"-> numberOfPassengers= consumeInt();
                    case "cargoWeight"-> cargoWeight= consumeDouble();
                    default -> throw new RuntimeException("Unknown key: " + key);
                }

                if(this.currentToken.type()== JsonToken.COMMA)
                    consume(JsonToken.COMMA);
            }

            consume(JsonToken.END_OBJECT);
            schedules.add(new FlySchedule(destination, startingPoint, timeOfArrival, timeOfDeparture, numberOfPassengers, cargoWeight));

            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);
        }

        consume(JsonToken.END_ARRAY);
        return schedules;
    }
}
