package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {

    GamePanel gp;
    public int worldX, worldY;
    public int speed;
    public int sizeScale = 1;
    public int type;

    //Arrays for direction of entities
    public BufferedImage[] up, down, left, right;
    public String direction = "down";

    public int spriteCounter = 0;
    public int spriteNum = 0;

    public Rectangle solidArea = new Rectangle(0,0,48,48);
    public boolean collisionOn = false;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public int actionLockCounter = 0;
    String[] dialogues = new String[20];
    public int dialogueIndex = 0;
    public BufferedImage image, image2, image3;
    public String name;
    public boolean collision = false;

    // CHARACTER STATUS
    public boolean invincible = false;
    public int invincibleCounter;
    public int maxLife;
    public int life;


    public Entity(GamePanel gp){
        this.gp = gp;
    }

    public void setAction(){}
    public void speak(){

        if (dialogues[dialogueIndex] == null){
            dialogueIndex = 0;
            gp.gameState = gp.playState;
        } else {
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            dialogueIndex++;
        }

        switch (gp.player.direction){
            case "up":
                direction = "down";
                break;
            case "down":
                direction = "up";
                break;
            case "left":
                direction = "right";
                break;
            case "right":
                direction = "left";
                break;
        }
    }
    public void update(){
        setAction();

        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        gp.cChecker.checkEntity(this, gp.npc);
        gp.cChecker.checkEntity(this, gp.monster);
        boolean contactPlayer = gp.cChecker.checkPlayer(this);

        if(this.type == 2 && contactPlayer){
            if(!gp.player.invincible){
                //we give damage
                gp.player.life -= 1;
                gp.player.invincible = true;
            }
        }

        //IF COLLISION FALSE, ENTITY MAY MOVE
        if (!collisionOn) {
            switch (direction) {
                case "up": worldY -= speed; break;
                case "down": worldY += speed; break;
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }

            spriteCounter++;
            if (spriteCounter >= 12) {
                spriteNum++;

                BufferedImage[] currentArray = getCurrentAnimationArray();

                if (currentArray != null && spriteNum >= currentArray.length) {
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        }
    }

    // Helper to get the correct array for the current direction
    public BufferedImage[] getCurrentAnimationArray() {
        return switch (direction) {
            case "up" -> up;
            case "down" -> down;
            case "left" -> left;
            case "right" -> right;
            default -> null;
        };
    }

    public void draw(Graphics2D g2){

        BufferedImage image = null;
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Frustum Culling (Only draw if on screen)
        if (worldX + gp.TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                worldX - gp.TILE_SIZE < gp.player.worldX + gp.player.screenX &&
                worldY + gp.TILE_SIZE > gp.player.worldY - gp.player.screenY &&
                worldY - gp.TILE_SIZE < gp.player.worldY + gp.player.screenY) {

            BufferedImage[] currentArray = getCurrentAnimationArray();

            if (currentArray != null && spriteNum < currentArray.length) {
                image = currentArray[spriteNum];
            }

            if (image != null) {
                g2.drawImage(image, screenX, screenY, gp.TILE_SIZE * sizeScale, gp.TILE_SIZE* sizeScale, null);
            }

        }

        g2.setColor(Color.red);
        g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);
    }
}