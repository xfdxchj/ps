package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.ExampleBoss;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * ═══════════════════════════════════════════════════════════════
 *  示例 Boss 场地
 * ═══════════════════════════════════════════════════════════════
 *
 * 手工竞技场：中间空地 + 四周墙 + 四角柱子。
 *
 * ⚠️ 挑战区两条铁律（见 05-避坑清单.md 坑 6/7）：
 *   1. 过渡类型必须 REGULAR_ENTRANCE
 *   2. 入口格 ≠ 出口格
 */
public class ExampleBossLevel extends Level {

    private static final int WIDTH  = 15;
    private static final int HEIGHT = 15;

    @Override
    protected boolean build() {
        setSize( WIDTH, HEIGHT );

        // ─────────── 地形 ───────────
        for (int i = 0; i < length(); i++) {
            int x = i % WIDTH;
            int y = i / WIDTH;

            boolean edge = (x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1);
            map[i] = edge ? Terrain.WALL : Terrain.EMPTY;
        }

        // 四根柱子（装饰 + 掩体）
        int[] pillars = {
                3 * WIDTH + 3,      3 * WIDTH + (WIDTH - 4),
                (HEIGHT - 4) * WIDTH + 3, (HEIGHT - 4) * WIDTH + (WIDTH - 4)
        };
        for (int p : pillars) {
            map[p] = Terrain.WALL_DECO;
        }

        // ─────────── 入口 / 出口 ───────────
        int center      = (HEIGHT / 2) * WIDTH + (WIDTH / 2);
        int entranceCell = center - 5;      // 入口在中心左侧
        int exitCell     = center + 5;      // 出口在中心右侧（不同格！）

        set( entranceCell, Terrain.ENTRANCE );
        set( exitCell,     Terrain.EXIT );

        // ⚠️ 挑战区必须用 REGULAR_ENTRANCE
        transitions.add( new LevelTransition( this, entranceCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );
        transitions.add( new LevelTransition( this, exitCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );

        return true;
    }

    @Override
    protected void createMobs() {
        ExampleBoss boss = new ExampleBoss();
        boss.pos = (HEIGHT / 2) * WIDTH + (WIDTH / 2);
        GameScene.add( boss );
    }

    @Override
    protected void createItems() {
        // 没有额外物品
    }

    // ─────────── 贴图 ───────────
    @Override
    public String tilesTex() {
        return "environment/tiles_halls.png";
    }

    @Override
    public String waterTex() {
        return "environment/water0.png";
    }

    // Boss 场地不刷普通怪
    @Override
    public int nMobs() {
        return 0;
    }

    @Override
    public String name() {
        return "示例天王之厅";
    }
}
