package com.moneodev.hud;

import com.moneodev.input.InputHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HotbarPreviewRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier HOTBAR_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar.png");
    private static final Identifier HOTBAR_SELECTED_TEXTURE = Identifier.of("creativeinventoryplus", "textures/hotbar_selected.png");
    private static int selectedHotbarRow = 0;
    private static HotbarStorage storage;
    public static boolean shouldRender = false;

    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_SLOTS = 9;

    private static float previewAlpha = 0f;

    public static void init() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.HOTBAR_AND_BARS, HOTBAR_TEXTURE, HotbarPreviewRenderer::renderHotbarPreview));
    }

    private static void renderHotbarPreview(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (client.player == null || !client.player.getAbilities().creativeMode) return;
        if (shouldRender && InputHandler.previewKeyPressed) {
            previewAlpha = Math.min(previewAlpha + 0.2f, 1f);  // Fade in
        } else {
            previewAlpha = Math.max(previewAlpha - 0.2f, 0f);  // Fade out
        }
        if (previewAlpha <= 0f) return;


        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Center bottom like the normal hotbar
        int baseX = (screenWidth - HOTBAR_WIDTH) / 2;
        int baseY = (screenHeight - 45); // Sort of arbitrary amount

        storage = client.getCreativeHotbarStorage();

        int alphaInt = (int)(previewAlpha * 255) << 24;
        int color = alphaInt | 0xFFFFFF;
        for (int row = 0; row < HOTBAR_SLOTS; row++) {
            boolean isSelected = row == selectedHotbarRow;

            Identifier texture = isSelected ? HOTBAR_SELECTED_TEXTURE : HOTBAR_TEXTURE;
            int y = baseY - row * 22;

            // Render hotbar
            drawContext.drawTexture(
                    RenderLayer::getGuiTextured,
                    texture,
                    baseX, y,
                    0, 0,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    color
            );

            // Render items
            List<ItemStack> itemStacks = getCreativeHotbar(storage, row);

            for (int slot = 0; slot < itemStacks.size(); slot++) {
                ItemStack stack = itemStacks.get(slot);

                int slotX = baseX + slot * 20 + 3;
                int slotY = y + 3;

                drawContext.drawItem(stack, slotX, slotY);
            }
        }

        renderFeedback(drawContext, screenWidth);
    }
    private static void renderFeedback(DrawContext drawContext, int screenWidth) {
        String saveKeyName = InputHandler.getKeyName("save");
        String previewKeyName = InputHandler.getKeyName("preview");

        String message = String.format(
                "%s to save | Release %s to switch",
                saveKeyName, previewKeyName
        );

        int textWidth = client.textRenderer.getWidth(message);
        int x = (screenWidth - textWidth) / 2;
        int y = 8;

        drawContext.drawTextWithShadow(client.textRenderer, message, x, y, 0xFFFFFF);
    }


    private static List<ItemStack> getCreativeHotbar(@NotNull HotbarStorage storage, int row) {
        HotbarStorageEntry entry = storage.getSavedHotbar(row);

        assert client.world != null;
        RegistryWrapper.WrapperLookup registries = client.world.getRegistryManager();
        return entry.deserialize(registries);
    }

    /**
     * @param direction
     * Adds direction to selectedHotbarIndex. Negative is to cycle back, positive to cycle forwards
     */
    public static void changeSelectedHotbar(int direction) {

        if (!shouldRender) return;
        if (selectedHotbarRow == -1 && direction == -1) selectedHotbarRow = 0;
        selectedHotbarRow = (selectedHotbarRow + direction + 9) % 9;
    }

    public static void startHotbarPreview() {
        selectedHotbarRow = -1;
        shouldRender = true;
    }
    public static void endHotbarPreview() {
        if (selectedHotbarRow == -1) return;
        if (client.player == null || !client.player.getAbilities().creativeMode) return;

        List<ItemStack> items = getCreativeHotbar(storage, selectedHotbarRow);

        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            ItemStack itemStack2 = itemStack.isItemEnabled(client.player.getWorld().getEnabledFeatures()) ? itemStack.copy() : ItemStack.EMPTY;
            client.player.getInventory().setStack(i, itemStack2);
            assert client.interactionManager != null;
            client.interactionManager.clickCreativeStack(itemStack2, 36 + i);
        }
        client.player.playerScreenHandler.sendContentUpdates();

        shouldRender = false;
    }

    public static void saveHotbar() {
        HotbarStorageEntry entry = storage.getSavedHotbar(selectedHotbarRow);
        assert client.world != null;
        assert client.player != null;
        entry.serialize(client.player.getInventory(), client.world.getRegistryManager());
    }
}