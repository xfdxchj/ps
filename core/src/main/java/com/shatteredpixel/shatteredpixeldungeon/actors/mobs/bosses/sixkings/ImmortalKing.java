package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.sixkings.ImmortalMark;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.ImmortalKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  2号 · 不灭追猎者
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · 本体攻击力为 0，输出全靠「印记」
 *   · 魔法免疫轮换（本 fork 没有"物理免疫"概念，用 Barkskin 代替硬抗）
 *   · 三阶段进入「1 血无敌追杀」20 回合，之后才能被击杀
 *
 * 与草稿版的修正：
 *   · PathFinder.NEIGHBOURS8（不是 Mob.NEIGHBOURS8）
 *   · title() 去掉 @Override
 *   · 印记改用本 fork 的 Buff 规范
 *   · 加物理减伤（用 Barkskin 代替"物理无敌"）
 */
public class ImmortalKing extends Boss {

    {
        spriteClass = ImmortalKingSprite.class;

        HT  = 1500;
        EXP = 120;
        baseHT = HT;
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 0;        // 本体不靠普攻
        baseMax = 0;
        baseAcc = 35;
        baseEva = 0;
        baseMinDef = 0;
        baseMaxDef = 0;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.WEP_T5;
        lootChance = 1f;

        immunityCd = 10;
    }

    private int immunityCd = 10;
    /** 0=物理减伤 1=魔法免疫 2=双抗 */
    private int immunityType = 0;
    private int phase = 1;
    private int finalPhaseTurns = 0;

    /** 三阶段无敌追杀持续回合 */
    private static final int FINAL_PHASE_LEN = 20;

    private static final String IMMUNITY_CD   = "immunityCd";
    private static final String IMMUNITY_TYPE = "immunityType";
    private static final String PHASE         = "phase";
    private static final String FINAL_TURNS   = "finalPhaseTurns";

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        //END(修复): 没有 assignBoss → Boss 血条不显示。"法术王没有血条"
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss( this );
        }

        updatePhase();

        // ── 三阶段：无敌追杀 ──
        if (phase == 3) {
            finalPhaseTurns++;

            if (finalPhaseTurns <= FINAL_PHASE_LEN) {
                // 保持无敌
                if (buff(Invulnerability.class) == null) {
                    Buff.affect(this, Invulnerability.class);
                }
                // 保持 1 血
                if (HP > 1) HP = 1;

                if (enemy != null && enemySeen) {
                    chaseTarget();
                    spend( TICK );
                    return true;
                }
                spend( TICK );
                return true;
            } else {
                // 时限已过：解除无敌，可以被打死
                Buff.detach(this, Invulnerability.class);
            }
        }

        // ── 免疫轮换 ──
        if (immunityCd-- <= 0) {
            immunityCd = 10;
            rotateImmunity();
            spend( TICK );
            return true;
        }

        // ── 追猎 ──
        if (enemy != null && enemySeen) {
            chaseTarget();
            spend( TICK );
            return true;
        }

        return super.act();
    }

    private void updatePhase() {
        if (phase == 1 && HP <= HT * 2 / 3) {
            phase = 2;
            sprite.flash();
            yell( Messages.get(this, "phase2") );
        }
        if (phase == 2 && HP <= HT / 3) {
            phase = 3;
            finalPhaseTurns = 0;
            HP = 1;
            sprite.flash();
            yell( Messages.get(this, "phase3") );
        }
    }

    /** 免疫类型轮换 */
    private void rotateImmunity() {
        if (phase == 1) {
            immunityType = (immunityType + 1) % 2;
        } else {
            immunityType = (immunityType + 1) % 3;
        }
        applyImmunity();
    }

    private void applyImmunity() {
        Buff.detach(this, MagicImmune.class);
        Buff.detach(this, Barkskin.class);

        // 1 或 2：魔法免疫
        if (immunityType == 1 || immunityType == 2) {
            Buff.affect(this, MagicImmune.class);
        }
        // 0 或 2：物理减伤（本 fork 无"物理无敌"，用树肤硬抗）
        if (immunityType == 0 || immunityType == 2) {
            Buff.affect(this, Barkskin.class).set( 20, 30 );
        }
    }

    /** 追猎：靠近并叠印记 */
    private void chaseTarget() {
        if (enemy == null) return;

        int dist = Dungeon.level.distance(pos, enemy.pos);

        if (dist <= 1) {
            markPlayer( enemy );
        } else {
            moveTowards( enemy.pos );
        }
    }

    /** 给玩家叠印记 */
    private void markPlayer(Char target) {
        ImmortalMark mark = Buff.affect(target, ImmortalMark.class);
        mark.addMark( 1 );

        if (phase >= 2) {
            Buff.prolong(target, Cripple.class, 2f);
            Buff.prolong(target, Chill.class, 2f);
        }
    }

    /** 简化寻路：朝目标走一格 */
    private void moveTowards(int target) {
        int best = -1;
        int bestDist = Dungeon.level.distance(pos, target);

        for (int n : PathFinder.NEIGHBOURS8) {
            int cell = pos + n;
            if (cell < 0 || cell >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[cell]) continue;
            if (Actor.findChar(cell) != null) continue;

            int d = Dungeon.level.distance(cell, target);
            if (d < bestDist) {
                bestDist = d;
                best = cell;
            }
        }

        if (best != -1) {
            move( best );
        }
    }

    // ═══════════════════════════════════════════════
    //  受伤：固定 50% 减伤；三阶段无敌期间免伤
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {

        if (phase == 3 && finalPhaseTurns <= FINAL_PHASE_LEN) {
            return 0;      // 无敌追杀期间免伤
        }

        damage = Math.round(damage * 0.5f);
        return super.defenseProc(enemy, damage);
    }

    // ═══════════════════════════════════════════════
    //  死亡：三阶段未到时不可死
    // ═══════════════════════════════════════════════
    @Override
    public void die(Object cause) {

        if (phase == 3 && finalPhaseTurns <= FINAL_PHASE_LEN) {
            HP = 1;
            if (buff(Invulnerability.class) == null) {
                Buff.affect(this, Invulnerability.class);
            }
            return;
        }

        Buff.detach(this, Invulnerability.class);
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
        bundle.put(IMMUNITY_CD, immunityCd);
        bundle.put(IMMUNITY_TYPE, immunityType);
        bundle.put(PHASE, phase);
        bundle.put(FINAL_TURNS, finalPhaseTurns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        immunityCd      = bundle.getInt(IMMUNITY_CD);
        immunityType    = bundle.getInt(IMMUNITY_TYPE);
        phase           = bundle.getInt(PHASE);
        finalPhaseTurns = bundle.getInt(FINAL_TURNS);
    }
}
