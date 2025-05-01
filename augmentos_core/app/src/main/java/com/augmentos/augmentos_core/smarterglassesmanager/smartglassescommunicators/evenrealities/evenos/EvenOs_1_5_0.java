import java.util.EnumMap;
import java.util.Map;

public class Even_Os_1_5_0 implements BaseFw {

    public enum DashboardMode {
        FULL(0),
        DUAL(1),
        MINIMAL(2);
        
        private final int value;
        DashboardMode(int value) {this.value = value;}
        public int getValue() {return value;}
    }

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

    private int seq;

    private int getAvailableSeq() {
        if (seq >= 255) {
            seq = 0;
        }
        return seq++;
    }

    public static final Map<CommandId, CommandMeta> COMMANDS;

    static {
        COMMANDS = CommandInterface.createEmptyCommandMap();

        COMMANDS.put(CommandId.SET_BRIGHTNESS,
            new CommandMeta(new byte[]{0x01}, PrefSide.BOTH));

        COMMANDS.put(CommandId.SET_SILENT_MODE,
            new CommandMeta(new byte[]{0x03}, PrefSide.BOTH));

        COMMANDS.put(CommandId.SET_NOTIFICATION_CONFIG,
            new CommandMeta(new byte[]{0x04}, PrefSide.BOTH));

        COMMANDS.put(CommandId.SET_DASHBOARD_MODE,
            new CommandMeta(new byte[]{0x06}, PrefSide.BOTH));

        COMMANDS.put(CommandId.MIC_ENABLE,
            new CommandMeta(new byte[]{0x0E}, PrefSide.BOTH));

        COMMANDS.put(CommandId.BMP_DISPLAY,
            new CommandMeta(new byte[]{0x15}, PrefSide.BOTH));

        COMMANDS.put(CommandId.BMP_CRC,
            new CommandMeta(new byte[]{0x16}, PrefSide.BOTH));

        COMMANDS.put(CommandId.CLEAR_SCREEN,
            new CommandMeta(new byte[]{0x18}, PrefSide.BOTH));

        COMMANDS.put(CommandId.QUICK_NOTE,
            new CommandMeta(new byte[]{0x1E}, PrefSide.BOTH));

        COMMANDS.put(CommandId.FIRMWARE_INFO,
            new CommandMeta(
                new byte[]{0x23}, // REQ
                new byte[]{0x6E, 0x65, 0x74, 0x20, 0x62, 0x75, 0x69, 0x6C, 0x64}, // "net build"
                PrefSide.DEVICE
            ));

        COMMANDS.put(CommandId.HEARTBEAT,
            new CommandMeta(new byte[]{0x25}, PrefSide.BOTH));

        COMMANDS.put(CommandId.WEAR_DETECTION,
            new CommandMeta(new byte[]{0x27}, PrefSide.BOTH));

        COMMANDS.put(CommandId.BATTERY_INFO,
            new CommandMeta(new byte[]{0x2C}, PrefSide.BOTH));

        COMMANDS.put(CommandId.UPTIME,
            new CommandMeta(new byte[]{0x37}, PrefSide.BOTH));

        COMMANDS.put(CommandId.USAGE_INFO,
            new CommandMeta(new byte[]{0x3E}, PrefSide.BOTH));

        COMMANDS.put(CommandId.DISPLAY_NOTIFICATION,
            new CommandMeta(new byte[]{0x4B}, PrefSide.BOTH));

        COMMANDS.put(CommandId.INIT,
            new CommandMeta(new byte[]{0x4D}, PrefSide.BOTH));

        COMMANDS.put(CommandId.TEXT_COMMMAND,
            new CommandMeta(new byte[]{0x4E}, PrefSide.BOTH));

        COMMANDS.put(CommandId.AUDIO_STREAM,
            new CommandMeta(new byte[]{(byte) 0xF1}, PrefSide.RIGHT));

        COMMANDS.put(CommandId.STATES_CHANGE,
            new CommandMeta(new byte[]{(byte) 0xF5}, PrefSide.BOTH));
    }

    /**
     * Set brightness
     * @param level (0-100)
     * @param auto (true/false)
     */
    public byte setBrightness(int level, boolean auto) {

        int fallbackLevel = 30;
        int safeLevel = (level >= 0 && level <= 100) ? level : fallbackLevel;

        // Scale the level to 0-63
        int scaledLevel = (safeLevel * 63) / 100;

        // Validate brightness rang
        return new byte[] {
            (byte) 0x23,
            (byte) scaledLevel,
            (byte)(auto ? 1 : 0)
        };
    }

    /**
     * Set silent mode
     * @param silent (true/false)
     */
    public byte setSilentMode(boolean silent) {
        return new byte[] {
            (byte) 0x03,
            (byte)(silent ? 1 : 0)
        };
    }   

    /** 
     * Set microphone enabled
     * @param enabled (true/false)
     */
    public byte setMicrophoneEnabled(boolean enabled) {
        return new byte[] {
            (byte) 0x0E,
            (byte)(enabled ? 1 : 0)
        };
    }

    public byte heartbeat(int seq, int nextSeq) {
        return new byte[] {
            (byte) 0x25,
            (byte) this.getAvailableSeq(),
            (byte) 0x04,
            (byte) this.getAvailableSeq()
        };
    }

    public byte clearScreen() {
        return new byte[] {
            (byte) 0x18
        };
    }

    public byte initialize() {
        return new byte[] {
            (byte) 0x4D,
            (byte) 0xFB // TODO: check if can change to another value
        };
    }


    /**
     * Set head up angle
     * @param angle (0-60)
     * @param unknown (1 or 0)
     */
    public byte setHeadUpAngle(int angle, int unknown) {

        // Validate angle range (0 ~ 60)
        int clamped = Math.max(0, Math.min(angle, 60));
        return new byte[] {
            (byte) 0x0B,
            (byte) clamped,
            (byte) 0x01 //@TODO to use the  unknown?
        };
    }
    
    
    /**
     * Set notification config  
     * @param jsonData (json data)
     * @return chunks (byte[][] array of chunks) Multiple sends
     */
    public byte[][] setNotificationConfig(String jsonData) {
        
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
        
        return chunks;
    }

    /**
     * Set dashboard mode
     * @param mode DashboardMode enum value
     * @param subMode DashboardSubMode enum value
     */
    public byte[] setDashboardMode(DashboardMode mode, DashboardSubMode subMode) {
        if (mode == DashboardMode.MINIMAL && subMode != DashboardSubMode.NOTES) {
            throw new IllegalArgumentException("SubMode not supported for MINIMAL mode");
        }
        return new byte[] {
            (byte) 0x06,
            (byte) 0x07,
            (byte) this.getAvailableSeq(), //seq ?
            (byte) 0x00, //seq ?
            (byte) 0x06, // API?
            (byte) mode.getValue(),
            (byte) subMode.getValue()
        };
    }


    public byte[][] textPackets(String text) {
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
            buffer.put((byte) this.getAvailableSeq());         // Sequence Number
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

        return packets;
    }

 
    public static byte[][] transferBmp(byte[] bmpData) {
        
        int PACKET_SIZE = 194;
        byte[] ADDRESS_HEADER = new byte[]{0x00, 0x1C, 0x00, 0x00}; 
        int totalChunks = (int) Math.ceil((double) bmpData.length / PACKET_SIZE);

        byte[][] result = new byte[totalChunks][];

        for (int i = 0; i < totalChunks; i++) {
            int start = i * PACKET_SIZE;
            int end = Math.min(start + PACKET_SIZE, bmpData.length);
            byte[] chunk = Arrays.copyOfRange(bmpData, start, end);

            ByteBuffer buffer;
            if (i == 0) {
                buffer = ByteBuffer.allocate(2 + ADDRESS_HEADER.length + chunk.length); //create buffer
                buffer.put((byte) 0x15);    //commandID
                buffer.put((byte) this.getAvailableSeq());       //seq
                buffer.put(ADDRESS_HEADER); //address header

            } else {
                buffer = ByteBuffer.allocate(2 + chunk.length); //create buffer
                buffer.put((byte) 0x15); //commandID
                buffer.put((byte) this.getAvailableSeq());   //seq
            }

            buffer.put(chunk);
            result[i] = buffer.array();
        }

        return result;
    }

    public static byte[][] endTransferBmp(byte[] bmpData) {
        byte[][] result = new byte[2][];

        // Comando de fim
        result[0] = new byte[]{0x20, 0x0D, 0x0E};

        // CRC
        byte[] withAddress = new byte[ADDRESS_HEADER.length + bmpData.length];
        System.arraycopy(ADDRESS_HEADER, 0, withAddress, 0, ADDRESS_HEADER.length);
        System.arraycopy(bmpData, 0, withAddress, ADDRESS_HEADER.length, bmpData.length);

        // Calculate CRC
        CRC32 crc32 = new CRC32(); // Substitua por CRC32-XZ se necessário
        crc32.update(withAddress);
        int crc = (int) crc32.getValue();

        result[1] = new byte[]{
            0x16,
            (byte) ((crc >> 24) & 0xFF),
            (byte) ((crc >> 16) & 0xFF),
            (byte) ((crc >> 8) & 0xFF),
            (byte) (crc & 0xFF)
        };

        return result;
    }

    
}
