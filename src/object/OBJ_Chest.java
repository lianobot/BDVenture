package object;

import javax.imageio.ImageIO;
import java.io.IOException;

public class OBJ_Chest extends SuperObject {
    public OBJ_Chest() {
        name = "Chest";
        collision = true;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/object/Chest.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}