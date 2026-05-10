package main;

import entity.Npc_Wizard;
import monster.MON_Slime;

public class AssetSetter {

    GamePanel gp;
    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    public void setObject(){

    }

    public void setNPC(){

        gp.npc[0] = new Npc_Wizard(gp);
        gp.npc[0].worldX = gp.TILE_SIZE*75;
        gp.npc[0].worldY = gp.TILE_SIZE*62;
    }

    public void setMonster() {

        synchronized (gp.monsterLock) {
            gp.monster[0] = new MON_Slime(gp);
            gp.monster[0].worldX = gp.TILE_SIZE * 50;
            gp.monster[0].worldY = gp.TILE_SIZE * 75;

            gp.monster[1] = new MON_Slime(gp);
            gp.monster[1].worldX = gp.TILE_SIZE * 45;
            gp.monster[1].worldY = gp.TILE_SIZE * 76;

            gp.monster[2] = new MON_Slime(gp);
            gp.monster[2].worldX = gp.TILE_SIZE * 53;
            gp.monster[2].worldY = gp.TILE_SIZE * 84;
        }
    }
}
