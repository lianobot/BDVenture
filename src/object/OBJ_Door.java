package object;

import javax.imageio.ImageIO;
import java.io.IOException;

public class OBJ_Door extends SuperObject {
    public OBJ_Door() {
        name = "Door";
        collision = true;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/object/Door.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}