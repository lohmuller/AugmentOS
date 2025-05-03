package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection.ConnectionManager;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.EvenOsApi;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.EvenOs_1_5_0;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.SmartGlassesDevice;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.SmartGlassesCommunicator;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.SmartGlassesFontSize;
import com.augmentos.augmentos_core.smarterglassesmanager.eventbusmessages.BatteryLevelEvent;
import com.augmentos.augmentos_core.smarterglassesmanager.eventbusmessages.GlassesBluetoothSearchDiscoverEvent;
import com.augmentos.augmentos_core.smarterglassesmanager.eventbusmessages.GlassesBluetoothSearchStopEvent;
import org.greenrobot.eventbus.EventBus;

public class EvenRealitiesG1SGCNew extends SmartGlassesCommunicator {

    private final EvenOsApi evenOsApi;
    private final ConnectionManager connectionManager;
    private ScheduledFuture<?> batteryMonitoringFuture;

    public EvenRealitiesG1SGCNew(Context context, SmartGlassesDevice smartGlassesDevice) {
        super();
        this.connectionManager = new ConnectionManager(context, smartGlassesDevice);
        this.evenOsApi = new EvenOs_1_5_0();
    }

    private void startBatteryMonitoring() {
        batteryMonitoringFuture = scheduler.scheduleAtFixedRate(() -> {
            leftResponse = connectionManager.sendAndWait(evenOsApi.getBatteryInfo(EvenOsApi.Sides.LEFT), 1000);
            rightResponse = connectionManager.sendAndWait(evenOsApi.getBatteryInfo(EvenOsApi.Sides.RIGHT), 1000);
            
            int minBatteryLevel = Math.min(leftResponse.leftBatteryLevel, rightResponse.rightBatteryLevel);

            EventBus.getDefault().post(new BatteryLevelEvent(minBatteryLevel));
            if (minBatteryLevel < 10 && batteryMonitoringFuture != null) {
                batteryMonitoringFuture.cancel(false);
            }
             
        }, 0, 1, TimeUnit.MINUTES); // 1 minute
    }

    private void initListening() {
        //connectionManager.setOnResponse(EvenOsApi.Sides.LEFT, evenOsApi.onDoubleTap(), (isTapped, side) -> {
        //    if (isTapped) {
        //        System.out.println("Double tap detected on " + side);
        //    }
        //});
       // connectionManager.setOnResponse(EvenOsApi.Sides.BOTH, evenOsApi.onCaseBattery(), (batteryLevel, side) -> {
        //        System.out.println("Double tap detected on " + side);
       // });
    }

    @Override
    public void connectToSmartGlasses() {
        this.connectionManager.init();
        this.startBatteryMonitoring();
     }

    @Override
    public void findCompatibleDeviceNames() {
        /*
        if (isScanningForCompatibleDevices) {
            Log.d(TAG, "Scan already in progress, skipping...");
            return;
        }
        isScanningForCompatibleDevices = true;

        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            Log.e(TAG, "BluetoothLeScanner not available");
            isScanningForCompatibleDevices = false;
            return;
        }

        List<String> foundDeviceNames = new ArrayList<>();
        if (findCompatibleDevicesHandler == null) {
            findCompatibleDevicesHandler = new Handler(Looper.getMainLooper());
        }

        // Optional: add filters if you want to narrow the scan
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        // Create a modern ScanCallback instead of the deprecated LeScanCallback
        final ScanCallback bleScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String name = device.getName();
                if (name != null && name.contains("Even G1_") && name.contains("_L_")) {
                    synchronized (foundDeviceNames) {
                        if (!foundDeviceNames.contains(name)) {
                            foundDeviceNames.add(name);
                            Log.d(TAG, "Found smart glasses: " + name);
                            String adjustedName = parsePairingIdFromDeviceName(name);
                            EventBus.getDefault().post(
                                    new GlassesBluetoothSearchDiscoverEvent(
                                            smartGlassesDevice.deviceModelName,
                                            adjustedName
                                    )
                            );
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                // If needed, handle batch results here
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "BLE scan failed with code: " + errorCode);
            }
        };

        // Start scanning
        scanner.startScan(filters, settings, bleScanCallback);
        Log.d(TAG, "Started scanning for smart glasses with BluetoothLeScanner...");

        // Stop scanning after 10 seconds (adjust as needed)
        findCompatibleDevicesHandler.postDelayed(() -> {
            scanner.stopScan(bleScanCallback);
            isScanningForCompatibleDevices = false;
            Log.d(TAG, "Stopped scanning for smart glasses.");
            EventBus.getDefault().post(
                    new GlassesBluetoothSearchStopEvent(
                            smartGlassesDevice.deviceModelName
                    )
            );
        }, 10000);
        */
    }

    @Override
    public void blankScreen() {
        Object result = this.connectionManager.sendAndWait(this.evenOsApi.exitApp(), 1000);
        System.out.println("response: " + result);
    }

    @Override
    public void destroy() {
        this.connectionManager.destroy();
    }

    @Override
    public void displayReferenceCardSimple(String title, String body) {
        // TODO: Implement reference card display command using firmware
    }

    @Override
    public void displayTextWall(String text) {
        Object result = this.connectionManager.sendAndWait(this.evenOsApi.sendText(text), 1000);
        System.out.println("response: " + result);
    }

    @Override
    public void displayDoubleTextWall(String textTop, String textBottom) {
        // TODO: Implement double text wall display command using firmware
    }

    @Override
    public void displayReferenceCardImage(String title, String body, String imgUrl) {
        // TODO: Implement reference card with image display command using firmware
    }

    @Override
    public void displayBulletList(String title, String[] bullets) {
        // TODO: Implement bullet list display command using firmware
    }

    @Override
    public void displayRowsCard(String[] rowStrings) {
        // TODO: Implement rows card display command using firmware
    }

    @Override
    public void showNaturalLanguageCommandScreen(String prompt, String naturalLanguageArgs) {
        // TODO: Implement natural language command screen display using firmware
    }

    @Override
    public void updateNaturalLanguageCommandScreen(String naturalLanguageArgs) {
        // TODO: Implement natural language command screen update using firmware
    }

    @Override
    public void scrollingTextViewIntermediateText(String text) {
        // TODO: Implement scrolling text view intermediate text update using firmware
    }

    @Override
    public void scrollingTextViewFinalText(String text) {
        // TODO: Implement scrolling text view final text update using firmware
    }

    @Override
    public void stopScrollingTextViewMode() {
        // TODO: Implement scrolling text view mode stop using firmware
    }

    @Override
    public void displayPromptView(String title, String[] options) {
        // TODO: Implement prompt view display using firmware
    }

    @Override
    public void displayTextLine(String text) {
        // TODO: Implement text line display using firmware
    }

    @Override
    public void displayBitmap(Bitmap bmp) {
        // TODO: Implement bitmap display using firmware
    }

    @Override
    public void displayCustomContent(String json) {
        // TODO: Implement custom content display using firmware
    }

    @Override
    public void showHomeScreen() {
        // TODO: Implement home screen display using firmware
    }

    @Override
    public void setFontSize(SmartGlassesFontSize fontSize) {
        // TODO: Implement font size setting using firmware
    }

    @Override
    protected void setFontSizes() {
        // TODO: Set appropriate font sizes for LARGE_FONT, MEDIUM_FONT, SMALL_FONT
        LARGE_FONT = 24;
        MEDIUM_FONT = 18;
        SMALL_FONT = 14;
    }

    @Override
    public void updateGlassesBrightness(int brightness) {
        // TODO: Implement brightness update command using firmware
    }

    @Override
    public void updateGlassesAutoBrightness(boolean autoBrightness) {
        // TODO: Implement auto brightness update command using firmware
    }

    @Override
    public void updateGlassesHeadUpAngle(int headUpAngle) {
        Object result = this.connectionManager.sendAndWait(this.evenOsApi.setHeadUpAngle(headUpAngle), 1000);
        System.out.println("response: " + result);
    }

    @Override
    public void changeSmartGlassesMicrophoneState(boolean isMicrophoneEnabled) {
        // TODO: Implement microphone state change command using firmware
    }
}
