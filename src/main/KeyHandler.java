package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed, ePressed, blockPressed, dodgePressed, sprintPressed;
    //DEBUG
    public boolean checkDrawTime = false;
    public boolean checkPlayTime = false;
    public boolean showCollisionBox = false;


    public KeyHandler(GamePanel gp){
        this.gp = gp;
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        //TITLE STATE
        if (gp.gameState == gp.titleState){
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (gp.ui.commandNum != 0) {
                    gp.ui.commandNum--;
                }
            }

            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                if (gp.ui.commandNum != 2) {
                    gp.ui.commandNum++;
                }
            }

            if (code == KeyEvent.VK_ENTER){
                gp.selectTitleCommand(gp.ui.commandNum);
            }
        }
        //PLAY STATE
        if(gp.gameState == gp.playState) {
            if (code == KeyEvent.VK_W) {
                upPressed = true;
            }

            if (code == KeyEvent.VK_S) {
                downPressed = true;
            }

            if (code == KeyEvent.VK_A) {
                leftPressed = true;
            }

            if (code == KeyEvent.VK_D) {
                rightPressed = true;
            }

            if (code == KeyEvent.VK_E){
                ePressed = true;
            }

            // ACTIVE SHIELD BLOCKING: hold Shift to enter the player's defensive stance.
            if (code == KeyEvent.VK_SHIFT) {
                blockPressed = true;
            }

            if (code == KeyEvent.VK_SPACE) {
                dodgePressed = true;
            }

            if (code == KeyEvent.VK_CONTROL) {
                sprintPressed = true;
            }

            if (code == KeyEvent.VK_P) {
                gp.gameState = gp.pauseState;
            }
        }

        //PAUSE STATE
        else if (gp.gameState == gp.pauseState){

            if (code == KeyEvent.VK_P) {
                gp.gameState = gp.playState;
            }
        }

        //SETTINGS STATE
        else if (gp.gameState == gp.settingsState){

            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (gp.ui.settingsCommandNum != 0) {
                    gp.ui.settingsCommandNum--;
                }
            }

            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                if (gp.ui.settingsCommandNum != UI.SETTINGS_BACK) {
                    gp.ui.settingsCommandNum++;
                }
            }

            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                if (gp.ui.settingsCommandNum == UI.SETTINGS_MUSIC) {
                    gp.changeMusicVolume(-1);
                } else if (gp.ui.settingsCommandNum == UI.SETTINGS_SFX) {
                    gp.changeSoundEffectVolume(-1);
                }
            }

            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                if (gp.ui.settingsCommandNum == UI.SETTINGS_MUSIC) {
                    gp.changeMusicVolume(1);
                } else if (gp.ui.settingsCommandNum == UI.SETTINGS_SFX) {
                    gp.changeSoundEffectVolume(1);
                }
            }

            if (code == KeyEvent.VK_ENTER && gp.ui.settingsCommandNum == UI.SETTINGS_BACK) {
                gp.returnToTitleScreen();
            }

            if (code == KeyEvent.VK_ESCAPE) {
                gp.returnToTitleScreen();
            }
        }

        //DIALOGUE STATE
        else if (gp.gameState == gp.dialogueState){

            if (code == KeyEvent.VK_E) {
                if (gp.ui.isDialogueTyping()) {
                    gp.ui.finishDialogueLine();
                } else {
                    gp.npc[gp.player.dialogueIndex].speak();
                }
            }

            if (code == KeyEvent.VK_ENTER){
                if (gp.ui.isDialogueTyping()) {
                    gp.ui.finishDialogueLine();
                } else {
                    gp.gameState = gp.playState;
                }
            }
        }

        else if (gp.gameState == gp.gameOverState){

            if (code == KeyEvent.VK_ENTER && gp.isGameOverPromptReady()){
                gp.returnToTileScreen();
            }
        }

        //DEBUG
        if (code == KeyEvent.VK_T) {
            checkDrawTime = !checkDrawTime; // Toggles draw time
        }
        if (code == KeyEvent.VK_L) {
            checkPlayTime = !checkPlayTime; // Toggles play time
        }
        if (code == KeyEvent.VK_O) {
            showCollisionBox = !showCollisionBox; // Toggles collision boxes
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W){
            upPressed = false;
        }

        if (code == KeyEvent.VK_S){
            downPressed = false;
        }

        if (code == KeyEvent.VK_A){
            leftPressed = false;
        }

        if (code == KeyEvent.VK_D){
            rightPressed = false;
        }

        if (code == KeyEvent.VK_SHIFT){
            blockPressed = false;
        }

        if (code == KeyEvent.VK_SPACE){
            dodgePressed = false;
        }

        if (code == KeyEvent.VK_CONTROL){
            sprintPressed = false;
        }
    }

    public void resetMovementKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        ePressed = false;
        blockPressed = false;
        dodgePressed = false;
        sprintPressed = false;
    }
}
