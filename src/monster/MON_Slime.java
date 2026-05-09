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

        // Re-think direction every 30 frames
        if (actionLockCounter >= 20) {

            // Calculate distance to player
            int xDistance = Math.abs(worldX - gp.player.worldX);
            int yDistance = Math.abs(worldY - gp.player.worldY);
            int tileDistance = (xDistance + yDistance) / gp.TILE_SIZE;

            // 1. CHASING STATE
            if (tileDistance < 6) {
                // Move towards player
                if (xDistance > yDistance) {
                    if (worldX < gp.player.worldX) {
                        direction = "right";
                    } else if (worldX > gp.player.worldX) {
                        direction = "left";
                    }
                } else {
                    if (worldY < gp.player.worldY) {
                        direction = "down";
                    } else if (worldY > gp.player.worldY) {
                        direction = "up";
                    }
                }
            }

            // 2. WANDERING STATE
            else {
                actionLockCounter++;
                if (actionLockCounter == 120) {
                    Random random = new Random();
                    int i = random.nextInt(125) + 1;
                    if (i <= 25) {
                        direction = "up";
                    } else if (i <= 50) {
                        direction = "down";
                    } else if (i <= 75) {
                        direction = "left";
                    } else if (i <= 100) {
                        direction = "right";
                    }
                    actionLockCounter = 0;
                }
            }
            actionLockCounter = 0;
        }
    }
}
