package ros.eagleoffire.roscorbeau.screen;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ros.eagleoffire.roscorbeau.ROSCorbeau;
import ros.eagleoffire.roscorbeau.item.custom.ParcheminScelleItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@OnlyIn(Dist.CLIENT)
public class ParcheminScelleScreen extends Screen {
    public static final ResourceLocation PARCHEMIN_SCREEN =
            new ResourceLocation(ROSCorbeau.MODID, "textures/screen/parchemin_screen.png");

    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    public static final ParcheminScelleScreen.BookAccess EMPTY_ACCESS = new ParcheminScelleScreen.BookAccess() {
        public int getPageCount() {
            return 0;
        }

        public FormattedText getPageRaw(int p_98306_) {
            return FormattedText.EMPTY;
        }
    };
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
    protected static final int TEXT_WIDTH = 114;
    protected static final int TEXT_HEIGHT = 128;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private ParcheminScelleScreen.BookAccess bookAccess;
    private int currentPage;
    private List<FormattedCharSequence> cachedPageComponents;
    private int cachedPage;
    private Component pageMsg;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public ParcheminScelleScreen(ParcheminScelleScreen.BookAccess p_98264_) {
        this(p_98264_, true);
    }

    public ParcheminScelleScreen() {
        this(EMPTY_ACCESS, false);
    }

    private ParcheminScelleScreen(ParcheminScelleScreen.BookAccess bookAccess, boolean shouldPlayTurnSound) {
        super(GameNarrator.NO_TITLE);
        this.cachedPageComponents = Collections.emptyList();
        this.cachedPage = -1;
        this.pageMsg = CommonComponents.EMPTY;
        this.bookAccess = bookAccess;
        this.playTurnSound = shouldPlayTurnSound;
    }


    public void setBookAccess(ParcheminScelleScreen.BookAccess bookAccess) {
        this.bookAccess = bookAccess;
        this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }


    public boolean setPage(int pageIndex) {
        int clampedPageIndex = Mth.clamp(pageIndex, 0, this.bookAccess.getPageCount() - 1);
        if (clampedPageIndex != this.currentPage) {
            this.currentPage = clampedPageIndex;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        } else {
            return false;
        }
    }

    protected boolean forcePage(int pageIndex) {
        return this.setPage(pageIndex);
    }


    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, (clickedButton) -> {
                    this.onClose();
                }).bounds(this.width / 2 - 100, 225, 200, 20).build()
        );
    }


    protected void createPageControlButtons() {
        int xOffset = (this.width - 192) / 2;

        this.forwardButton = (PageButton) this.addRenderableWidget(
                new PageButton(xOffset + 116, 159, true, (button) -> {
                    this.pageForward();
                }, this.playTurnSound)
        );

        this.backButton = (PageButton) this.addRenderableWidget(
                new PageButton(xOffset + 43, 159, false, (button) -> {
                    this.pageBack();
                }, this.playTurnSound)
        );

        this.updateButtonVisibility();
    }


    private int getNumPages() {
        return this.bookAccess.getPageCount();
    }

    protected void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }

        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }

        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            switch (keyCode) {
                case 266: // Page Up
                    this.backButton.onPress();
                    return true;
                case 267: // Page Down
                    this.forwardButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }


    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int bookX = (this.width - 192) / 2;

        int screenCenterX = (this.width - 256) / 2;
        int screenCenterY = (this.height - 256) / 2;
        guiGraphics.blit(PARCHEMIN_SCREEN, screenCenterX, screenCenterY, 0, 0, 256, 256, 256, 256);

        if (this.cachedPage != this.currentPage) {
            FormattedText pageText = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split(pageText, 114);
        }

        this.cachedPage = this.currentPage;

        int pageIndicatorWidth = this.font.width(this.pageMsg);
        guiGraphics.drawString(this.font, this.pageMsg, bookX - pageIndicatorWidth + 192 - 44, 100, 0, false);

        int maxLines = Math.min(128 / 9, this.cachedPageComponents.size());
        for (int lineIndex = 0; lineIndex < maxLines; ++lineIndex) {
            FormattedCharSequence line = this.cachedPageComponents.get(lineIndex);
            guiGraphics.drawString(this.font, line, bookX + 36, 40 + lineIndex * 9, 0, false);
        }

        Style hoveredStyle = this.getClickedComponentStyleAt((double) mouseX, (double) mouseY);
        if (hoveredStyle != null) {
            guiGraphics.renderComponentHoverEffect(this.font, hoveredStyle, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left-click
            Style clickedStyle = this.getClickedComponentStyleAt(mouseX, mouseY);
            if (clickedStyle != null && this.handleComponentClicked(clickedStyle)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    public boolean handleComponentClicked(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        } else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String pageValue = clickEvent.getValue();

            try {
                int pageIndex = Integer.parseInt(pageValue) - 1;
                return this.forcePage(pageIndex);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            boolean handled = super.handleComponentClicked(style);
            if (handled && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }

            return handled;
        }
    }


    protected void closeScreen() {
        this.minecraft.setScreen((Screen) null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        }

        int relativeX = Mth.floor(mouseX - ((this.width - 192) / 2.0) - 36.0);
        int relativeY = Mth.floor(mouseY - 32.0); // 2.0 + 30.0 from original

        if (relativeX < 0 || relativeY < 0) {
            return null;
        }

        int maxLines = Math.min(128 / 9, this.cachedPageComponents.size());

        if (relativeX > 114 || relativeY >= 9 * maxLines + maxLines) {
            return null;
        }

        int lineIndex = relativeY / 9;
        if (lineIndex < 0 || lineIndex >= this.cachedPageComponents.size()) {
            return null;
        }

        FormattedCharSequence line = this.cachedPageComponents.get(lineIndex);
        return this.minecraft.font.getSplitter().componentStyleAtWidth(line, relativeX);
    }


    static List<String> loadPages(CompoundTag bookTag) {
        ImmutableList.Builder<String> pagesBuilder = ImmutableList.builder();
        Objects.requireNonNull(pagesBuilder);
        loadPages(bookTag, pagesBuilder::add);
        return pagesBuilder.build();
    }

    public static void loadPages(CompoundTag bookTag, Consumer<String> pageConsumer) {
        ListTag pagesList = bookTag.getList("pages", 8).copy();
        IntFunction<String> pageResolver;

        if (Minecraft.getInstance().isTextFilteringEnabled() && bookTag.contains("filtered_pages", 10)) {
            CompoundTag filteredPages = bookTag.getCompound("filtered_pages");
            pageResolver = (pageIndex) -> {
                String pageKey = String.valueOf(pageIndex);
                return filteredPages.contains(pageKey) ? filteredPages.getString(pageKey) : pagesList.getString(pageIndex);
            };
        } else {
            Objects.requireNonNull(pagesList);
            pageResolver = pagesList::getString;
        }

        for (int i = 0; i < pagesList.size(); ++i) {
            pageConsumer.accept(pageResolver.apply(i));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface BookAccess {
        int getPageCount();

        FormattedText getPageRaw(int var1);

        default FormattedText getPage(int p_98311_) {
            return p_98311_ >= 0 && p_98311_ < this.getPageCount() ? this.getPageRaw(p_98311_) : FormattedText.EMPTY;
        }

        static ParcheminScelleScreen.BookAccess fromItem(ItemStack stack) {
            if (stack.getItem() instanceof ParcheminScelleItem) {
                return new WrittenBookAccess(stack);
            } else if (stack.is(Items.WRITTEN_BOOK)) {
                return new WrittenBookAccess(stack);
            } else if (stack.is(Items.WRITABLE_BOOK)) {
                return new WritableBookAccess(stack);
            } else {
                return EMPTY_ACCESS;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WritableBookAccess implements ParcheminScelleScreen.BookAccess {
        private final List<String> pages;

        public WritableBookAccess(ItemStack bookStack) {
            this.pages = readPages(bookStack);
        }

        private static List<String> readPages(ItemStack bookStack) {
            CompoundTag bookTag = bookStack.getTag();
            return (bookTag != null) ? ParcheminScelleScreen.loadPages(bookTag) : ImmutableList.of();
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageRaw(int pageIndex) {
            return FormattedText.of(this.pages.get(pageIndex));
        }
    }


    @OnlyIn(Dist.CLIENT)
    public static class WrittenBookAccess implements ParcheminScelleScreen.BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack bookStack) {
            this.pages = readPages(bookStack);
        }

        private static List<String> readPages(ItemStack bookStack) {
            CompoundTag bookTag = bookStack.getTag();
            return (bookTag != null && ParcheminScelleItem.makeSureTagIsValid(bookTag))
                    ? ParcheminScelleScreen.loadPages(bookTag)
                    : ImmutableList.of(Component.Serializer.toJson(
                    Component.translatable("book.invalid.tag").withStyle(ChatFormatting.DARK_RED)
            ));
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageRaw(int pageIndex) {
            String rawJson = this.pages.get(pageIndex);

            try {
                FormattedText parsedText = Component.Serializer.fromJson(rawJson);
                if (parsedText != null) {
                    return parsedText;
                }
            } catch (Exception ignored) {
                // Ignored: fallback to plain text if JSON parsing fails
            }

            return FormattedText.of(rawJson);
        }

    }
}
