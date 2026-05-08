package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class OBJ_Door extends Entity {
    public OBJ_Door(GamePanel gp) {
        super(gp);
        name = "Door";
        collision = true;
        down = new BufferedImage[1];

        try {
            down[0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/object/Door.png")));
            collision = true;

            solidArea.x = 0;
            solidArea.y = 16;
            solidArea.width = 48;
            solidArea.height = 32;
            solidAreaDefaultX = solidArea.x;
            solidAreaDefaultY = solidArea.y;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}