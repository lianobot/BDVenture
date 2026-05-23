package entity;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;


public class Npc_Wizard extends Entity{

    public Npc_Wizard(GamePanel gp) {
        super(gp);

        name = "Wizard";
        direction = "down";

        solidArea.x = 8;
        solidArea.y = 4;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
        setDialogue();
    }

    public void getImage(){
        idleDown = new BufferedImage[2];
        try {

            idleDown[0] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/npc/wizard_down_1.png")));
            idleDown[1] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/npc/wizard_down_2.png")));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setDialogue(){

        dialogues[0] = "Don't just stand there! You're blocking my mana!";
        dialogues[1] = "I was in the middle of a very delicate potion.";
        dialogues[2] = "Wait... did I add the frog scales or the bat wings?";
        dialogues[3] = "Actually, don't answer that. Just move along!";
    }


    public void speak(){

        if (dialogues[dialogueIndex] == null){
            dialogueIndex = 0;
            gp.gameState = gp.playState;
        } else {
            gp.ui.setDialogue(dialogues[dialogueIndex]);
            dialogueIndex++;
        }
    }
}
