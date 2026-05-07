package main;

import object.OBJ_Heart;
import object.SuperObject;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UI {

    GamePanel gp;
    Graphics2D g2;
    Font arial_40, arial_80B;
    BufferedImage heart_full, heart_half, heart_blank;
    public Boolean messageON = false;
    public String message = "";
    public String currentDialogue;
    public int commandNum = 0;
//    int messageCounter = 0;
//    public Boolean gameFinished = false;

//    double playTime;
//    DecimalFormat decimalFormat = new DecimalFormat("#0.00");


    public UI(GamePanel gp){
        this.gp = gp;

        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 40);

        //CREATE HUG OBJECT
        SuperObject heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;
    }

    public void showMessage(String text){

        message = text;
        messageON = true;
    }

    public void draw(Graphics2D g2){

        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        //TitleState
        if (gp.gameState == gp.titleState){
            drawTitleScreen();
        }

        //PlayState
        if (gp.gameState == gp.playState){
            drawPlayerLife();
        }

        //PauseState
        if (gp.gameState == gp.pauseState){
            drawPlayerLife();
            drawPauseScreen();
        }

        //DialogueState
        if (gp.gameState == gp.dialogueState){
            drawPlayerLife();
            drawDialogueScreen();
        }
    }

    public void drawPlayerLife(){

        int x = gp.TILE_SIZE/2;
        int y = gp.TILE_SIZE/2;
        int i = 0;

        //DRAW MAX LIFE
        while (i < gp.player.maxLife/2){
            g2.drawImage(heart_blank,x,y,gp.TILE_SIZE, gp.TILE_SIZE,null);
            i++;
            x += gp.TILE_SIZE;
        }

        //RESET
        x = gp.TILE_SIZE/2;
        y = gp.TILE_SIZE/2;
        i = 0;

        //DRAW CURRENT LIFE
        while (i < gp.player.life){
            g2.drawImage(heart_half,x,y,gp.TILE_SIZE, gp.TILE_SIZE, null);
            i++;
            if (i < gp.player.life){
                g2.drawImage(heart_full,x,y,gp.TILE_SIZE, gp.TILE_SIZE,null);
            }
            i++;
            x += gp.TILE_SIZE;
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

    public void drawTitleScreen(){

        g2.setColor(new Color(70,120,80));
        g2.fillRect(0,0,gp.SCREEN_WIDTH,gp.SCREEN_HEIGHT);

        //TITLE NAME
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
        String text = "The Game.";
        int x = getXForCenteredText(text);
        int y = gp.TILE_SIZE * 3;

        //SHADOW
        g2.setColor(Color.BLACK);
        g2.drawString(text,x+5,y+5);
        //MAIN COLOR
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        //MC IMAGE
        x = gp.SCREEN_WIDTH / 2 - (gp.TILE_SIZE*2);
        y += (gp.TILE_SIZE * 2);
        g2.drawImage(gp.player.idleDown[0],x,y, gp.TILE_SIZE*4, gp.TILE_SIZE*4, null);

        //MENU
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

        text = "New Game";
        x = getXForCenteredText(text);
        y += gp.TILE_SIZE*6;
        g2.drawString(text,x,y);
        if(commandNum == 0){
            g2.drawString(">",x-gp.TILE_SIZE,y);
        }

        text = "Load Game";
        x = getXForCenteredText(text);
        y += gp.TILE_SIZE;
        g2.drawString(text,x,y);
        if(commandNum == 1){
            g2.drawString(">",x-gp.TILE_SIZE,y);
        }

        text = "Quit";
        x = getXForCenteredText(text);
        y += gp.TILE_SIZE;
        g2.drawString(text,x,y);
        if(commandNum == 2){
            g2.drawString(">",x-gp.TILE_SIZE,y);
        }
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
