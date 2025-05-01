package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.evenos;

import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

public class EvenOsFirmwareFactory {
    // Mapa de firmwares disponíveis
    private static final Map<String, Class<? extends BaseFirmware>> AVAILABLE_FIRMWARES = new HashMap<String, Class<? extends BaseFirmware>>() {{
        put("1.5.0", Firmware_1_5_0.class);
        // Adicione outros firmwares aqui quando disponíveis
        // put("1.4.0", Firmware_1_4_0.class);
        // put("1.3.0", Firmware_1_3_0.class);
    }};

    // Lista de firmwares suportados em ordem de preferência
    private static final List<String> SUPPORTED_FIRMWARES = Arrays.asList(
        "1.5.0" // Versão mais recente primeiro
        // Adicione outras versões quando disponíveis
        // "1.4.0",
        // "1.3.0"
    );

    private static final long FIRMWARE_CHECK_TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;
    private static final String LATEST_FIRMWARE = "1.5.0";

    public static EvenOsFirmware create(Connection leftConnection, Connection rightConnection) {
        AtomicInteger retryCount = new AtomicInteger(0);
        
        while (retryCount.get() < MAX_RETRIES) {
            // Verifica conexão e tenta reconectar se necessário
            if (!leftConnection.isInitialized() || !rightConnection.isInitialized()) {
                retryCount.incrementAndGet();
                Thread.sleep(1000); // delay 1 second
                continue;
            }

            EvenOsFirmware firmware = AVAILABLE_FIRMWARES.get(LATEST_FIRMWARE).newInstance(leftConnection, rightConnection);

            leftConnection.addOnRxDataListener(data -> {
                firmwareVersion = firmware.res.getFirmwareInfo(data);
                if (firmwareVersion) {
                    //TODO: handle firmware version
                }
            });

            rightConnection.addOnRxDataListener(data -> {
                firmwareVersion = firmware.res.getFirmwareInfo(data);
                if (firmwareVersion) {
                    //TODO: handle firmware version
                }
            });

            leftConnection.send(firmware.left.getFirmwareInfo());
            rightConnection.send(firmware.right.getFirmwareInfo());

        throw new IllegalStateException("Failed to detect firmware version after " + MAX_RETRIES + " attempts");
    }

    private static String detectFirmwareVersion(Connection leftConnection, Connection rightConnection) throws Exception {
        // Tenta cada versão de firmware suportada
        for (String version : SUPPORTED_FIRMWARES) {
            CompletableFuture<String> leftVersionFuture = new CompletableFuture<>();
            CompletableFuture<String> rightVersionFuture = new CompletableFuture<>();

            // Configura listeners para receber as respostas
            leftConnection.setOnRxDataListener(data -> {
                String detectedVersion = parseFirmwareVersion(data);
                if (detectedVersion.equals(version)) {
                    leftVersionFuture.complete(detectedVersion);
                }
            });

            rightConnection.setOnRxDataListener(data -> {
                String detectedVersion = parseFirmwareVersion(data);
                if (detectedVersion.equals(version)) {
                    rightVersionFuture.complete(detectedVersion);
                }
            });

            // Envia comando de detecção de versão
            byte[] command = getVersionCommand(version);
            leftConnection.send(command);
            rightConnection.send(command);

            try {
                // Aguarda as respostas com timeout
                String leftVersion = leftVersionFuture.get(FIRMWARE_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                String rightVersion = rightVersionFuture.get(FIRMWARE_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                // Se ambos os lados responderam com a mesma versão, encontramos o firmware correto
                if (leftVersion.equals(rightVersion)) {
                    return leftVersion;
                }
            } catch (Exception e) {
                // Se falhar, continua para a próxima versão
                continue;
            }
        }
        return null;
    }

    private static byte[] getVersionCommand(String version) {
        // TODO: Implementar lógica para retornar o comando correto para cada versão
        // Por enquanto, usando o comando do firmware 1.5.0
        return new byte[] { 0x01, 0x00 };
    }

    private static boolean ensureConnection(Connection connection) {
        if (!connection.isConnected()) {
            connection.reconnect();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return connection.isInitialized();
    }

    private static String parseFirmwareVersion(byte[] data) {
        // TODO: Implementar a lógica real de parsing da versão do firmware
        if (data.length >= 6) {
            return String.format("%d.%d.%d", data[2], data[3], data[4]);
        }
        throw new IllegalArgumentException("Invalid firmware version response");
    }
} 