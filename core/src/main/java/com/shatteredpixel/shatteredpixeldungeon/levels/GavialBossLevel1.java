//END(port from Arknights): GavialBossLevel1（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tomimi;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Group;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.Arrays;

public class GavialBossLevel1
extends Level {
    private static final int ROOM_TOP = 12;

    public GavialBossLevel1() {
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
        for (int i = 0; i < this.length(); ++i) {
            int flags = Terrain.flags[this.map[i]];
            if ((flags & 0x80) == 0) continue;
            this.avoid[i] = false;
            this.passable[i] = false;
            this.solid[i] = true;
        }
    }

    @Override
    protected boolean build() {
        this.setSize(21, 21);
        Arrays.fill(this.map, 1);
        int MID = this.width / 2;
        Painter.fill(this, 0, 0, 21, 1, 4);
        Painter.fill(this, 0, 20, 21, 1, 4);
        Painter.fill(this, 0, 0, 1, 21, 4);
        Painter.fill(this, 20, 0, 1, 21, 4);
        Painter.fill(this, 7, 0, 2, 8, 4);
        Painter.fill(this, 12, 0, 2, 8, 4);
        Painter.fill(this, 4, 4, 13, 4, 4);
        Painter.fill(this, 3, 18, 15, 1, 4);
        Painter.fill(this, 2, 10, 4, 1, 4);
        Painter.fill(this, 15, 10, 4, 1, 4);
        this.map[360] = 4;
        this.map[374] = 4;
        this.map[287] = 25;
        this.map[307] = 25;
        this.map[308] = 25;
        this.map[218] = 25;
        this.map[238] = 25;
        this.map[239] = 25;
        this.map[240] = 25;
        this.map[260] = 25;
        this.map[261] = 25;
        this.map[319] = 25;
        this.entrance = 388;
        this.exit = 178;
        this.map[this.entrance] = 7;
        this.map[this.exit] = 12;
        this.feeling = Level.Feeling.NONE;
        return true;
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos != this.map[this.entrance] && this.map[this.exit] == 12) {
            Tomimi boss = new Tomimi();
            boss.pos = 241;
            GameScene.add(boss);
            this.seal();
        }
    }

    @Override
    public void seal() {
        super.seal();
        GavialBossLevel1.set(this.entrance, 1);
        GavialBossLevel1.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        Dungeon.observe();
    }

    @Override
    public void unseal() {
        super.unseal();
        GavialBossLevel1.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        GavialBossLevel1.set(this.exit, 8);
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

	//END(port from Arknights): 方舟区域音乐（原来没有覆盖此方法 → 进关卡没音乐）
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.BOSS_SARGON1, true );
	}

}
