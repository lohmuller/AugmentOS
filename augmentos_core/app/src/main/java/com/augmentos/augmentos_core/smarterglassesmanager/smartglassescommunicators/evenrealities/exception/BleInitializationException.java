package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.exception;


/**
 * Exception thrown when the BLE initialization fails.
 * Wrong UUIDs, wrong device, etc.
 */
public class BleInitializationException extends RuntimeException {
    public BleInitializationException(String message) {
        super(message);
    }
}