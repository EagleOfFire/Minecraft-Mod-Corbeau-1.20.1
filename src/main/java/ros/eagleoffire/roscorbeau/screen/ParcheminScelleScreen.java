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

    private ParcheminScelleScreen(ParcheminScelleScreen.BookAccess p_98266_, boolean p_98267_) {
        super(GameNarrator.NO_TITLE);
        this.cachedPageComponents = Collections.emptyList();
        this.cachedPage = -1;
        this.pageMsg = CommonComponents.EMPTY;
        this.bookAccess = p_98266_;
        this.playTurnSound = p_98267_;
    }

    public void setBookAccess(ParcheminScelleScreen.BookAccess p_98289_) {
        this.bookAccess = p_98289_;
        this.currentPage = Mth.clamp(this.currentPage, 0, p_98289_.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }

    public boolean setPage(int p_98276_) {
        int $$1 = Mth.clamp(p_98276_, 0, this.bookAccess.getPageCount() - 1);
        if ($$1 != this.currentPage) {
            this.currentPage = $$1;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        } else {
            return false;
        }
    }

    protected boolean forcePage(int p_98295_) {
        return this.setPage(p_98295_);
    }

    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_289629_) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int $$0 = (this.width - 192) / 2;
        boolean $$1 = true;
        this.forwardButton = (PageButton) this.addRenderableWidget(new PageButton($$0 + 116, 159, true, (p_98297_) -> {
            this.pageForward();
        }, this.playTurnSound));
        this.backButton = (PageButton) this.addRenderableWidget(new PageButton($$0 + 43, 159, false, (p_98287_) -> {
            this.pageBack();
        }, this.playTurnSound));
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

    public boolean keyPressed(int p_98278_, int p_98279_, int p_98280_) {
        if (super.keyPressed(p_98278_, p_98279_, p_98280_)) {
            return true;
        } else {
            switch (p_98278_) {
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    public void render(GuiGraphics p_281997_, int p_281262_, int p_283321_, float p_282251_) {
        this.renderBackground(p_281997_);
        int $$4 = (this.width - 192) / 2;
        boolean $$5 = true;

        int screenCenterX = (this.width - 256) / 2;
        int screenCenterY = (this.height - 256) / 2;
        p_281997_.blit(PARCHEMIN_SCREEN, screenCenterX, screenCenterY, 0, 0, 256, 256, 256, 256);


        if (this.cachedPage != this.currentPage) {
            FormattedText $$6 = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split($$6, 114);
            this.pageMsg = Component.translatable("book.pageIndicator", new Object[]{this.currentPage + 1, Math.max(this.getNumPages(), 1)});
        }

        this.cachedPage = this.currentPage;
        int $$7 = this.font.width(this.pageMsg);
        p_281997_.drawString(this.font, this.pageMsg, $$4 - $$7 + 192 - 44, 18, 0, false);
        Objects.requireNonNull(this.font);
        int $$8 = Math.min(128 / 9, this.cachedPageComponents.size());

        for (int $$9 = 0; $$9 < $$8; ++$$9) {
            FormattedCharSequence $$10 = (FormattedCharSequence) this.cachedPageComponents.get($$9);
            Font var10001 = this.font;
            int var10003 = $$4 + 36;
            Objects.requireNonNull(this.font);
            p_281997_.drawString(var10001, $$10, var10003, 32 + $$9 * 9, 0, false);
        }

        Style $$11 = this.getClickedComponentStyleAt((double) p_281262_, (double) p_283321_);
        if ($$11 != null) {
            p_281997_.renderComponentHoverEffect(this.font, $$11, p_281262_, p_283321_);
        }

        super.render(p_281997_, p_281262_, p_283321_, p_282251_);
    }

    public boolean mouseClicked(double p_98272_, double p_98273_, int p_98274_) {
        if (p_98274_ == 0) {
            Style $$3 = this.getClickedComponentStyleAt(p_98272_, p_98273_);
            if ($$3 != null && this.handleComponentClicked($$3)) {
                return true;
            }
        }

        return super.mouseClicked(p_98272_, p_98273_, p_98274_);
    }

    public boolean handleComponentClicked(Style p_98293_) {
        ClickEvent $$1 = p_98293_.getClickEvent();
        if ($$1 == null) {
            return false;
        } else if ($$1.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String $$2 = $$1.getValue();

            try {
                int $$3 = Integer.parseInt($$2) - 1;
                return this.forcePage($$3);
            } catch (Exception var5) {
                return false;
            }
        } else {
            boolean $$4 = super.handleComponentClicked(p_98293_);
            if ($$4 && $$1.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }

            return $$4;
        }
    }

    protected void closeScreen() {
        this.minecraft.setScreen((Screen) null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double p_98269_, double p_98270_) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        } else {
            int $$2 = Mth.floor(p_98269_ - (double) ((this.width - 192) / 2) - 36.0);
            int $$3 = Mth.floor(p_98270_ - 2.0 - 30.0);
            if ($$2 >= 0 && $$3 >= 0) {
                Objects.requireNonNull(this.font);
                int $$4 = Math.min(128 / 9, this.cachedPageComponents.size());
                if ($$2 <= 114) {
                    Objects.requireNonNull(this.minecraft.font);
                    if ($$3 < 9 * $$4 + $$4) {
                        Objects.requireNonNull(this.minecraft.font);
                        int $$5 = $$3 / 9;
                        if ($$5 >= 0 && $$5 < this.cachedPageComponents.size()) {
                            FormattedCharSequence $$6 = (FormattedCharSequence) this.cachedPageComponents.get($$5);
                            return this.minecraft.font.getSplitter().componentStyleAtWidth($$6, $$2);
                        }

                        return null;
                    }
                }

                return null;
            } else {
                return null;
            }
        }
    }

    static List<String> loadPages(CompoundTag p_169695_) {
        ImmutableList.Builder<String> $$1 = ImmutableList.builder();
        Objects.requireNonNull($$1);
        loadPages(p_169695_, $$1::add);
        return $$1.build();
    }

    public static void loadPages(CompoundTag p_169697_, Consumer<String> p_169698_) {
        ListTag $$2 = p_169697_.getList("pages", 8).copy();
        IntFunction $$5;
        if (Minecraft.getInstance().isTextFilteringEnabled() && p_169697_.contains("filtered_pages", 10)) {
            CompoundTag $$3 = p_169697_.getCompound("filtered_pages");
            $$5 = (p_169702_) -> {
                String $$3x = String.valueOf(p_169702_);
                return $$3.contains($$3x) ? $$3.getString($$3x) : $$2.getString(p_169702_);
            };
        } else {
            Objects.requireNonNull($$2);
            $$5 = $$2::getString;
        }

        for (int $$6 = 0; $$6 < $$2.size(); ++$$6) {
            p_169698_.accept((String) $$5.apply($$6));
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

        public WritableBookAccess(ItemStack p_98314_) {
            this.pages = readPages(p_98314_);
        }

        private static List<String> readPages(ItemStack p_98319_) {
            CompoundTag $$1 = p_98319_.getTag();
            return (List) ($$1 != null ? ParcheminScelleScreen.loadPages($$1) : ImmutableList.of());
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageRaw(int p_98317_) {
            return FormattedText.of((String) this.pages.get(p_98317_));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WrittenBookAccess implements ParcheminScelleScreen.BookAccess {
        private final List<String> pages;

        public WrittenBookAccess(ItemStack p_98322_) {
            this.pages = readPages(p_98322_);
        }

        private static List<String> readPages(ItemStack p_98327_) {
            CompoundTag $$1 = p_98327_.getTag();
            return (List) ($$1 != null && ParcheminScelleItem.makeSureTagIsValid($$1) ? ParcheminScelleScreen.loadPages($$1) : ImmutableList.of(Component.Serializer.toJson(Component.translatable("book.invalid.tag").withStyle(ChatFormatting.DARK_RED))));
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageRaw(int p_98325_) {
            String $$1 = (String) this.pages.get(p_98325_);

            try {
                FormattedText $$2 = Component.Serializer.fromJson($$1);
                if ($$2 != null) {
                    return $$2;
                }
            } catch (Exception var4) {
            }

            return FormattedText.of($$1);
        }
    }
}
