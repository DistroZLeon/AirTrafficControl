import React from 'react';
import ProgressBar from '../ProgressBar/ProgressBar';
import { formatRemainingTime } from '../../utils/timeFormatter';
import './FlightCard.css';

export default function FlightCard({ plane, onClick }) {
    const details = plane.stateDetails || plane.stateDate || {};
    const status = details.status || "UNKNOWN";
    const isEmergency = plane.emergency !== "NONE";

    let timerDisplay = "--:--";
    if (status === 'EN_ROUTE' && details.flightDuration && details.timePassed !== undefined) {
        const actualTimeRemaining = details.flightDuration - details.timePassed;
        timerDisplay = formatRemainingTime(actualTimeRemaining);
    }

    const fuelDisplay = plane.fuelRemaining
        ? (plane.fuelRemaining / 1000000).toFixed(2)
        : "0.00";

    return (
        <div
            className={`flight-card ${isEmergency ? 'emergency-border' : ''}`}
            onClick={() => onClick(plane)}
            style={{ cursor: 'pointer' }}
        >
            <div className="card-header">
                <h2>FLT-{plane.planeId} <span>({plane.type || "UNKNOWN"})</span></h2>
                {isEmergency && <div className="emergency-badge">EMERGENCY: {plane.emergency}</div>}
            </div>

            <div className="card-metrics">
                <div className="metric">
                    <label>Route</label>
                    <span>{plane.startingPoint || "?"} ➔ {plane.destination || "?"}</span>
                </div>
                <div className="metric">
                    <label>Fuel</label>
                    <span className={plane.fuelRemaining < 30000000 ? 'low-fuel' : ''}>
                        {fuelDisplay} M
                    </span>
                </div>

                <div className="metric timer-block">
                    <label>Est. Time</label>
                    <span className="timer">{timerDisplay}</span>
                </div>
            </div>

            <ProgressBar details= {details} />

            <div className="raw-status-text">{status.replace(/_/g, ' ')}</div>
        </div>
    );
}