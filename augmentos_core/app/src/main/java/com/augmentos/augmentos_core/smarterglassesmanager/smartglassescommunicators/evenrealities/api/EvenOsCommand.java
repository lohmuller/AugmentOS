package com.augmentos.augmentos_core.smarterglassesmanager.smartglassescommunicators.evenrealities.api;

import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

public class EvenOsCommand<T> {

    public final byte[][] requestPackets;
    public final byte[] responseHeader;
    public final Sides sides;
    public final Function<byte[], T> responseParser;
    public final CompletableFuture<T> future = new CompletableFuture<>();

    public enum Sides {
        LEFT, RIGHT, BOTH, EITHER;
    }

    public EvenOsCommand(byte[][] requestPackets, byte[] responseHeader, Sides sides, Function<byte[], T> responseParser) {
        this.requestPackets = requestPackets;
        this.responseHeader = responseHeader;
        this.sides = sides;
        this.responseParser = responseParser;
    }

    public EvenOsCommand(byte[] singleRequest, byte[] responseHeader, Sides sides, Function<byte[], T> responseParser) {
        this(new byte[][]{ singleRequest }, responseHeader, sides, responseParser);
    }
}