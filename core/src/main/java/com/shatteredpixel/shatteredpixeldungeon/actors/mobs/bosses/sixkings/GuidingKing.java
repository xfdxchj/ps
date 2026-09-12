package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.SummonKingSprite;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  5号 · 召唤王（六古神之拳）
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · HP = 1，**永久无敌**，本体不参与战斗
 *   · 开战直接召唤【原版】的 6 个古神之拳
 *   · 玩家无法通过打本体推进战斗，必须清掉六拳
 *   · 六拳全灭后本体失去无敌，可被一击杀死
 *
 * END(用户要求·重要): **不给古神之拳加任何 buff**。
 * 玩家就是直接面对原版的 6 个古神之拳（燃烧/泥土/腐烂/锈蚀/光明/黑暗），
 * 它们的血量、伤害、技能、特效全部保持原版数值，不做任何强化。
 *
 * 原版古神之拳（{@link YogFist}）：
 *   贴图 sprites/yog_fists.png，帧布局 TextureFilm(24, 17)
 */
public class GuidingKing extends Boss {

    {
        spriteClass = SummonKingSprite.class;

        HT  = 1;            // 本体 1 血
        EXP = 120;
        baseHT = HT;
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 0;        // 本体不攻击
        baseMax = 0;
        baseAcc = 0;
        baseEva = 0;
        baseMinDef = 0;
        baseMaxDef = 0;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

        properties.add( Property.BOSS );
        properties.add( Property.IMMOVABLE );   // 本体不动
        alignment = Alignment.ENEMY;

        loot = Generator.Category.ARTIFACT;
        lootChance = 1f;
    }

    /** 存活的拳 */
    private final ArrayList<Mob> fists = new ArrayList<>();
    private boolean summoned = false;

    private static final String SUMMONED = "summoned";

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        //END(修复): 没有 assignBoss → Boss 血条不显示。"法术王没有血条"
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss( this );
        }

        // ── 开战：召唤 6 个古神之拳 ──
        if (!summoned) {
            summoned = true;
            summonSixFists();
            spend( TICK );
            return true;
        }

        // 清理已死引用
        fists.removeIf( f -> f == null || !f.isAlive() );

        // 只要还有拳活着，本体维持无敌；全灭则解除
        if (!fists.isEmpty()) {
            if (buff(Invulnerability.class) == null) {
                Buff.affect(this, Invulnerability.class);
            }
        } else {
            Buff.detach(this, Invulnerability.class);
        }

        spend( TICK );
        return true;
    }

    /** 召唤原版 6 种古神之拳（**不做任何强化**） */
    private void summonSixFists() {

        yell( Messages.get(this, "summon") );

        Class<? extends YogFist>[] kinds = new Class[]{
                YogFist.BurningFist.class,   // 燃烧之拳
                YogFist.SoiledFist.class,    // 泥土之拳
                YogFist.RottingFist.class,   // 腐烂之拳
                YogFist.RustedFist.class,    // 锈蚀之拳
                YogFist.BrightFist.class,    // 光明之拳
                YogFist.DarkFist.class       // 黑暗之拳
        };

        int placed = 0;
        for (Class<? extends YogFist> k : kinds) {
            int cell = findSpawnCell( placed );
            if (cell == -1) {
                placed++;
                continue;
            }
            try {
                YogFist fist = k.getDeclaredConstructor().newInstance();
                fist.pos = cell;
                fists.add( fist );
                //END: 这里是 act() 阶段，Dungeon.level 已存在，用 GameScene.add 正确。
                GameScene.add( fist );

                if (Dungeon.level != null && Dungeon.level.heroFOV[cell]) {
                    CellEmitter.get(cell).burst( ShadowParticle.UP, 12 );
                }
            } catch (Exception e) {
                com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
                        "[六王] 召唤古神之拳失败: " + e);
            }
            placed++;
        }

        Buff.affect(this, Invulnerability.class);
    }

    /** 找环绕本体的空格 */
    private int findSpawnCell(int index) {
        int w = Dungeon.level.width();
        int cx = pos % w, cy = pos / w;

        int[][] offsets = {
                { 0, -2}, { 2, -1}, { 2,  1}, { 0,  2}, {-2,  1}, {-2, -1},
                { 1, -3}, { 3, -1}, { 3,  1}, { 1,  3}, {-1,  3}, {-3,  1},
                {-3, -1}, {-1, -3}
        };

        for (int i = 0; i < offsets.length; i++) {
            int idx = (index + i) % offsets.length;
            int x = cx + offsets[idx][0];
            int y = cy + offsets[idx][1];

            if (x < 1 || y < 1 || x >= w - 1 || y >= Dungeon.level.height() - 1) continue;

            int c = y * w + x;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (Actor.findChar(c) != null) continue;

            return c;
        }
        return -1;
    }

    // ═══════════════════════════════════════════════
    //  本体：只要还有拳活着就免伤
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        if (!fists.isEmpty()) {
            return 0;
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    public void die(Object cause) {
        for (Mob f : fists) {
            if (f != null && f.isAlive()) f.die( null );
        }
        fists.clear();

        super.die(cause);
        GameScene.bossSlain();
    }

    @Override
    public String name() { return Messages.get(this, "name"); }

    public String title() { return Messages.get(this, "title"); }

    @Override
    public void notice() {
        super.notice();
        yell( Messages.get(this, "notice") );
    }

    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SUMMONED, summoned);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        summoned = bundle.getBoolean(SUMMONED);
    }
}
