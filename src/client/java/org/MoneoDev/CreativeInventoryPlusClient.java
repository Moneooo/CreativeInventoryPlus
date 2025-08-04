package org.MoneoDev;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemGroup;
import org.MoneoDev.config.HotbarMode;
import org.MoneoDev.hud.HotbarPreviewRenderer;
import org.MoneoDev.input.InputHandler;
import net.fabricmc.api.ClientModInitializer;

public class CreativeInventoryPlusClient implements ClientModInitializer {
    private static HotbarMode currentMode = HotbarMode.COPY;

    public static final ItemGroup OPERATOR_TAB = FabricItemGroup.builder()
            .displayName(Text.literal("Operator Items"))
            .icon(() -> new ItemStack(Items.COMMAND_BLOCK))
            .entries((displayContext, entries) -> {
                entries.add(new ItemStack(Items.COMMAND_BLOCK));
                entries.add(new ItemStack(Items.STRUCTURE_BLOCK));
                entries.add(new ItemStack(Items.BARRIER));
                entries.add(new ItemStack(Items.JIGSAW));
                entries.add(new ItemStack(Items.LIGHT));
            })
            .build();


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