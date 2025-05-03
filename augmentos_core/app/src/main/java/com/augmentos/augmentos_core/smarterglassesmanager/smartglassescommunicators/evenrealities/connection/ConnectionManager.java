/**
 * ConnectionManager is responsible for orchestrating communication between the Even Realities smart glasses
 * and the application. It manages the left and right BLE (Bluetooth Low Energy) connections, handles
 * command dispatching, response listening, and provides an optional synchronous interface using futures.
 *
 * This class ensures that commands are sent to the correct device (left/right/both), manages a queue
 * to prevent overlapping commands with conflicting responses, and resolves responses using predefined
 * headers and response parsers.
 *
 * It also supports both asynchronous and synchronous command execution (via `sendCommand` and `sendAndWait`)
 * and delegates all BLE operations to the underlying `Connection` instances.
 *
 * Designed to serve as the main communication bridge for SDK-like integrations with Even Realities G1 (firmware 1.5.0),
 * with support for future enhancements like heartbeat monitoring, retries, or extended device status.
 */

package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import android.content.Context;

import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.EvenOsCommand;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.EvenOsEventListener;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.Sides;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection.ConnectionConfig;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection.Connection;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos.EvenOsBase;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos.connection.CommandQueue;


public class ConnectionManager {

    private final Connection leftConnection;
    private final Connection rightConnection;
    private EvenOsBase evenOsApi;
    private final int maxRetries = 3;

    private final CommandQueue commandQueue = new CommandQueue();
    private final Map<String, EvenOsEventListener> responseListeners = new HashMap<>(); 


    public ConnectionManager(Context context, SmartGlassesDevice smartGlassesDevice) {
        this.context = context;
        this.smartGlassesDevice = smartGlassesDevice;

        UartServiceUuid = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
        uartTxCharUuid = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
        uartRxCharUuid = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
        clientCharacteristicConfigUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        int mtu = 512;
        ConnectionConfig config = new ConnectionConfig(UartServiceUuid, uartTxCharUuid, uartRxCharUuid, clientCharacteristicConfigUuid, mtu);

        this.leftConnection = new Connection(context, smartGlassesDevice.getLeftDevice(), config);
        this.rightConnection = new Connection(context, smartGlassesDevice.getRightDevice(), config);

        this.init();
    }

    public void setEvenOsApi(EvenOsBase evenOsApi) {
        this.evenOsApi = evenOsApi;
    }

    public void init() {
        this.leftConnection.connect();
        this.rightConnection.connect();
        this.leftConnection.setRxDataListener((data) -> onDataReceived(data, "LEFT"));
        this.rightConnection.setRxDataListener((data) -> onDataReceived(data, "RIGHT"));
    }


    public void destroy() {
        this.leftConnection.disconnect();
        this.rightConnection.disconnect();
    }

    private void setupHeartbeat() {
        //@TODO: implement heartbeat response/reply handler?
    }

    /**
     * Envia um comando para o dispositivo e retorna a resposta
     * @param sendCommand
     * @return
     */
    public <T> T sendCommand(EvenOsCommand sendCommand) {
        if (!this.commandQueue.isAvailable(sendCommand)) {
            //@TODO retry logic, create a sleep between retries
            return null;
        }

        for (byte[] packet : sendCommand.requestPackets) {  
            this.commandQueue.add(sendCommand);
        }
        
        if (sendCommand.sides == EvenOsCommand.Sides.LEFT || sendCommand.sides == EvenOsCommand.Sides.BOTH) {
            for (byte[] packet : sendCommand.requestPackets) {  
                this.leftConnection.send(packet);
            }
        }
        if (sendCommand.sides == EvenOsCommand.Sides.RIGHT || sendCommand.sides == EvenOsCommand.Sides.BOTH) {
            for (byte[] packet : sendCommand.requestPackets) {
                this.rightConnection.send(packet);
            }
        }
        return sendCommand.future;
    }

    public <T> T sendAndWait(EvenOsCommand<T> command, long timeoutMillis) throws Exception {
        CompletableFuture<T> future = sendCommand(command);
        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public <T> void setOnResponse(Sides side, EvenOsEventListener<T> listener, BiConsumer<T, Sides> handler) {
        // Remove old entry if it exists
        responseHandlers.removeIf(entry ->
            entry.listener.equals(listener) && entry.side == side
        );

        // Add the new handler
        responseHandlers.add(new ResponseHandler<>(side, listener, handler));
    }
        

    private void onDataReceived(byte[] data, String side) {
        EvenOsCommand<?> matching = commandQueue.findMatching(data, side);
        if (matching != null) {
            try {
                Object result = matching.onDataReceived.apply(data);
                matching.future.complete(result);
            } catch (Exception e) {
                matching.future.completeExceptionally(e);
            }
            commandQueue.remove(matching, side);
        }

        for (Map.Entry<EvenOsEventListener<?>, BiConsumer<?, Sides>> entry : responseListeners.entrySet()) {
            EvenOsEventListener<?> listener = entry.getKey();
            if (listener.side == Sides.BOTH || listener.side == side) {
                if (listener.matches(data, side)) {
                    Object parsed = listener.parse(data, side);
                    @SuppressWarnings("unchecked")
                    BiConsumer<Object, Sides> handler = (BiConsumer<Object, Sides>) entry.getValue();
                    handler.accept(parsed, side);
                    break;
                }
            }
        }

    }
    
}

public abstract class EvenOsEventListener<T> {
    public Sides side = Sides.BOTH; // default
    public abstract boolean matches(byte[] data, Sides side);
    public abstract T parse(byte[] data, Sides side);
}

public class ResponseHandler<T> {
    public final Sides side;
    public final EvenOsEventListener<T> listener;
    public final BiConsumer<T, Sides> handler;

    public ResponseHandler(Sides side, EvenOsEventListener<T> listener, BiConsumer<T, Sides> handler) {
        this.side = side;
        this.listener = listener;
        this.handler = handler;
    }
}