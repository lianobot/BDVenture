package monster;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class MON_Slime extends Entity {
    private static final int CHASE_UPDATE_INTERVAL = 8;
    private static final int ATTACK_Y_TOLERANCE = 10;
    private static final int ATTACK_X_TOLERANCE = 10;
    private static final int SPRITE_SIZE = 32;
    private static final int BODY_BOTTOM_PIXELS = 24;
    private static final int MIN_WANDER_DURATION = 60;
    private static final int MAX_WANDER_DURATION = 120;
    private final Random random = new Random();
    private String lastHorizontalDirection = "left";
    private int wanderTimer = 0;

    public MON_Slime(GamePanel gp) {
        super(gp);

        //MONSTER STATS
        name = "Slime";
        speed = 1;
        maxLife = 2;
        life = maxLife;
        type = 2;
        sizeScale = 1.5;
        attackArea.width = 0;
        attackArea.height = 0;
        this.attack = 1;

        //ANIMATIONS
        idleUp = new BufferedImage[4];
        idleDown = new BufferedImage[4];
        idleLeft = new BufferedImage[4];
        idleRight = new BufferedImage[4];

        walkUp = new BufferedImage[6];
        walkDown = new BufferedImage[6];
        walkLeft = new BufferedImage[6];
        walkRight = new BufferedImage[6];

        attackUp = new BufferedImage[4];
        attackDown = new BufferedImage[4];
        attackLeft = new BufferedImage[4];
        attackRight = new BufferedImage[4];

        damageUp = new BufferedImage[3];
        damageDown = new BufferedImage[3];
        damageLeft = new BufferedImage[3];
        damageRight = new BufferedImage[3];

        dieUp = new BufferedImage[5];
        dieDown = new BufferedImage[5];
        dieLeft = new BufferedImage[5];
        dieRight = new BufferedImage[5];

        //HITBOX
        solidArea.x = 24;
        solidArea.y = 16;
        solidArea.width = 24;
        solidArea.height = 16;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage(){
        try {
            // Read the main 48x48 sprite sheet
            BufferedImage spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/monster/slime.png")));
            int size = 32;

            for (int i = 0; i < 8; i++) {

                // IDLE ANIMATIONS
                if (i < 4) {
                    idleDown[i] = spriteSheet.getSubimage(i * size, 0, size, size);
                    idleRight[i] = spriteSheet.getSubimage(i * size, size, size, size);
                    idleLeft[i] = flipImage(idleRight[i]);
                    idleUp[i] = spriteSheet.getSubimage(i * size, 2 * size, size, size);
                }

                // MOVE ANIMATIONS
                if (i < 6) {
                    walkDown[i] = spriteSheet.getSubimage(i * size, 3 * size, size, size);
                    walkRight[i] = spriteSheet.getSubimage(i * size, 4 * size, size, size);
                    walkLeft[i] = flipImage(walkRight[i]);
                    walkUp[i] = spriteSheet.getSubimage(i * size, 5 * size, size, size);
                }

                // ATTACK ANIMATIONS
                if (i < 4) {
                    attackDown[i] = spriteSheet.getSubimage(i * size, 6 * size, size, size);
                    attackRight[i] = spriteSheet.getSubimage(i * size, 7 * size, size, size);
                    attackLeft[i] = flipImage(attackRight[i]);
                    attackUp[i] = spriteSheet.getSubimage(i * size, 8 * size, size, size);
                }

                // DAMAGE ANIMATION
                if (i < 3) {
                    damageDown[i] = spriteSheet.getSubimage(i * size, 9 * size, size, size);
                    damageRight[i] = spriteSheet.getSubimage(i * size, 10 * size, size, size);
                    damageLeft[i] = flipImage(damageRight[i]);
                    damageUp[i] = spriteSheet.getSubimage(i * size, 11 * size, size, size);
                }

                // DEATH ANIMATION
                if (i < 5) {
                    dieDown[i] = spriteSheet.getSubimage(i * size, 12 * size, size, size);
                    dieRight[i] = dieDown[i];
                    dieLeft[i] = flipImage(dieRight[i]);
                    dieUp[i] = dieDown[i];
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

    @Override
    public void setAction() {
        actionLockCounter++;

        int slimeCenterX = getSlimeCenterX();
        int playerCenterX = getPlayerCenterX();
        int slimeDrawBottomY = getSlimeDrawBottomY();
        int playerDrawBottomY = getPlayerDrawBottomY();

        // Calculate distance to player
        int xDifference = playerCenterX - slimeCenterX;
        int yDifference = playerDrawBottomY - slimeDrawBottomY;
        int xDistance = Math.abs(xDifference);
        int yDistance = Math.abs(yDifference);
        int tileDistance = (xDistance + yDistance) / gp.TILE_SIZE;

        // 1. WANDER STATE
        if (tileDistance >= 6) {
            if (wanderTimer <= 0 || collisionOn) {
                int i = random.nextInt(100) + 1;
                if (i <= 25) {
                    direction = "up";
                } else if (i <= 50) {
                    direction = "down";
                } else if (i <= 75) {
                    direction = "left";
                } else {
                    direction = "right";
                }
                wanderTimer = MIN_WANDER_DURATION + random.nextInt(MAX_WANDER_DURATION - MIN_WANDER_DURATION + 1);
            }
            wanderTimer--;

            if (direction.equals("left") || direction.equals("right")) {
                lastHorizontalDirection = direction;
            }

            actionLockCounter = 0;
            return;
        }

        wanderTimer = 0;
        actionLockCounter++;

        // Re-think direction every 8 frames
        if (actionLockCounter >= CHASE_UPDATE_INTERVAL) {
            if (yDistance <= ATTACK_Y_TOLERANCE) {
                direction = getHorizontalDirection(xDifference);
            } else if (xDistance > ATTACK_X_TOLERANCE) {
                direction = getHorizontalDirection(xDifference);
            } else {
                direction = yDifference > 0 ? "down" : "up";
            }
            if (direction.equals("left") || direction.equals("right")) {
                lastHorizontalDirection = direction;
            }
            actionLockCounter = 0;

        }
    }

    //HELPER FUNCTIONS
    private String getHorizontalDirection(int xDifference) {
        if (xDifference > ATTACK_X_TOLERANCE) {
            return "right";
        }
        if (xDifference < -ATTACK_X_TOLERANCE) {
            return "left";
        }
        return lastHorizontalDirection;
    }

    private int getSlimeCenterX() {
        int drawSize = (int)(gp.TILE_SIZE * sizeScale);
        return worldX + drawSize / 2;
    }

    private int getSlimeDrawBottomY() {
        int drawSize = (int)(gp.TILE_SIZE * sizeScale);
        double drawScale = (double) drawSize / SPRITE_SIZE;
        int drawOffsetY = -(drawSize / 4);
        return worldY + drawOffsetY + (int) Math.round(BODY_BOTTOM_PIXELS * drawScale);
    }

    private int getPlayerCenterX() {
        return gp.player.worldX + gp.player.solidAreaDefaultX + gp.player.solidArea.width / 2;
    }

    private int getPlayerDrawBottomY() {
        return gp.player.worldY + gp.player.solidAreaDefaultY + gp.player.solidArea.height;
    }
}
