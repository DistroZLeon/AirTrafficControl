import React from 'react';
import './FlightModal.css'; // Paste the .modal-overlay and .modal-content CSS here

export default function FlightModal({ plane, onClose }) {
    if (!plane) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h2>Flight Details: FLT-{plane.planeId}</h2>
                <hr/>
                <div className="modal-grid">
                    <p><strong>Type:</strong> {plane.type || "UNKNOWN"}</p>
                    <p><strong>Passengers:</strong> {plane.passengerCount || 0}</p>
                    <p><strong>{plane.type=== "CARGO"?"Cargo Weight": "Luggage Weight"}:</strong> {plane.cargoWeight || 0} kg</p>
                    <p><strong>Base Consumption:</strong> {plane.consumptionRate ? plane.consumptionRate.toFixed(2) : "0.00"} units/s</p>
                </div>
                <button className="close-btn" onClick={onClose}>Close</button>
            </div>
        </div>
    );
}