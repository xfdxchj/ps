package com.shatteredpixel.shatteredpixeldungeon.levels.hollow;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.ENTRANCE;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EXIT;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;

public class TheatreLevel extends Level {
    {
        color1 = 0x801500;
        color2 = 0xa68521;
        viewDistance = 16;
        extraGlass = false;
    }

    @Override
    public boolean activateTransition(Hero hero, LevelTransition transition) {
        if(Dungeon.depth == 31){
            super.activateTransition(hero, transition);
        }
        return false;
    }

    @Override
    public void playLevelMusic(){
        if(Dungeon.depth == 31){
            Music.INSTANCE.end();
        } else {
            Music.INSTANCE.play(Assets.Music.HOLLOW_CITY_HARD, true);
        }

    }

    private static final int WIDTH = 21;
    private static final int HEIGHT = 17;

    private static final int R = EMPTY;
    private static final int S = com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CUSTOM_DECO_EMPTY; //END(port): 本fork无 SIGN
    private static final int A = EXIT;
    private static final int B = ENTRANCE;

    private static final int[] code_map = {
            S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,B,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,R,S,S,S,S,S,S,S,S,S,S,
            S,S,S,S,S,S,S,S,S,S,R,S,S,S,S,S,S,S,S,S,S,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,
            S,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,S,
            S,S,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,R,S,S,
            S,S,S,S,S,S,S,R,R,R,A,R,R,R,S,S,S,S,S,S,S,
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

        int entrance = 115;   //END(修复): 真实 ENTRANCE 格
        int exit = 346;   //END(修复): 真实 EXIT 格

        LevelTransition enter = new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE);
        transitions.add(enter);

        //END(修复·关键): depth>=26 只有 REGULAR_ENTRANCE 能被触发
        LevelTransition exits = new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT);
        transitions.add(exits);

        CustomTilemap vis = new Theatre();
        vis.pos(0, 0);
        customTiles.add(vis);

        addClickableExits();

        return true;
    }

    public static class Theatre extends CustomTilemap {

        {
            texture = Assets.Environment.TILES_THEATRE;

            tileW = WIDTH;
            tileH = HEIGHT;
        }

        final int TEX_WIDTH = WIDTH*16;

        @Override
        public Tilemap create() {

            Tilemap v = super.create();

            int[] data = mapSimpleImage(0, 0, TEX_WIDTH);

            v.map(data, tileW);
            return v;
        }

    }

    @Override
    protected void createMobs() {
    }

    @Override
    protected void createItems() {
        //END: 生成剧情 NPC 莫菲斯（小游戏相关分支已裁剪）
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.hollow.MorphsNPC npc =
                new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.hollow.MorphsNPC();
        npc.pos = 136;
        mobs.add(npc);
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_HOLLOW_CS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_HALLS;
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
