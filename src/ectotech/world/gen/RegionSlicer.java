package ectotech.world.gen;

import arc.graphics.g2d.TextureRegion;

public class RegionSlicer  {
    public static TextureRegion[] splitRegion(TextureRegion region, int tileWidth, int tileHeight) {
        return splitRegion(region, tileWidth, tileHeight, 0);
    }

    public static TextureRegion[] splitRegion(TextureRegion region, int tileWidth, int tileHeight, int pad) {
        return splitRegion(region, tileWidth, tileHeight, pad, null);
    }

    public static TextureRegion[] splitRegion(TextureRegion region, int tileWidth, int tileHeight, int pad, int[] indexMap) {
        if (region.texture == null) return null;
        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int pWidth = tileWidth + pad * 2;
        int pHeight = tileHeight + pad * 2;

        int sw = width / pWidth;
        int sh = height / pHeight;

        int startX = x;
        TextureRegion[] tiles = new TextureRegion[sw * sh];
        for (int cy = 0; cy < sh; cy++, y += pHeight) {
            x = startX;
            for (int cx = 0; cx < sw; cx++, x += pWidth) {
                int index = cx + cy * sw;
                if (indexMap != null) {
                    tiles[indexMap[index]] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                } else {
                    tiles[index] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }
            }
        }
        return tiles;
    }

}
