package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection;

public class ConnectionConfig {
    public final UUID uartServiceUuid;
    public final UUID uartTxCharUuid;
    public final UUID uartRxCharUuid;
    public final int mtu;

    public ConnectionConfig(UUID uartServiceUuid, UUID uartTxCharUuid, UUID uartRxCharUuid, int mtu) {
        this.uartServiceUuid = uartServiceUuid;
        this.uartTxCharUuid = uartTxCharUuid;
        this.uartRxCharUuid = uartRxCharUuid;
        this.mtu = mtu;
    }
}