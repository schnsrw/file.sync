package in.lazygod.websocket.handlers;

public class HandlerInitializer {

    public static void registerAll() {
        FeatureHandler.register();
        PingPongHandler.register();
        // add more as you create them
    }
}
