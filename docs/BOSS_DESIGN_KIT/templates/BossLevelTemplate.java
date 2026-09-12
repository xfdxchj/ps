package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.SorcererKing;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * ═══════════════════════════════════════════════════════════════
 *  Boss 场地模板 —— 复制本文件并改名
 * ═══════════════════════════════════════════════════════════════
 *
 * 这是一个**最简单的手工竞技场**：中间空地 + 四周墙。
 * 想做复杂地形，参考：
 *   core/.../levels/hollow/CerDogBossLevel.java   （魔绫冥犬场地）
 *   core/.../levels/LaveCavesBossLevel.java       （魔绫火龙场地）
 *
 * ⚠️ 挑战区（26F+）的两条铁律：
 *   1. 过渡类型必须是 REGULAR_ENTRANCE（否则踩楼梯无反应）
 *   2. 入口格和出口格必须不同（否则进入就被弹到下一层）
 */
public class SixKingsLevel1 extends Level {

    private static final int WIDTH  = 15;
    private static final int HEIGHT = 15;

    @Override
    protected boolean build() {
        setSize( WIDTH, HEIGHT );

        // ─────────── 地形：中间空地，四周墙 ───────────
        for (int i = 0; i < length(); i++) {
            int x = i % WIDTH;
            int y = i / WIDTH;
            boolean edge = (x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1);
            map[i] = edge ? Terrain.WALL : Terrain.EMPTY;
        }

        int center = (HEIGHT / 2) * WIDTH + (WIDTH / 2);

        // ─────────── 入口 / 出口格 ───────────
        int entranceCell = center;          // 玩家从上一层的楼梯下来落到这里
        int exitCell     = center + 4;      // Boss 死后从这里去下一层

        set( entranceCell, Terrain.ENTRANCE );
        set( exitCell,     Terrain.EXIT );

        // ⚠️ 挑战区必须用 REGULAR_ENTRANCE（Hero.java 的 depth>=26 分支要求）
        transitions.add( new LevelTransition( this, entranceCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );
        transitions.add( new LevelTransition( this, exitCell,
                LevelTransition.Type.REGULAR_ENTRANCE ) );

        return true;
    }

    // ═══════════════════════════════════════════════
    //  放置 Boss
    // ═══════════════════════════════════════════════
    @Override
    protected void createMobs() {
        SorcererKing boss = new SorcererKing();
        boss.pos = (HEIGHT / 2) * WIDTH + (WIDTH / 2) + 3;   // 场地中心偏右
        GameScene.add( boss );

        // 例：额外放几个小怪
        // Bat b = new Bat();
        // b.pos = ...;
        // GameScene.add( b );
    }

    @Override
    protected void createItems() {
        // 例：Boss 场地放个补给
        // addItemToSpawn( new PotionOfHealing() );
    }

    // ═══════════════════════════════════════════════
    //  贴图
    // ═══════════════════════════════════════════════
    @Override
    public String tilesTex() {
        return "environment/tiles_halls.png";   // 可换成任意 terrain 图集
        // 可选：tiles_city / tiles_caves / tiles_iberia / tiles_siesta ...
    }

    @Override
    public String waterTex() {
        return "environment/water0.png";
    }

    // ═══════════════════════════════════════════════
    //  可选：不让随机刷怪
    // ═══════════════════════════════════════════════
    @Override
    public int nMobs() {
        return 0;   // Boss 场地通常不刷普通怪
    }

    // ═══════════════════════════════════════════════
    //  可选：关卡名（图鉴/日志里显示）
    // ═══════════════════════════════════════════════
    @Override
    public String name() {
        return "法术天王之厅";
    }
}
