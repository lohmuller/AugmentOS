package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators;

import java.util.HashMap;
import java.util.Map;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.CommandId;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.FirmwareCommands;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command.Parameter;
import static com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.EvenRealitiesG1Commands.Command.ParameterType
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FirmwareCommands_1_5_0 implements FirmwareCommands {

    /**
     * Maps hex codes to their corresponding CommandId for fast lookup.
     * Each hex code must be unique.
     */
    private static final Map<Byte, CommandId> HEX_TO_COMMAND = new HashMap<Byte, CommandId>() {{
        // Response commands (Glasses->APP)
        //@TODO allow multiple bytes for command id
        put((byte)0x01, CommandId.SET_BRIGHTNESS);
        put((byte)0x03, CommandId.SET_SILENT_MODE);
        put((byte)0x04, CommandId.NOTIFICATION_CONFIGURATION);
        put((byte)0x06, CommandId.SET_DASHBOARD_MODE);
        put((byte)0x0E, CommandId.MIC_ENABLE);
        put((byte)0x15, CommandId.BMP_DISPLAY);
        put((byte)0x16, CommandId.BMP_CRC);
        put((byte)0x18, CommandId.CLEAR_SCREEN);
        put((byte)0x1E, CommandId.QUICK_NOTE);
        put((byte)0x23, CommandId.FIRMWARE_INFO_REQ);
        put((byte)0x6e, CommandId.FIRMWARE_INFO_RES); // "net build" response  0x6e 0x65 0x74 0x20 0x62 0x75 0x69 0x6c 0x64
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
     * Status codes for command responses
     */
    private static final byte SUCCESS = (byte)0xC9;
    private static final byte FAILURE = (byte)0xCA;
    private static final byte CONTINUE_DATA = (byte)0xCB;

    /**
     * Parse the command from the byte array (from g1 glasses), to a Command object
     * 
     * @param data the byte array containing the command data (must have at least 1 byte)
     * @return the parsed Command object, or null if the data is invalid
     */
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

            // Success or failure response
            case commandId.SET_SILENT_MODE:
            case commandId.BRIGHTNESS:
            case commandId.NOTIFICATION_CONFIGURATION:
            case commandId.MIC_ENABLE:
            case commandId.BMP_DISPLAY:
            case commandId.BMP_CRC:
            case commandId.CLEAR_SCREEN:
                String status = STATUS.get(parameterBytes[0]);
                if (status == SUCCESS || status == FAILURE) {
                    params.put("value", new Parameter(ParameterType.BOOLEAN, (status == SUCCESS)));
                }
                break;

            case commandId.FIRMWARE_INFO_RES: 
                String firmwareInfo = new String(parameterBytes, StandardCharsets.US_ASCII);
                params.put("value", new Parameter(ParameterType.STRING, firmwareInfo));
                break;

            case commandId.BATTERY_INFO:
                if (parameterBytes[0] == (byte) 0x66) {
                    params.put("value", new Parameter(ParameterType.INTEGER, parameterBytes[0]));
                    String g1status = new String(Arrays.copyOfRange(parameterBytes, 1, parameterBytes.length), StandardCharsets.US_ASCII);
                    params.put("text", new Parameter(ParameterType.STRING, g1status)); //In case, arms closed, charging.  
                }
                break;
        }

        if (parameterBytes.length > 0  && params.isEmpty()) {
            //TODO ignore 0 byte values
            // If there are parameters, but they don't match any of the cases, add them as UNKNOWN
            params.put("UNKNOWN", new Parameter(ParameterType.UNKNOWN, parameterBytes));
        }

        return new Command(commandId, params);
    }

    
}
