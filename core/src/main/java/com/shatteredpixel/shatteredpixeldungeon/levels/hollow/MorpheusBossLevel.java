package com.shatteredpixel.shatteredpixeldungeon.levels.hollow;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_SP;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.Morphs;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.TowerGods;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.TowerMachine;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.TowerMind;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.hollow.TowerTime;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;

public class MorpheusBossLevel extends Level {

    public int levelIDStatus = 0;

    {
        color1 = 0x801500;
        color2 = 0xa68521;
        viewDistance = 16;
        extraGlass = false;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put("level_id_status", levelIDStatus);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        levelIDStatus = bundle.getInt("level_id_status");
    }

    @Override
    public boolean activateTransition(Hero hero, LevelTransition transition) {
        return false;
    }

    @Override
    public void playLevelMusic(){
        Music.playModeBGM(Assets.Music.MORP_BOSS, true);
    }

    //END(移植调整): 本 fork 无 Level.playBossMusic() → 删除该 override

    private static final int WIDTH = 25;
    private static final int HEIGHT = 25;

    private static final int S = CHASM;
    private static final int G = WALL;
    private static final int E = EMPTY_SP;

    private static final int[] code_map = {
            S,S,S,S,S,S,S,S,G,G,G,G,G,G,G,G,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,G,G,G,G,E,E,E,E,E,E,G,G,G,G,S,S,S,S,S,S,
            S,S,S,S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,S,S,S,
            S,S,S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,S,S,
            S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,G,S,S,
            S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,
            S,S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
            S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
            S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,
            G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,
            S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,
            S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
            S,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,
            S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,
            S,S,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,G,S,S,
            S,S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,G,S,S,S,
            S,S,S,S,S,G,G,G,E,E,E,E,E,E,E,E,E,E,G,G,G,G,S,S,S,
            S,S,S,S,S,S,S,G,G,G,E,E,E,E,E,G,G,G,G,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,G,G,G,G,G,G,G,S,S,S,S,S,S,S,S,S,
    };

    @Override
    protected boolean build() {
        //END(修复): 清理残留锁（上一层的 LockedFloor 会让踩楼梯失效）
        unseal();
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(
                com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero,
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor.class);

        setSize(WIDTH, HEIGHT);
        map = code_map.clone();

        int entrance = 412;
        int exit = 312;   //END(修复): 原为 0(=CHASM 深渊，玩家走不到) → 改用场地中心空地

        //END(修复): 地图里没有 ENTRANCE/EXIT 地形，直接写进 map，否则踩不上/掉深渊
        map[entrance] = Terrain.ENTRANCE;
        map[exit]     = Terrain.EXIT;

        LevelTransition enter = new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE);
        transitions.add(enter);

        //END(修复·关键): depth>=26 只有 REGULAR_ENTRANCE 能被触发
        LevelTransition exits = new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT);
        transitions.add(exits);

        CustomTilemap vis = new GalaxyBackGround();
        vis.pos(0, 0);
        customTiles.add(vis);

        addClickableExits();

        return true;
    }

    @Override
    protected void createMobs() {
        Morphs morphs = new Morphs();
        morphs.pos = 312;
        mobs.add(morphs);
    }

    @Override
    public void occupyCell( Char ch ) {
        super.occupyCell(ch);

        if (levelIDStatus == 0 && ch == Dungeon.hero) {
            seal();
            levelIDStatus++;
        }
    }


    @Override
    public void seal() {
        super.seal();

        TowerGods towerGods = new TowerGods();
        towerGods.pos = 304;
        Buff.affect(towerGods, Barrier.class).setShield(100);
        GameScene.add(towerGods);

        TowerTime towerTime = new TowerTime();
        towerTime.pos = 512;
        Buff.affect(towerTime, Barrier.class).setShield(100);
        GameScene.add(towerTime);

        TowerMachine towerMachine = new TowerMachine();
        towerMachine.pos = 112;
        Buff.affect(towerMachine, Barrier.class).setShield(100);
        GameScene.add(towerMachine);

        TowerMind towerMind = new TowerMind();
        towerMind.pos = 320;
        Buff.affect(towerMind, Barrier.class).setShield(100);
        GameScene.add(towerMind);

        TowerMind.MindCore mindCore = new TowerMind.MindCore();
        mindCore.pos = 272;
        GameScene.add(mindCore);

        TowerMind.MindCore mindCore2 = new TowerMind.MindCore();
        mindCore2.pos = 396;
        GameScene.add(mindCore2);

        TowerMind.MindCore mindCore3 = new TowerMind.MindCore();
        mindCore3.pos = 293;
        GameScene.add(mindCore3);

        AlarmTrap alarmTrap = new AlarmTrap();
        alarmTrap.pos = 312;
        alarmTrap.activate();

        Buff.affect(Dungeon.hero, Invisibility.class, 10f);
    }

    public static class GalaxyBackGround extends CustomTilemap {

        {
            texture = Assets.Environment.GALAXY_BACKGROUND;

            tileW = 25;
            tileH = 25;
        }

        final int TEX_WIDTH = 25*16;

        @Override
        public Tilemap create() {

            Tilemap v = super.create();

            int[] data = mapSimpleImage(0, 0, TEX_WIDTH);

            v.map(data, tileW);
            return v;
        }

    }

    @Override
    protected void createItems() {

    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_MORGALAXY;
    }

    @Override
    public String waterTex() {
        return Assets.Interfaces.BLACK_RECT;
    }

	/**
	 * END(修复·挑战区楼梯): 挑战区（26F+）有两条互相冲突的限制：
	 *  1) Hero.java:1977 —— depth>=26 时只有 REGULAR_ENTRANCE 能被**点击触发**；
	 *  2) InterlevelScene.ascend() —— 上楼要落在目标层的 REGULAR_EXIT 上，
	 *     若目标层没有 EXIT，就会退化成第一个过渡 → **上楼落在"入口"上**。
	 *
	 * 因此不能把 EXIT 直接改成 ENTRANCE（会破坏第 2 条）。
	 * 正确做法：**保留 EXIT，并额外补一个同格的 ENTRANCE**。
	 * 同格两个过渡在 getTransition(cell) 时都能命中，
	 * 而 getTransition(Type) 仍能正确区分入口/出口。
	 */
	protected void addClickableExits() {
		if (transitions == null) return;
		java.util.ArrayList<LevelTransition> add = new java.util.ArrayList<>();
		for (LevelTransition t : transitions) {
			if (t != null && t.type == LevelTransition.Type.REGULAR_EXIT) {
				boolean dup = false;
				for (LevelTransition o : transitions) {
					if (o != null && o.cell() == t.cell()
							&& o.type == LevelTransition.Type.REGULAR_ENTRANCE) {
						dup = true;
						break;
					}
				}
				if (!dup) {
					add.add(new LevelTransition(this, t.cell(),
							LevelTransition.Type.REGULAR_ENTRANCE));
				}
			}
		}
		transitions.addAll(add);
	}

}
