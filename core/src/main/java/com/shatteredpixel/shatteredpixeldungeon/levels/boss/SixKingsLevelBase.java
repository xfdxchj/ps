package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * ═══════════════════════════════════════════════════════════════
 *  六王场地基类 —— 手工竞技场
 * ═══════════════════════════════════════════════════════════════
 *
 * 布局：17x17 的方形大厅 + 四角柱子 + 中央 Boss 位。
 *
 * ⚠️ 挑战区两条铁律（见 docs/BOSS_DESIGN_KIT/05-避坑清单.md）：
 *   1. 过渡类型必须 REGULAR_ENTRANCE（depth>=26 时只有它能被点击触发）
 *   2. 入口格 ≠ 出口格（否则进入就被弹到下一层）
 */
public abstract class SixKingsLevelBase extends Level {

    protected static final int W = 17;
    protected static final int H = 17;

    /** 子类返回该层要放的 Boss */
    protected abstract Mob createBoss();

    /** 子类返回关卡名 */
    protected abstract String levelName();

    /** 子类可覆写：地形贴图 */
    protected String terrainTex() {
        return "environment/tiles_halls.png";
    }

    @Override
    protected boolean build() {
        setSize( W, H );

        // ── 地形：方形大厅 + 四周墙 ──
        for (int i = 0; i < length(); i++) {
            int x = i % W, y = i / W;
            boolean edge = (x == 0 || y == 0 || x == W - 1 || y == H - 1);
            map[i] = edge ? Terrain.WALL : Terrain.EMPTY;
        }

        // ── 四角柱子（掩体，让战斗有走位空间）──
        int[][] pillars = { {3,3}, {3,W-4}, {H-4,3}, {H-4,W-4} };
        for (int[] p : pillars) {
            map[p[0] * W + p[1]] = Terrain.WALL_DECO;
        }

        // ── 中央装饰地面 ──
        int cx = W / 2, cy = H / 2;
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int c = (cy + dy) * W + (cx + dx);
                if (c >= 0 && c < length() && map[c] == Terrain.EMPTY) {
                    map[c] = Terrain.EMPTY_DECO;
                }
            }
        }

        // ── 入口 / 出口（不同格！）──
        int entranceCell = cy * W + 2;          // 左侧
        int exitCell     = cy * W + (W - 3);    // 右侧

        set( entranceCell, Terrain.ENTRANCE );
        set( exitCell,     Terrain.EXIT );

        // ⚠️ 必须 REGULAR_ENTRANCE
        transitions.add( new LevelTransition( this, entranceCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );
        transitions.add( new LevelTransition( this, exitCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );

        return true;
    }

    @Override
    protected void createMobs() {
        Mob boss = createBoss();
        boss.pos = (H / 2) * W + (W / 2);      // 场地正中
        GameScene.add( boss );
    }

    @Override
    protected void createItems() { }

    @Override
    public String tilesTex() { return terrainTex(); }

    @Override
    public String waterTex() { return "environment/water0.png"; }

    /** Boss 场地不刷普通怪 */
    @Override
    public int nMobs() { return 0; }

    //END(移植调整): 本 fork 的 Level 没有 name() 方法，这里作为普通方法提供。
    public String name() { return levelName(); }
}
