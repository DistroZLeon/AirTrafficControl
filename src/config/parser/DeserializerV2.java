package config.parser;

import simulator.plane.Plane;
import simulator.plane.PlaneFactory;
import states.plane.FlySchedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeserializerV2 extends DeserializerV1{
    public DeserializerV2(JsonTokenizer tokenizer) throws IOException {
        super(tokenizer);
    }

    @Override
    protected Plane parsePlane() throws IOException {
        consume(JsonToken.START_OBJECT);
        String type= "";
        List<FlySchedule> schedules= new ArrayList<>();
        double weight= 0.0, consumptionRate= 0.0;

        while(this.currentToken.type() != JsonToken.END_OBJECT){
            String key= consumeString();
            consume(JsonToken.COLON);
            switch (key) {
                case "type"-> type= consumeString();
                case "schedules"-> {
                    if(type.isEmpty())
                        throw new RuntimeException("Error: 'type' must be declared before 'schedules' in a plane object.");
                    schedules= parseSchedules(type);
                }
                case "weight"-> weight= consumeDouble();
                case "consumptionRate"-> consumptionRate= consumeDouble();
                default -> throw new RuntimeException("Unknown key: " + key);
            }

            if(this.currentToken.type()== JsonToken.COMMA)
                consume(JsonToken.COMMA);
        }

        consume(JsonToken.END_OBJECT);
        return PlaneFactory.createPlane(type, schedules, weight, consumptionRate);
    }

    protected List<FlySchedule> parseSchedules(String planeType) throws IOException {
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
                    case "numberOfPassengers"-> {
                        if(planeType.equalsIgnoreCase("Cargo"))
                            throw new RuntimeException("Error: Cargo planes cannot have 'numberOfPassengers' in their schedule!");
                        numberOfPassengers= consumeInt();
                    }
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
