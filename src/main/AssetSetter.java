package main;

import entity.Npc_Wizard;


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
}
