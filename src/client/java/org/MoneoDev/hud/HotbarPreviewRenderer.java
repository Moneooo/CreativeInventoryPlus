package org.MoneoDev.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gl.RenderPipelines;
import org.MoneoDev.CreativeInventoryPlusClient;
import org.MoneoDev.config.HotbarMode;
import org.MoneoDev.input.InputHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
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
    private static int activeHotbar = 0; // Only used in switch mode

    private static HotbarStorage storage;
    public static boolean shouldRender = false;

    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HOTBAR_SLOTS = 9;

    private static float previewAlpha = 0f;

    public static void init() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.of("creativeinventoryplus", "hotbar_preview_renderer"),
                HotbarPreviewRenderer::renderHotbarPreview
                );
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

        if (CreativeInventoryPlusClient.getCurrentMode() == HotbarMode.COPY)
            renderHotbarPreviewCopy(drawContext, baseX, baseY, color);
        else
            renderHotbarPreviewSwitch(drawContext, baseX, baseY, color);


        renderFeedback(drawContext, screenWidth);
    }

    private static void renderHotbarPreviewCopy(DrawContext drawContext, int baseX, int baseY, int color) {
        for (int row = 0; row < HOTBAR_SLOTS; row++) {
            boolean isSelected = row == selectedHotbarRow;

            Identifier texture = isSelected ? HOTBAR_SELECTED_TEXTURE : HOTBAR_TEXTURE;
            int y = baseY - row * 22;

            // Render hotbar
            drawContext.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    baseX, y,
                    0, 0,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    color
            );

            renderItems(drawContext, row, baseX, y);
        }
    }

    private static void renderHotbarPreviewSwitch(DrawContext drawContext, int baseX, int baseY, int color) {
        int skipActive = 0;
        for (int row = 0; row < HOTBAR_SLOTS; row++) {
            boolean isSelected = row == selectedHotbarRow;
            boolean isActive = row == activeHotbar;
            // Don't draw the active hotbar because that's the main one in game right now
            if (isActive) {
                skipActive = 22;
                continue;
            }

            Identifier texture = isSelected ? HOTBAR_SELECTED_TEXTURE : HOTBAR_TEXTURE;

            int y = baseY - row * 22 - skipActive;

            // Render hotbar
            drawContext.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    baseX, y,
                    0, 0,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    HOTBAR_WIDTH, HOTBAR_HEIGHT,
                    color
            );

            renderItems(drawContext, row, baseX, y);
        }
    }

    private static void renderItems(DrawContext drawContext, int row, int baseX, int y) {
        List<ItemStack> itemStacks = getCreativeHotbar(storage, row);

        for (int slot = 0; slot < itemStacks.size(); slot++) {
            ItemStack stack = itemStacks.get(slot);

            int slotX = baseX + slot * 20 + 3;
            int slotY = y + 3;

            drawContext.drawItem(stack, slotX, slotY);
        }
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
        if (CreativeInventoryPlusClient.getCurrentMode() == HotbarMode.COPY)
            selectedHotbarRow = -1;
        shouldRender = true;
    }
    public static void endHotbarPreview() {
        if (selectedHotbarRow == -1) return;
        if (client.player == null || !client.player.getAbilities().creativeMode) return;

        if (CreativeInventoryPlusClient.getCurrentMode() == HotbarMode.COPY)
            copyHotbar(selectedHotbarRow);

        activeHotbar = selectedHotbarRow;
        shouldRender = false;
    }

    private static void copyHotbar(int row) {
        List<ItemStack> items = getCreativeHotbar(storage, selectedHotbarRow);

        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            ItemStack itemStack2 = itemStack.isItemEnabled(client.player.getWorld().getEnabledFeatures()) ? itemStack.copy() : ItemStack.EMPTY;
            client.player.getInventory().setStack(i, itemStack2);
            assert client.interactionManager != null;
            client.interactionManager.clickCreativeStack(itemStack2, 36 + i);
        }
        client.player.playerScreenHandler.sendContentUpdates();
    }

    public static void saveHotbar() {
        HotbarStorageEntry entry = storage.getSavedHotbar(selectedHotbarRow);
        assert client.world != null;
        assert client.player != null;
        entry.serialize(client.player.getInventory(), client.world.getRegistryManager());
    }
}