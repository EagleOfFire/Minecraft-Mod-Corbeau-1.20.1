package ros.eagleoffire.roscorbeau.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ros.eagleoffire.roscorbeau.ROSCorbeau;

import java.util.function.Consumer;

public class TargetPlayerNameScreen extends Screen {
    private EditBox textField;
    private Button sendButton;
    private final Consumer<String> callback;
    private static final Component TITLE =
            Component.translatable("gui." + ROSCorbeau.MODID + ".target_Player_screen");

    public TargetPlayerNameScreen(Consumer<String> callback) {
        super(TITLE);
        this.callback = callback;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        textField = new EditBox(font, centerX - 100, centerY - 10, 200, 20, Component.literal("Enter text..."));
        textField.setMaxLength(100);
        textField.setFocused(true);
        addRenderableWidget(textField);

        sendButton = Button.builder(Component.literal("Send"), button -> {
            String text = textField.getValue();
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("You typed: " + text));
            callback.accept(text);
            onClose();
        }).bounds(centerX - 40, centerY + 20, 80, 20).build();

        addRenderableWidget(sendButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        textField.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256){
            onClose();
            return true;
        }

        if(textField.isFocused() && textField.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textField.isFocused() && textField.charTyped(codePoint, modifiers)){
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (textField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
        super.onClose();
    }
}
