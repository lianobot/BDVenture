package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed, ePressed;
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
            if (code == KeyEvent.VK_W) {
                if (gp.ui.commandNum != 0) {
                    gp.ui.commandNum--;
                }
            }

            if (code == KeyEvent.VK_S) {
                if (gp.ui.commandNum != 2) {
                    gp.ui.commandNum++;
                }
            }

            if (code == KeyEvent.VK_ENTER){
                if (gp.ui.commandNum == 0) {
                    gp.gameState = gp.playState;
                    gp.playMusic(0);
                }
                else if (gp.ui.commandNum == 1){
                    //add loading option
                }
                else if (gp.ui.commandNum == 2){
                    System.exit(0);
                }
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

        //DIALOGUE STATE
        else if (gp.gameState == gp.dialogueState){

            if (code == KeyEvent.VK_E) {
                gp.npc[gp.player.dialogueIndex].speak();
            }

            if (code == KeyEvent.VK_ENTER){
                gp.gameState = gp.playState;
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
    }

    public void resetMovementKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        ePressed = false;
    }
}
