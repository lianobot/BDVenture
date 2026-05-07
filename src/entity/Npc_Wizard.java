package entity;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;


public class Npc_Wizard extends Entity{

    public Npc_Wizard(GamePanel gp) {
        super(gp);

        direction = "down";

        getImage();
        setDialogue();
    }

    public void getImage(){
        try {

            down1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/npc/wizard_down_1.png")));
            down2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/npc/wizard_down_2.png")));

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
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            dialogueIndex++;
        }
    }
}
