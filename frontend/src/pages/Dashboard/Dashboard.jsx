import React, { useState } from 'react';
import FlightCard from '../../components/FlightCard/FlightCard';
import FlightModal from '../../components/FlightModal/FlightModal';
import WeatherBanner from '../../components/WeatherBanner/WeatherBanner';
import { useRadarSocket } from '../../hooks/useRadarSocket';
import { useWeatherSocket } from '../../hooks/useWeatherSocket';
import './Dashboard.css';

export default function Dashboard() {
    const WEBSOCKET_URL = 'http://localhost:8080/atc-websocket';

    const planes = useRadarSocket(WEBSOCKET_URL);
    const weather = useWeatherSocket(WEBSOCKET_URL);

    const [selectedPlane, setSelectedPlane] = useState(null);

    return (
        <div className="dashboard">
            <FlightModal
                plane={selectedPlane}
                onClose={() => setSelectedPlane(null)}
            />

            <header className="dashboard-header">
                <div className="header-left">
                    <h1>ATC Flight Control</h1>
                    <WeatherBanner weather={weather} />
                </div>
                <div className="active-count">
                    Active Flights: {Object.keys(planes).length}
                </div>
            </header>

            <div className="flight-grid">
                {Object.values(planes).map(plane => (
                    <FlightCard
                        key={plane.planeId}
                        plane={plane}
                        onClick={setSelectedPlane}
                    />
                ))}
            </div>
        </div>
    );
}