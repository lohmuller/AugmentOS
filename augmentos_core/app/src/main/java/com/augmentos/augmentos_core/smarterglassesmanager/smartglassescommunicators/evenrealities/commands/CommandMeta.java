import java.util.Arrays;

public class CommandMeta {
    private final byte[] hex;
    private final byte[] hexResponse;
    private final byte[] hexRequest;
    private final PrefSide preferredSide;

    public enum PrefSide {
        LEFT, // Send only to left side
        RIGHT, // Send only to right side
        BOTH,  // Must be send on both sides
        EITHER, //Left or Right is okay
    }

    /**
     * Set hex for both REQ and RES
     */
    public CommandMeta(byte[] hex, PrefSide preferredSide) {
        this.hex = hex;
        this.preferredSide = preferredSide;
    }

    /**
     * Set hex for REQ and RES different
     */
    public CommandMeta(byte[] hexRequest, byte[] hexResponse, PrefSide preferredSide) {
        this.hexRequest = hexRequest;
        this.hexResponse = hexResponse;
        this.preferredSide = preferredSide;
    }
    
    public byte[] getHexRequest() {
        return this.hexRequest != null ? this.hexRequest : this.hex;
    }

    public byte[] getHexResponse() {
        return this.hexResponse != null ? this.hexResponse : this.hex;
    }

    @Override
    public String toString() {
        return "res=" + Arrays.toString(this.getHexRequest()) +
               ", req=" + Arrays.toString(this.getHexResponse()) +
               ", side=" + this.preferredSide;
    }
}
