/**
 * EvenOsBase defines the abstract API surface for interacting with Even Realities smart glasses firmware.
 * It includes command definitions like brightness control, image/text transfer, system info queries, and gesture handlers.
 * Subclasses like Even_Os_1_5_0 should implement these methods for specific firmware versions.
 */

package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api;

import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

public abstract class EvenOsApi<T> {

    public enum Sides {
        LEFT, RIGHT, BOTH, EITHER;
    }

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

    public abstract EvenOsCommand<T> setBrightness(int level, boolean auto);
    public abstract EvenOsCommand<T> setSilentMode(boolean silent);
    public abstract EvenOsCommand<T> setNotificationConfig(String json);
    public abstract EvenOsCommand<T> setDashboardMode(DashboardMode mode, DashboardSubMode subMode);
    public abstract EvenOsCommand<T> setMicrophoneEnabled(boolean enabled);
    public abstract EvenOsCommand<T> sendBmp(byte[] bmpData);
    public abstract EvenOsCommand<T> sendText(String text);
    public abstract EvenOsCommand<T> endTransferBmp();
    public abstract EvenOsCommand<T> crcCheck(byte[] bmpData);
    public abstract EvenOsCommand<T> heartbeat();
    public abstract EvenOsCommand<T> exitApp();
    public abstract EvenOsCommand<T> initialize();
    public abstract EvenOsCommand<T> getFirmwareInfo();
    public abstract EvenOsCommand<T> setWearDetection(boolean enabled);
    public abstract EvenOsCommand<T> getBatteryInfo();
    public abstract EvenOsCommand<T> getDeviceUptime();
    public abstract EvenOsCommand<T> getUsageInfo();
    public abstract Function<byte[], T> onDoubleTap(Sides side);
    public abstract Function<byte[], T> onSingleTap(Sides side);
    public abstract Function<byte[], T> onTripleTap(Sides side);
    public abstract Function<byte[], T> onLongPress(Sides side);
}