//END(port from Arknights): SiestaBossLevel_part2（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pompeii;
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

public class SiestaBossLevel_part2
extends Level {
    private static final int ROOM_TOP = 12;
    public int Moneygirl;

    public SiestaBossLevel_part2() {
        this.color1 = 8393984;
        this.color2 = 10913057;
        this.Moneygirl = 229;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_siesta2.png";
    }

    @Override
    public String waterTex() {
        return "environment/water6.png";
    }

    @Override
    public void create() {
        int i;
        super.create();
        for (i = 0; i < this.length(); ++i) {
            int flags = Terrain.flags[this.map[i]];
            if ((flags & 0x80) == 0) continue;
            this.avoid[i] = false;
            this.passable[i] = false;
            this.solid[i] = true;
        }
        for (i = (this.height - 12 + 2) * this.width; i < this.length; ++i) {
            this.avoid[i] = false;
            this.passable[i] = false;
            this.solid[i] = true;
        }
        for (i = (this.height - 12 + 1) * this.width; i < this.length; ++i) {
            if (i % this.width < 4 || i % this.width > 12 || i >= this.length - this.width) {
                this.discoverable[i] = false;
                continue;
            }
            this.visited[i] = true;
        }
    }

    @Override
    protected boolean build() {
        this.setSize(21, 32);
        Arrays.fill(this.map, 1);
        int MID = this.width / 2;
        Painter.fill(this, 0, 0, 21, 1, 4);
        Painter.fill(this, 0, 20, 21, 12, 4);
        Painter.fill(this, 0, 0, 1, 31, 4);
        Painter.fill(this, 20, 0, 1, 31, 4);
        this.map[22] = 4;
        this.map[23] = 4;
        this.map[43] = 4;
        this.map[39] = 4;
        this.map[40] = 4;
        this.map[61] = 4;
        this.map[29] = 12;
        this.map[31] = 12;
        this.map[30] = 12;
        this.map[32] = 12;
        this.map[33] = 12;
        this.map[49] = 12;
        this.map[50] = 12;
        this.map[70] = 12;
        this.map[54] = 12;
        this.map[55] = 12;
        this.map[76] = 12;
        this.map[69] = 12;
        this.map[91] = 12;
        this.map[90] = 12;
        this.map[77] = 12;
        this.map[97] = 12;
        this.map[98] = 12;
        this.map[51] = 25;
        this.map[71] = 25;
        this.map[53] = 25;
        this.map[75] = 25;
        this.map[176] = 4;
        this.map[218] = 4;
        this.map[196] = 4;
        this.map[197] = 4;
        this.map[198] = 4;
        this.map[219] = 4;
        this.map[175] = 4;
        this.map[180] = 4;
        this.map[181] = 4;
        this.map[222] = 4;
        this.map[200] = 4;
        this.map[201] = 4;
        this.map[202] = 4;
        this.map[221] = 4;
        this.map[232] = 4;
        this.map[253] = 4;
        this.map[274] = 4;
        this.map[254] = 4;
        this.map[275] = 4;
        this.map[276] = 4;
        this.map[250] = 4;
        this.map[271] = 4;
        this.map[292] = 4;
        this.map[270] = 4;
        this.map[291] = 4;
        this.map[290] = 4;
        this.map[341] = 25;
        this.map[342] = 25;
        this.map[361] = 25;
        this.map[362] = 25;
        this.map[266] = 25;
        this.map[267] = 25;
        this.map[286] = 25;
        this.map[287] = 25;
        this.map[288] = 25;
        this.map[309] = 25;
        this.map[352] = 25;
        this.map[374] = 25;
        this.map[380] = 25;
        this.map[400] = 25;
        this.map[215] = 25;
        this.map[236] = 25;
        this.map[258] = 25;
        this.map[67] = 25;
        this.map[87] = 25;
        this.map[88] = 25;
        this.map[128] = 25;
        this.map[129] = 25;
        this.map[58] = 25;
        this.map[80] = 25;
        this.map[141] = 25;
        this.map[163] = 25;
        this.map[164] = 25;
        this.map[206] = 25;
        this.map[207] = 25;
        this.map[228] = 25;
        this.map[192] = 25;
        this.map[282] = 25;
        this.map[317] = 25;
        this.map[376] = 25;
        this.map[391] = 25;
        this.map[411] = 25;
        this.map[347] = 25;
        this.map[407] = 25;
        this.map[73] = 20;
        this.entrance = 388;
        this.exit = 52;
        this.map[this.entrance] = 7;
        this.map[this.exit] = 8;
        this.feeling = Level.Feeling.NONE;
        return true;
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos != this.map[this.entrance] && this.map[this.exit] == 8) {
            Pompeii boss = new Pompeii();
            boss.pos = 73;
            GameScene.add(boss);
            this.seal();
        }
    }

    @Override
    public void seal() {
        super.seal();
        SiestaBossLevel_part2.set(this.entrance, 1);
        SiestaBossLevel_part2.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        Dungeon.observe();
    }

    @Override
    public void unseal() {
        super.unseal();
        SiestaBossLevel_part2.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        this.map[this.exit] = 12;
        SiestaBossLevel_part2.set(this.exit, 8);
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
        int i;
        super.restoreFromBundle(bundle);
        for (i = 0; i < this.length(); ++i) {
            int flags = Terrain.flags[this.map[i]];
            if ((flags & 0x80) == 0) continue;
            this.avoid[i] = false;
            this.passable[i] = false;
            this.solid[i] = true;
        }
        for (i = (this.height - 12 + 2) * this.width; i < this.length; ++i) {
            this.avoid[i] = false;
            this.passable[i] = false;
            this.solid[i] = true;
        }
        for (i = (this.height - 12 + 1) * this.width; i < this.length; ++i) {
            if (i % this.width < 4 || i % this.width > 12 || i >= this.length - this.width) {
                this.discoverable[i] = false;
                continue;
            }
            this.visited[i] = true;
        }
    }

	//END(port from Arknights): 方舟区域音乐（原来没有覆盖此方法 → 进关卡没音乐）
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.BOSS_SIESTA2, true );
	}
}
