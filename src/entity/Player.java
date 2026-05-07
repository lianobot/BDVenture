package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity{

    KeyHandler keyH;
    public final int screenX;
    public final int screenY;

    // Player State tracker
    public boolean isIdle = true;

    // Arrays to hold our 6-frame animations
    public BufferedImage[] idleUp = new BufferedImage[6];
    public BufferedImage[] idleDown = new BufferedImage[6];
    public BufferedImage[] idleLeft = new BufferedImage[6];
    public BufferedImage[] idleRight = new BufferedImage[6];

    public BufferedImage[] walkUp = new BufferedImage[6];
    public BufferedImage[] walkDown = new BufferedImage[6];
    public BufferedImage[] walkLeft = new BufferedImage[6];
    public BufferedImage[] walkRight = new BufferedImage[6];


    public Player(GamePanel gp, KeyHandler keyH){
        super(gp);
        this.keyH = keyH;

        screenX = gp.SCREEN_WIDTH/2 - (gp.TILE_SIZE/2);
        screenY = gp.SCREEN_HEIGHT/2 - (gp.TILE_SIZE/2);

        //Collision Box
        solidArea = new Rectangle() ;
        solidArea.x=12 ;
        solidArea.y=22 ;
        solidArea.height=20;
        solidArea.width=20 ;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        spriteNum = 0;

        setDefaultValues();
        getImage();
    }
    public void setDefaultValues(){

        worldX = gp.TILE_SIZE * 78;
        worldY = gp.TILE_SIZE * 63;

        speed = 4;
        direction = "down";

        //PLAYER STATUS
        maxLife = 6;
        life = maxLife;
    }

    public void getImage(){
        try {
            // Read the main 48x48 sprite sheet
            BufferedImage spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/player.png")));
            int size = 48;

            for (int i = 0; i < 6; i++) {
                // IDLE ANIMATIONS
                idleDown[i]  = spriteSheet.getSubimage(i * size, 0, size, size);
                idleRight[i] = spriteSheet.getSubimage(i * size, size, size, size);
                idleLeft[i]  = flipImage(idleRight[i]);
                idleUp[i]    = spriteSheet.getSubimage(i * size, 2 * size, size, size);

                // MOVE ANIMATIONS
                walkDown[i]  = spriteSheet.getSubimage(i * size, 3 * size, size, size);
                walkRight[i] = spriteSheet.getSubimage(i * size, 4 * size, size, size);
                walkLeft[i]  = flipImage(walkRight[i]);
                walkUp[i]    = spriteSheet.getSubimage(i * size, 5 * size, size, size);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // HELPER FUNCTION : CONVERT RIGHT IMAGE TO LEFT
    public BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g.dispose();
        return flipped;
    }

    public void update(){

        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            isIdle = false;

            if (keyH.upPressed) {
                direction = "up";
            } else if (keyH.downPressed) {
                direction = "down";
            } else if (keyH.leftPressed) {
                direction = "left";
            } else {
                direction = "right";
            }

            // CHECK TILE COLLISION
            collisionOn = false ;
            gp.cChecker.checkTile(this) ;

            // CHECK NPC COLLISION
            int npcIndex = gp.cChecker.checkEntity(this,gp.npc);
            interactNPC(npcIndex);

            // CHECK OBJECT COLLISION
            int objIndex = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndex);

            // CHECK EVENT
            gp.eventHandler.checkEvent();

            // IF COLLISION FALSE, PLAYER MAY MOVE
            if (!collisionOn) {
                switch (direction) {
                    case "up":    worldY -= speed; break;
                    case "down":  worldY += speed; break;
                    case "left":  worldX -= speed; break;
                    case "right": worldX += speed; break;
                }
            }
        } else {
            isIdle = true;
        }

        // ANIMATION TICKER
        spriteCounter++;
        if (spriteCounter >= 13) {
            spriteNum++;
            if (spriteNum >= 6) { // Loop back to 0 after the 6th frame
                spriteNum = 0;
            }
            spriteCounter = 0;
        }
    }

    public void pickUpObject(int i) {
        if (i != 999) {
            //add objects
        }
    }

    public void interactNPC(int i){
        if (i != 999) {

            if (gp.keyH.ePressed) {
                gp.gameState = gp.dialogueState;
                gp.npc[i].speak();
            }
        }
        gp.keyH.ePressed = false;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        if (isIdle) {
            switch (direction) {
                case "up":    image = idleUp[spriteNum];    break;
                case "down":  image = idleDown[spriteNum];  break;
                case "left":  image = idleLeft[spriteNum];  break;
                case "right": image = idleRight[spriteNum]; break;
            }
        } else {
            switch (direction) {
                case "up":    image = walkUp[spriteNum];    break;
                case "down":  image = walkDown[spriteNum];  break;
                case "left":  image = walkLeft[spriteNum];  break;
                case "right": image = walkRight[spriteNum]; break;
            }
        }
        double multiplier = 2.5;
        int drawSize = (int) (gp.TILE_SIZE * multiplier);

        // Offset the X and Y so the character stays centered on their collision box
        int x = screenX - (drawSize / 3);
        int y = screenY - (drawSize / 2);


        g2.drawImage(image, x, y, drawSize, drawSize, null);

//        DEBUG: SHOWS COLLISION BOX
//        g2.setColor(Color.red);
//        g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);
        }
    }