package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_Chest extends Entity {
    public OBJ_Chest(GamePanel gp) {
        super(gp);
        name = "Chest";
        collision = true;


        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/object/Chest.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}