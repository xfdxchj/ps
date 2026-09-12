//END(port from Arknights): GavialBossLevel2（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eunectes;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.Arrays;

public class GavialBossLevel2
extends Level {
    private static final int ROOM_TOP = 12;
    private static int W = 4;
    private static int D = 12;
    private static int e = 1;
    private static int E = 20;
    private static int S = 25;
    private static final int[] endMap = new int[]{W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, S, S, E, S, S, S, S, S, S, S, S, S, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W, W, S, S, S, S, S, S, S, e, e, E, e, e, S, S, S, S, S, S, S, W};

    public GavialBossLevel2() {
        this.color1 = 8393984;
        this.color2 = 10913057;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_sargon.png";
    }

    @Override
    public String waterTex() {
        return "environment/water6.png";
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    protected boolean build() {
        this.setSize(21, 19);
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
        this.entrance = 325;
        this.exit = 31;
        this.map[this.entrance] = 7;
        this.map[this.exit] = 8;
        CustomeMap vis = new CustomeMap();
        vis.setRect(0, 0, this.width(), this.height());
        this.customTiles.add(vis);
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos != this.map[this.entrance] && this.map[this.exit] == 8) {
            Eunectes boss = new Eunectes();
            boss.pos = 136;
            GameScene.add(boss);
            this.seal();
            boss.notice();
        }
    }

    @Override
    public void seal() {
        super.seal();
        GavialBossLevel2.set(this.entrance, 1);
        GavialBossLevel2.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        Dungeon.observe();
    }

    @Override
    public void unseal() {
        super.unseal();
        GavialBossLevel2.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        GavialBossLevel2.set(this.exit, 8);
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
            this.texture = "environment/custom_tiles/gavial_boss2.png";
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
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.BOSS_SARGON2, true );
	}
}
