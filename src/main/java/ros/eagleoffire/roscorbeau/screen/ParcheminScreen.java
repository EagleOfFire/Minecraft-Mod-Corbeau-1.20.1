package ros.eagleoffire.roscorbeau.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import ros.eagleoffire.roscorbeau.item.custom.ParcheminItem;

public class ParcheminScreen extends Screen {

    public static final ResourceLocation PARCHEMIN_SCREEN =
            new ResourceLocation(ROSCorbeau.MODID, "textures/screen/parchemin_screen.png");
    private MultiLineEditBox textBox;
    private final int MAX_TEXT_LENGTH = 200; // Limit text length as needed

    public ParcheminScreen() {
        super(Component.literal("Parchemin"));
    }

    @Override
    protected void init() {
        // Initialize the EditBox for text input
        int textBoxWidth = 200;
        int textBoxHeight = 20;
        int x = (this.width - textBoxWidth) / 2;
        int y = this.height / 2 + 30; // Position the text box below the image

        this.textBox = new MultiLineEditBox(this.font, textBoxWidth, textBoxHeight, x, y, Component.literal("Write here..."),Component.literal("Write here too..."));
        this.textBox.setCharacterLimit(MAX_TEXT_LENGTH);
        this.textBox.setValue("Write here..."); // Default placeholder
        this.textBox.setFocused(true);

        this.addRenderableWidget(this.textBox);

        // Calculate the position for the button near the bottom of the screen
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = this.height - buttonHeight - 10;  // 10 pixels from the bottom

        this.addRenderableWidget(
                Button.builder(Component.literal("Save"), button -> onSave())
                        .pos(buttonX, buttonY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );

        // Load saved text from the item’s NBT data, now in init() where Minecraft instance is available
        if (this.minecraft != null) {
            var player = this.minecraft.player;
            if (player != null) {
                var heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof ParcheminItem) {
                    var nbt = heldItem.getTag();
                    if (nbt != null && nbt.contains("ParcheminText")) {
                        String savedText = nbt.getString("ParcheminText");
                        this.textBox.setValue(savedText);  // Load saved text into the EditBox
                    }
                }
            }
        }
    }

    private void onSave() {
        String textContent = this.textBox.getValue();  // Get the text content from the EditBox

        Player player = this.minecraft.player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ParcheminItem) {  // Ensure the item is a ParcheminItem
                CompoundTag nbt = heldItem.getOrCreateTag();
                nbt.putString("ParcheminText", textContent);  // Save the text under a custom key, e.g., "ParcheminText"
            }
        }
        this.onClose();  // Close the screen after saving
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        RenderSystem.setShaderTexture(0, PARCHEMIN_SCREEN);
        int x = (this.width - 256) / 2;  // Adjust position to center or place as needed
        int y = (this.height - 256) / 2; // Adjust position as needed
        graphics.blit(PARCHEMIN_SCREEN, x, y, 0, 0, 256, 256, 256, 256); // Render the image with width/height 256x256

        super.render(graphics, mouseX, mouseY, partialTicks);

        this.textBox.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow the EditBox to process key input
        if (this.textBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Allow the EditBox to process character input
        if (this.textBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
