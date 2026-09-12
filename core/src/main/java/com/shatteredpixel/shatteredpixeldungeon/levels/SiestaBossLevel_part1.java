//END(port from Arknights): SiestaBossLevel_part1（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SiestaBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ceylon;
import com.shatteredpixel.shatteredpixeldungeon.items.ArmorUpKit;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Group;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.Arrays;

public class SiestaBossLevel_part1
extends Level {
    private static final int ROOM_TOP = 12;
    public int Moneygirl;

    public SiestaBossLevel_part1() {
        this.color1 = 8393984;
        this.color2 = 10913057;
        this.viewDistance = 4;
        this.Moneygirl = 229;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_city.png";
    }

    @Override
    public String waterTex() {
        return "environment/water3.png";
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
        this.setSize(17, 32);
        Arrays.fill(this.map, 1);
        int MID = this.width / 2;
        Painter.fill(this, 0, 0, 17, 1, 4);
        Painter.fill(this, 0, 20, 17, 12, 4);
        Painter.fill(this, 0, 0, 1, 31, 4);
        Painter.fill(this, 16, 0, 1, 31, 4);
        Painter.fill(this, 2, 2, 13, 1, 4);
        Painter.fill(this, 2, 18, 13, 1, 4);
        Painter.fill(this, 2, 2, 1, 16, 4);
        Painter.fill(this, 14, 2, 1, 16, 4);
        this.map[42] = 5;
        this.map[314] = 5;
        this.map[184] = 5;
        this.map[172] = 5;
        this.map[54] = 4;
        this.map[64] = 4;
        this.map[292] = 4;
        this.map[302] = 4;
        this.map[290] = 4;
        this.map[66] = 4;
        this.map[52] = 4;
        this.map[304] = 4;
        this.map[36] = 1;
        this.map[48] = 1;
        this.map[308] = 1;
        this.map[320] = 1;
        Painter.fill(this, 6, 2, 1, 16, 4);
        Painter.fill(this, 10, 2, 1, 16, 4);
        Painter.fill(this, 2, 8, 12, 1, 4);
        Painter.fill(this, 2, 12, 12, 1, 4);
        this.map[176] = 5;
        this.map[180] = 5;
        this.map[144] = 5;
        this.map[212] = 5;
        this.map[76] = 4;
        this.map[110] = 4;
        this.map[246] = 4;
        this.map[280] = 4;
        Painter.fill(this, 4, 2, 1, 6, 4);
        Painter.fill(this, 4, 12, 1, 6, 4);
        Painter.fill(this, 12, 2, 1, 6, 4);
        Painter.fill(this, 12, 12, 1, 6, 4);
        this.map[139] = 5;
        this.map[207] = 5;
        this.map[149] = 5;
        this.map[217] = 5;
        this.map[91] = 5;
        this.map[95] = 5;
        this.map[261] = 5;
        this.map[265] = 5;
        this.map[274] = 5;
        this.map[286] = 5;
        this.map[70] = 5;
        this.map[82] = 5;
        this.map[39] = 5;
        this.map[45] = 5;
        this.map[311] = 5;
        this.map[317] = 5;
        this.entrance = 331;
        this.exit = 25;
        this.map[this.entrance] = 7;
        this.feeling = Level.Feeling.NONE;
        return true;
    }

    @Override
    public void occupyCell(Char ch) {
        super.occupyCell(ch);
        if (ch.pos == 212 && ch == Dungeon.hero && this.map[this.entrance] == 7 && this.map[this.exit] != 8) {
            this.seal();
        }
    }

    @Override
    public void seal() {
        super.seal();
        SiestaBossLevel_part1.set(this.entrance, 1);
        SiestaBossLevel_part1.set(this.exit, 1);
        GameScene.updateMap(this.entrance);
        GameScene.updateMap(this.exit);
        Dungeon.observe();
        if (!Ceylon.Quest.isSpawnd()) {
            new ArmorUpKit().doPickUp(Dungeon.hero);
        }
        SiestaBoss boss = new SiestaBoss();
        boss.pos = 178;
        GameScene.add(boss);
    }

    @Override
    public void unseal() {
        super.unseal();
        SiestaBossLevel_part1.set(this.entrance, 7);
        GameScene.updateMap(this.entrance);
        SiestaBossLevel_part1.set(this.exit, 8);
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
    public String tileName(int tile) {
        switch (tile) {
            case 29: {
                return Messages.get(HallsLevel.class, "water_name");
            }
            case 2: {
                return Messages.get(HallsLevel.class, "grass_name");
            }
            case 15: {
                return Messages.get(HallsLevel.class, "high_grass_name");
            }
            case 25: 
            case 26: {
                return Messages.get(HallsLevel.class, "statue_name");
            }
        }
        return super.tileName(tile);
    }

    @Override
    public String tileDesc(int tile) {
        switch (tile) {
            case 29: {
                return Messages.get(HallsLevel.class, "water_desc");
            }
            case 25: 
            case 26: {
                return Messages.get(HallsLevel.class, "statue_desc");
            }
            case 27: {
                return Messages.get(HallsLevel.class, "bookshelf_desc");
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
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.BOSS_SIESTA1, true );
	}
}
