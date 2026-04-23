package main;

import entity.Entity;

public class CollisionChecker {
    GamePanel gp;
    public  CollisionChecker(GamePanel gp){
        this.gp=gp;
    }
    public void CheckTile(Entity entity){
        int entityLeftWorldX= entity.worldX + entity.SolidArea.x ;
        int entityRightWorldX= entity.worldX + entity.SolidArea.x + entity.SolidArea.width ;
        int entityTopWorldY= entity.worldY + entity.SolidArea.y;
        int entityBottomWorldY= entity.worldY + entity.SolidArea.y + entity.SolidArea.height ;

        int entityLeftCol= entityLeftWorldX/ gp.TILE_SIZE;
        int entityRightCol= entityRightWorldX/ gp.TILE_SIZE;
        int entityTopRow= entityTopWorldY / gp.TILE_SIZE;
        int entityBottomRow= entityBottomWorldY/ gp.TILE_SIZE;

        int tileNum1, tileNum2;
        switch (entity.direction ) {
            case "up":
                entityTopRow = (entityTopWorldY - entity.speed) / gp.TILE_SIZE;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true) {
                    entity.CollisionOn = true;
                }
                break;
            case "down":
                entityBottomRow = (entityBottomWorldY + entity.speed) / gp.TILE_SIZE;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true) {
                    entity.CollisionOn = true;
                }
                    break;

                    case "left":
                        entityLeftCol = (entityLeftWorldX - entity.speed) / gp.TILE_SIZE;
                        tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                        tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                        if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true) {
                            entity.CollisionOn = true;
                        }
                            break;
                            case "right":
                                entityRightCol = (entityRightWorldX + entity.speed) / gp.TILE_SIZE;
                                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true) {
                                    entity.CollisionOn = true;
                                }
                                    break;

                                }

                        }
                }
