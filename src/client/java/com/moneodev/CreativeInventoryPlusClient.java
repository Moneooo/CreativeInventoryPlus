package com.moneodev;

import com.moneodev.hud.HotbarPreviewRenderer;
import com.moneodev.input.InputHandler;
import net.fabricmc.api.ClientModInitializer;

public class CreativeInventoryPlusClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		InputHandler.init();

		// You could also register your renderer here, or elsewhere
		HotbarPreviewRenderer.init();
	}

}