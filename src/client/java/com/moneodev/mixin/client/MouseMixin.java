package com.moneodev.mixin.client;

import com.moneodev.hud.HotbarPreviewRenderer;
import com.moneodev.input.InputHandler;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (vertical != 0 || horizontal != 0) {
			if (InputHandler.previewKeyPressed) {
				HotbarPreviewRenderer.changeSelectedHotbar((int) Math.signum(vertical));
				ci.cancel();
			}
		}
	}
}
