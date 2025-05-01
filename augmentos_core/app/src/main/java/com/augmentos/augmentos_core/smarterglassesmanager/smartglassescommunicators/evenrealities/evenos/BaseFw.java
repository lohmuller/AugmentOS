/**BaseFirmware.java
comandos de um firmware.

 * Essa classe eh responsavel por mapear todos os commandos,
 * e tambem por mapear as funcoes dos comandos de cada firmware.
 */

public abstract class BaseFw {

    public enum CommandId {
        SET_BRIGHTNESS(1),
        SET_SILENT_MODE(3),
        SET_NOTIFICATION_CONFIG(4),
        SET_DASHBOARD_MODE(6),
        MIC_ENABLE(14),
        BMP_DISPLAY(21),
        BMP_CRC(22),
        CLEAR_SCREEN(24),
        QUICK_NOTE(30),
        FIRMWARE_INFO(35), 
        HEARTBEAT(37),
        WEAR_DETECTION(39),
        BATTERY_INFO(44), // @TODO: consider rename
        UPTIME(55),
        USAGE_INFO(62),
        DISPLAY_NOTIFICATION(75),
        INIT(77),
        TEXT_COMMMAND(78),
        AUDIO_STREAM(241),
        STATES_CHANGE(245);

        private final int id;

        CommandId(int id) {
            this.id = id;
        }
    }

    private final Connection leftConnection;
    private final Connection rightConnection;

    public BaseFw(Connection leftConnection, Connection rightConnection) {
        this.leftConnection = leftConnection;
        this.rightConnection = rightConnection;
    }

    public abstract void setBrightness(int level, boolean auto);
    public abstract void setSilentMode(boolean silent);
    public abstract void setNotificationConfig(boolean silent);
    public abstract void setDashboardMode(boolean silent);
    public abstract void setMicrophoneEnabled(boolean enabled);
    public abstract void sendImage(boolean enabled);

}