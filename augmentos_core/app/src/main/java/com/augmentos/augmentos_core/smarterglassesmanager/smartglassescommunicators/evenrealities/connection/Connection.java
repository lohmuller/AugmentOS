import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public class Connection {

    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic txChar;
    private BluetoothGattCharacteristic rxChar;
    private OnRxDataListener rxDataListener;
    private boolean isInitialized = false;

    private Context context;
    private BluetoothDevice device;
    private UUID uartServiceUuid;
    private UUID uartTxCharUuid;
    private UUID uartRxCharUuid;
    private int mtu;

    private static final String TAG = "BLE";
    

    /**
     * Internal callback for the BluetoothGatt
     */
    private final BluetoothGattCallback internalCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(mtu);
            } else {
                Connection.this.isInitialized = false;
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices(); 
            } else {
                Log.e(TAG, "Error while negotiating MTU (status=" + status + ")");
                gatt.disconnect();
                gatt.close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                new Handler(Looper.getMainLooper()).post(() -> init(gatt));               
            }
        }

        @Override   
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (rxChar != null && characteristic.getUuid().equals(rxChar.getUuid())) {
                byte[] data = characteristic.getValue();
                if (rxDataListener != null) {
                    rxDataListener.onDataReceived(data);
                }
            }
        }
    };

    public Connection(@NonNull Context context, @NonNull BluetoothDevice device, @NonNull BleConfig config) {
        this.context = context.getApplicationContext();
        this.device = device;
        this.gatt = device.connectGatt(context, false, internalCallback);
        this.uartServiceUuid = config.uartServiceUuid;
        this.uartTxCharUuid = config.uartTxCharUuid;
        this.uartRxCharUuid = config.uartRxCharUuid;
        this.mtu = config.mtu;
    }

    public boolean isConnected() {
        if (this.context == null || this.gatt == null) return false;

        BluetoothManager manager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        int state = manager.getConnectionState(this.gatt.getDevice(), BluetoothProfile.GATT);
        return state == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isInitialized() {
        return isInitialized && isConnected();
    }

    /**
     * Initialize the connection
     */
    private void init() {

        BluetoothGattService uartService = gatt.getService(uartServiceUuid);
        if (uartService == null) {
            throw new BleInitializationException("UART service not found (UUID: " + uartServiceUuid + ")");
        }

        this.txChar = uartService.getCharacteristic(uartTxCharUuid);
        this.rxChar = uartService.getCharacteristic(uartRxCharUuid);

        if (this.txChar == null) {
            throw new BleInitializationException("TX characteristic not found (UUID: " + uartTxCharUuid + ")");
        }

        if (this.rxChar == null) {
            throw new BleInitializationException("RX characteristic not found (UUID: " + uartRxCharUuid + ")");
        }
        
        enableRxNotification();
        isInitialized = true;
        Log.d(TAG, "Initialization completed successfully");
    }

    public void reconnect() {
        if (device == null || context == null) {
            Log.w(TAG, "Not possible to reconnect: context or device are null");
            return;
        }

        if (gatt != null) {
            gatt.close();
        }
        gatt = device.connectGatt(context, false, internalCallback);
    }

    public void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
            txChar = null;
            rxChar = null;
            isInitialized = false;
        }
    } 

    /**
     * Send data to the glasses
     * @param data
     * @return
     */
    public boolean send(byte[] data) {
        if (txChar == null || gatt == null) return false;
        txChar.setValue(data);
        return gatt.writeCharacteristic(txChar);
    }

    /**
     * Set the listener for the RX data, so the app can receive the data from the glasses
     * @param listener
     */
    public void setOnRxDataListener(OnRxDataListener listener) {
        this.rxDataListener = listener;
    }

    /**
     * Enable the RX notification (response from the glasses)
     */
    private void enableRxNotification() {
        gatt.setCharacteristicNotification(rxChar, true);

        BluetoothGattDescriptor descriptor = rxChar.getDescriptor(clientCharacteristicConfigUuid);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    public interface OnRxDataListener {
        void onDataReceived(byte[] data);
    }
}

public class BleInitializationException extends RuntimeException {
    public BleInitializationException(String message) {
        super(message);
    }
}

public class BleConfig {
    public final UUID uartServiceUuid;
    public final UUID uartTxCharUuid;
    public final UUID uartRxCharUuid;
    public final int mtu;

    public BleConfig(UUID uartServiceUuid, UUID uartTxCharUuid, UUID uartRxCharUuid, int mtu) {
        this.uartServiceUuid = uartServiceUuid;
        this.uartTxCharUuid = uartTxCharUuid;
        this.uartRxCharUuid = uartRxCharUuid;
        this.mtu = mtu;
    }
}