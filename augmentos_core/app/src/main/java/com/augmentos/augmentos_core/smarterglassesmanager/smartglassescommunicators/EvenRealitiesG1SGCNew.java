package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities;

import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection.EvenOsConnection;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos.EvenOsBase;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos.EvenOsFirmwareFactory;

import java.util.concurrent.CompletableFuture;
import org.greenrobot.eventbus.EventBus;

public class EvenRealitiesG1SGCNew extends SmartGlassesCommunicator {

    private final EvenOsBase evenOsApi;
    private final ConnectionManager connectionManager;
    private ScheduledFuture<?> batteryMonitoringFuture;

    public EvenRealitiesG1SGCNew(Context context, SmartGlassesDevice smartGlassesDevice) {
        super();
        this.connectionManager = new ConnectionManager(context, smartGlassesDevice);
        this.evenOsApi = new EvenOs_1_5_0();
    }

    private void startBatteryMonitoring() {
        batteryMonitoringFuture = scheduler.scheduleAtFixedRate(() -> {
            connectionManager
                .sendCommand(evenOsApi.getBatteryInfo())
                .thenAccept(result -> {
                    int minBatteryLevel = Math.min(result.leftBatteryLevel, result.rightBatteryLevel);
                    //@TODO: Glasses charging state True/False 
                    //@TODO: Case charging state is true/false
                    //@TODO: Case Battery level 
                    EventBus.getDefault().post(new BatteryLevelEvent(minBatteryLevel));
                    if (minBatteryLevel < 10 && batteryMonitoringFuture != null) {
                        batteryMonitoringFuture.cancel(false);
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
        }, 0, 1, TimeUnit.MINUTES); // 1 minute
    }

    @Override
    public void connectToSmartGlasses() {
        this.connectionManager.init();
        this.startBatteryMonitoring();
     }

    @Override
    public void findCompatibleDeviceNames() {
        // TODO: Implement device discovery logic
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
