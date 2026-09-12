package com.shatteredpixel.shatteredpixeldungeon.levels.painters;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.RegularPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Random;
import java.util.ArrayList;

//END(port from Arknights): 方舟地形装饰 Painter（数字常量已换成 Terrain.*）
public class IberiaPainter
extends RegularPainter {
    @Override
    protected void decorate(Level level, ArrayList<Room> rooms) {
        int i;
        int[] map = level.map;
        int w = level.width();
        int l = level.length();
        for (i = 0; i < w; ++i) {
            if (map[i] != Terrain.WALL || map[i + w] != Terrain.WATER || Random.Int(3) != 0) continue;
            map[i] = Terrain.WALL_DECO;
        }
        for (i = w; i < l - w; ++i) {
            if (map[i] != Terrain.WALL || map[i - w] != Terrain.WALL || map[i + w] != Terrain.WATER || Random.Int(2) != 0) continue;
            map[i] = Terrain.WALL_DECO;
        }
        for (i = w + 1; i < l - w - 1; ++i) {
            if (map[i] != Terrain.EMPTY) continue;
            int count = (map[i + 1] == Terrain.WALL ? 1 : 0) + (map[i - 1] == Terrain.WALL ? 1 : 0) + (map[i + w] == Terrain.WALL ? 1 : 0) + (map[i - w] == Terrain.WALL ? 1 : 0);
            if (Random.Int(16) >= count * count) continue;
            map[i] = Terrain.EMPTY_DECO;
        }
    }
}
