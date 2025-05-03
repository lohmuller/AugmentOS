/**
 * EvenOsBase defines the abstract API surface for interacting with Even Realities smart glasses firmware.
 * It includes command definitions like brightness control, image/text transfer, system info queries, and gesture handlers.
 * Subclasses like Even_Os_1_5_0 should implement these methods for specific firmware versions.
 */

package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api;

import java.util.function.Function;
import java.util.concurrent.CompletableFuture;


public abstract class EvenOsApi {

    //Convert enum to class?
    public enum DashboardMode {
        FULL(0),
        DUAL(1),
        MINIMAL(2);
        private final int value;
        DashboardMode(int value) {this.value = value;}
        public int getValue() {return value;}
    }

    //Convert enum to class? 
    public enum DashboardSubMode {
        NOTES(0),
        STOCK(1),
        NEWS(2),
        CALENDAR(3),
        NAVIGATION(4),
        EMPTY1(5),
        EMPTY2(6);
        private final int value;
        DashboardSubMode(int value) {this.value = value;}
        public int getValue() {return value;}
    }

    EvenOsCommand setBrightness(int level, boolean auto);
    EvenOsCommand setSilentMode(boolean silent);
    EvenOsCommand setNotificationConfig(String json);
    EvenOsCommand setDashboardMode(DashboardMode mode, DashboardSubMode subMode);
    EvenOsCommand setMicrophoneEnabled(boolean enabled);
    EvenOsCommand sendBmp(byte[] bmpData);
    EvenOsCommand sendText(String text);
    EvenOsCommand endTransferBmp();
    EvenOsCommand crcCheck(byte[] bmpData);
    EvenOsCommand heartbeat();
    EvenOsCommand exitApp();
    EvenOsCommand initialize();
    EvenOsCommand getFirmwareInfo();
    EvenOsCommand setWearDetection(boolean enabled);
    EvenOsCommand getBatteryInfo();
    EvenOsCommand getDeviceUptime();
    EvenOsCommand getUsageInfo();
    Function<byte[], T> onDoubleTap(Sides side);
    Function<byte[], T> onSingleTap(Sides side);
    Function<byte[], T> onTripleTap(Sides side);
    Function<byte[], T> onLongPress(Sides side);

}