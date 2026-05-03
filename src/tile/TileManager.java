package tile;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class TileManager {

    GamePanel gp;
   public Tile[] tile;
    public int[][] mapTileNum;


    public TileManager(GamePanel gp){
        this.gp = gp;

        tile = new Tile[256];
        mapTileNum = new int[gp.MAX_WORLD_COL][gp.MAX_WORLD_ROW];

        getTileImage();
        loadMap("/maps/world01.csv");
    }

    //HELPER FUNCTION TO LOAD COLLISION DATA
    public void loadCollisionData(String filepath) {
        try {
            InputStream is = getClass().getResourceAsStream(filepath);
            if (is == null) throw new AssertionError();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();

            if (line != null) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    part = part.trim();
                    if (part.contains("-")) {
                        // It's a range (e.g., 112-133)
                        String[] range = part.split("-");
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int i = start; i <= end; i++) {
                            tile[i].collision = true;
                        }
                    } else {
                        // It's a single ID
                        int id = Integer.parseInt(part);
                        tile[id].collision = true;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error loading collision data: " + e.getMessage());
        }
    }

    public void getTileImage() {
        //To load all the tile data it's done in 3 parts.
        //1. The full png is buffered.
        //2. The image is sliced in 16x16 tiles and stored in the tile array.
        //3. The collision data is added from helper function.
        try {
            //Load the main spritesheet
            BufferedImage spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/tiles/tileset.png")));

            int tileSize = 16;
            int cols = spriteSheet.getWidth() / tileSize;
            int rows = spriteSheet.getHeight() / tileSize;


            int index = 0;

            //Loop through the image and slice it into 16x16 chunks
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    tile[index] = new Tile();
                    // Extract the 16x16 sub-image
                    tile[index].image = spriteSheet.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                    index++;
                }
            }

            //Collision data for each tile.
            loadCollisionData("/tiles/collision_ids.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filepath){
        try{
            InputStream is = getClass().getResourceAsStream(filepath);
            if (is == null) throw new AssertionError();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;
            while (row < gp.MAX_WORLD_ROW) {

                String line = br.readLine();

                // Check if we've reached the end of the file unexpectedly
                if (line == null) break;

                String[] numbers = line.split(",");

                for (int col = 0; col < gp.MAX_WORLD_COL; col++) {
                    int num = Integer.parseInt(numbers[col].trim());
                    mapTileNum[col][row] = num;
                }

                row++;
            }
            br.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void draw(Graphics2D g2){

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.MAX_WORLD_COL && worldRow < gp.MAX_WORLD_ROW){

            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.TILE_SIZE;
            int worldY = worldRow * gp.TILE_SIZE;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if (
                    worldX + gp.TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.TILE_SIZE< gp.player.worldX + gp.player.screenX &&
                    worldY + gp.TILE_SIZE> gp.player.worldY - gp.player.screenY &&
                    worldY - gp.TILE_SIZE< gp.player.worldY + gp.player.screenY)  {

                g2.drawImage(tile[tileNum].image, screenX, screenY, gp.TILE_SIZE, gp.TILE_SIZE, null);
            }

            worldCol++;

            if (worldCol == gp.MAX_WORLD_COL){
                worldCol = 0;
                worldRow++;
            }

        }

    }

}