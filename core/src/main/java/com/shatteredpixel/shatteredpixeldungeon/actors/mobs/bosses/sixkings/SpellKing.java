package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.SpellKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  1号 · 法术王
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · 所有攻击均为法术伤害
 *   · 魔法追击（累计命中触发）
 *   · 三阶段法术强化
 *   · 三阶段获得 25% 减伤
 *
 * 与草稿版的修正：
 *   · attackHook 不加 @Override（本 fork 里它不是覆写）
 *   · yell() 改为从 Messages 读取，避免硬编码中文
 *   · 加了特效（CellEmitter）
 */
public class SpellKing extends Boss {

    {
        spriteClass = SpellKingSprite.class;

        HT  = 1000;
        EXP = 100;
        baseHT = HT;

        baseMin = 25;
        baseMax = 25;
        baseAcc = 35;
        baseEva = 15;
        baseMinDef = 0;
        baseMaxDef = 0;

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.WEP_T5;
        lootChance = 1f;

        spellCd = 5;
    }

    private int spellCd = 5;
    /** 一二阶段累计命中数，每 3 次触发追击 */
    private int magicHits = 0;
    private int phase = 1;

    private static final String SPELL_CD   = "spellCd";
    private static final String MAGIC_HITS = "magicHits";
    private static final String PHASE      = "phase";

    // ═══════════════════════════════════════════════
    //  AI 主循环
    // ═══════════════════════════════════════════════
    
    @Override
    protected boolean act() {

        updatePhase();

        if (enemy != null && enemySeen && spellCd-- <= 0) {
            castSpell();
            spellCd = 5;
            spend( TICK );
            return true;
        }

        return super.act();
    }

    private void updatePhase() {
        int newPhase;
        if (HP > HT * 2 / 3)      newPhase = 1;
        else if (HP > HT / 3)     newPhase = 2;
        else                      newPhase = 3;

        if (newPhase != phase) {
            phase = newPhase;
            sprite.flash();
            if (phase == 2) yell( Messages.get(this, "phase2") );
            if (phase == 3) yell( Messages.get(this, "phase3") );
        }
    }

    private void castSpell() {
        if (enemy == null) return;
        switch (phase) {
            case 1:  castPhaseOne(enemy);   break;
            case 2:  castPhaseTwo(enemy);   break;
            default: castPhaseThree(enemy); break;
        }
    }

    // ── 一阶段：火焰 / 解离 / 落石 ──
    private void castPhaseOne(Char target) {
        switch (Random.Int(3)) {
            case 0:
                target.damage( Random.IntRange(10, 20), this );
                Buff.affect(target, Burning.class).reignite(target);
                CellEmitter.get(target.pos).burst( FlameParticle.FACTORY, 8 );
                break;
            case 1:
                target.damage( Random.IntRange(15, 25), this );
                CellEmitter.get(target.pos).burst( SparkParticle.FACTORY, 8 );
                break;
            default:
                target.damage( Random.IntRange(12, 22), this );
                break;
        }
    }

    // ── 二阶段：全图火焰 / 冲击波 / 闪电 / 冰冻 ──
    private void castPhaseTwo(Char target) {
        switch (Random.Int(4)) {
            case 0:
                target.damage( Random.IntRange(15, 25), this );
                Buff.affect(target, Burning.class).reignite(target);
                CellEmitter.get(target.pos).burst( FlameParticle.FACTORY, 12 );
                break;
            case 1:
                target.damage( Random.IntRange(20, 30), this );
                break;
            case 2:
                target.damage( Random.IntRange(20, 35), this );
                CellEmitter.get(target.pos).burst( SparkParticle.FACTORY, 14 );
                break;
            default:
                target.damage( Random.IntRange(10, 20), this );
                Buff.prolong(target, Frost.class, 2f);
                break;
        }
    }

    // ── 三阶段：伤害 +25%，附带控制 ──
    private void castPhaseThree(Char target) {
        int damage;
        switch (Random.Int(4)) {
            case 0:
                damage = Math.round( Random.IntRange(10, 20) * 1.25f );
                target.damage( damage, this );
                Buff.affect(target, Burning.class).reignite(target);
                break;
            case 1:
                damage = Math.round( Random.IntRange(15, 25) * 1.25f );
                target.damage( damage, this );
                break;
            case 2:
                damage = Math.round( Random.IntRange(20, 35) * 1.25f );
                target.damage( damage, this );
                Buff.prolong(target, Paralysis.class, 1f);
                break;
            default:
                damage = Math.round( Random.IntRange(15, 25) * 1.25f );
                target.damage( damage, this );
                Buff.prolong(target, Chill.class, 3f);
                break;
        }
    }

    // ═══════════════════════════════════════════════
    //  魔法追击（累计命中触发）
    // ═══════════════════════════════════════════════
    public boolean attackHook(Char enemy) {
        magicHits++;
        if (phase < 3) {
            if (magicHits >= 3) {
                magicHits = 0;
                magicChase(enemy);
            }
        } else {
            magicChase(enemy);
        }
        return false;
    }

    private void magicChase(Char target) {
        if (target == null) return;
        int damage;
        if (phase == 1) {
            damage = Random.IntRange(20, 35);
            target.damage( damage, this );
        } else {
            damage = Random.IntRange(20, 30);
            target.damage( damage, this );
            Buff.prolong(target, Paralysis.class, 1f);
        }
    }

    // ═══════════════════════════════════════════════
    //  三阶段 25% 减伤
    // ═══════════════════════════════════════════════
    
    @Override
    public int defenseProc(Char enemy, int damage) {
        updatePhase();
        if (phase >= 3) {
            damage = Math.round(damage * 0.75f);
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        GameScene.bossSlain();
    }

    @Override
    public String name() { return Messages.get(this, "name"); }

    //END(移植调整): 本 fork 的 Mob/Char 没有 title() 方法，
    //Boss 血条上的称号交给 name() 处理，这里保留一个普通方法备用。
    public String title() { return Messages.get(this, "title"); }

    @Override
    public void notice() {
        super.notice();
        yell( Messages.get(this, "notice") );
    }

    // ═══════════════════════════════════════════════
    //  存档
    // ═══════════════════════════════════════════════
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SPELL_CD, spellCd);
        bundle.put(MAGIC_HITS, magicHits);
        bundle.put(PHASE, phase);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        spellCd   = bundle.getInt(SPELL_CD);
        magicHits = bundle.getInt(MAGIC_HITS);
        phase     = bundle.getInt(PHASE);
    }
}
