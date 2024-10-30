package ros.eagleoffire.roscorbeau.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ros.eagleoffire.roscorbeau.ROSCorbeau;

public class ParcheminScreen extends Screen {

    public static final ResourceLocation PARCHEMIN_SCREEN =
            new ResourceLocation(ROSCorbeau.MODID, "textures/screen/parchemin_screen.png");
    
    public ParcheminScreen() {
        super(Component.literal("Parchemin"));
    }

    @Override
    protected void init() {
        // Add buttons or elements if needed
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Render background and screen elements
        super.render(graphics, mouseX, mouseY, partialTicks);

        // Bind and render the custom image
        RenderSystem.setShaderTexture(0, PARCHEMIN_SCREEN);
        int x = (this.width - 256) / 2;  // Adjust position to center or place as needed
        int y = (this.height - 256) / 2; // Adjust position as needed
        graphics.blit(PARCHEMIN_SCREEN, x, y, 0, 0, 256, 256, 256, 256); // Render the image with width/height 256x256

    }
}
