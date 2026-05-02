package main;

import object.OBJ_Key;


public class AssetSetter {

    GamePanel gp;
    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    public void setObject(){
        gp.obj[0] = new object.OBJ_Key();
        gp.obj[0].worldX = 23 * gp.TILE_SIZE;
        gp.obj[0].worldY = 7 * gp.TILE_SIZE;

        gp.obj[1] = new object.OBJ_Key();
        gp.obj[1].worldX = 23 * gp.TILE_SIZE;
        gp.obj[1].worldY = 40 * gp.TILE_SIZE;

        gp.obj[2] = new object.OBJ_Door();
        gp.obj[2].worldX = 10 * gp.TILE_SIZE;
        gp.obj[2].worldY = 11 * gp.TILE_SIZE;

        gp.obj[3] = new object.OBJ_Chest();
        gp.obj[3].worldX = 10 * gp.TILE_SIZE;
        gp.obj[3].worldY = 7 * gp.TILE_SIZE;
    }
}
