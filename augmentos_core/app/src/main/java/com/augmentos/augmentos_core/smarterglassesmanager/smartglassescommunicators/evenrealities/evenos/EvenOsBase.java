public abstract class EvenOsBase {

    //TODO remove this enum?
    public enum CommandId {
        SET_BRIGHTNESS,
        SET_SILENT_MODE,
        SET_NOTIFICATION_CONFIG,
        SET_DASHBOARD_MODE,
        MIC_ENABLE,
        BMP_DISPLAY,
        BMP_CRC,
        CLEAR_SCREEN,
        QUICK_NOTE,
        FIRMWARE_INFO, 
        HEARTBEAT,
        WEAR_DETECTION,
        BATTERY_INFO, 
        UPTIME,
        USAGE_INFO,
        DISPLAY_NOTIFICATION,
        INIT,
        TEXT_COMMMAND,
        AUDIO_STREAM,
        STATES_CHANGE;
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

}