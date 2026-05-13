package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity {

    KeyHandler keyH;
     public final int screenX;
     public final int screenY;
     private static final double DRAW_SCALE = 2.5;
     private static final int BODY_PIXELS_X = 18;
     private static final int BODY_PIXELS_Y = 30;
     private static final int BODY_PIXELS_WIDTH = 13;
     private static final int BODY_PIXELS_HEIGHT = 13;
     private static final int ATTACK_REACH = 24;
     private static final int ATTACK_PADDING = 4;

    // Player Stats
    public int attack = 1;
    public int defense = 0;

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


    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;

        screenX = gp.SCREEN_WIDTH / 2 - (gp.TILE_SIZE / 2);
        screenY = gp.SCREEN_HEIGHT / 2 - (gp.TILE_SIZE / 2);

        //Collision Box
        solidArea = createBodyCollisionBox();
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        //Attack Hitbox
        attackArea.width = ATTACK_REACH;
        attackArea.height = ATTACK_REACH;

        spriteNum = 0;

        setDefaultValues();
        getImage();
    }

    public void setDefaultValues() {

         worldX = gp.TILE_SIZE * 78;
         worldY = gp.TILE_SIZE * 63;

        speed = 4;
        direction = "down";

        //PLAYER STATUS
        maxLife = 6;
        life = maxLife;

        isIdle = true;
        attacking = false;
        invincible = false;
        invincibleCounter = 0;
        spriteNum = 0;
        spriteCounter = 0;
        collisionOn = false;
    }

    public void getImage() {
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
                walkDown[i] = spriteSheet.getSubimage(i * size, 3 * size, size, size);
                walkRight[i] = spriteSheet.getSubimage(i * size, 4 * size, size, size);
                walkLeft[i] = flipImage(walkRight[i]);
                walkUp[i] = spriteSheet.getSubimage(i * size, 5 * size, size, size);

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

    //Player HitBox
    private Rectangle createBodyCollisionBox() {
        int drawSize = (int) (gp.TILE_SIZE * DRAW_SCALE);
        int drawOffsetX = -(drawSize / 3);
        int drawOffsetY = -(drawSize / 2);

        int x = drawOffsetX + (int) Math.round(BODY_PIXELS_X * DRAW_SCALE);
        int y = drawOffsetY + (int) Math.round(BODY_PIXELS_Y * DRAW_SCALE);
        int width = (int) Math.round(BODY_PIXELS_WIDTH * DRAW_SCALE);
        int height = (int) Math.round(BODY_PIXELS_HEIGHT * DRAW_SCALE);

        return new Rectangle(x, y, width, height);
    }

    //Player AttackBox
    private Rectangle getAttackWorldBox() {
        Rectangle bodyBox = new Rectangle(
                worldX + solidAreaDefaultX,
                worldY + solidAreaDefaultY,
                solidArea.width,
                solidArea.height
        );

        return switch (direction) {
            case "up" -> new Rectangle(
                    bodyBox.x - ATTACK_PADDING,
                    bodyBox.y - ATTACK_REACH * 2,
                    bodyBox.width + ATTACK_PADDING * 2,
                    ATTACK_REACH * 2
            );
            case "down" -> new Rectangle(
                    bodyBox.x - ATTACK_PADDING,
                    bodyBox.y + bodyBox.height,
                    bodyBox.width + ATTACK_PADDING * 2,
                    ATTACK_REACH * 2
            );
            case "left" -> new Rectangle(
                    bodyBox.x - ATTACK_REACH * 2,
                    bodyBox.y,
                    ATTACK_REACH * 2,
                    bodyBox.height
            );
            case "right" -> new Rectangle(
                    bodyBox.x + bodyBox.width,
                    bodyBox.y,
                    ATTACK_REACH * 2,
                    bodyBox.height
            );
            default -> bodyBox;
        };
    }

    private int checkAttackMonster(Rectangle attackWorldBox) {
          synchronized (gp.monsterLock) {
            for (int i = 0; i < gp.monster.length; i++) {
                if (gp.monster[i] != null) {
                    Rectangle monsterBox = new Rectangle(
                            gp.monster[i].worldX + gp.monster[i].solidArea.x,
                            gp.monster[i].worldY + gp.monster[i].solidArea.y,
                            gp.monster[i].solidArea.width,
                            gp.monster[i].solidArea.height
                    );

                    if (attackWorldBox.intersects(monsterBox)) {
                        return i;
                    }
                }
            }
        }

        return 999;
    }

    public void update() {

        if (attacking) {
            attacking();
        } else {

            // CHECK NPC COLLISION
            collisionOn = false;
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

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
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

            int monsterIndex = checkAttackMonster(getAttackWorldBox());
            damageMonster(monsterIndex);

        }

        if (spriteCounter > 30 && spriteCounter <= 40) {
            spriteNum = 3;
        }

        if (spriteCounter > 40) {
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

    public void interactNPC(int i) {
        if (gp.keyH.ePressed) {
            if (i != 999) {
                gp.gameState = gp.dialogueState;
                gp.npc[i].speak();
            } else {
                // Only start attack if we aren't already attacking
                if (!attacking) {
                    gp.playSE(3);
                    attacking = true;
                    spriteCounter = 0; // Reset counter for animation
                    spriteNum = 0;
                }
            }
        }
        gp.keyH.ePressed = false;
    }

    public void contactMonster(int i) {

        if (i != 999) {
            synchronized (gp.monsterLock) {
                if (gp.monster[i] != null && !invincible && !gp.monster[i].dying) {
                    gp.playSE(2);
                    life -= 1;

                    if (life <= 0){
                        gp.triggerGameOver();
                    }
                    invincible = true;
                }
            }
        }
    }

    private void damageMonster(int i) {

        if (i != 999) {
            synchronized (gp.monsterLock) {
                if (gp.monster[i] != null && !gp.monster[i].invincible) {

                    gp.playSE(1);

                    gp.monster[i].life -= attack;
                    gp.monster[i].invincible = true;
                    gp.monster[i].damaged = true;

                    if (gp.monster[i].life <= 0) {
                        gp.monster[i].dying = true;
                        gp.monster[i].spriteNum = 0;
                        gp.monster[i].spriteCounter = 0;

                    }
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        if (attacking) {
            image = switch (direction) {
                case "up" -> attackUp[spriteNum];
                case "down" -> attackDown[spriteNum];
                case "left" -> attackLeft[spriteNum];
                case "right" -> attackRight[spriteNum];
                default -> image;

            };
        } else if (isIdle) {
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

        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        }

        g2.drawImage(image, x, y, drawSize, drawSize, null);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        //DEBUG: SHOWS HIT BOX AND ATTACK BOX
        if (gp.keyH.showCollisionBox) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.red);
            g2.drawRect(screenX + solidAreaDefaultX, screenY + solidAreaDefaultY, solidArea.width, solidArea.height);

            if (attacking) {
                Rectangle attackBox = getAttackWorldBox();
                int attackScreenX = attackBox.x - worldX + screenX;
                int attackScreenY = attackBox.y - worldY + screenY;

                g2.setColor(Color.green);
                g2.drawRect(attackScreenX, attackScreenY, attackBox.width, attackBox.height);
            }
            g2.setStroke(oldStroke);
        }
    }
}