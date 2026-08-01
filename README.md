# Air Traffic Control Simulator

A concurrent Air Traffic Control simulation system. This project models complex airport operations, real-time flight physics, dynamically changing weather conditions, and runway management using a multithreaded Java backend and a reactive React frontend.

## Architecture & Design Patterns

This simulator was built prioritizing robust software engineering principles. The core logic relies on the following design patterns:

### 1. State Pattern (Flight Lifecycle)
Planes do not rely on conditional logic. Instead, the flight lifecycle is governed by the State Pattern. Each plane transitions seamlessly through distinct behavioral states (e.g., `GateState`, `TakeoffState`, `FlyingState`, `ArrivingState`). Inside the `FlyingState`, a physical progression loop calculates `timePassed` and `flightDuration` based on dynamic fuel burn rates, delivering real-time telemetry.

### 2. Observer Pattern (Event-Driven Architecture)
The backend uses a decoupled, event-driven architecture to broadcast updates without tightly coupling components. The `WeatherEngine` and `EventGenerator` act as Subjects that randomly generate environment shifts and mid-air emergencies. `ControlTower`, `WebSocketRadarService` and `WebSocketWeatherService` act as Observers, whereas `Plane` acts as both an Observer and a Subject. When a plane's state changes or a global event occurs, these services instantly intercept the update and broadcast it to the STOMP message broker or backend logic components.

### 3. Singleton Pattern (Resource Management)
Critical shared resources are strictly controlled to prevent thread collisions. The `ControlTower` manages airspace and runway access. The `GateManager` ensures planes safely lock and release airport gates. The `WeatherEngine` and `EventGenerator` ensure only one source of truth exists for global environment variables.

### 4. Factory Pattern (Object Instantiation)
Centralizes the instantiation of different aircraft types. A `PlaneFactory` dynamically generates specific plane instances (e.g., `PassengerPlane` or `CargoPlane`) based on configuration files and schedules, completely decoupling the creation logic from the main simulation loop.

### 5. Mediator Pattern (Airspace Coordination)
The `ControlTower` acts as the central mediator for the entire airport. Instead of planes communicating directly with one another to negotiate runway access or avoid collisions, they route all requests through the tower. This keeps object dependencies clean and strictly centralized.

### 6. Template Method Pattern (Standardized Processes)
Defines the skeleton of core algorithms and behaviors (such as state execution pipelines or event handling). Abstract classes define the overarching structure, allowing subclasses to implement or override specific steps without altering the fundamental execution flow, ensuring consistency across all simulated entities.

### 7. Custom Lexical Scanner & Recursive Descent Parser
Instead of relying entirely on external libraries for configuration bootstrapping, the system implements a custom JSON lexical scanner (`JsonTokenizer`) and parser (`DeserializerV2`). It reads the configuration file character-by-character, builds explicit tokens (`JsonToken`), and maps them directly into immutable Java Records.

## Core Logic & Features

### Backend (Java / Spring Boot)
*   **Multithreading:** Every plane operates on its own dedicated Java thread. They calculate their own fuel consumption, request runway access asynchronously, and sleep when holding at gates.
*   **Weather Engine:** Weather is not just visual; it alters physics. Blizzards and Storms dynamically increase the consumption rate of airborne planes and close down specific airport lanes.
*   **Event Generator (Emergencies):** A dedicated background thread randomly injects critical events (e.g., `FUEL_LEAK`) into active flights based on configured intervals, testing the system's dynamic response to mid-air crises.
*   **Configuration Registry:** The `Registry` holds immutable configuration states (`GeneratorSettings`, `AirportConfig`) parsed on startup, ensuring thread-safe access to simulation parameters globally.
*   **Real-Time Data Streaming:** Uses Spring Boot WebSocket with STOMP over SockJS. The backend continuously pipes JSON data to the frontend at high frequencies.

### Frontend (React / Vite)
*   **Modular Dashboard:** Built with modern React. The UI is decoupled into distinct, reusable components.
*   **Custom WebSocket Hooks:** The STOMP connection is abstracted into custom hooks, strictly enforcing the Single Responsibility Principle.
*   **Travel Animation:** It calculates the plane's exact position on the track by mapping the backend's duration data into a dynamically scaling CSS layout.
*   **Ghost-Busting Memory:** When a Java thread completes its flight schedule and broadcasts a finished signal, the React state dynamically purges the object from memory, keeping the dashboard clean.

## Tech Stack

*   **Java 23**
*   **Spring Boot** (Web, WebSocket, Messaging)
*   **JSON**
*   **React.js 18 with Vite**
*   **STOMP.js & SockJS-Client**

## How to Run

### 1. Start the Spring Boot Backend
Ensure you have Java installed. Run the application via your IDE or terminal from the `backend` directory:
```bash
cd backend
./mvnw spring-boot:run
# The WebSocket broker will start on http://localhost:8080/atc-websocket.
```

### 2. Start the React Frontend
Navigate to the frontend directory, install dependencies, and start the Vite development server:

```Bash
cd frontend
npm install
npm run dev
# Access the dashboard at http://localhost:5173
```