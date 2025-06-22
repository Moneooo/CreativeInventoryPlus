package org.MoneoDev;

import org.MoneoDev.config.HotbarMode;
import org.MoneoDev.hud.HotbarPreviewRenderer;
import org.MoneoDev.input.InputHandler;
import net.fabricmc.api.ClientModInitializer;

public class CreativeInventoryPlusClient implements ClientModInitializer {
    private static HotbarMode currentMode = HotbarMode.COPY;

    public static HotbarMode getCurrentMode() {
        return currentMode;
    }

    @Override
    public void onInitializeClient() {
        InputHandler.init();

        // You could also register your renderer here, or elsewhere
        HotbarPreviewRenderer.init();
    }

}