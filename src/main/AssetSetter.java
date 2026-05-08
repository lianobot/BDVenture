package main;

import entity.Npc_Wizard;
import monster.MON_Slime;
import object.OBJ_Door;


public class AssetSetter {

    GamePanel gp;
    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    public void setObject(){

        gp.obj[0] = new OBJ_Door(gp);
        gp.obj[0].worldX = gp.TILE_SIZE*70;
        gp.obj[0].worldY = gp.TILE_SIZE*65;
    }

    public void setNPC(){

        gp.npc[0] = new Npc_Wizard(gp);
        gp.npc[0].worldX = gp.TILE_SIZE*75;
        gp.npc[0].worldY = gp.TILE_SIZE*62;
    }

    public void setMonster(){

        gp.monster[0] = new MON_Slime(gp);
        gp.monster[0].worldX = gp.TILE_SIZE*75;
        gp.monster[0].worldY = gp.TILE_SIZE*65;
    }
}
