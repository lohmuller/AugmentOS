import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.EvenOsCommand;

public class CommandQueue {

    private final List<EvenOsCommand> leftQueue = new CopyOnWriteArrayList<>();
    private final List<EvenOsCommand> rightQueue = new CopyOnWriteArrayList<>();

    public void add(EvenOsCommand command) {
        EvenOsCommand cmd = command;
        if (cmd.sides == EvenOsCommand.Sides.LEFT || cmd.sides == EvenOsCommand.Sides.BOTH) {
            leftQueue.add(command);
        }
        if (cmd.sides == EvenOsCommand.Sides.RIGHT || cmd.sides == EvenOsCommand.Sides.BOTH) {
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
