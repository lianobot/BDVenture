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

        name = "Slime";
        speed = 1;
        maxLife = 2;
        life = maxLife;
        type = 2;
        sizeScale = 2;

        solidArea.x = 32;
        solidArea.y = 34;
        solidArea.width = 32;
        solidArea.height = 20;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage(){
        try {
            // Read the main 48x48 sprite sheet
            BufferedImage spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/monster/slime.png")));
            int size = 32;

            up = new BufferedImage[6];
            down = new BufferedImage[6];
            left = new BufferedImage[6];
            right = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {

                int x = (i % 4) * size;
                int rowOffset = (i < 4) ? 0 : 1;

                // MOVE ANIMATIONS
                down[i]  = spriteSheet.getSubimage(x, 3 + rowOffset, size, size);
                right[i] = spriteSheet.getSubimage(x, 4 + rowOffset, size, size);
                left[i]  = flipImage(right[i]);
                up[i]    = spriteSheet.getSubimage(x, 5 + rowOffset, size, size);
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

    public void setAction(){

        actionLockCounter++;

        if(actionLockCounter == 120){

            Random random = new Random();
            int i = random.nextInt(100)+1;

            if(i <= 25){
                direction = "up";
            }
            if(i > 25 && i <= 50){
                direction = "down";
            }
            if(i > 50 && i <= 75){
                direction = "left";
            }
            if(i > 75){
                direction = "right";
            }

            actionLockCounter = 0;
        }
    }
}
