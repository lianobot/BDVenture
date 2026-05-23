package main;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseHandler implements MouseListener, MouseMotionListener {

    GamePanel gp;

    public MouseHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gp.requestFocusInWindow();

        if (gp.gameState == gp.titleState) {
            int command = getTitleCommandAt(e.getPoint());
            if (command != -1) {
                gp.selectTitleCommand(command);
            }
        } else if (gp.gameState == gp.settingsState) {
            handleSettingsClick(e.getPoint());
        }
    }

    private void handleSettingsClick(Point point) {
        for (int i = 0; i < 2; i++) {
            Rectangle slider = gp.ui.getSettingsSliderBounds(i);
            Rectangle clickableSlider = new Rectangle(slider.x - 12, slider.y - 16, slider.width + 24, slider.height + 32);
            if (clickableSlider.contains(point)) {
                gp.ui.settingsCommandNum = i;
                int scale = slider.height / 12;
                int relativeX = Math.max(0, Math.min(slider.width, point.x - slider.x));
                int value = Math.round(relativeX / (5F * scale));
                if (i == UI.SETTINGS_MUSIC) {
                    gp.setMusicVolume(value);
                } else {
                    gp.setSoundEffectVolume(value);
                }
                return;
            }
        }

        if (gp.ui.getSettingsBackButtonBounds().contains(point)) {
            gp.returnToTitleScreen();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        gp.ui.hoverCommandNum = -1;
        gp.ui.hoverSettingsCommandNum = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);

        if (gp.gameState == gp.settingsState) {
            handleSettingsClick(e.getPoint());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gp.gameState == gp.titleState) {
            gp.ui.hoverCommandNum = getTitleCommandAt(e.getPoint());
        } else {
            gp.ui.hoverCommandNum = -1;
        }

        if (gp.gameState == gp.settingsState) {
            gp.ui.hoverSettingsCommandNum = getSettingsCommandAt(e.getPoint());
        } else {
            gp.ui.hoverSettingsCommandNum = -1;
        }
    }

    private int getTitleCommandAt(Point point) {
        for (int i = 0; i < 3; i++) {
            if (gp.ui.getTitleButtonBounds(i).contains(point)) {
                return i;
            }
        }
        return -1;
    }

    private int getSettingsCommandAt(Point point) {
        for (int i = 0; i < 2; i++) {
            Rectangle slider = gp.ui.getSettingsSliderBounds(i);
            Rectangle clickableSlider = new Rectangle(slider.x - 12, slider.y - 16, slider.width + 24, slider.height + 32);
            if (clickableSlider.contains(point)) {
                return i;
            }
        }

        if (gp.ui.getSettingsBackButtonBounds().contains(point)) {
            return UI.SETTINGS_BACK;
        }
        return -1;
    }
}
