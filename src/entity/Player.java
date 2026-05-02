package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity{

    GamePanel gp;
    KeyHandler keyH;
    int hasKey = 0;

    public final int screenX;
    public final int screenY;

    public Player(GamePanel gp, KeyHandler keyH){
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.SCREEN_WIDTH/2 - (gp.TILE_SIZE/2);
        screenY = gp.SCREEN_HEIGHT/2 - (gp.TILE_SIZE/2);

        SolidArea = new Rectangle() ;
        SolidArea.x=8 ;
        SolidArea.y=16 ;
        SolidArea.height=32;
        SolidArea.width=32 ;
        solidAreaDefaultX = SolidArea.x;
        solidAreaDefaultY = SolidArea.y;

        setDefaultvalues();
        getPlayerImage();
    }
    public void setDefaultvalues(){

        worldX = gp.TILE_SIZE * 23;
        worldY = gp.TILE_SIZE * 21;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage(){
        try {

            up1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_up_2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_down_2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_left_2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/player/boy_right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/player/boy_right_2.png"));

        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void update(){

        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

            if (keyH.upPressed) {
                direction = "up";

            } else if (keyH.downPressed) {
                direction = "down";

            } else if (keyH.leftPressed) {
                direction = "left";

            } else if (keyH.rightPressed) {
                direction = "right";

            }
             CollisionOn=false ;
            gp.cChecker.checkTile(this) ;
            int objIndex = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndex);
            //If collision is false//
            if(CollisionOn==false){
                switch(direction){
                    case"up":
                        worldY -= speed;
                        break;
                    case "down":
                        worldY += speed;
                        break;
                    case "left":
                        worldX -= speed;
                        break;
                    case"right":
                        worldX += speed;
                        break ;
                }

            }

            spriteCounter++;
            if (spriteCounter >= 12) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }

    }
    public void draw(Graphics2D g2){

//        g2.setColor(Color.white);
//        g2.fillRect(x,y, gp.TILE_SIZE, gp.TILE_SIZE);

        BufferedImage image = null;

        switch(direction){
            case "up":
                if (spriteNum == 1) {
                    image = up1;
                }
                if (spriteNum == 2) {
                    image = up2;
                }
                break;
            case "down":
                if (spriteNum == 1) {
                    image = down1;
                }
                if (spriteNum == 2) {
                    image = down2;
                }
                break;
            case "left":
                if (spriteNum == 1) {
                    image = left1;
                }
                if (spriteNum == 2) {
                    image = left2;
                }
                break;
            case "right":
                if (spriteNum == 1){
                    image = right1;
                }
                if (spriteNum == 2) {
                    image = right2;
                }
                break;
        }
        g2.drawImage(image, screenX, screenY, gp.TILE_SIZE, gp.TILE_SIZE, null);

    }
    public void pickUpObject(int i) {
        if (i != 999) {
            String objectName = gp.obj[i].name;

            switch (objectName) {
                case "Key":
                    gp.obj[i] = null;
                    gp.playSE(1);
                    hasKey++;
                    System.out.println("Key picked up! Keys: " + hasKey);
                    break;
                case "Door":
                    if (hasKey > 0) {
                        gp.obj[i] = null;
                        gp.playSE(3);
                        hasKey--;
                        System.out.println("Door opened! Keys left: " + hasKey);
                    } else {
                        System.out.println("You need a key!");
                    }
                    break;
                case "Chest":
                    gp.obj[i] = null;
                    System.out.println("You found treasure!");
                    break;
            }
        }
    }
}