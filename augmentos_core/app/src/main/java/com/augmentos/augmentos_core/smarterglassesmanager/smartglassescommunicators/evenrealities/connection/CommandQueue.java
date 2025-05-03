/**
 * CommandQueue is responsible for managing pending commands for the Even Realities device,
 * maintaining separate queues for LEFT and RIGHT connections.
 *
 * It ensures that only one command per side is active at a time, preventing overlapping
 * commands with similar response headers, which could cause unexpected behavior during
 * response parsing.
 *
 * The main goal is to avoid command collisions by validating whether a command can safely
 * be added to the queue (`isAvailable`) before sending it, based on its expected response signature.
 */

package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.connection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api.EvenOsCommand;

public class CommandQueue {

    private final List<EvenOsCommand> leftQueue = new CopyOnWriteArrayList<>();
    private final List<EvenOsCommand> rightQueue = new CopyOnWriteArrayList<>();

    public void add(EvenOsCommand command) {
        if (command.sides == EvenOsCommand.Sides.LEFT || command.sides == EvenOsCommand.Sides.BOTH) {
            leftQueue.add(command);
        }
        if (command.sides == EvenOsCommand.Sides.RIGHT || command.sides == EvenOsCommand.Sides.BOTH) {
            rightQueue.add(command);
        }
    }

    public void remove(EvenOsCommand command, String side) {
        if (side.equals("LEFT") || side.equals("BOTH")) {
            leftQueue.removeIf(entry -> entry == command);
        }
        if (side.equals("RIGHT") || side.equals("BOTH")) {
            rightQueue.removeIf(entry -> entry == command);
        }
    }

    public boolean isAvailable(EvenOsCommand command) {
        if (command.sides == EvenOsCommand.Sides.LEFT || command.sides == EvenOsCommand.Sides.BOTH) {
            for (EvenOsCommand leftQueueCommand : leftQueue) {
                if (hasByteConflict(command.responseHeader, leftQueueCommand.responseHeader)) {
                    return false;
                }
            }
        }
        if (command.sides == EvenOsCommand.Sides.RIGHT || command.sides == EvenOsCommand.Sides.BOTH) {
            for (EvenOsCommand rightQueueCommand : rightQueue) {
                if (hasByteConflict(command.responseHeader, rightQueueCommand.responseHeader)) {
                    return false;
                }
            }
        }
        return true; 
    }

    private boolean hasByteConflict(byte[] a, byte[] b) {
        int minLen = Math.min(a.length, b.length);
        for (int i = 0; i < minLen; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    
}
