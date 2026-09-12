//END(port from Arknights): CoreRoom
package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
//END(暂缓): FireCore 本轮未搬
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

public class CoreRoom
extends SpecialRoom {
    @Override
    public void paint(Level level) {
        Painter.fill(level, this, 4);
        Painter.fill(level, this, 1, 1);
        Point c = this.center();
        int cx = c.x;
        int cy = c.y;
        Room.Door door = this.entrance();
        door.set(Room.Door.Type.UNLOCKED);
        //END(暂缓): FireCore 属怪物，本轮未搬 → 暂不生成
        //（原本在此生成 FireCore 并加入 level.mobs）
    }

    @Override
    public boolean connect(Room room) {
        if (room instanceof EntranceRoom) {
            return false;
        }
        return super.connect(room);
    }

    @Override
    public boolean canPlaceTrap(Point p) {
        return false;
    }

    @Override
    public boolean canPlaceWater(Point p) {
        return false;
    }

    @Override
    public boolean canPlaceGrass(Point p) {
        return false;
    }

    public static class CustomFloor
    extends CustomTilemap {
        public CustomFloor() {
            this.texture = "environment/custom_tiles/halls_special.png";
        }

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            int cell = this.tileX + this.tileY * Dungeon.level.width();
            int[] map = Dungeon.level.map;
            int[] data = new int[this.tileW * this.tileH];
            for (int i = 0; i < data.length; ++i) {
                if (i % this.tileW == 0) {
                    cell = this.tileX + (this.tileY + i / this.tileW) * Dungeon.level.width();
                }
                if (false /*END(暂缓): FireCore未搬*/) {
                    data[i - 1] = 37;
                    data[i] = 38;
                    data[i + 1] = 39;
                    ++i;
                    ++cell;
                } else {
                    data[i] = map[cell] == 20 ? (Statistics.amuletObtained ? 31 : 27) : 19;
                }
                ++cell;
            }
            v.map(data, this.tileW);
            return v;
        }
    }
}
