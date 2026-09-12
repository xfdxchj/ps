//END(port from Arknights): SeaBossLevel1（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SeaBoss1;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SeaObject;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.Arrays;

public class SeaBossLevel1
extends Level {
    private static final int ROOM_TOP = 12;
    private static int W = 4;
    private static int D = 12;
    private static int e = 1;
    private static int E = 20;
    private static int S = 32;
    private static final int[] endMap = new int[]{S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, D, S, S, S, S, S, S, S, S, S, S, e, e, e, e, e, e, e, e, e, e, e, e, e, S, S, S, S, e, S, e, e, e, S, S, e, e, e, e, e, e, S, S, S, S, e, e, e, e, e, e, e, S, e, e, S, e, e, S, S, S, S, e, e, e, S, S, S, E, S, e, e, e, e, e, S, S, S, S, e, e, e, e, e, e, e, e, e, e, e, e, e, S, S, S, S, S, e, e, e, e, e, e, e, S, S, S, S, S, S, S, S, S, S, e, e, e, e, e, e, e, e, e, e, e, e, E, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S};
    public static boolean isBossDefeated = true;
    private static final String BOSS_DEFEATED = "bossDefeated";

    public SeaBossLevel1() {
        this.color1 = 8393984;
        this.color2 = 10913057;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_iberia.png";
    }

    @Override
    public String waterTex() {
        return "environment/water7.png";
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    protected boolean build() {
        this.setSize(17, 13);
        Arrays.fill(this.map, 1);
        this.feeling = Level.Feeling.NONE;
        this.setMap();
        return true;
    }

    private void setMap() {
        int i = 0;
        for (int cell = 0; cell < this.length(); cell += this.width()) {
            System.arraycopy(endMap, i, this.map, cell, 17);
            i += 17;
        }
        this.entrance = 195;
        this.exit = 59;
        this.map[this.entrance] = 7;
        CustomeMap vis = new CustomeMap();
        vis.setRect(0, 0, this.width(), this.height());
        this.customTiles.add(vis);
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos != this.map[this.entrance] && this.map[this.exit] == 12) {
            this.seal();
        }
    }

    @Override
    public void seal() {
        super.seal();
        SeaBossLevel1.set(this.entrance, 1);
        SeaBossLevel1.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        SeaObject obj = new SeaObject();
        obj.pos = 127;
        SeaBossLevel1.set(obj.pos, 25);
        GameScene.add(obj);
        SeaBoss1 boss = new SeaBoss1();
        boss.pos = 76;
        GameScene.add(boss);
        isBossDefeated = false;
        Dungeon.observe();
    }

    @Override
    public void unseal() {
        super.unseal();
        isBossDefeated = true;
        SeaBossLevel1.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        SeaBossLevel1.set(this.exit, 8);
        GameScene.updateMap(this.exit);
        this.customTiles.clear();
        CustomeMap2 vis = new CustomeMap2();
        vis.setRect(0, 0, this.width(), this.height());
        this.customTiles.add(vis);
        ShatteredPixelDungeon.resetScene();
        Dungeon.observe();
    }

    @Override
    protected void createMobs() {
    }

    @Override
    public Actor addRespawner() {
        return null;
    }

    @Override
    protected void createItems() {
    }

    @Override
    public String tileDesc(int tile) {
        switch (tile) {
            case 7: {
                return Messages.get(CityLevel.class, "entrance_desc");
            }
            case 8: {
                return Messages.get(CityLevel.class, "exit_desc");
            }
            case 12: 
            case 20: {
                return Messages.get(CityLevel.class, "deco_desc");
            }
            case 14: {
                return Messages.get(CityLevel.class, "sp_desc");
            }
            case 25: 
            case 26: {
                return Messages.get(CityLevel.class, "statue_desc");
            }
            case 27: {
                return Messages.get(CityLevel.class, "bookshelf_desc");
            }
        }
        return super.tileDesc(tile);
    }

    @Override
    public int randomRespawnCell(Char ch) {
        int cell;
        while (!this.passable[cell = this.entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]] || Char.hasProp(ch, Char.Property.LARGE) && !this.openSpace[cell] || Actor.findChar(cell) != null) {
        }
        return cell;
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        HallsLevel.addHallsVisuals(this, this.visuals);
        return this.visuals;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BOSS_DEFEATED, isBossDefeated);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        isBossDefeated = bundle.getBoolean(BOSS_DEFEATED);
    }

    public static class CustomeMap
    extends CustomTilemap {
        public CustomeMap() {
            this.texture = "environment/custom_tiles/iberia_boss1_1.png";
        }

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            int[] data = new int[this.tileW * this.tileH];
            for (int i = 0; i < data.length; ++i) {
                data[i] = i;
            }
            v.map(data, this.tileW);
            return v;
        }
    }

    public static class CustomeMap2
    extends CustomTilemap {
        public CustomeMap2() {
            this.texture = "environment/custom_tiles/iberia_boss1_2.png";
        }

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            int[] data = new int[this.tileW * this.tileH];
            for (int i = 0; i < data.length; ++i) {
                data[i] = i;
            }
            v.map(data, this.tileW);
            return v;
        }
    }
}
