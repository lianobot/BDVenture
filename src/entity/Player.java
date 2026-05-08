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

    public BufferedImage[] attackUp = new BufferedImage[4];
    public BufferedImage[] attackDown = new BufferedImage[4];
    public BufferedImage[] attackLeft = new BufferedImage[4];
    public BufferedImage[] attackRight = new BufferedImage[4];


    public Player(GamePanel gp, KeyHandler keyH){
        super(gp);
        this.keyH = keyH;

        screenX = gp.SCREEN_WIDTH/2 - (gp.TILE_SIZE/2);
        screenY = gp.SCREEN_HEIGHT/2 - (gp.TILE_SIZE/2);

        //Collision Box
        solidArea = new Rectangle() ;
        solidArea.x = 12 ;
        solidArea.y = 22 ;
        solidArea.height = 20;
        solidArea.width = 20 ;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        //Attack Hitbox
        attackArea.width = 36;
        attackArea.height = 36;

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
                idleDown[i] = spriteSheet.getSubimage(i * size, 0, size, size);
                idleRight[i] = spriteSheet.getSubimage(i * size, size, size, size);
                idleLeft[i] = flipImage(idleRight[i]);
                idleUp[i] = spriteSheet.getSubimage(i * size, 2 * size, size, size);


                // MOVE ANIMATIONS
                walkDown[i]  = spriteSheet.getSubimage(i * size, 3 * size, size, size);
                walkRight[i] = spriteSheet.getSubimage(i * size, 4 * size, size, size);
                walkLeft[i]  = flipImage(walkRight[i]);
                walkUp[i]    = spriteSheet.getSubimage(i * size, 5 * size, size, size);

                // ATTACK ANIMATIONS
                if (i < 4) {
                    attackDown[i] = spriteSheet.getSubimage(i * size, 6 * size, size, size);
                    attackRight[i] = spriteSheet.getSubimage(i * size, 7 * size, size, size);
                    attackLeft[i] = flipImage(attackRight[i]);
                    attackUp[i] = spriteSheet.getSubimage(i * size, 8 * size, size, size);
                }
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

        if(attacking){
            attacking();
        }
        else {
            // CHECK NPC COLLISION
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interactNPC(npcIndex);


            if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
                isIdle = false;

                if (keyH.upPressed) { direction = "up"; }
                if (keyH.downPressed) { direction = "down"; }
                if (keyH.leftPressed) { direction = "left"; }
                if (keyH.rightPressed) { direction = "right"; }

                // Normalize Speed
                double currentSpeed = speed;

                // Check if moving diagonally
                boolean isDiagonal = (keyH.upPressed || keyH.downPressed) && (keyH.leftPressed || keyH.rightPressed);

                if (isDiagonal) {
                    currentSpeed *= 0.85;
                }

                // CHECK TILE COLLISION
                collisionOn = false;
                gp.cChecker.checkTile(this);

                // CHECK OBJECT COLLISION
                int objIndex = gp.cChecker.checkObject(this, true);
                pickUpObject(objIndex);

                // CHECK MONSTER COLLISION
                int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
                contactMonster(monsterIndex);

                // CHECK EVENT
                gp.eventHandler.checkEvent();

                // IF COLLISION FALSE, PLAYER MAY MOVE
                if (!collisionOn && !keyH.ePressed) {
                    if (keyH.upPressed) { worldY -= (int)currentSpeed; }
                    if (keyH.downPressed) { worldY += (int)currentSpeed; }
                    if (keyH.leftPressed) { worldX -= (int)currentSpeed; }
                    if (keyH.rightPressed) { worldX += (int)currentSpeed; }
                }
            } else {
                isIdle = true;
            }
        }

        // ANIMATION TICKER
        spriteCounter++;
        if (!attacking) {
            if (spriteCounter >= 13) {
                spriteNum++;
                if (spriteNum >= 6) { // Loop back to 0 after the 6th frame
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        }

        if (invincible){
            invincibleCounter++;
            if(invincibleCounter > 120){
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    public void attacking() {

        spriteCounter++;

        if (spriteCounter <= 10) {
            spriteNum = 0;
        }

        if (spriteCounter > 10 && spriteCounter <= 20) {
            spriteNum = 1;
        }

        if (spriteCounter > 20 && spriteCounter <= 30) {
            spriteNum = 2;

            // Save current worldX, worldY, solidArea
            int currentWorldX = worldX;
            int currentWorldY = worldY;
            int solidAreaWidth = solidArea.width;
            int solidAreaHeight = solidArea.height;

            // Adjust player's worldX/Y for attackArea
            switch (direction){
                case "up" : worldY -= attackArea.height; break;
                case "down" : worldY += attackArea.height; break;
                case "left" : worldX -= attackArea.width; break;
                case "right" : worldX += attackArea.width; break;
            }

            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            damageMonster(monsterIndex);

            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidth;
            solidArea.height = solidAreaHeight;

        }

        if (spriteCounter > 30 && spriteCounter <= 40) {
            spriteNum = 3;
        }

        if (spriteCounter > 40){
            spriteNum = 0;
            spriteCounter = 0;
            attacking = false;
        }

    }

    public void pickUpObject(int i) {
        if (i != 999) {
            //add objects
        }
    }

    public void interactNPC(int i){
        if (gp.keyH.ePressed) {
            if (i != 999) {
                gp.gameState = gp.dialogueState;
                gp.npc[i].speak();
            } else {
                // Only start attack if we aren't already attacking
                if (!attacking) {
                    attacking = true;
                    spriteCounter = 0; // Reset counter for animation
                    spriteNum = 0;
                }
            }
        }
        gp.keyH.ePressed = false;
    }

    public void contactMonster(int i){

        if (i != 999){

            if (!invincible) {
                life -= 1;
                invincible = true;
            }
        }
    }

    private void damageMonster(int i) {

        if (i != 999 ){

            if (!gp.monster[i].invincible){

                gp.monster[i].life -= 1;
                gp.monster[i].invincible = true;

                if (gp.monster[i].life <= 0){
                    gp.monster[i] = null;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        if (attacking) {
            image = switch (direction){
                case "up" -> attackUp[spriteNum];
                case "down" -> attackDown[spriteNum];
                case "left" -> attackLeft[spriteNum];
                case "right" -> attackRight[spriteNum];
                default -> image;

            };
        } else if (isIdle){
            image = switch (direction) {
                case "up" -> idleUp[spriteNum];
                case "down" -> idleDown[spriteNum];
                case "left" -> idleLeft[spriteNum];
                case "right" -> idleRight[spriteNum];
                default -> image;
            };
        } else {
            image = switch (direction) {
                case "up" -> walkUp[spriteNum];
                case "down" -> walkDown[spriteNum];
                case "left" -> walkLeft[spriteNum];
                case "right" -> walkRight[spriteNum];
                default -> image;
            };
        }
        double multiplier = 2.5;
        int drawSize = (int) (gp.TILE_SIZE * multiplier);

        // Offset the X and Y so the character stays centered on their collision box
        int x = screenX - (drawSize / 3);
        int y = screenY - (drawSize / 2);

        if(invincible){
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.7f));
        }

        g2.drawImage(image, x, y, drawSize, drawSize, null);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));

//        DEBUG: SHOWS COLLISION BOX
        g2.setColor(Color.red);
        g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);

//        g2.setFont(new Font("Arial", Font.PLAIN, 26));
//        g2.setColor(Color.white);
//        g2.drawString("Invincible: " + invincibleCounter, 10, 400);

        }
    }