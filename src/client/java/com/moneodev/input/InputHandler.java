package com.moneodev.input;

import com.moneodev.hud.HotbarPreviewRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class InputHandler {
    private static KeyBinding previewKey;
    private static KeyBinding saveKey;
    private static boolean wasPreviewKeyPressed = false;
    private static boolean wasSaveKeyPressed = false;
    public static boolean previewKeyPressed = false;

    public static void init() {
        previewKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.creativeinventoryplus.preview",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,  // Default key: Left Alt
                "category.creativeinventoryplus.main"
        ));

        saveKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.creativeinventoryplus.saveHotbar",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.creativeinventoryplus.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> InputHandler.handleKeybindTick());
    }

    public static void handleKeybindTick() {
        boolean isPreviewKeyPressedNow = previewKey.isPressed();
        boolean isSaveKeyPressedNow = saveKey.isPressed();

        if (isPreviewKeyPressedNow && !wasPreviewKeyPressed) {
            onPreviewKeyPressed();
        } else if (!isPreviewKeyPressedNow && wasPreviewKeyPressed) {
            onPreviewKeyReleased();
        }

        if (isSaveKeyPressedNow && !wasSaveKeyPressed) {
            onSaveKeyPressed();
        }

        wasPreviewKeyPressed = isPreviewKeyPressedNow;
        wasSaveKeyPressed = isSaveKeyPressedNow;

    }

    public static void onPreviewKeyPressed() {
        HotbarPreviewRenderer.startHotbarPreview();
        previewKeyPressed = true;
    }

    public static void onPreviewKeyReleased() {
        previewKeyPressed = false;
        HotbarPreviewRenderer.endHotbarPreview();
    }

    public static void onSaveKeyPressed() {
        HotbarPreviewRenderer.saveHotbar();
    }

    public static String getKeyName(String key) {
        if (Objects.equals(key, "preview")) {
            return previewKey.getBoundKeyLocalizedText().getString();
        }
        else if (Objects.equals(key, "save")) {
            return saveKey.getBoundKeyLocalizedText().getString();
        }
        return "";
    }
}
