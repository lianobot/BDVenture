package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {

    GamePanel gp;

    // --- POSITION & PHYSICAL PROPERTIES ---
    public int worldX, worldY;          // Coordinates in the game world
    public int speed;                   // Movement speed of the entity
    public int sizeScale = 1;           // Visual scale multiplier
    public int type;                    // 0 = player, 1 = npc, 2 = monster
    public String name;                 // Identification name
    public boolean collision = false;   // Whether the entity is solid/collidable

    // --- SPRITES & ANIMATION ---
    public BufferedImage[] up, down, left, right;  // Animation frame arrays
    public String direction = "down";              // Current facing direction
    public int spriteNum = 0;                      // Current frame index in the array
    public int spriteCounter = 0;                  // Timer to switch between frames
    public BufferedImage image, image2, image3;    // Individual/static sprites

    // --- COLLISION DETECTION ---
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); // Hitbox area
    public Rectangle attackArea = new Rectangle(0,0,0,0); // Attack Hitbox area
    public int solidAreaDefaultX, solidAreaDefaultY;         // Reset coordinates for hitbox
    public boolean collisionOn = false;                       // State of current collision

    // --- DIALOGUE & BEHAVIOR ---
    public int actionLockCounter = 0;     // Timer to prevent rapid action switching
    String[] dialogues = new String[20];  // Storage for text lines
    public int dialogueIndex = 0;         // Current line being spoken

    // --- CHARACTER STATUS ---
    public int maxLife;                   // Maximum health capacity
    public int life;                      // Current health points
    public boolean invincible = false;    // Damage immunity state
    public int invincibleCounter = 0;     // Timer for how long immunity lasts
    boolean attacking = false;            // Attacking state


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

        if (invincible){
            invincibleCounter++;
            if(invincibleCounter > 45){
                invincible = false;
                invincibleCounter = 0;
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
                if(invincible){
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.7f));
                }
                g2.drawImage(image, screenX, screenY, gp.TILE_SIZE * sizeScale, gp.TILE_SIZE* sizeScale, null);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
            }



        }

        g2.setColor(Color.red);
        g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);
    }
}