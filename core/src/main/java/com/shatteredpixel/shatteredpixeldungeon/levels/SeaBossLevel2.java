//END(port from Arknights): SeaBossLevel2（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Isharmla;
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

public class SeaBossLevel2
extends Level {
    private static final int ROOM_TOP = 12;
    private static int W = 4;
    private static int D = 12;
    private static int e = 1;
    private static int E = 20;
    private static int S = 25;
    private static int T = 90;
    private static final int[] endMap = new int[]{W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W, W, W, W, W, W, W, W, e, e, e, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, e, e, e, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, e, E, e, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W};

    public SeaBossLevel2() {
        this.color1 = 8393984;
        this.color2 = 10913057;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_iberia2.png";
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
        this.setSize(21, 24);
        Arrays.fill(this.map, 1);
        this.feeling = Level.Feeling.NONE;
        this.setMap();
        return true;
    }

    private void setMap() {
        int i = 0;
        for (int cell = 0; cell < this.length(); cell += this.width()) {
            System.arraycopy(endMap, i, this.map, cell, 21);
            i += 21;
        }
        this.setBrand();
        this.entrance = 430;
        this.exit = 31;
        this.map[this.entrance] = 7;
        this.map[this.exit] = 8;
        CustomeMap vis = new CustomeMap();
        vis.setRect(0, 0, this.width(), this.height());
        this.customTiles.add(vis);
    }

    private void setBrand() {
        for (int i = 168; i < 368; ++i) {
            if (this.map[i] != 90) continue;
            this.addSeaTerror(i);
        }
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos != this.map[this.entrance] && this.map[this.exit] == 8) {
            Isharmla boss = new Isharmla();
            boss.pos = 199;
            GameScene.add(boss);
            SeaBossLevel2.set(boss.pos, 24);
            GameScene.updateMap(this.entrance);
            Dungeon.observe();
            this.seal();
        }
    }

    @Override
    public void seal() {
        super.seal();
        SeaBossLevel2.set(this.entrance, 1);
        SeaBossLevel2.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        Dungeon.observe();
    }

    @Override
    public void unseal() {
        super.unseal();
        SeaBossLevel2.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        SeaBossLevel2.set(this.exit, 8);
        GameScene.updateMap(this.exit);
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
    public int randomRespawnCell(Char ch) {
        int cell;
        while (!this.passable[cell = this.entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]] || Char.hasProp(ch, Char.Property.LARGE) && !this.openSpace[cell] || Actor.findChar(cell) != null) {
        }
        return cell;
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
    public Group addVisuals() {
        super.addVisuals();
        HallsLevel.addHallsVisuals(this, this.visuals);
        return this.visuals;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
    }

    public static class CustomeMap
    extends CustomTilemap {
        public CustomeMap() {
            this.texture = "environment/custom_tiles/iberia_boss2.png";
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

	//END(port from Arknights): 方舟区域音乐（原来没有覆盖此方法 → 进关卡没音乐）
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.BOSS_IBERIA2, true );
	}

}
