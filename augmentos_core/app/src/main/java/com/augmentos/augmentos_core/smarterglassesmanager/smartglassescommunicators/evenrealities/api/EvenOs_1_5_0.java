/**
 * Implementation of EvenOsBase for Even Realities smart glasses (firmware 1.5.0).
 * 
 * This class defines supported commands with request structures, expected response headers,
 * and response parsers. It abstracts BLE communication into a high-level API (e.g. brightness, 
 * silent mode, image/text transfer).
 * 
 * Based on reverse-engineering of AugmentOS, the Even Realities DemoApp, and shared docs.
 */

package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api;

import java.util.EnumMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.CRC32;
import java.nio.ByteBuffer;

import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos.EvenOsBase;


public class Even_Os_1_5_0 implements EvenOsApi {

    /**
     * Set brightness
     * @param level (0-100)
     * @param auto (true/false)
     */
    public EvenOsCommand setBrightness(int level, boolean auto) {
        int fallbackLevel = 30;
        int safeLevel = (level >= 0 && level <= 100) ? level : fallbackLevel;

        // Scale the level to 0-63
        int scaledLevel = (safeLevel * 63) / 100;

        // Validate brightness rang
        byte[] requestBytes = new byte[] {
            (byte) 0x01,
            (byte) scaledLevel,
            (byte)(auto ? 1 : 0)
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); //Success or failure response
        });
    }

    /**
     * Set silent mode
     * @param silent (true/false)
     */
    public EvenOsCommand setSilentMode(boolean silent) {
        byte[] requestBytes = new byte[] {
            (byte) 0x03,
            (byte)(silent ? 1 : 0)
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); //Success or failure response
        });
    }   

    /** 
     * Set microphone enabled
     * @param enabled (true/false)
     */
    public EvenOsCommand setMicrophoneEnabled(boolean enabled) {
        byte[] requestBytes = new byte[] {
            (byte) 0x0E,            //opcode
            (byte)(enabled ? 1 : 0)
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); //Success or failure response
        });
    }


    private int heartbeatSeq;
    /**
     * Heartbeat
     * @param seq (sequence number)
     * @param length (length of the heartbeat)
     */
    public EvenOsCommand heartbeat() {
        int length = 6;
        int seq = heartbeatSeq & 0xFF;

        byte[] requestBytes = new byte[] {
            (byte) 0x25,                        // Opcode for heartbeat
            (byte) (length & 0xFF),             // Length LSB (little-endian)
            (byte) ((length >> 8) & 0xFF),      // Length MSB (normally 0)
            (byte) (seq & 0xFF),                // Sequence number (first instance)
            (byte) 0x04,                        // Fixed value, what is it?
            (byte) ((seq + 1) & 0xFF)           // Sequence number (second instance). Maybe can split in two packets?
        };
        // Increment sequence, wrapping at 256
        heartbeatSeq = (heartbeatSeq + 1) % 256;
        
        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); //Success or failure response
        });
    }

    /**
     * Exit app
     * tell the glasses to exit function to dashboard
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand exitApp() {
        byte[] requestBytes = new byte[] {
            (byte) 0x18
        };
        byte[] responseHeader = { requestBytes[0] };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); 
        });
    }

    public EvenOsCommand initialize() {
        byte[] requestBytes = new byte[] {
            (byte) 0x4D,
            (byte) 0xFB // Maybe there is more options to send?
        };
        byte[] responseHeader = { requestBytes[0] };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return (data[0] == 0xC9); 
        });
    }

    /**
     * Get firmware info
     * @return (EvenOsCommand)
     */
    public EvenOsCommand getFirmwareInfo() {
        byte[] requestBytes = new byte[] {
            (byte) 0x23,
        };
        byte[] responseHeader = new byte[] {
            (byte) 0x6E,
            (byte) 0x65,
            (byte) 0x74,
            (byte) 0x20,
            (byte) 0x62,
            (byte) 0x75,
            (byte) 0x69,
            (byte) 0x6C,
            (byte) 0x64
        };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }

    /**
     * Set wear detection
     * @param enabled (true/false)
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand setWearDetection(boolean enabled) {
        byte[] requestBytes = new byte[] {
            (byte) 0x27,
            (byte) (enabled ? 1 : 0)
        };
        byte[] responseHeader = { requestBytes[0] };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }
    
    /**
     * Get battery info for both arms
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand getBatteryInfo() {
        byte[] requestBytes = new byte[] {
            (byte) 0x2C,
        };
        byte[] responseHeader = { requestBytes[0] };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            int batteryLevel = data[2];
            return {
                batteryLevel: batteryLevel,
            };
        });
    }

    /**
    * Get device (glasses) uptime
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand getDeviceUptime() {
        byte[] requestBytes = new byte[] {
            (byte) 0x37,
        };
        byte[] responseHeader = { requestBytes[0] };
        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }

    /**
     * Fetch buried point data, which is essentially user usage tracking
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand getUsageInfo() {
        byte[] requestBytes = new byte[] {
            (byte) 0x3E,
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }
    

    /**
     * Set quick note
     * @TODO: Need more information about this command!
     * @param note (String)
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand setQuickNote(String note) {
        throw new UnsupportedOperationException("Not implemented yet");
        /*
        return new byte[] {
            (byte) 0x1E,
            (byte) note.getBytes().length,
            note.getBytes(),
        };
        */
    }
    

    /**
     * Set head up angle
     * @param angle (0-60)
     * @param unknownParameter (1 or 0) @TODO: Check if this parameter is used...
     */
    public EvenOsCommand setHeadUpAngle(int angle, boolean unknownParameter) {

        // Validate angle range (0 ~ 60)
        int clamped = Math.max(0, Math.min(angle, 60));
        byte[] requestBytes = new byte[] {
            (byte) 0x0B,
            (byte) clamped,
            (byte) (unknownParameter ? 1 : 0) 
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }
    
    
    /**
     * Set notification config  
     * @param jsonData (json data)
     * @return chunks (byte[][] array of chunks) Multiple sends
     */
    public EvenOsCommand setNotificationConfig(String jsonData) {
        
        int maxSize = 180;
        byte[] jsonBytes = jsonData.getBytes();
        int totalChunks = (int) Math.ceil(jsonBytes.length / (double) maxSize);
        byte[][] chunks = new byte[totalChunks][];

        if (totalChunks > 255) {
            throw new IllegalArgumentException("json data is too large to send");
        }
        
        for (int i = 0; i < totalChunks; i++) {
            int start = i * maxSize;
            int end = Math.min(start + maxSize, jsonBytes.length);
            int chunkSize = end - start;
            
            byte[] data = new byte[chunkSize + 2];
            data[0] = 0x04; 
            data[1] = (byte) chunkSize; //total chunks
            data[2] = (byte) i; //current chunk
            System.arraycopy(jsonBytes, start, data, 2, chunkSize); //append jsonbytes to data
        
            chunks[i] = data;
        }

        byte[] responseHeader = { 0x04 };
        
        return new EvenOsCommand(chunks, responseHeader, EvenOsCommand.Sides.LEFT, (byte[] data) -> {
            return null;
        });
    }

    /**
     * Set dashboard mode
     * @param mode DashboardMode enum value
     * @param subMode DashboardSubMode enum value
     */
    public EvenOsCommand setDashboardMode(DashboardMode mode, DashboardSubMode subMode) {
        if (mode == DashboardMode.MINIMAL && subMode != DashboardSubMode.NOTES) {
            throw new IllegalArgumentException("SubMode not supported for MINIMAL mode");
        }
        byte[] requestBytes = new byte[] {
            (byte) 0x06,
            (byte) 0x07,
            (byte) this.getAvailableSeq(), //seq ?
            (byte) 0x00, //seq ?
            (byte) 0x06, // API?
            (byte) mode.getValue(),
            (byte) subMode.getValue()
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH,(byte[] data) -> {
            return null;
        });
    }

    public EvenOsCommand sendText(String text) {
        final int maxPayloadPerPacket = 180; // @TODO check the correct max value

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        int totalLength = textBytes.length;
        int totalPackets = (int) Math.ceil((double) totalLength / maxPayloadPerPacket);

        byte[][] packets = new byte[totalPackets][];
        for (int i = 0; i < totalPackets; i++) {
            int start = i * maxPayloadPerPacket;
            int end = Math.min(start + maxPayloadPerPacket, totalLength);
            byte[] chunk = Arrays.copyOfRange(textBytes, start, end);

            ByteBuffer buffer = ByteBuffer.allocate(9 + chunk.length);
            buffer.put((byte) 0x4E);                          // Command ID
            buffer.put((byte) i);                             // Sequence Number
            buffer.put((byte) totalPackets);                  // Total packages
            buffer.put((byte) i);                             // Current package number
            buffer.put((byte) 0x71);                          // newscreen: 0x70 (Text) + 0x01 (New content)
            buffer.put((byte) 0);                             // new_char_pos0
            buffer.put((byte) 0);                             // new_char_pos1
            buffer.put((byte) (i + 1));                       // current page
            buffer.put((byte) totalPackets);                  // max page
            buffer.put(chunk);                                // Actual text chunk

            packets[i] = buffer.array();
        }

        byte[] responseHeader = { 0x04 };

        return new EvenOsCommand(packets, responseHeader, EvenOsCommand.Sides.LEFT, (byte[] data) -> {
            return null;
        });
    }

 
    /**
     * Transfer bmp
     * @param bmpData (byte[] array of bytes)
     * @return (byte[][] array of chunks)
     */
    public EvenOsCommand sendBmp(byte[] bmpData) {
        int PACKET_SIZE = 194;
        byte[] ADDRESS_HEADER = new byte[]{0x00, 0x1C, 0x00, 0x00}; 
        int totalChunks = (int) Math.ceil((double) bmpData.length / PACKET_SIZE);

        if (totalChunks > 255) {
            throw new IllegalArgumentException("bmp data is too large to send");
        }

        byte[][] result = new byte[totalChunks][];

        for (int i = 0; i < totalChunks; i++) {
            int start = i * PACKET_SIZE;
            int end = Math.min(start + PACKET_SIZE, bmpData.length);
            byte[] chunk = Arrays.copyOfRange(bmpData, start, end);

            ByteBuffer buffer;
            if (i == 0) {
                buffer = ByteBuffer.allocate(2 + ADDRESS_HEADER.length + chunk.length); //create buffer
                buffer.put((byte) 0x15);    //opcode
                buffer.put((byte) i);       //seq
                buffer.put(ADDRESS_HEADER); //address header

            } else {
                buffer = ByteBuffer.allocate(2 + chunk.length); //create buffer
                buffer.put((byte) 0x15); //opcode
                buffer.put((byte) i);   //seq
            }

            buffer.put(chunk);
            result[i] = buffer.array();
        }

        byte[] responseHeader = { 0x15 };

        return new EvenOsCommand(result, responseHeader, EvenOsCommand.Sides.LEFT, (byte[] data) -> {
            return null;
        });
    }

    /**
     * End transfer bmp, and show the image
     * @return (byte[][] array of chunks)
     */
    public EvenOsCommand endTransferBmp() {
        byte[] requestBytes = new byte[] {
            (byte) 0x20, 
            (byte) 0x0D, 
            (byte) 0x0E, 
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }

    /**
     * CRC check
     * @param bmpData (byte[] array of bytes)
     * @return (byte[] array of bytes)
     */
    public EvenOsCommand crcCheck(byte[] bmpData) {
        byte[] ADDRESS_HEADER = new byte[]{0x00, 0x1C, 0x00, 0x00}; 
        // CRC
        byte[] withAddress = new byte[ADDRESS_HEADER.length + bmpData.length];
        System.arraycopy(ADDRESS_HEADER, 0, withAddress, 0, ADDRESS_HEADER.length);
        System.arraycopy(bmpData, 0, withAddress, ADDRESS_HEADER.length, bmpData.length);

        // Calculate CRC
        CRC32 crc32 = new CRC32(); //Maybe we can use CRC32-XZ instead
        crc32.update(withAddress);
        int crc = (int) crc32.getValue();

        // Calculate CRC
        byte[] requestBytes = new byte[] {
            (byte) 0x16,                   
            (byte) ((crc >> 24) & 0xFF),   //crc part 1
            (byte) ((crc >> 16) & 0xFF),   //crc part 2
            (byte) ((crc >> 8) & 0xFF),    //crc part 3
            (byte) (crc & 0xFF)            //crc part 4
        };

        byte[] responseHeader = { requestBytes[0] };

        return new EvenOsCommand(requestBytes, responseHeader, EvenOsCommand.Sides.BOTH, (byte[] data) -> {
            return null;
        });
    }

    public Function<byte[], T> onDoubleTap(Sides side) {
        return (byte[] data) -> {
            return null;
        };
    }
    
    public Function<byte[], T> onSingleTap(Sides side) {
        return (byte[] data) -> {
            return null;
        };
    }

    public Function<byte[], T> onTripleTap(Sides side) {
        return (byte[] data) -> {
            return null;
        };
    }

    public Function<byte[], T> onLongPress(Sides side) {
        return (byte[] data) -> {
            return null;
        };
    }
    


}
