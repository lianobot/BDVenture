package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {

    protected GamePanel gp;

    // --- POSITION & PHYSICAL PROPERTIES ---
    public int worldX, worldY;          // Coordinates in the game world
    public int speed;                   // Movement speed of the entity
    public double sizeScale = 1;        // Visual scale multiplier
    public int type;                    // 0 = player, 1 = npc, 2 = monster
    public String name;                 // Identification name
    public boolean collision = false;   // Whether the entity is solid/collidable

    // --- SPRITES & ANIMATION ---
    public BufferedImage[] idleUp, idleDown, idleLeft, idleRight;
    public BufferedImage[] walkUp, walkDown, walkLeft, walkRight;
    public BufferedImage[] attackUp, attackDown, attackLeft, attackRight;
    public BufferedImage[] damageUp, damageDown, damageLeft, damageRight;
    public BufferedImage[] dieUp, dieDown, dieLeft, dieRight;
    public String direction = "down";              // Current facing direction
    public int spriteNum = 0;                      // Current frame index in the array
    public int spriteCounter = 0;                  // Timer to switch between frames
    public BufferedImage image, image2, image3;    // Individual/static sprites

    // --- COLLISION DETECTION ---
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); // Hitbox area
    public Rectangle attackArea = new Rectangle(0, 0, 0, 0); // Attack Hitbox area
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
    public boolean attacking = false;     // Attacking state
    public boolean dying = false;         // Dying state
    public boolean damaged = false;       // Damaged state
    public boolean alive = true;          // Tracks if entity exists
    public int dyingCounter = 0;          // Timer for the death animation
    public int attack = 0;
    public int defense = 0;


    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void setAction() {
    }

    public void speak() {

        if (dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
            gp.gameState = gp.playState;
        } else {
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            dialogueIndex++;
        }

        switch (gp.player.direction) {
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

    public void update() {

        // 1. MONSTER Death Guard
        if (dying) {
            dyingAnimation();
            return;
        }

        // 2. Entity Ai
        setAction();

        // 3. Entity Collision Checks
        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        gp.cChecker.checkEntity(this, gp.npc);
        gp.cChecker.checkEntity(this, gp.monster);

        // Check Entity contact with player
        boolean contactPlayer = gp.cChecker.checkPlayer(this);

        // Player takes damage if contact and MONSTER not dying
        if (this.type == 2 && contactPlayer && !dying) {
            if (!gp.player.invincible) {
                //player gets damaged
                gp.player.life -= 1;
                gp.player.invincible = true;
            }
        }

        // 4. Entity Movement
        if (!collisionOn) {
            switch (direction) {
                case "up" -> worldY -= speed;
                case "down" -> worldY += speed;
                case "left" -> worldX -= speed;
                case "right" -> worldX += speed;
            }
        }

        // 5. Entity Animation
        spriteCounter++;
        if (spriteCounter >= 12) {
            spriteNum++;
            BufferedImage[] currentArray = getActiveArray();

            if (currentArray != null && spriteNum >= currentArray.length) {
                spriteNum = 0;
            }
            spriteCounter = 0;
        }

        // 6. Entity Invincibility
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 45) {
                invincible = false;
                invincibleCounter = 0;
                damaged = false;
            }
        }

    }


    // Helper to get dying animation
    private void dyingAnimation() {
        dyingCounter++;

        // Remove hitbox
        solidArea.width = 0;
        solidArea.height = 0;

        // Animate death frames
        spriteCounter++;
        if (spriteCounter >= 8) { // Speed of death animation
            spriteNum++;

            BufferedImage[] dieArray = getActiveArray(); // Will return dieDown/Up etc.
            if (dieArray != null && spriteNum >= dieArray.length) {
                spriteNum = dieArray.length - 1; // Stay on last frame
            }
            spriteCounter = 0;
        }

        if (dyingCounter > 40) {
            alive = false; // Final removal
        }
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

            BufferedImage[] currentArray = getActiveArray();


            // GET AND DRAW IMAGE
            if (currentArray != null && spriteNum < currentArray.length) {
                image = currentArray[spriteNum];
            }


            if (image != null) {

                int drawSize = (int)(gp.TILE_SIZE * sizeScale);

                int x = screenX;
                int y = screenY - (drawSize / 4);

                if(invincible){
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
                }
                g2.drawImage(image, x, y, (int)(gp.TILE_SIZE * sizeScale), (int)(gp.TILE_SIZE* sizeScale), null);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
            }



        }

        g2.setColor(Color.red);
        g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);
    }

    // Helper to get the correct array for the current direction
    public BufferedImage[] getActiveArray() {
        if (dying) {
            return switch(direction) { case "up" -> dieUp; case "left" -> dieLeft; case "right" -> dieRight; default -> dieDown; };
        }
        if (damaged) {
            return switch(direction) { case "up" -> damageUp; case "left" -> damageLeft; case "right" -> damageRight; default -> damageDown; };
        }
        if (attacking) {
            return switch(direction) { case "up" -> attackUp; case "left" -> attackLeft; case "right" -> attackRight; default -> attackDown; };
        }
        if (!collisionOn && speed > 0) {
            return switch(direction) { case "up" -> walkUp; case "left" -> walkLeft; case "right" -> walkRight; default -> walkDown; };
        }
        return switch(direction) { case "up" -> idleUp; case "left" -> idleLeft; case "right" -> idleRight; default -> idleDown; };
    }

}