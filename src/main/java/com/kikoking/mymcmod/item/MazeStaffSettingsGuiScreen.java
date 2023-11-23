package com.kikoking.mymcmod.item;

import com.kikoking.mymcmod.MyMcMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MazeStaffSettingsGuiScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("gui." + MyMcMod.MOD_ID + ".example_block_screen");
    private static final Component EXAMPLE_BUTTON =
            Component.translatable("gui." + MyMcMod.MOD_ID + ".example_block_screen.button.example_button");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MyMcMod.MOD_ID, "textures/gui/example_block.png");

    private final int imageWidth, imageHeight;

    private int leftPos, topPos;

    private Button button;
    protected MazeStaffSettingsGuiScreen() {
        super(TITLE);

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.button = addRenderableWidget(
                Button.builder(
                                EXAMPLE_BUTTON,
                                this::handleButton)
                        .bounds(this.leftPos + 8, this.topPos + 20, 80, 20)
                        .tooltip(Tooltip.create(EXAMPLE_BUTTON))
                        .build());

    }

    private void handleButton(Button button) {
        System.out.println("BUTTON CLICKED!");
    }

    // Other methods for handling input and rendering if needed
}