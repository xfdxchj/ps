package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.sixkings.SixKingsGuide;

/**
 * 六王 · 第一层：引路人之厅
 *
 * 没有战斗。NPC 在此讲述六人的往事。
 */
public class SixKingsLevel0 extends SixKingsLevelBase {

    //END(修复): 直接返回 NPC，由基类 createMobs 用 mobs.add 加入
    //（createMobs 期间 Dungeon.level 为 null，不能用 GameScene.add）
    @Override
    protected Mob createBoss() {
        return new SixKingsGuide();
    }

    @Override
    protected String levelName() {
        return "引路人之厅";
    }

    /**
     * END(修复): 第 1 层是剧情层（只有 NPC，没有 Boss），**不能锁门** ——
     * 否则玩家听完故事就出不去了（没有 Boss 可杀来触发 unseal）。
     */
    @Override
    protected boolean sealOnEnter() {
        return false;
    }
}