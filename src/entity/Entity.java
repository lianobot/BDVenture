package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {

    protected GamePanel gp;

    // --- POSITION & PHYSICAL PROPERTIES ---
    public int worldX, worldY;          // Coordinates in the game world
    public int speed;                   // Movement speed of the entity
    public int sizeScale = 1;           // Visual scale multiplier
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

        if (dying) {
            dyingAnimation();
        } else if (attacking) {
            monsterAttack();
        } else {
            setAction();

            collisionOn = false;
            gp.cChecker.checkTile(this);
            gp.cChecker.checkObject(this, false);
            gp.cChecker.checkEntity(this, gp.npc);
            gp.cChecker.checkEntity(this, gp.monster);
            boolean contactPlayer = gp.cChecker.checkPlayer(this);

            if (this.type == 2 && contactPlayer) {
                if (!gp.player.invincible) {
                    //player gets damaged
                    gp.player.life -= 1;
                    gp.player.invincible = true;
                }
            }

            //IF COLLISION FALSE, ENTITY MAY MOVE
            if (!collisionOn) {
                switch (direction) {
                    case "up":
                        worldY -= speed;
                        break;
                    case "down":
                        worldY += speed;
                        break;
                    case "left":
                        worldX -= speed;
                        break;
                    case "right":
                        worldX += speed;
                        break;
                }
            }

            //Normal Animation
            spriteCounter++;
            if (spriteCounter >= 12) {
                spriteNum++;
                BufferedImage[] currentArray = getActiveArray();
                if (currentArray != null && spriteNum >= currentArray.length) {
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }

            if (invincible) {
                invincibleCounter++;
                if (invincibleCounter > 45) {
                    invincible = false;
                    invincibleCounter = 0;
                    damaged = false;
                }
            }
        }
    }


    // Helper to get dying animation
    private void dyingAnimation() {
        dyingCounter++;

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

    public void monsterAttack() {
        spriteCounter++;

        // 1. STARTUP: The "Wind-up" phase (Frame 0-1)
        if (spriteCounter <= 5) {
            spriteNum = 0;
        }
        if (spriteCounter > 5 && spriteCounter <= 15) {
            spriteNum = 1;
        }

        // 2. ACTIVE: The "Hit" phase (Frame 2)
        if (spriteCounter > 15 && spriteCounter <= 25) {
            spriteNum = 2;

            // Save current solidArea to restore it later
            int currentWorldX = worldX;
            int currentWorldY = worldY;
            int solidAreaWidth = solidArea.width;
            int solidAreaHeight = solidArea.height;

            // Shift the entity's position/hitbox to represent the "Attack Area"
            switch (direction) {
                case "up": worldY -= attackArea.height; break;
                case "down": worldY += attackArea.height; break;
                case "left": worldX -= attackArea.width; break;
                case "right": worldX += attackArea.width; break;
            }

            // Change solidArea to attackArea for the collision check
            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            // Check if this temporary "Attack Box" hits the player
            if (gp.cChecker.checkPlayer(this)) {
                damagePlayer(attack); // You can pass a specific attack value here
            }

            // Restore original property values
            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidth;
            solidArea.height = solidAreaHeight;
        }

        // 3. RECOVERY: The "Cool-down" phase (Frame 3)
        if (spriteCounter > 25 && spriteCounter <= 35) {
            spriteNum = 3;
        }

        // 4. FINISH
        if (spriteCounter > 35) {
            spriteNum = 0;
            spriteCounter = 0;
            attacking = false;
        }
    }

    public void damagePlayer(int attack) {
        if (!gp.player.invincible) {
            // We can add a hit sound effect here: gp.playSE(index);

            int damage = attack - gp.player.defense; // Basic damage math
            if (damage < 0) damage = 0;

            gp.player.life -= (damage > 0) ? damage : 1; // Minimum 1 damage
            gp.player.invincible = true;
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
                if(invincible){
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
                }
                g2.drawImage(image, screenX, screenY, gp.TILE_SIZE * sizeScale, gp.TILE_SIZE* sizeScale, null);

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