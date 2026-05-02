package main;

import object.OBJ_Key;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class UI {

    GamePanel gp;
    Font arial_40, arial_80B;
    BufferedImage keyImage;
    public Boolean messageON = false;
    public String message = "";
    int messageCounter = 0;
    public Boolean gameFinished = false;

    double playTime;
    DecimalFormat decimalFormat = new DecimalFormat("#0.00");


    public UI(GamePanel gp){
        this.gp = gp;

        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 40);
        OBJ_Key key = new OBJ_Key();
        keyImage = key.image;
    }

    public void showMessage(String text){

        message = text;
        messageON = true;
    }

    public void draw(Graphics2D g2){

        if(gameFinished){
            String text;
            int textLenght;
            int x;
            int y;


            g2.setFont(arial_40);
            g2.setColor(Color.white);
            text = "You found the Treasure!";
            textLenght = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            x = gp.SCREEN_WIDTH/2 - textLenght/2;
            y = gp.SCREEN_HEIGHT/2 - (gp.TILE_SIZE*3);
            g2.drawString(text, x, y);

            g2.setFont(arial_80B);
            g2.setColor(Color.yellow);
            text = "Congratulations";
            textLenght = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            x = gp.SCREEN_WIDTH/2 - textLenght/2;
            y = gp.SCREEN_HEIGHT/2 + (gp.TILE_SIZE*2);
            g2.drawString(text, x, y);

            gp.gameThread = null;


        }
        else {

            g2.setFont(arial_40);
            g2.setColor(Color.white);
            g2.drawImage(keyImage, gp.TILE_SIZE / 2, gp.TILE_SIZE / 2, gp.TILE_SIZE, gp.TILE_SIZE, null);
            g2.drawString("= " + gp.player.hasKey, 74, 67);

            //TIME
            playTime +=(double) 1/60;
            g2.drawString("Time: "+ decimalFormat.format(playTime), gp.TILE_SIZE*11, 65);

            //MESSAGE
            if (messageON) {

                g2.setFont(g2.getFont().deriveFont(30F));
                g2.drawString(message, gp.TILE_SIZE / 2, gp.TILE_SIZE * 5);
                messageCounter++;

                if (messageCounter > 120) {
                    messageCounter = 0;
                    messageON = false;
                }
            }
        }
    }
}
