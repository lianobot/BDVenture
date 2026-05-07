package main;

import java.awt.*;

public class UI {

    GamePanel gp;
    Graphics2D g2;
    Font arial_40, arial_80B;
    public Boolean messageON = false;
    public String message = "";
    public String currentDialogue;
//    int messageCounter = 0;
//    public Boolean gameFinished = false;

//    double playTime;
//    DecimalFormat decimalFormat = new DecimalFormat("#0.00");


    public UI(GamePanel gp){
        this.gp = gp;

        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 40);
    }

    public void showMessage(String text){

        message = text;
        messageON = true;
    }

    public void draw(Graphics2D g2){

        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        //PlayState
        if (gp.gameState == gp.playState){
            // Make play state stuff later
        }

        //PauseState
        if (gp.gameState == gp.pauseState){
            drawPauseScreen();
        }

        //DialogueState
        if (gp.gameState == gp.dialogueState){
            drawDialogueScreen();
        }
    }

    public void drawDialogueScreen(){

        //WINDOW
        int x = gp.TILE_SIZE * 2;
        int y = gp.TILE_SIZE * 10;
        int width = gp.SCREEN_WIDTH - (gp.TILE_SIZE * 4);
        int height = gp.TILE_SIZE * 5;
        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        x += gp.TILE_SIZE;
        y += gp.TILE_SIZE;

        //Split the dialogue when drawing to fit in dialogue box
        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, x, y);
            y += 40;
        }
    }

    public void drawSubWindow(int x, int y, int width, int height){

        Color c = new Color(0,0,0, 210);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255,255,255);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x+5, y+5, width-10, height-10, 25, 25);
    }

    public void drawPauseScreen(){

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN,60));
        String text = "Paused";
        int x = getXForCenteredText(text);
        int y = gp.SCREEN_HEIGHT/2;

        g2.drawString(text, x, y);
    }

    public int getXForCenteredText(String text){
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.SCREEN_WIDTH/2 - length/2;

    }
}
