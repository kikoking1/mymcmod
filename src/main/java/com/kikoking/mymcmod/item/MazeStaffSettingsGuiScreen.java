package com.kikoking.mymcmod.item;

import com.kikoking.mymcmod.MyMcMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MazeStaffSettingsGuiScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("gui." + MyMcMod.MOD_ID + ".maze_staff_screen.title");
    private static final Component MAZE_EDIT_BOX_TEXT =
            Component.translatable("gui." + MyMcMod.MOD_ID + ".maze_staff_screen.edit_box.maze_height");
    private static final Component CREATE_MAZE =
            Component.translatable("gui." + MyMcMod.MOD_ID + ".maze_staff_screen.button_label.create_maze");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MyMcMod.MOD_ID, "textures/gui/background.png");
    private final int imageWidth, imageHeight;
    private int leftPos, topPos;
    private Button createMazeButton;
    private EditBox mazeNoOfFloorsEditBox;
    private EditBox mazeWidthEditBox;
    private MazeStaff mazeStaff;
    private Level world;
    private Player player;

    protected MazeStaffSettingsGuiScreen(MazeStaff mazeStaff, Level world, Player player) {
        super(TITLE);

        this.mazeStaff = mazeStaff;
        this.world = world;
        this.player = player;

        this.imageWidth = 256;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if(this.minecraft == null) return;
        Level level = this.minecraft.level;
        if(level == null) return;

        this.createMazeButton = addRenderableWidget(
                Button.builder(CREATE_MAZE, this::handleButton)
                    .bounds(this.leftPos + 8, this.topPos + 115, 80, 20)
                    .tooltip(Tooltip.create(CREATE_MAZE))
                    .build());

        this.mazeNoOfFloorsEditBox = addRenderableWidget(new EditBox(
                this.font, this.leftPos + 8,
                this.topPos + 40, 80, 20, MAZE_EDIT_BOX_TEXT));

        this.mazeNoOfFloorsEditBox.setValue(String.valueOf(this.mazeStaff.mazeNoOfFloors));

        this.mazeWidthEditBox = addRenderableWidget(new EditBox(
                this.font, this.leftPos + 8,
                this.topPos + 82, 80, 20, MAZE_EDIT_BOX_TEXT));

        this.mazeWidthEditBox.setValue(String.valueOf(this.mazeStaff.mazeWidth));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawString(this.font,
                TITLE,
                this.leftPos + 8,
                this.topPos + 8,
                0x404040,
                false);

        graphics.drawString(this.font,
                "# of Floors: ",
                this.leftPos + 8,
                this.topPos + 30,
                0x404040,
                false);

        graphics.drawString(this.font,
                "Maze Size (positive int divisble by 4): ",
                this.leftPos + 8,
                this.topPos + 70,
                0x404040,
                false);
    }

    private void handleButton(Button button) {
        this.mazeStaff.mazeWidth = Integer.parseInt(this.mazeWidthEditBox.getValue());
        this.mazeStaff.mazeNoOfFloors = Integer.parseInt(this.mazeNoOfFloorsEditBox.getValue());

        this.mazeStaff.createMaze(this.world, this.player);
        this.minecraft.popGuiLayer();
    }
}