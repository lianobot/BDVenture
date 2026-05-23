package main;

import entity.Entity;
import object.OBJ_Heart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UI {

    GamePanel gp;
    Graphics2D g2;
    Font arial_40, arial_80B, pixelFont;
    BufferedImage heart_full, heart_half, heart_blank;
    BufferedImage titleButtonDefault, titleButtonHover;
    BufferedImage settingsPanel;
    BufferedImage dialogueBox;
    BufferedImage[] sliderEmptyTicks;
    BufferedImage[] sliderFilledTicks;
    public String currentDialogue = "";
    private int dialogueVisibleChars = 0;
    private int dialogueTypeCounter = 0;
    public int commandNum = 0;
    public int settingsCommandNum = 0;
    public int hoverCommandNum = -1;
    public int hoverSettingsCommandNum = -1;

    public static final int TITLE_PLAY = 0;
    public static final int TITLE_SETTINGS = 1;
    public static final int TITLE_QUIT = 2;
    public static final int SETTINGS_MUSIC = 0;
    public static final int SETTINGS_SFX = 1;
    public static final int SETTINGS_BACK = 2;
    public static final int SLIDER_TICK_COUNT = 9;

    private static final int BUTTON_SHEET_COLS = 2;
    private static final int BUTTON_SHEET_ROWS = 2;
    private static final int TITLE_BUTTON_SCALE = 4;
    private static final int SETTINGS_PANEL_SCALE = 4;
    private static final int SETTINGS_BUTTON_SCALE = 3;
    private static final int DIALOGUE_SCALE = 5;
    private static final int SETTINGS_SLIDER_TICK_X = 2;
    private static final int SETTINGS_SLIDER_FILLED_TICK_X = 50;
    private static final int SETTINGS_SLIDER_TICK_Y = 194;
    private static final int SETTINGS_SLIDER_TICK_WIDTH = 4;
    private static final int SETTINGS_SLIDER_TICK_HEIGHT = 12;
    private static final int SETTINGS_SLIDER_TICK_SPACING = 5;

    public UI(GamePanel gp) {
        this.gp = gp;

        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 40);
        pixelFont = loadPixelFont();

        //CREATE HUD OBJECT
        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;

        loadSproutLandsImages();
    }

    private Font loadPixelFont() {
        try (InputStream is = getClass().getResourceAsStream(
                "/Sprout Lands - UI Pack - Basic pack/fonts/pixelFont-7-8x14-sproutLands.ttf")) {
            if (is != null) {
                return Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return new Font("Arial", Font.BOLD, 24);
    }

    private void loadSproutLandsImages() {
        try {
            BufferedImage buttonSheet = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/UI Big Play Button.png")));
            int buttonCellWidth = buttonSheet.getWidth() / BUTTON_SHEET_COLS;
            int buttonCellHeight = buttonSheet.getHeight() / BUTTON_SHEET_ROWS;
            // UI Big Play Button.png is a 2x2 grid. The top row contains blank button skins.
            titleButtonDefault = buttonSheet.getSubimage(0, 0, buttonCellWidth, buttonCellHeight);
            titleButtonHover = buttonSheet.getSubimage(buttonCellWidth, 0, buttonCellWidth, buttonCellHeight);

            BufferedImage settingSheet = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/Setting menu.png")));
            int panelWidth = settingSheet.getWidth() / 2;
            // Setting menu.png has two vertical panels. The left half has the baked SETTINGS title.
            settingsPanel = settingSheet.getSubimage(0, 0, panelWidth, settingSheet.getHeight());

            dialogueBox = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/Premade dialog box small.png")));

            BufferedImage settingButtons = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/UI Settings Buttons.png")));
            sliderEmptyTicks = new BufferedImage[SLIDER_TICK_COUNT];
            sliderFilledTicks = new BufferedImage[SLIDER_TICK_COUNT];
            for (int i = 0; i < SLIDER_TICK_COUNT; i++) {
                int emptyX = SETTINGS_SLIDER_TICK_X + i * SETTINGS_SLIDER_TICK_SPACING;
                int filledX = SETTINGS_SLIDER_FILLED_TICK_X + i * SETTINGS_SLIDER_TICK_SPACING;
                sliderEmptyTicks[i] = settingButtons.getSubimage(
                        emptyX,
                        SETTINGS_SLIDER_TICK_Y,
                        SETTINGS_SLIDER_TICK_WIDTH,
                        SETTINGS_SLIDER_TICK_HEIGHT
                );
                sliderFilledTicks[i] = settingButtons.getSubimage(
                        filledX,
                        SETTINGS_SLIDER_TICK_Y,
                        SETTINGS_SLIDER_TICK_WIDTH,
                        SETTINGS_SLIDER_TICK_HEIGHT
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        this.g2 = g2;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }

        if (gp.gameState == gp.settingsState) {
            drawSettingsScreen();
        }

        if (gp.gameState == gp.playState) {
            drawPlayerLife();
            drawPlayerStamina();
            drawInteractionPrompt();
            drawComboIndicator();
            drawObjectiveTracker();
            drawLowHealthWarning();
        }

        if (gp.gameState == gp.pauseState) {
            drawPlayerLife();
            drawPauseScreen();
        }

        if (gp.gameState == gp.dialogueState) {
            drawPlayerLife();
            drawDialogueScreen();
            drawLowHealthWarning();
        }

        if (gp.gameState == gp.gameOverState) {
            drawGameOverScreen();
        }

        if (gp.gameState != gp.titleState && gp.gameState != gp.settingsState && gp.keyH.checkPlayTime) {
            drawPlayTime();
        }
    }

    public void drawPlayerLife() {

        int x = gp.TILE_SIZE / 2;
        int y = gp.TILE_SIZE / 2;
        int i = 0;

        while (i < gp.player.maxLife / 2) {
            g2.drawImage(heart_blank, x, y, gp.TILE_SIZE, gp.TILE_SIZE, null);
            i++;
            x += gp.TILE_SIZE;
        }

        x = gp.TILE_SIZE / 2;
        y = gp.TILE_SIZE / 2;
        i = 0;

        while (i < gp.player.life) {
            g2.drawImage(heart_half, x, y, gp.TILE_SIZE, gp.TILE_SIZE, null);
            i++;
            if (i < gp.player.life) {
                g2.drawImage(heart_full, x, y, gp.TILE_SIZE, gp.TILE_SIZE, null);
            }
            i++;
            x += gp.TILE_SIZE;
        }

    }

    public void drawDialogueScreen() {

        Rectangle box = getDialogueBoxBounds();
        g2.drawImage(dialogueBox, box.x, box.y, box.width, box.height, null);

        // Premade dialog box small.png is 176x64. Text lives in the right speech area.
        int portraitX = box.x + 10 * DIALOGUE_SCALE;
        int portraitY = box.y + 8 * DIALOGUE_SCALE;
        int portraitSize = 40 * DIALOGUE_SCALE;
        drawDialoguePortrait(portraitX, portraitY, portraitSize);

        int textX = box.x + 70 * DIALOGUE_SCALE;
        int textY = box.y + 25 * DIALOGUE_SCALE;
        int textWidth = 86 * DIALOGUE_SCALE;
        int lineHeight = 10 * DIALOGUE_SCALE;

        g2.setFont(pixelFont.deriveFont(Font.PLAIN, 22F));
        g2.setColor(new Color(92, 62, 51));
        FontMetrics fm = g2.getFontMetrics();

        int y = textY + fm.getAscent();
        String visibleDialogue = currentDialogue.substring(0, Math.min(dialogueVisibleChars, currentDialogue.length()));
        for (String line : wrapText(visibleDialogue, fm, textWidth)) {
            g2.drawString(line, textX, y);
            y += lineHeight;
        }
    }

    public void setDialogue(String dialogue) {
        currentDialogue = dialogue == null ? "" : dialogue;
        dialogueVisibleChars = 0;
        dialogueTypeCounter = 0;
    }

    public void updateDialogueTypewriter() {
        if (!isDialogueTyping()) {
            return;
        }

        dialogueTypeCounter++;
        if (dialogueTypeCounter >= 2) {
            dialogueVisibleChars++;
            dialogueTypeCounter = 0;
        }
    }

    public boolean isDialogueTyping() {
        return dialogueVisibleChars < currentDialogue.length();
    }

    public void finishDialogueLine() {
        dialogueVisibleChars = currentDialogue.length();
    }

    private void drawDialoguePortrait(int x, int y, int size) {
        Entity speaker = null;
        if (gp.player.dialogueIndex >= 0 && gp.player.dialogueIndex < gp.npc.length) {
            speaker = gp.npc[gp.player.dialogueIndex];
        }

        BufferedImage portrait = null;
        if (speaker != null && speaker.idleDown != null && speaker.idleDown.length > 0) {
            portrait = speaker.idleDown[0];
        } else if (gp.player.idleDown != null && gp.player.idleDown.length > 0) {
            portrait = gp.player.idleDown[0];
        }

        if (portrait != null) {
            g2.drawImage(portrait, x, y, size, size, null);
        }
    }

    public void drawSubWindow(int x, int y, int width, int height) {

        Color c = new Color(0, 0, 0, 210);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255, 255, 255);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    public void drawTitleScreen() {

        g2.setColor(new Color(58, 99, 78));
        g2.fillRect(0, 0, gp.SCREEN_WIDTH, gp.SCREEN_HEIGHT);

        g2.setFont(pixelFont.deriveFont(Font.BOLD, 64F));
        String text = "UNDER A PIXEL SKY";
        int x = getXForCenteredText(text);
        int y = gp.TILE_SIZE * 3;

        g2.setColor(new Color(48, 37, 34));
        g2.drawString(text, x + 4, y + 4);
        g2.setColor(new Color(249, 233, 190));
        g2.drawString(text, x, y);

        drawTitleButton(TITLE_PLAY, "PLAY");
        drawTitleButton(TITLE_SETTINGS, "SETTINGS");
        drawTitleButton(TITLE_QUIT, "QUIT");
    }

    private void drawPlayerStamina() {
        int x = gp.TILE_SIZE / 2;
        int y = gp.TILE_SIZE + 34;
        int width = gp.TILE_SIZE * 3;
        int height = 12;
        double staminaPercent = gp.player.stamina / (double) gp.player.maxStamina;
        int fillWidth = (int) Math.round(width * staminaPercent);

        g2.setColor(new Color(40, 31, 34, 210));
        g2.fillRect(x - 2, y - 2, width + 4, height + 4);
        g2.setColor(new Color(92, 62, 51));
        g2.fillRect(x, y, width, height);
        g2.setColor(gp.player.canDodge() ? new Color(119, 214, 91) : new Color(190, 96, 75));
        if (gp.player.isSprinting()) {
            g2.setColor(new Color(255, 236, 150));
        }
        g2.fillRect(x, y, fillWidth, height);
    }

    private void drawComboIndicator() {
        int comboStep = gp.player.getComboStep();
        if (comboStep <= 0) {
            return;
        }

        String text = "COMBO x" + (comboStep + 1);
        g2.setFont(pixelFont.deriveFont(Font.BOLD, 24F));
        int x = gp.SCREEN_WIDTH / 2 - g2.getFontMetrics().stringWidth(text) / 2;
        int y = gp.TILE_SIZE * 2;
        g2.setColor(new Color(40, 31, 34));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(new Color(255, 236, 150));
        g2.drawString(text, x, y);
    }

    private void drawObjectiveTracker() {
        String text = "Slimes " + gp.monstersDefeated + "/3";
        g2.setFont(pixelFont.deriveFont(Font.BOLD, 24F));
        FontMetrics fm = g2.getFontMetrics();
        int width = fm.stringWidth(text) + 28;
        int height = 34;
        int x = gp.SCREEN_WIDTH - width - gp.TILE_SIZE / 2;
        int y = gp.TILE_SIZE / 2;

        g2.setColor(new Color(40, 31, 34, 210));
        g2.fillRoundRect(x, y, width, height, 8, 8);
        g2.setColor(gp.monstersDefeated >= 3 ? new Color(183, 255, 132) : new Color(249, 233, 190));
        g2.drawString(text, x + 14, y + 25);
    }

    private void drawInteractionPrompt() {
        if (gp.player.nearbyNpcIndex == 999) {
            return;
        }

        Entity npc = gp.npc[gp.player.nearbyNpcIndex];
        if (npc == null) {
            return;
        }

        int npcScreenX = npc.worldX - gp.player.worldX + gp.player.screenX;
        int npcScreenY = npc.worldY - gp.player.worldY + gp.player.screenY;
        int centerX = npcScreenX + npc.solidAreaDefaultX + npc.solidArea.width / 2;
        int x = centerX - 18;
        int y = npcScreenY + npc.solidAreaDefaultY - 42;

        if (npc.name != null && !npc.name.isBlank()) {
            g2.setFont(pixelFont.deriveFont(Font.BOLD, 18F));
            FontMetrics fm = g2.getFontMetrics();
            int nameWidth = fm.stringWidth(npc.name);
            g2.setColor(new Color(40, 31, 34, 210));
            g2.fillRoundRect(centerX - nameWidth / 2 - 8, y - 27, nameWidth + 16, 22, 6, 6);
            g2.setColor(new Color(249, 233, 190));
            g2.drawString(npc.name, centerX - nameWidth / 2, y - 10);
        }

        g2.setFont(pixelFont.deriveFont(Font.BOLD, 22F));
        g2.setColor(new Color(40, 31, 34, 220));
        g2.fillRoundRect(x, y, 36, 28, 6, 6);
        g2.setColor(new Color(249, 233, 190));
        g2.drawString("E", x + 12, y + 21);
    }

    private void drawLowHealthWarning() {
        if (gp.player.life > 2) {
            return;
        }

        float pulse = 0.13f + (float) (Math.sin(System.currentTimeMillis() / 120.0) * 0.06f);
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.08f, pulse)));
        g2.setColor(new Color(180, 20, 24));
        int edge = gp.TILE_SIZE;
        g2.fillRect(0, 0, gp.SCREEN_WIDTH, edge);
        g2.fillRect(0, gp.SCREEN_HEIGHT - edge, gp.SCREEN_WIDTH, edge);
        g2.fillRect(0, 0, edge, gp.SCREEN_HEIGHT);
        g2.fillRect(gp.SCREEN_WIDTH - edge, 0, edge, gp.SCREEN_HEIGHT);
        g2.setComposite(oldComposite);
    }

    private void drawTitleButton(int index, String text) {
        Rectangle bounds = getTitleButtonBounds(index);
        boolean selected = commandNum == index || hoverCommandNum == index;
        BufferedImage button = selected ? titleButtonHover : titleButtonDefault;
        g2.drawImage(button, bounds.x, bounds.y, bounds.width, bounds.height, null);

        g2.setFont(pixelFont.deriveFont(Font.BOLD, index == TITLE_SETTINGS ? 33F : 38F));
        g2.setColor(selected ? new Color(92, 62, 51) : new Color(143, 103, 73));
        drawCenteredString(text, bounds, -3 * TITLE_BUTTON_SCALE);
    }

    public void drawSettingsScreen() {
        g2.setColor(new Color(58, 99, 78));
        g2.fillRect(0, 0, gp.SCREEN_WIDTH, gp.SCREEN_HEIGHT);

        Rectangle panel = getSettingsPanelBounds();
        g2.drawImage(settingsPanel, panel.x, panel.y, panel.width, panel.height, null);

        drawSettingsSlider(SETTINGS_MUSIC, "MUSIC", gp.musicVolume);
        drawSettingsSlider(SETTINGS_SFX, "SFX", gp.soundEffectVolume);
        drawSettingsBackButton();
    }

    private void drawSettingsSlider(int index, String label, int value) {
        Rectangle panel = getSettingsPanelBounds();
        Rectangle slider = getSettingsSliderBounds(index);
        int scale = SETTINGS_PANEL_SCALE;
        boolean selected = settingsCommandNum == index || hoverSettingsCommandNum == index;

        int labelX = slider.x;
        int labelY = slider.y - 8 * scale;

        g2.setFont(pixelFont.deriveFont(Font.BOLD, 24F));
        g2.setColor(selected ? new Color(77, 111, 63) : new Color(92, 62, 51));
        g2.drawString(label, labelX, labelY);

        if (selected) {
            g2.drawString(">", labelX - 8 * scale, labelY);
        }

        for (int i = 0; i < SLIDER_TICK_COUNT; i++) {
            BufferedImage tick = i < value ? sliderFilledTicks[i] : sliderEmptyTicks[i];
            int x = slider.x + i * SETTINGS_SLIDER_TICK_SPACING * scale;
            g2.drawImage(tick, x, slider.y, SETTINGS_SLIDER_TICK_WIDTH * scale, SETTINGS_SLIDER_TICK_HEIGHT * scale, null);
        }
    }

    private void drawSettingsBackButton() {
        Rectangle bounds = getSettingsBackButtonBounds();
        boolean selected = settingsCommandNum == SETTINGS_BACK || hoverSettingsCommandNum == SETTINGS_BACK;
        g2.drawImage(selected ? titleButtonHover : titleButtonDefault, bounds.x, bounds.y, bounds.width, bounds.height, null);

        g2.setFont(pixelFont.deriveFont(Font.BOLD, 28F));
        g2.setColor(selected ? new Color(92, 62, 51) : new Color(143, 103, 73));
        drawCenteredString("BACK", bounds, -2 * SETTINGS_BUTTON_SCALE);
    }

    public void drawPauseScreen() {

        g2.setFont(pixelFont.deriveFont(Font.BOLD, 56F));
        String text = "PAUSED";
        int x = getXForCenteredText(text);
        int y = gp.SCREEN_HEIGHT / 2;

        g2.drawString(text, x, y);
    }

    public void drawGameOverScreen() {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(new Color(90, 90, 90));
        g2.fillRect(0, 0, gp.SCREEN_WIDTH, gp.SCREEN_HEIGHT);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        g2.setFont(arial_80B.deriveFont(Font.BOLD, 96F));
        String text = "GAME OVER";
        int x = getXForCenteredText(text);
        int y = gp.SCREEN_HEIGHT / 2;

        g2.setColor(Color.black);
        g2.drawString(text, x + 4, y + 4);
        g2.setColor(new Color(190, 20, 20));
        g2.drawString(text, x, y);

        if (gp.isGameOverPromptReady()) {
            g2.setFont(arial_40.deriveFont(Font.PLAIN, 32F));
            text = "Press Enter to go back to title screen";
            x = getXForCenteredText(text);
            y += gp.TILE_SIZE;

            g2.setColor(Color.black);
            g2.drawString(text, x + 2, y + 2);
            g2.setColor(Color.white);
            g2.drawString(text, x, y);
        }
    }

    public void drawPlayTime() {
        g2.setFont(arial_40.deriveFont(Font.BOLD, 28F));
        g2.setColor(Color.white);

        String text = "Time: " + gp.getPlayTimeSeconds() + "s";

        int x = gp.SCREEN_WIDTH - (gp.TILE_SIZE * 5);
        int y = gp.TILE_SIZE;

        g2.setColor(Color.black);
        g2.drawString(text, x + 2, y + 2);

        g2.setColor(Color.white);
        g2.drawString(text, x, y);
    }

    public Rectangle getTitleButtonBounds(int index) {
        int width = titleButtonDefault.getWidth() * TITLE_BUTTON_SCALE;
        int height = titleButtonDefault.getHeight() * TITLE_BUTTON_SCALE;
        int gap = gp.TILE_SIZE / 2;
        int totalHeight = height * 3 + gap * 2;
        int x = gp.SCREEN_WIDTH / 2 - width / 2;
        int y = gp.SCREEN_HEIGHT / 2 - totalHeight / 2 + index * (height + gap) + gp.TILE_SIZE;
        return new Rectangle(x, y, width, height);
    }

    public Rectangle getSettingsPanelBounds() {
        int width = settingsPanel.getWidth() * SETTINGS_PANEL_SCALE;
        int height = settingsPanel.getHeight() * SETTINGS_PANEL_SCALE;
        int x = gp.SCREEN_WIDTH / 2 - width / 2;
        int y = gp.SCREEN_HEIGHT / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    public Rectangle getSettingsSliderBounds(int index) {
        Rectangle panel = getSettingsPanelBounds();
        int scale = SETTINGS_PANEL_SCALE;
        int originalY = index == SETTINGS_MUSIC ? 60 : 89;
        int width = (SETTINGS_SLIDER_TICK_SPACING * (SLIDER_TICK_COUNT - 1) + SETTINGS_SLIDER_TICK_WIDTH) * scale;
        int x = panel.x + panel.width / 2 - width / 2;
        int y = panel.y + originalY * scale;
        int height = SETTINGS_SLIDER_TICK_HEIGHT * scale;
        return new Rectangle(x, y, width, height);
    }

    public Rectangle getSettingsBackButtonBounds() {
        Rectangle panel = getSettingsPanelBounds();
        int width = titleButtonDefault.getWidth() * SETTINGS_BUTTON_SCALE;
        int height = titleButtonDefault.getHeight() * SETTINGS_BUTTON_SCALE;
        int x = panel.x + panel.width / 2 - width / 2;
        int y = panel.y + panel.height - height - 13 * SETTINGS_PANEL_SCALE;
        return new Rectangle(x, y, width, height);
    }

    private Rectangle getDialogueBoxBounds() {
        int width = dialogueBox.getWidth() * DIALOGUE_SCALE;
        int height = dialogueBox.getHeight() * DIALOGUE_SCALE;
        int x = gp.SCREEN_WIDTH / 2 - width / 2;
        int y = gp.SCREEN_HEIGHT - height - gp.TILE_SIZE / 2;
        return new Rectangle(x, y, width, height);
    }

    private void drawCenteredString(String text, Rectangle bounds, int yOffset) {
        FontMetrics fm = g2.getFontMetrics();
        int x = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent() + yOffset;
        g2.drawString(text, x, y);
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line = new StringBuilder(candidate);
                } else {
                    if (line.length() > 0) {
                        lines.add(line.toString());
                    }
                    line = new StringBuilder(word);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    public int getXForCenteredText(String text){
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.SCREEN_WIDTH / 2 - length / 2;

    }
}
