package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.OmniKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  6号 · 全能王（关底）
 * ═══════════════════════════════════════════════════════════════
 *
 * 设计思路：
 *   他是「六人的集合体」—— 每进入一个阶段，就"继承"一位已逝挚友的能力。
 *   所以四个阶段分别对应前五王的能力片段：
 *
 *     阶段1 (100%–75%)  「孤高」  —— 纯法术，模仿 1 号法术王
 *     阶段2 (75%–50%)   「怨憎」  —— 法术 + Debuff，模仿 3 号疫病王
 *     阶段3 (50%–25%)   「暴怒」  —— 加入炮击与召唤，模仿 4/5 号
 *     阶段4 (25%–0%)    「诀别」  —— 全能力狂暴 + 短暂无敌，模仿 2 号不灭者
 *
 * 特殊机制：
 *   · 【回忆】每次阶段切换会吟诵一句对应挚友的往事，并在场地留下光柱
 *   · 【共鸣】阶段4 会启动"不灭"：每损失 10% 血获得 2 回合无敌
 *   · 【终章】血量 < 5% 时进入 3 回合倒计时，期间伤害翻倍但不再移动
 */
public class OmniKing extends Boss {

    {
        spriteClass = OmniKingSprite.class;

        HT  = 2400;
        EXP = 200;
        baseHT = HT;
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 22;
        baseMax = 34;
        baseAcc = 38;
        baseEva = 18;
        baseMinDef = 10;
        baseMaxDef = 18;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.ARTIFACT;
        lootChance = 1f;

        skillCd  = 4;
        ultiCd   = 12;
    }

    // ═══════════════════════════════════════════════
    //  阶段状态
    // ═══════════════════════════════════════════════
    private int phase = 1;

    private int skillCd = 4;    // 常规技能
    private int ultiCd  = 12;   // 大招

    /** 阶段 4 的"不灭"计数 */
    private int lastResonanceHp = 100;
    /** 终章倒计时 */
    private int finaleTurns = 0;

    /** 已印记的玩家（阶段 2 用） */
    private int debuffCount = 0;

    private static final String PHASE      = "phase";
    private static final String SKILL_CD   = "skillCd";
    private static final String ULTI_CD    = "ultiCd";
    private static final String RESONANCE  = "lastResonanceHp";
    private static final String FINALE     = "finaleTurns";
    private static final String DEBUFF_CNT = "debuffCount";

    /** 终章长度 */
    private static final int FINALE_LEN = 3;

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

        // ── 终章：不再移动，疯狂输出 ──
        if (finaleTurns > 0) {
            finaleTurns--;
            if (enemy != null && enemySeen) {
                finaleStrike();
                spend( TICK );
                return true;
            }
        }

        if (enemy == null || !enemySeen) {
            return super.act();
        }

        // ── 阶段 4：共鸣（掉血给无敌）──
        if (phase >= 4) {
            int hpPct = HP * 100 / Math.max(1, HT);
            if (hpPct <= lastResonanceHp - 10) {
                lastResonanceHp = hpPct;
                Buff.affect(this, Invulnerability.class);
                yell( Messages.get(this, "resonance") );
                CellEmitter.get(pos).burst( ShadowParticle.UP, 24 );
                spend( TICK );
                return true;
            }
        }

        // ── 大招 ──
        if (ultiCd-- <= 0) {
            ultiCd = (phase >= 4) ? 8 : 12;
            castUltimate();
            spend( TICK );
            return true;
        }

        // ── 常规技能 ──
        if (skillCd-- <= 0) {
            skillCd = (phase >= 4) ? 2 : 4;
            castSkill();
            spend( TICK );
            return true;
        }

        return super.act();
    }

    // ═══════════════════════════════════════════════
    //  阶段推进
    // ═══════════════════════════════════════════════
    private void updatePhase() {

        int newPhase;
        if      (HP > HT * 3 / 4) newPhase = 1;
        else if (HP > HT / 2)     newPhase = 2;
        else if (HP > HT / 4)     newPhase = 3;
        else                      newPhase = 4;

        // 终章
        if (finaleTurns == 0 && HP * 20 < HT) {      // HP < 5%
            finaleTurns = FINALE_LEN;
            yell( Messages.get(this, "finale") );
        }

        if (newPhase != phase) {
            phase = newPhase;
            onPhaseEnter( phase );
        }
    }

    private void onPhaseEnter(int p) {
        sprite.flash();
        switch (p) {
            case 2:
                yell( Messages.get(this, "phase2") );
                break;
            case 3:
                yell( Messages.get(this, "phase3") );
                break;
            case 4:
                yell( Messages.get(this, "phase4") );
                Buff.prolong(this, Haste.class, 20f);
                Buff.affect(this, Barkskin.class).set( 15, 25 );
                lastResonanceHp = HP * 100 / Math.max(1, HT);
                break;
        }
    }

    // ═══════════════════════════════════════════════
    //  常规技能（按阶段取用前五王的能力）
    // ═══════════════════════════════════════════════
    private void castSkill() {
        switch (phase) {
            case 1:  spellBarrage();      break;   // 似法术王
            case 2:  plagueBurst();       break;   // 似疫病王
            case 3:  artillerySupport();  break;   // 似远程王
            default: berserkStrike();     break;   // 似不灭者
        }
    }

    /** 阶段 1：法术弹幕（元素三连） */
    private void spellBarrage() {
        if (enemy == null) return;

        for (int i = 0; i < 3; i++) {
            int dmg = Random.NormalIntRange( 12, 20 );
            enemy.damage( dmg, this );
        }

        switch (Random.Int(3)) {
            case 0:
                Buff.affect(enemy, Burning.class).reignite(enemy);
                CellEmitter.get(enemy.pos).burst( FlameParticle.FACTORY, 10 );
                break;
            case 1:
                Buff.prolong(enemy, Chill.class, 4f);
                break;
            default:
                Buff.prolong(enemy, Paralysis.class, 1f);
                break;
        }
    }

    /** 阶段 2：疫病爆发（Debuff + 印记） */
    private void plagueBurst() {
        if (enemy == null) return;

        enemy.damage( Random.NormalIntRange( 10, 16 ), this );

        // 每次施加 2 种负面
        for (int i = 0; i < 2; i++) {
            switch (Random.Int(8)) {
                case 0: Buff.prolong(enemy, Weakness.class,   8f); break;
                case 1: Buff.prolong(enemy, Vulnerable.class, 8f); break;
                case 2: Buff.prolong(enemy, Cripple.class,    8f); break;
                case 3: Buff.prolong(enemy, Slow.class,       8f); break;
                case 4: Buff.affect(enemy, Poison.class).set( 8 );  break;
                case 5: Buff.affect(enemy, Bleeding.class).set( 8f ); break;
                case 6: Buff.prolong(enemy, Blindness.class,  4f); break;
                default: Buff.prolong(enemy, Vertigo.class,   4f); break;
            }
        }

        debuffCount++;
        yell( Messages.get(this, "plague") );
    }

    /** 阶段 3：简易炮击支援（复用 ArtilleryShell） */
    private void artillerySupport() {
        if (enemy == null) return;

        int cell = enemy.pos;

        ArtilleryShell shell = new ArtilleryShell(
                cell,
                1,                          // 1 回合预警
                1,                          // 3×3
                20, 35,
                this
        );
        Actor.add( shell );

        yell( Messages.get(this, "artillery") );
    }

    /** 阶段 4：狂暴突进 */
    private void berserkStrike() {
        if (enemy == null) return;

        int dist = Dungeon.level.distance( pos, enemy.pos );

        if (dist <= 2) {
            // 近身：三连击
            for (int i = 0; i < 3; i++) {
                enemy.damage( Random.NormalIntRange( 18, 28 ), this );
            }
            Buff.prolong(enemy, Cripple.class, 3f);
        } else {
            // 远距离：冲刺接近
            chargeTowards( enemy.pos );
        }
    }

    /** 向目标冲一格 */
    private void chargeTowards(int target) {
        int best = -1;
        int bestDist = Dungeon.level.distance( pos, target );

        for (int n : PathFinder.NEIGHBOURS8) {
            int c = pos + n;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (Actor.findChar(c) != null) continue;

            int d = Dungeon.level.distance( c, target );
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        if (best != -1) move( best );
    }

    // ═══════════════════════════════════════════════
    //  大招
    // ═══════════════════════════════════════════════
    private void castUltimate() {
        switch (phase) {
            case 1:  ultiElementStorm();   break;
            case 2:  ultiMassPlague();     break;
            case 3:  ultiCarpetBombing();  break;
            default: ultiOblivion();       break;
        }
    }

    /** 阶段 1 大招：元素风暴 —— 以自身为中心的 5×5 */
    private void ultiElementStorm() {
        yell( Messages.get(this, "ulti1") );

        ArtilleryShell shell = new ArtilleryShell( pos, 1, 2, 30, 45, this );
        shell.centerMult = 1f;      // 自己脚下不需要翻倍
        Actor.add( shell );
    }

    /** 阶段 2 大招：大疫 —— 给玩家上 5 种负面 */
    private void ultiMassPlague() {
        if (enemy == null) return;
        yell( Messages.get(this, "ulti2") );

        Buff.prolong(enemy, Weakness.class,   12f);
        Buff.prolong(enemy, Vulnerable.class, 12f);
        Buff.prolong(enemy, Cripple.class,    12f);
        Buff.prolong(enemy, Slow.class,       12f);
        Buff.affect(enemy, Poison.class).set( 12 );
        Buff.affect(enemy, Bleeding.class).set( 12f );
    }

    /** 阶段 3 大招：地毯式轰炸 —— 随机 8 个位置 */
    private void ultiCarpetBombing() {
        yell( Messages.get(this, "ulti3") );

        int w = Dungeon.level.width();
        int h = Dungeon.level.height();
        int placed = 0;

        for (int guard = 0; guard < 80 && placed < 8; guard++) {
            int x = Random.IntRange(1, w - 2);
            int y = Random.IntRange(1, h - 2);
            int c = y * w + x;

            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;

            ArtilleryShell bomb = new ArtilleryShell( c, Random.IntRange(2, 4), 1, 25, 40, this );
            Actor.add( bomb );
            placed++;
        }
    }

    /** 阶段 4 大招：湮灭 —— 全场 7×7 + 自身短暂无敌 */
    private void ultiOblivion() {
        yell( Messages.get(this, "ulti4") );
        sprite.flash();

        if (enemy != null) {
            ArtilleryShell shell = new ArtilleryShell( enemy.pos, 2, 3, 50, 80, this );
            shell.centerMult = 1.5f;
            Actor.add( shell );
        }

        Buff.affect( this, Invulnerability.class );
    }

    /** 终章攻击（血量 <5% 时） */
    private void finaleStrike() {
        if (enemy == null) return;

        // 伤害翻倍（baseMin/baseMax 是 float，先转 int）
        int dmg = Random.NormalIntRange( (int)baseMin, (int)baseMax ) * 2;
        enemy.damage( dmg, this );

        if (Dungeon.level.heroFOV[enemy.pos]) {
            CellEmitter.get(enemy.pos).burst( SparkParticle.FACTORY, 8 );
        }
    }

    // ═══════════════════════════════════════════════
    //  受伤
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        updatePhase();

        // 阶段 4：常驻 20% 减伤
        if (phase >= 4) {
            damage = Math.round( damage * 0.8f );
        }
        return super.defenseProc(enemy, damage);
    }

    public boolean attackHook(Char enemy) {
        // 近战命中附加：阶段 2 起叠印记
        if (phase >= 2) {
            Buff.prolong( enemy, Vulnerable.class, 4f );
        }
        return false;
    }

    // ═══════════════════════════════════════════════
    @Override
    public void die(Object cause) {
        super.die(cause);
        GameScene.bossSlain();
        yell( Messages.get(this, "death") );
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
    //  存档
    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase);
        bundle.put(SKILL_CD, skillCd);
        bundle.put(ULTI_CD, ultiCd);
        bundle.put(RESONANCE, lastResonanceHp);
        bundle.put(FINALE, finaleTurns);
        bundle.put(DEBUFF_CNT, debuffCount);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase           = bundle.getInt(PHASE);
        skillCd         = bundle.getInt(SKILL_CD);
        ultiCd          = bundle.getInt(ULTI_CD);
        lastResonanceHp = bundle.getInt(RESONANCE);
        finaleTurns     = bundle.getInt(FINALE);
        debuffCount     = bundle.getInt(DEBUFF_CNT);
    }
}
