package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import android.util.Log;
import androidx.annotation.Nullable;

public class EvenRealitiesG1Commands {

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
        BATTERY_INFO(44),
        UPTIME(55),
        USAGE_INFO(62),
        DISPLAY_NOTIFICATION(75),
        INIT(77),
        TEXT_COMMMAND(78),
        AUDIO_STREAM(241),
        STATES_CHANGE(245); 

        private final int value;

        CommandId(int value) {
            this.value = value;
        }
    }

    // Interface for firmware-specific implementation
    public interface G1FwCmdInterface {
        Command getCommandByData(byte[] data);
    }

    // Mapping of firmware versions to their implementations
    private static final Map<String, G1FwCmdInterface> FIRMWARE_VERSION_MAP = new HashMap<String, G1FwCmdInterface>() {{
        put("1.5.0", new FirmwareCommands_1_5_0());
        // Keep the latest version at the end of the map
    }};

    private final G1FwCmdInterface firmwareCmd;

    public EvenRealitiesG1Commands(String firmwareVersion) {
        this.firmwareCmd = FIRMWARE_VERSION_MAP.get(firmwareVersion);
        
        // If the version is not found, use the latest version
        if (this.firmwareCmd == null) {
            String latestVersion = Collections.max(FIRMWARE_VERSION_MAP.keySet());
            this.firmwareCmd = FIRMWARE_VERSION_MAP.get(latestVersion);
            Log.w("EvenRealitiesG1Commands", "WARNING: G1 firmware "+firmwareVersion+" not found Using firmware implementation " + latestVersion );
        }
    }

    /**
     * Parse the command from the byte array
     * @param data the byte array containing the command data (must have at least 1 byte)
     * @return the parsed Command object, or null if the data is invalid
     */
    @Nullable
    public Command BytesToCommand(byte[] data) {
        if (data == null || data.length == 0) return null;
        return firmwareCmd.getCommandByData(data);
    }

    public byte[] CommandToBytes(CommandId command, Map<String, Parameter> parameters) {
        return firmwareCmd.getBytesByCommand(command, parameters);
    }


    // Class to represent a command
    public static class Command {
        private final CommandId commandId;
        private final Map<String, Parameter> parameters;

        public Command(CommandId commandId, Map<String, Parameter> parameters) {
            this.commandId = commandId;
            this.parameters = parameters;
        }

        // Command specific types
        public enum ParameterType {
            // Generic types
            STATUS, // SUCCESS/FAIL
            UNKNOWN,

            // Command specific types
            BRIGHTNESS_VALUE,      // Value from 0-255
            SILENT_MODE_STATUS,    // ON/OFF
            WHITELIST_ADDED,       // Device list
            HEAD_ANGLE_VALUE,      // Angle in degrees
            MIC_STATUS,           // ENABLED/DISABLED
            BMP_DISPLAY_STATUS,    // SUCCESS/FAIL
            BMP_CRC_VALUE,        // CRC value
            BATTERY_LEVEL,        // Level from 0-100
            TEXT_DISPLAY_STATUS,   // SUCCESS/FAIL
            NOTIFICATION_STATUS,   // SUCCESS/FAIL
            AUDIO_STREAM_DATA,     // Audio data
            HEAD_MOVEMENT_TYPE,    // UP/DOWN
            FIRMWARE_VERSION,      // Firmware version
            INIT_STATUS,          // SUCCESS/FAIL
            SCREEN_STATUS,        // ON/OFF
            WEAR_DETECTION_STATUS  // DETECTED/NOT_DETECTED
        }
        // Class to represent a command parameter
        public static class Parameter {
            private final ParameterType type;
            private final Object value;

            public Parameter(ParameterType type, Object value) {
                this.type = type;
                this.value = value;
            }

            public ParameterType getType() { return type; }
            public Object getValue() { return value; }
        }

        public CommandId getCommandId() { return commandId; }
        public Map<String, Parameter> getParameters() { return parameters; }
        public Parameter getParameter(String name) { return parameters.get(name); }
    }
} 