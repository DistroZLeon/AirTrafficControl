import React from 'react';
import './WeatherBanner.css';

export default function WeatherBanner({ weather }) {
    const type = weather.type.toUpperCase();
    const isBadWeather = type.includes("BLIZZARD") || type.includes("STORM");

    let icon = "☀️";
    if (type.includes("RAIN")) icon = "🌧️";
    if (type.includes("STORM")) icon = "⛈️";
    if (type.includes("BLIZZARD")) icon = "❄️";

    return (
        <div className={`weather-banner ${isBadWeather ? 'weather-warning' : 'weather-clear'}`}>
            <div className="weather-icon">{icon}</div>
            <div className="weather-details">
                <span className="weather-title">CONDITION: {type}</span>
                <span className="weather-stats">
                    WIND: <strong>{weather.windStrength.toFixed(2)}x</strong> |
                    BURN RATE: <strong>{weather.consumptionRate.toFixed(2)}x</strong>
                </span>
            </div>
        </div>
    );
}