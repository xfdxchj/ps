package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.FistSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  「古神之拳」 —— 5号召唤王召唤的小 Boss
 * ═══════════════════════════════════════════════════════════════
 *
 * 6 个同时存在。
 *
 * 强化规则（由父 Boss 每回合检查）：
 *   击杀 3 个后 → 剩余 3 个进入强化（攻速+、伤害+）
 *   只剩 1 个     → 攻速 +100%、受伤 -50%
 *
 * 死亡时通知父 Boss。
 */
public class AncientFist extends Mob {

    {
        spriteClass = FistSprite.class;

        //END(移植调整): AncientFist 继承 Mob（不是 Boss），所以用 Mob 的字段，
        //没有 Boss 的 baseXxx。伤害/命中由 damageRoll()/attackSkill() 直接返回。
        HT      = 200;
        EXP     = 20;
        defenseSkill = 15;      // 闪避基准

        alignment = Alignment.ENEMY;
    }

    /** 父 Boss */
    public GuidingKing parent;

    /** 强化等级：0=普通 1=强化(剩3个) 2=狂暴(剩1个) */
    public int empowered = 0;

    private static final String EMPOWERED = "empowered";

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        // 三阶段强化：攻速
        if (empowered >= 2) {
            // 狂暴：每回合有概率额外行动一次
            if (Random.Int(2) == 0 && enemy != null && enemySeen) {
                if (Dungeon.level.adjacent(pos, enemy.pos)) {
                    attack( enemy );
                } else {
                    // 额外移动一步
                }
            }
        }

        return super.act();
    }

    /** 设置强化等级 */
    public void empower(int level) {
        if (level <= empowered) return;
        empowered = level;

        if (level == 1) {
            // 强化：伤害 +50%
            damageMult = 1.5f;
            sprite.flash();
        } else if (level == 2) {
            // 狂暴：攻速 +100%（用 speed 表示）、受伤 -50%
            speedMultiplier = 2f;
            damageReduction = 0.5f;
            sprite.flash();
            yell( Messages.get(this, "rage") );
        }
    }

    /** 伤害倍率（强化 +50% = 1.5） */
    private float damageMult = 1f;
    /** 速度倍率（攻速 +100% = 2 倍速） */
    private float speedMultiplier = 1f;
    /** 受伤减免（0~1） */
    private float damageReduction = 0f;

    @Override
    public float speed() {
        return super.speed() * speedMultiplier;
    }

    /** 伤害：12-20 基础，按强化倍率放大 */
    @Override
    public int damageRoll() {
        return Math.round( Random.NormalIntRange(12, 20) * damageMult );
    }

    /** 命中 */
    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (damageReduction > 0f) {
            damage = Math.round( damage * (1f - damageReduction) );
        }
        return super.defenseProc(enemy, damage);
    }

    // ═══════════════════════════════════════════════
    @Override
    public void die(Object cause) {
        super.die(cause);

        if (Dungeon.level.heroFOV[pos]) {
            CellEmitter.get(pos).burst( ShadowParticle.UP, 20 );
        }

        // 通知父 Boss
        if (parent != null) {
            parent.onFistDied( this );
        }
    }

    @Override
    public String name() { return Messages.get(this, "name"); }

    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(EMPOWERED, empowered);
        bundle.put("speedMult", speedMultiplier);
        bundle.put("damageMult", damageMult);
        bundle.put("dmgRed", damageReduction);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        empowered        = bundle.getInt(EMPOWERED);
        speedMultiplier  = bundle.getFloat("speedMult");
        damageMult       = bundle.getFloat("damageMult");
        damageReduction  = bundle.getFloat("dmgRed");
    }
}
