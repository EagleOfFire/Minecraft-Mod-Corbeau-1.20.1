package ros.eagleoffire.roscorbeau.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import ros.eagleoffire.roscorbeau.ROSCorbeau;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ParcheminScreen extends Screen {
    public static final ResourceLocation PARCHEMIN_SCREEN =
            new ResourceLocation(ROSCorbeau.MODID, "textures/screen/parchemin_screen.png");
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
    private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
    private static final FormattedCharSequence BLACK_CURSOR;
    private static final FormattedCharSequence GRAY_CURSOR;
    private final Player owner;
    private final ItemStack book;
    private boolean isModified;
    private boolean isSigning;
    private int frameTick;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private String title = "";
    private final TextFieldHelper pageEdit = new TextFieldHelper(this::getCurrentPageText, this::setCurrentPageText, this::getClipboard, this::setClipboard, (p_280853_) -> {
        return p_280853_.length() < 1024 && this.font.wordWrapHeight(p_280853_, 114) <= 128;
    });
    private final TextFieldHelper titleEdit = new TextFieldHelper(() -> {
        return this.title;
    }, (p_98175_) -> {
        this.title = p_98175_;
    }, this::getClipboard, this::setClipboard, (p_98170_) -> {
        return p_98170_.length() < 16;
    });
    private long lastClickTime;
    private int lastIndex = -1;
    private Button doneButton;
    private Button signButton;
    private Button finalizeButton;
    private Button cancelButton;
    private final InteractionHand hand;
    @Nullable
    private DisplayCache displayCache;
    private Component pageMsg;
    private final Component ownerText;

    public ParcheminScreen(Player player, ItemStack stack, InteractionHand hand) {
        super(GameNarrator.NO_TITLE);
        this.displayCache = ParcheminScreen.DisplayCache.EMPTY;
        this.pageMsg = CommonComponents.EMPTY;
        this.owner = player;
        this.book = stack;
        this.hand = hand;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            List pages = this.pages;
            Objects.requireNonNull(pages);
            BookViewScreen.loadPages(tag, pages::add);
        }

        if (this.pages.isEmpty()) {
            this.pages.add("");
        }

        this.ownerText = Component.translatable("book.byAuthor", new Object[]{player.getName()}).withStyle(ChatFormatting.DARK_GRAY);
    }

    private void setClipboard(String string) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, string);
        }
    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    private int getNumPages() {
        return this.pages.size();
    }

    public void tick() {
        super.tick();
        ++this.frameTick;
    }

    protected void init() {
        this.clearDisplayCache();
        this.signButton = (Button) this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (sign) -> {
            this.isSigning = true;
            this.updateButtonVisibility();
        }).bounds(this.width / 2 - 100, 225, 98, 20).build());
        this.doneButton = (Button) this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen((Screen) null);
            this.saveChanges(false);
        }).bounds(this.width / 2 + 2, 225, 98, 20).build());
        this.finalizeButton = (Button) this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (finish) -> {
            if (this.isSigning) {
                this.saveChanges(true);
                this.minecraft.setScreen((Screen) null);
            }

        }).bounds(this.width / 2 - 100, 225, 98, 20).build());
        this.cancelButton = (Button) this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (cancel) -> {
            if (this.isSigning) {
                this.isSigning = false;
            }

            this.updateButtonVisibility();
        }).bounds(this.width / 2 + 2, 225, 98, 20).build());
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.doneButton.visible = !this.isSigning;
        this.signButton.visible = !this.isSigning;
        this.cancelButton.visible = this.isSigning;
        this.finalizeButton.visible = this.isSigning;
        this.finalizeButton.active = !this.title.trim().isEmpty();
    }

    private void eraseEmptyTrailingPages() {
        ListIterator<String> x = this.pages.listIterator(this.pages.size());

        while (x.hasPrevious() && ((String) x.previous()).isEmpty()) {
            x.remove();
        }

    }

    private void saveChanges(boolean save) {
        if (this.isModified) {
            this.eraseEmptyTrailingPages();
            this.updateLocalCopy(save);
            int flag = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;
            this.minecraft.getConnection().send(new ServerboundEditBookPacket(flag, this.pages, save ? Optional.of(this.title.trim()) : Optional.empty()));
        }
    }

    private void updateLocalCopy(boolean update) {
        ListTag flag = new ListTag();
        Stream pages = this.pages.stream().map(StringTag::valueOf);
        Objects.requireNonNull(flag);
        if (update) {
            this.book.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
            this.book.addTagElement("title", StringTag.valueOf(this.title.trim()));
        }
    }

    public boolean keyPressed(int keycode, int scanCode, int modifiers) {
        if (super.keyPressed(keycode, scanCode, modifiers)) {
            return true;
        } else if (this.isSigning) {
            return this.titleKeyPressed(keycode, scanCode, modifiers);
        } else {
            boolean keyPressed = this.bookKeyPressed(keycode, scanCode, modifiers);
            if (keyPressed) {
                this.clearDisplayCache();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (super.charTyped(codePoint, modifiers)) {
            return true;
        } else if (this.isSigning) {
            boolean charTyped = this.titleEdit.charTyped(codePoint);
            if (charTyped) {
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            } else {
                return false;
            }
        } else if (SharedConstants.isAllowedChatCharacter(codePoint)) {
            this.pageEdit.insertText(Character.toString(codePoint));
            this.clearDisplayCache();
            return true;
        } else {
            return false;
        }
    }

    private boolean bookKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isSelectAll(keyCode)) {
            this.pageEdit.selectAll();
            return true;
        } else if (Screen.isCopy(keyCode)) {
            this.pageEdit.copy();
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.pageEdit.paste();
            return true;
        } else if (Screen.isCut(keyCode)) {
            this.pageEdit.cut();
            return true;
        } else {
            TextFieldHelper.CursorStep keyPressed = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
            switch (keyCode) {
                case 257:
                case 335:
                    this.pageEdit.insertText("\n");
                    return true;
                case 259:
                    this.pageEdit.removeFromCursor(-1, keyPressed);
                    return true;
                case 261:
                    this.pageEdit.removeFromCursor(1, keyPressed);
                    return true;
                case 262:
                    this.pageEdit.moveBy(1, Screen.hasShiftDown(), keyPressed);
                    return true;
                case 263:
                    this.pageEdit.moveBy(-1, Screen.hasShiftDown(), keyPressed);
                    return true;
                case 264:
                    this.keyDown();
                    return true;
                case 265:
                    this.keyUp();
                    return true;
                case 268:
                    this.keyHome();
                    return true;
                case 269:
                    this.keyEnd();
                    return true;
                default:
                    return false;
            }
        }
    }

    private void keyUp() {
        this.changeLine(-1);
    }

    private void keyDown() {
        this.changeLine(1);
    }

    private void changeLine(int p_98098_) {
        int flag = this.pageEdit.getCursorPos();
        int charTyped = this.getDisplayCache().changeLine(flag, p_98098_);
        this.pageEdit.setCursorPos(charTyped, Screen.hasShiftDown());
    }

    private void keyHome() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToStart(Screen.hasShiftDown());
        } else {
            int x = this.pageEdit.getCursorPos();
            int flag = this.getDisplayCache().findLineStart(x);
            this.pageEdit.setCursorPos(flag, Screen.hasShiftDown());
        }
    }

    private void keyEnd() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
        } else {
            ParcheminScreen.DisplayCache x = this.getDisplayCache();
            int flag = this.pageEdit.getCursorPos();
            int charTyped = x.findLineEnd(flag);
            this.pageEdit.setCursorPos(charTyped, Screen.hasShiftDown());
        }

    }

    private boolean titleKeyPressed(int p_98164_, int p_98165_, int p_98166_) {
        switch (p_98164_) {
            case 257:
            case 335:
                if (!this.title.isEmpty()) {
                    this.saveChanges(true);
                    this.minecraft.setScreen((Screen) null);
                }

                return true;
            case 259:
                this.titleEdit.removeCharsFromCursor(-1);
                this.updateButtonVisibility();
                this.isModified = true;
                return true;
            default:
                return false;
        }
    }

    private String getCurrentPageText() {
        return this.currentPage >= 0 && this.currentPage < this.pages.size() ? (String) this.pages.get(this.currentPage) : "";
    }

    private void setCurrentPageText(String newText) {
        if (newText == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
            this.pages.set(this.currentPage, newText);
            this.isModified = true;
            this.clearDisplayCache();
        } else {
            throw new IndexOutOfBoundsException("Current page index is out of bounds");
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Render the background
        this.renderBackground(graphics);
        this.setFocused((GuiEventListener) null);

        int screenCenterX = (this.width - 256) / 2;
        int screenCenterY = (this.height - 256) / 2;

        // Draw the parchment background
        graphics.blit(PARCHEMIN_SCREEN, screenCenterX, screenCenterY, 0, 0, 256, 256, 256, 256);

        if (this.isSigning) {
            // Draw title with a blinking cursor
            boolean showCursor = this.frameTick / 6 % 2 == 0;
            FormattedCharSequence titleText = FormattedCharSequence.composite(
                    FormattedCharSequence.forward(this.title, Style.EMPTY),
                    showCursor ? BLACK_CURSOR : GRAY_CURSOR
            );

            int titleLabelWidth = this.font.width(EDIT_TITLE_LABEL);
            graphics.drawString(this.font, EDIT_TITLE_LABEL, screenCenterX + 36 + (114 - titleLabelWidth) / 2, 34, 0, false);

            int titleTextWidth = this.font.width(titleText);
            graphics.drawString(this.font, titleText, screenCenterX + 36 + (114 - titleTextWidth) / 2, 50, 0, false);

            int ownerTextWidth = this.font.width(this.ownerText);
            graphics.drawString(this.font, this.ownerText, screenCenterX + 36 + (114 - ownerTextWidth) / 2, 60, 0, false);

            // Draw the finalization warning message
            graphics.drawWordWrap(this.font, FINALIZE_WARNING_LABEL, screenCenterX + 36, 82, 114, 0);
        } else {
            // Display current page message
            int pageMsgWidth = this.font.width(this.pageMsg);
            graphics.drawString(this.font, this.pageMsg, screenCenterX - pageMsgWidth + 192 - 44, 18, 0, false);

            // Retrieve and render display cache lines
            ParcheminScreen.DisplayCache displayCache = this.getDisplayCache();
            ParcheminScreen.LineInfo[] displayLines = displayCache.lines;

            for (ParcheminScreen.LineInfo lineInfo : displayLines) {
                graphics.drawString(this.font, lineInfo.asComponent, lineInfo.x, lineInfo.y, -16777216, false);
            }

            // Render additional elements like highlights and cursor
            this.renderHighlight(graphics, displayCache.selection);
            this.renderCursor(graphics, displayCache.cursor, displayCache.cursorAtEnd);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderCursor(GuiGraphics graphics, Pos2i cursorPosition, boolean cursorAtEnd) {
        // Check if cursor should blink (based on frame tick timing)
        if (this.frameTick / 6 % 2 == 0) {
            // Convert the local cursor position to screen coordinates
            cursorPosition = this.convertLocalToScreen(cursorPosition);

            if (!cursorAtEnd) {
                // Draw the cursor as a vertical line when not at the end of text
                int cursorStartX = cursorPosition.x;
                int cursorStartY = cursorPosition.y - 1;
                int cursorEndX = cursorPosition.x + 1;
                int cursorEndY = cursorPosition.y + 8;  // Height set to 9 pixels for the cursor line

                // Render the cursor line
                graphics.fill(cursorStartX, cursorStartY, cursorEndX, cursorEndY, -16777216);
            } else {
                // Draw the cursor as an underscore when at the end of text
                graphics.drawString(this.font, "_", cursorPosition.x, cursorPosition.y, 0, false);
            }
        }
    }

    private void renderHighlight(GuiGraphics graphics, Rect2i[] array) {
        Rect2i[] var3 = array;
        int var4 = array.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Rect2i charTyped = var3[var5];
            int keyPressed = charTyped.getX();
            int x = charTyped.getY();
            int flag = keyPressed + charTyped.getWidth();
            int $$6 = x + charTyped.getHeight();
            graphics.fill(RenderType.guiTextHighlight(), keyPressed, x, flag, $$6, -16776961);
        }

    }

    private Pos2i convertScreenToLocal(Pos2i screenPos) {
        // Converts screen coordinates to local coordinates by adjusting for the UI offset
        int localX = screenPos.x - (this.width - 192) / 2 - 25;
        int localY = screenPos.y - 45;
        return new Pos2i(localX, localY);
    }

    private Pos2i convertLocalToScreen(Pos2i localPos) {
        // Converts local coordinates to screen coordinates by adding the UI offset
        int screenX = localPos.x + (this.width - 192) / 2 + 25;
        int screenY = localPos.y + 45;
        return new Pos2i(screenX, screenY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // If the superclass handles the click, return true immediately
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            // Check for left mouse button click
            if (button == 0) {
                long currentTime = Util.getMillis();
                DisplayCache displayCache = this.getDisplayCache();

                // Convert screen coordinates to local coordinates and get index at position
                int clickedIndex = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int) mouseX, (int) mouseY)));

                if (clickedIndex >= 0) {
                    // Detect double-click to select word or all text
                    if (clickedIndex == this.lastIndex && currentTime - this.lastClickTime < 250L) {
                        if (!this.pageEdit.isSelecting()) {
                            this.selectWord(clickedIndex);  // Double-click selects word
                        } else {
                            this.pageEdit.selectAll();  // Further clicks select all
                        }
                    } else {
                        // Move cursor to clicked position, holding Shift selects to position
                        this.pageEdit.setCursorPos(clickedIndex, Screen.hasShiftDown());
                    }

                    this.clearDisplayCache();
                }

                // Update last click information for double-click detection
                this.lastIndex = clickedIndex;
                this.lastClickTime = currentTime;
            }

            return true;
        }
    }

    private void selectWord(int position) {
        String currentPageText = this.getCurrentPageText();
        int start = StringSplitter.getWordPosition(currentPageText, -1, position, false);
        int end = StringSplitter.getWordPosition(currentPageText, 1, position, false);
        this.pageEdit.setSelectionRange(start, end);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // If superclass handles the drag, return true immediately
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        } else {
            // Check for left mouse button drag
            if (button == 0) {
                ParcheminScreen.DisplayCache displayCache = this.getDisplayCache();

                // Convert screen coordinates to local and get index at dragged position
                int draggedIndex = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new ParcheminScreen.Pos2i((int) mouseX, (int) mouseY)));

                // Update the cursor position and select text while dragging
                this.pageEdit.setCursorPos(draggedIndex, true);

                // Clear display cache to refresh visual feedback
                this.clearDisplayCache();
            }

            return true;
        }
    }

    private ParcheminScreen.DisplayCache getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
        }

        return this.displayCache;
    }

    private void clearDisplayCache() {
        this.displayCache = null;
    }

    private DisplayCache rebuildDisplayCache() {
        String pageText = this.getCurrentPageText();

        // Return an empty cache if the page text is empty
        if (pageText.isEmpty()) {
            return ParcheminScreen.DisplayCache.EMPTY;
        } else {
            // Retrieve cursor and selection positions
            int cursorPos = this.pageEdit.getCursorPos();
            int selectionPos = this.pageEdit.getSelectionPos();

            // Prepare containers for storing line information and positions
            IntList lineStartIndices = new IntArrayList();
            List<LineInfo> lines = Lists.newArrayList();
            MutableInt lineCounter = new MutableInt();
            MutableBoolean endsWithNewline = new MutableBoolean();

            // Get the StringSplitter to split lines based on font width
            StringSplitter splitter = this.font.getSplitter();
            splitter.splitLines(pageText, 114, Style.EMPTY, true, (style, start, end) -> {
                int lineIndex = lineCounter.getAndIncrement();
                String lineText = pageText.substring(start, end);
                endsWithNewline.setValue(lineText.endsWith("\n"));

                // Strip trailing spaces or newlines for cleaner rendering
                String trimmedLineText = StringUtils.stripEnd(lineText, " \n");

                // Calculate position of the line on the screen
                int lineYPosition = lineIndex * 9;
                Pos2i linePosition = this.convertLocalToScreen(new Pos2i(0, lineYPosition));

                // Store the start index of each line and add line information
                lineStartIndices.add(start);
                lines.add(new LineInfo(style, trimmedLineText, linePosition.x, linePosition.y));
            });

            int[] lineStartPositions = lineStartIndices.toIntArray();
            boolean isCursorAtEnd = cursorPos == pageText.length();
            Pos2i cursorScreenPos;

            // Determine the cursor position based on whether it is at the end or within text
            if (isCursorAtEnd && endsWithNewline.isTrue()) {
                int lastLineY = lines.size() * 9;
                cursorScreenPos = new Pos2i(0, lastLineY);
            } else {
                int cursorLineIndex = findLineFromPos(lineStartPositions, cursorPos);
                int cursorXOffset = this.font.width(pageText.substring(lineStartPositions[cursorLineIndex], cursorPos));
                cursorScreenPos = new Pos2i(cursorXOffset, cursorLineIndex * 9);
            }

            // Calculate text selection rectangles if selection exists
            List<Rect2i> selectionRectangles = Lists.newArrayList();
            if (cursorPos != selectionPos) {
                int selectionStart = Math.min(cursorPos, selectionPos);
                int selectionEnd = Math.max(cursorPos, selectionPos);

                int startLine = findLineFromPos(lineStartPositions, selectionStart);
                int endLine = findLineFromPos(lineStartPositions, selectionEnd);

                if (startLine == endLine) {
                    int lineY = startLine * 9;
                    selectionRectangles.add(this.createPartialLineSelection(pageText, splitter, selectionStart, selectionEnd, lineY, lineStartPositions[startLine]));
                } else {
                    int startLineEndPos = startLine + 1 > lineStartPositions.length ? pageText.length() : lineStartPositions[startLine + 1];
                    selectionRectangles.add(this.createPartialLineSelection(pageText, splitter, selectionStart, startLineEndPos, startLine * 9, lineStartPositions[startLine]));

                    for (int lineIndex = startLine + 1; lineIndex < endLine; ++lineIndex) {
                        int lineY = lineIndex * 9;
                        String lineText = pageText.substring(lineStartPositions[lineIndex], lineStartPositions[lineIndex + 1]);
                        int lineWidth = (int) splitter.stringWidth(lineText);
                        selectionRectangles.add(this.createSelection(new Pos2i(0, lineY), new Pos2i(lineWidth, lineY + 9)));
                    }

                    int endLineStartPos = lineStartPositions[endLine];
                    selectionRectangles.add(this.createPartialLineSelection(pageText, splitter, endLineStartPos, selectionEnd, endLine * 9, lineStartPositions[endLine]));
                }
            }

            // Return the constructed display cache with text, cursor, and selection data
            return new DisplayCache(pageText, cursorScreenPos, isCursorAtEnd, lineStartPositions,
                    lines.toArray(new LineInfo[0]), selectionRectangles.toArray(new Rect2i[0]));
        }
    }

    static int findLineFromPos(int[] linePositions, int position) {
        int lineIndex = Arrays.binarySearch(linePositions, position);
        return lineIndex < 0 ? -(lineIndex + 2) : lineIndex;
    }

    private Rect2i createPartialLineSelection(String text, StringSplitter stringSplitter, int endPosition, int startPosition, int yPosition, int lineStartIndex) {
        String substringStart = text.substring(lineStartIndex, endPosition);
        String substringEnd = text.substring(lineStartIndex, startPosition);

        Pos2i startPos = new Pos2i((int) stringSplitter.stringWidth(substringStart), yPosition);
        int endXPosition = (int) stringSplitter.stringWidth(substringEnd);

        // Assuming this.font refers to a specific font height
        Objects.requireNonNull(this.font);  // Ensure font is not null
        Pos2i endPos = new Pos2i(endXPosition, yPosition + 9);

        return this.createSelection(startPos, endPos);
    }

    private Rect2i createSelection(Pos2i start, Pos2i end) {
        Pos2i screenStart = this.convertLocalToScreen(start);
        Pos2i screenEnd = this.convertLocalToScreen(end);

        int xMin = Math.min(screenStart.x, screenEnd.x);
        int xMax = Math.max(screenStart.x, screenEnd.x);
        int yMin = Math.min(screenStart.y, screenEnd.y);
        int yMax = Math.max(screenStart.y, screenEnd.y);

        return new Rect2i(xMin, yMin, xMax - xMin, yMax - yMin);
    }


    static {
        BLACK_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
        GRAY_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
    }

    @OnlyIn(Dist.CLIENT)
    static class DisplayCache {
        static final DisplayCache EMPTY;

        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String fullText, Pos2i cursor, boolean cursorAtEnd, int[] lineStarts, LineInfo[] lines, Rect2i[] selection) {
            this.fullText = fullText;
            this.cursor = cursor;
            this.cursorAtEnd = cursorAtEnd;
            this.lineStarts = lineStarts;
            this.lines = lines;
            this.selection = selection;
        }

        public int getIndexAtPosition(Font font, Pos2i position) {
            int lineIndex = position.y / 9;  // Assuming font height is 9
            if (lineIndex < 0) {
                return 0;
            } else if (lineIndex >= this.lines.length) {
                return this.fullText.length();
            } else {
                LineInfo line = this.lines[lineIndex];
                return this.lineStarts[lineIndex] + font.getSplitter().plainIndexAtWidth(line.contents, position.x, line.style);
            }
        }

        public int changeLine(int position, int lineOffset) {
            int currentLineIndex = ParcheminScreen.findLineFromPos(this.lineStarts, position);
            int targetLineIndex = currentLineIndex + lineOffset;

            if (targetLineIndex >= 0 && targetLineIndex < this.lineStarts.length) {
                int positionInLine = position - this.lineStarts[currentLineIndex];
                int targetLineLength = this.lines[targetLineIndex].contents.length();
                return this.lineStarts[targetLineIndex] + Math.min(positionInLine, targetLineLength);
            } else {
                return position;
            }
        }

        public int findLineStart(int position) {
            int lineIndex = ParcheminScreen.findLineFromPos(this.lineStarts, position);
            return this.lineStarts[lineIndex];
        }

        public int findLineEnd(int position) {
            int lineIndex = ParcheminScreen.findLineFromPos(this.lineStarts, position);
            return this.lineStarts[lineIndex] + this.lines[lineIndex].contents.length();
        }

        static {
            EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style style, String contents, int x, int y) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.asComponent = Component.literal(contents).setStyle(style);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
