package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators;

import java.util.HashMap;
import java.util.Map;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.CommandId;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.FirmwareCommands;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command.Parameter;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command.ParameterType
import javax.annotation.Nullable;

public class FirmwareCommands_1_5_0 implements FirmwareCommands {

    /**
     * Maps hex codes to their corresponding CommandId for fast lookup.
     * Each hex code must be unique.
     */
    private static final Map<Byte, CommandId> HEX_TO_COMMAND = new HashMap<Byte, CommandId>() {{
        // Response commands (Glasses->APP)
        put((byte)0x01, CommandId.SET_BRIGHTNESS);
        put((byte)0x03, CommandId.SET_SILENT_MODE);
        put((byte)0x04, CommandId.NOTIFICATION_CONFIGURATION);
        put((byte)0x06, CommandId.SET_DASHBOARD_MODE);
        put((byte)0x0E, CommandId.MIC_ENABLE);
        put((byte)0x15, CommandId.BMP_DISPLAY);
        put((byte)0x16, CommandId.BMP_CRC);
        put((byte)0x18, CommandId.CLEAR_SCREEN);
        put((byte)0x1E, CommandId.QUICK_NOTE);
        put((byte)0x23, CommandId.FIRMWARE_INFO);
        put((byte)0x25, CommandId.HEARTBEAT);
        put((byte)0x27, CommandId.WEAR_DETECTION);
        put((byte)0x2C, CommandId.BATTERY_INFO);
        put((byte)0x37, CommandId.UPTIME);
        put((byte)0x3E, CommandId.USAGE_INFO);
        put((byte)0x4B, CommandId.DISPLAY_NOTIFICATION);
        put((byte)0x4D, CommandId.INIT);
        put((byte)0x4E, CommandId.TEXT_COMMAND);
        put((byte)0xF1, CommandId.AUDIO_STREAM);
        put((byte)0xF5, CommandId.STATES_CHANGE);
    }};

    /**
     * Maps status subcommand codes to their meanings
     */
    private static final Map<Byte, String> STATUS = new HashMap<Byte, String>() {{
        put((byte)0xC9, "Success");
        put((byte)0xCA, "Failure");
        put((byte)0xCB, "Continue data");
    }};

    @Override
    @Nullable
    public Command parseCommand(byte[] data) {
        if (data == null || data.length == 0) return null;
        
        byte headerByte = data[0];
        byte[] parameterBytes = Arrays.copyOfRange(data, 1, data.length);   
        
        // Get the CommandId directly from the hex code
        CommandId commandId = HEX_TO_COMMAND.get(headerByte);
        if (commandId == null) return null;

        // Create parameters map
        Map<String, Command.Parameter> params = new HashMap<>();

        // Add parameters based on command type and ID
        switch (commandId) {

            // True or false
            case SILENT_MODE:
            case BRIGHTNESS:
                // Brightness value is a single byte (0-255)
                String status = STATUS.get(parameterBytes[0]);
                if (status != null) {
                    params.put("UNKNOWN", new Parameter(ParameterType.STATUS, parameterBytes));
                } else {
                    params.put("value", new Parameter(ParameterType.BRIGHTNESS_VALUE, parameterBytes[0] & 0xFF));
                }
                break;

            case BATTERY:
                // Battery level is a single byte (0-100)
                params.put("value", new Parameter(ParameterType.BATTERY_LEVEL, parameterBytes[0] & 0xFF));
                break;

                // Silent mode is a single byte (0 = OFF, 1 = ON)
                params.put("value", new Parameter(ParameterType.SILENT_MODE_STATUS, parameterBytes[0] == 1));
                break;
        }

        return new Command(commandId, params);
    }

    
}
