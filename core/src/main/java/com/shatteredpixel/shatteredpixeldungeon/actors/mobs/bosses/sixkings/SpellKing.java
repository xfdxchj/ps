package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
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
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 25;
        baseMax = 25;
        baseAcc = 35;
        baseEva = 15;
        baseMinDef = 0;
        baseMaxDef = 0;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

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

        //END(修复): 没有 assignBoss → Boss 血条不显示。"法术王没有血条"
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss( this );
        }

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

    // ═══════════════════════════════════════════════
    //  END(用户反馈): "法师的魔弹，闪电都没有特效"
    //  下面每个法术都配了【光束/闪电/爆炸】的可视化。
    // ═══════════════════════════════════════════════

    /** 从 Boss 到目标画一条光束（魔弹） */
    private void bolt( Char target, int color ) {
        try {
            if (sprite == null || sprite.parent == null) return;
            if (target == null || target.sprite == null) return;

            com.shatteredpixel.shatteredpixeldungeon.effects.Beam.LightRay ray =
                    new com.shatteredpixel.shatteredpixeldungeon.effects.Beam.LightRay(
                            sprite.center(), target.sprite.center() );
            ray.hardlight( color );
            sprite.parent.add( ray );

            // 沿线火花
            com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica b =
                    new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(
                            pos, target.pos,
                            com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT );
            for (int c : b.path) {
                if (Dungeon.level.heroFOV[c]) {
                    CellEmitter.get(c).burst( SparkParticle.FACTORY, 2 );
                }
            }
        } catch (Throwable ignored) { }
    }

    /** 从 Boss 到目标的闪电（真正的闪电弧） */
    private void lightning( Char target ) {
        try {
            if (sprite == null || sprite.parent == null) return;
            if (target == null || target.sprite == null) return;

            com.shatteredpixel.shatteredpixeldungeon.effects.Lightning l =
                    new com.shatteredpixel.shatteredpixeldungeon.effects.Lightning(
                            sprite.center(), target.sprite.center(), null );
            sprite.parent.add( l );

            CellEmitter.get( target.pos ).burst( SparkParticle.FACTORY, 16 );
        } catch (Throwable ignored) { }
    }

    /** 目标处的小爆炸 */
    private void burst( Char target, int count ) {
        if (target == null) return;
        if (Dungeon.level.heroFOV[target.pos]) {
            CellEmitter.get( target.pos ).burst( FlameParticle.FACTORY, count );
            CellEmitter.get( target.pos ).burst( SparkParticle.FACTORY, count );
        }
    }

    // ── 一阶段：火焰 / 魔弹 / 落石 ──
    private void castPhaseOne(Char target) {
        switch (Random.Int(3)) {
            case 0:   // 火焰
                bolt( target, 0xFF6622 );
                target.damage( Random.IntRange(10, 20), this );
                Buff.affect(target, Burning.class).reignite(target);
                burst( target, 10 );
                break;
            case 1:   // 奥术魔弹
                bolt( target, 0x66CCFF );
                target.damage( Random.IntRange(15, 25), this );
                burst( target, 12 );
                break;
            default:  // 落石
                bolt( target, 0xAAAAAA );
                target.damage( Random.IntRange(12, 22), this );
                burst( target, 8 );
                break;
        }
    }

    // ── 二阶段：火焰 / 冲击波 / 闪电 / 冰冻 ──
    private void castPhaseTwo(Char target) {
        switch (Random.Int(4)) {
            case 0:   // 火焰
                bolt( target, 0xFF4400 );
                target.damage( Random.IntRange(15, 25), this );
                Buff.affect(target, Burning.class).reignite(target);
                burst( target, 14 );
                break;
            case 1:   // 奥术冲击
                bolt( target, 0xCC66FF );
                target.damage( Random.IntRange(20, 30), this );
                burst( target, 14 );
                break;
            case 2:   // ★ 闪电
                lightning( target );
                target.damage( Random.IntRange(20, 35), this );
                break;
            default:  // 冰锥
                bolt( target, 0x99DDFF );
                target.damage( Random.IntRange(10, 20), this );
                Buff.prolong(target, Frost.class, 2f);
                burst( target, 10 );
                break;
        }
    }

    // ── 三阶段：伤害 +25%，附带控制 ──
    private void castPhaseThree(Char target) {
        switch (Random.Int(4)) {
            case 0:   // 烈焰
                bolt( target, 0xFF3300 );
                target.damage( Math.round( Random.IntRange(10, 20) * 1.25f ), this );
                Buff.affect(target, Burning.class).reignite(target);
                burst( target, 16 );
                break;
            case 1:   // 奥术
                bolt( target, 0xDD66FF );
                target.damage( Math.round( Random.IntRange(15, 25) * 1.25f ), this );
                burst( target, 16 );
                break;
            case 2:   // ★ 闪电 + 麻痹
                lightning( target );
                target.damage( Math.round( Random.IntRange(20, 35) * 1.25f ), this );
                Buff.prolong(target, Paralysis.class, 1f);
                break;
            default:  // 冰霜
                bolt( target, 0x66EEFF );
                target.damage( Math.round( Random.IntRange(15, 25) * 1.25f ), this );
                Buff.prolong(target, Chill.class, 3f);
                burst( target, 14 );
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
