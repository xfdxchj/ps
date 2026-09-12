package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.SummonKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  5号 · 召唤王（六古神之拳）
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · HP = 1，**永久无敌**，本体不参与战斗
 *   · 开战直接召唤 6 个「古神之拳」
 *   · 玩家无法通过打本体推进战斗，必须清掉拳
 *
 * 强化规则：
 *   · 击杀 3 个拳 → 剩余 3 个强化（伤害 +50%）
 *   · 只剩 1 个   → 该拳狂暴（攻速 +100%、受伤 −50%）
 *
 * 胜利条件：
 *   · 6 个拳全部被击杀 → 本体失去无敌 → 可以被一击杀死
 */
public class GuidingKing extends Boss {

    {
        spriteClass = SummonKingSprite.class;

        HT  = 1;            // 本体 1 血
        EXP = 120;
        baseHT = HT;

        baseMin = 0;        // 本身不攻击
        baseMax = 0;
        baseAcc = 0;
        baseEva = 0;
        baseMinDef = 0;
        baseMaxDef = 0;

        properties.add( Property.BOSS );
        properties.add( Property.IMMOVABLE );   // 本体不动
        alignment = Alignment.ENEMY;

        loot = Generator.Category.ARTIFACT;
        lootChance = 1f;
    }

    /** 存活的拳 */
    private final ArrayList<AncientFist> fists = new ArrayList<>();
    /** 已召唤过（避免重复召唤） */
    private boolean summoned = false;
    /** 已死亡数量 */
    private int deadCount = 0;

    private static final String SUMMONED   = "summoned";
    private static final String DEAD_COUNT = "deadCount";

    private static final int FIST_COUNT = 6;

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        // ── 开战：召唤 6 个拳 ──
        if (!summoned) {
            summoned = true;
            summonSixFists();
            spend( TICK );
            return true;
        }

        // ── 清理已死的引用 ──
        fists.removeIf( f -> f == null || !f.isAlive() );

        // ── 维持无敌（只要还有拳活着）──
        if (!fists.isEmpty()) {
            if (buff(Invulnerability.class) == null) {
                Buff.affect(this, Invulnerability.class);
            }
        } else {
            // 所有拳都死了 → 解除无敌
            Buff.detach(this, Invulnerability.class);
        }

        // ── 强化判定 ──
        checkEmpower();

        spend( TICK );
        return true;
    }

    /** 召唤 6 个古神之拳，围绕本体分布 */
    private void summonSixFists() {

        yell( Messages.get(this, "summon") );

        int placed = 0;
        int guard = 0;

        while (placed < FIST_COUNT && guard++ < 200) {
            int cell = findSpawnCell( placed );
            if (cell == -1) continue;

            AncientFist fist = new AncientFist();
            fist.pos = cell;
            fist.parent = this;
            GameScene.add( fist );
            fists.add( fist );

            if (Dungeon.level.heroFOV[cell]) {
                CellEmitter.get(cell).burst( ShadowParticle.UP, 12 );
            }
            placed++;
        }

        // 保证无敌
        Buff.affect(this, Invulnerability.class);
    }

    /** 找一个环绕本体的空格 */
    private int findSpawnCell(int index) {
        int w = Dungeon.level.width();
        int cx = pos % w, cy = pos / w;

        // 以本体为中心，半径 2-3 的环形位置
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

    /** 强化判定 */
    private void checkEmpower() {
        int alive = fists.size();

        if (alive == 3) {
            // 剩 3 个：全部强化
            boolean any = false;
            for (AncientFist f : fists) {
                if (f.empowered < 1) {
                    f.empower( 1 );
                    any = true;
                }
            }
            if (any) yell( Messages.get(this, "empower1") );

        } else if (alive == 1) {
            // 剩 1 个：狂暴
            AncientFist last = fists.get(0);
            if (last.empowered < 2) {
                last.empower( 2 );
                yell( Messages.get(this, "empower2") );
            }
        }
    }

    /** 由 AncientFist 死亡时回调 */
    public void onFistDied(AncientFist fist) {
        deadCount++;
        fists.remove( fist );

        if (fists.isEmpty()) {
            // 全部清完 → 解除无敌 + 台词
            Buff.detach(this, Invulnerability.class);
            yell( Messages.get(this, "broken") );
        } else {
            checkEmpower();
        }
    }

    // ═══════════════════════════════════════════════
    //  本体不能被打（无敌期间）
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        // 只要还有拳活着，本体免伤
        if (!fists.isEmpty()) {
            return 0;
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    public boolean isAlive() {
        // 只要有拳活着，本体"活着"
        return super.isAlive();
    }

    @Override
    public void die(Object cause) {
        // 清掉残余的拳
        for (AncientFist f : fists) {
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
        bundle.put(DEAD_COUNT, deadCount);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        summoned  = bundle.getBoolean(SUMMONED);
        deadCount = bundle.getInt(DEAD_COUNT);
    }
}
