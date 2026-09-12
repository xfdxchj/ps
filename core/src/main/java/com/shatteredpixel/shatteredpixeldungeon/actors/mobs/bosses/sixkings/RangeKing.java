package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.RangeKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  4号 · 远程王
 * ═══════════════════════════════════════════════════════════════
 *
 * 定位：远程型。不以近战作为主要输出。
 *
 * 一阶段
 *   ① 普通远程攻击   30–45
 *   ② 穿透弹  CD 5   可打障碍物后的玩家（无视阻挡）
 *   ③ 炮击    CD 8   选玩家所在位置 → 预警 1 回合 → 爆炸（范围伤害）
 *
 * 二阶段
 *   ① 普通攻击强化：连续两发
 *   ② 随机炸弹 CD 8  随机 6 个位置生成炸弹/预警，一定时间后爆炸
 *                    不追踪玩家，只制造危险区域（限制走位）
 *
 * 三阶段
 *   ① 火力强化：所有伤害 +25%
 *   ② 炮击强化：CD 8 → 4，每次同时选 2 个区域
 *   ③ 毁灭炮 CD 10   明显预警 → 5×5 爆炸 → 80–120 伤害
 *                    中心格伤害翻倍
 *
 * 实现说明：
 *   预警/爆炸由 {@link ArtilleryShell}（独立 Actor）负责，
 *   它会真的把地面标成陷阱格并显示粒子，之后还原。
 */
public class RangeKing extends Boss {

    {
        spriteClass = RangeKingSprite.class;

        HT  = 1200;
        EXP = 110;
        baseHT = HT;
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 30;
        baseMax = 45;
        baseAcc = 40;
        baseEva = 20;
        baseMinDef = 4;
        baseMaxDef = 10;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.MIS_T5;
        lootChance = 1f;

        // 初始冷却
        pierceCd  = 5;
        barrageCd = 8;
        bombCd    = 8;
        doomCd    = 10;
    }

    private int phase = 1;

    private int pierceCd  = 5;    // 穿透弹
    private int barrageCd = 8;    // 炮击
    private int bombCd    = 8;    // 随机炸弹（二阶段）
    private int doomCd    = 10;   // 毁灭炮（三阶段）

    /** 理想射程：保持距离 */
    private static final int IDEAL_RANGE = 5;
    /** 太近就后撤 */
    private static final int MIN_RANGE   = 3;

    private static final String PHASE      = "phase";
    private static final String PIERCE_CD  = "pierceCd";
    private static final String BARRAGE_CD = "barrageCd";
    private static final String BOMB_CD    = "bombCd";
    private static final String DOOM_CD    = "doomCd";

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

        if (enemy == null || !enemySeen) {
            return super.act();
        }

        int dist = Dungeon.level.distance( pos, enemy.pos );

        // ── 三阶段：毁灭炮优先（CD 最长）──
        if (phase >= 3 && doomCd-- <= 0) {
            doomCd = 10;
            fireDoomCannon();
            spend( TICK );
            return true;
        }

        // ── 炮击（三阶段 CD 4，且每次 2 个区域）──
        if (barrageCd-- <= 0) {
            barrageCd = (phase >= 3) ? 4 : 8;
            fireBarrage( phase >= 3 ? 2 : 1 );
            spend( TICK );
            return true;
        }

        // ── 穿透弹 ──
        if (pierceCd-- <= 0) {
            pierceCd = 5;
            firePiercingShot();
            spend( TICK );
            return true;
        }

        // ── 随机炸弹（二阶段起）──
        if (phase >= 2 && bombCd-- <= 0) {
            bombCd = 8;
            dropRandomBombs();
            spend( TICK );
            return true;
        }

        // ── 普通远程攻击 ──
        if (dist >= IDEAL_RANGE) {
            shoot( phase >= 2 ? 2 : 1 );       // 二阶段连发两发
            spend( TICK );
            return true;
        }

        // ── 太近：后撤 ──
        if (dist <= MIN_RANGE && retreat()) {
            spend( TICK );
            return true;
        }

        // ── 退无可退：硬打 ──
        shoot( phase >= 2 ? 2 : 1 );
        spend( TICK );
        return true;
    }

    private void updatePhase() {
        int newPhase;
        if (HP > HT * 2 / 3)   newPhase = 1;
        else if (HP > HT / 3)  newPhase = 2;
        else                   newPhase = 3;

        if (newPhase != phase) {
            phase = newPhase;
            sprite.flash();
            if (phase == 2) yell( Messages.get(this, "phase2") );
            if (phase == 3) yell( Messages.get(this, "phase3") );
        }
    }

    /** 三阶段火力强化 */
    private int boost(int dmg) {
        return phase >= 3 ? Math.round(dmg * 1.25f) : dmg;
    }

    // ═══════════════════════════════════════════════
    //  ① 普通远程攻击
    // ═══════════════════════════════════════════════
    private void shoot(int shots) {
        if (enemy == null) return;

        for (int i = 0; i < shots; i++) {
            int dmg = boost( Random.NormalIntRange(30, 45) );
            enemy.damage( dmg, this );

            if (Dungeon.level.heroFOV[enemy.pos]) {
                CellEmitter.get(enemy.pos).burst( SparkParticle.FACTORY, 6 );
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  ② 穿透弹（无视障碍）
    // ═══════════════════════════════════════════════
    private void firePiercingShot() {
        if (enemy == null) return;

        //END(穿透): 用 MAGIC_BOLT（只被角色阻挡，不被墙阻挡）实现穿透
        Ballistica bolt = new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT );

        Char hit = Actor.findChar( bolt.collisionPos );
        if (hit != null && hit != this) {
            hit.damage( boost( Random.NormalIntRange(35, 50) ), this );
        }

        // 视觉：沿路径打一条
        for (int c : bolt.path) {
            if (Dungeon.level.heroFOV[c]) {
                CellEmitter.get(c).burst( SparkParticle.FACTORY, 3 );
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  ③ 炮击：选玩家位置 → 预警 → 爆炸
    // ═══════════════════════════════════════════════
    private void fireBarrage(int targets) {
        for (int i = 0; i < targets; i++) {
            int cell;

            if (i == 0 && enemy != null) {
                // 第一个炮击锁定玩家当前位置
                cell = enemy.pos;
            } else {
                // 后续炮击选玩家附近
                cell = nearHeroCell();
                if (cell == -1) continue;
            }

            // 预警 1 回合后爆炸，范围 2（5×5 的近似 = 半径2）
            ArtilleryShell shell = new ArtilleryShell(
                    cell,
                    1,                                 // 1 回合预警
                    2,                                 // 半径 2
                    boost(25), boost(40),              // 伤害
                    this
            );
            Actor.add( shell );   //END: 非 Mob 的 Actor 用 Actor.add
        }
    }

    // ═══════════════════════════════════════════════
    //  毁灭炮（三阶段）：5×5，80–120，中心翻倍
    // ═══════════════════════════════════════════════
    private void fireDoomCannon() {
        if (enemy == null) return;

        ArtilleryShell shell = new ArtilleryShell(
                enemy.pos,
                2,                                     // 2 回合明显预警
                2,                                     // 半径 2 → 5×5
                boost(80), boost(120),                 // 80–120
                this
        );
        shell.centerMult = 2f;                         // 中心翻倍
        Actor.add( shell );   //END: 非 Mob 的 Actor 用 Actor.add

        yell( Messages.get(this, "doom") );
    }

    // ═══════════════════════════════════════════════
    //  随机炸弹（二阶段）：6 个随机位置
    // ═══════════════════════════════════════════════
    private void dropRandomBombs() {
        int placed = 0;
        int guard = 0;

        while (placed < 6 && guard++ < 60) {
            int cell = randomFieldCell();
            if (cell == -1) break;

            ArtilleryShell bomb = new ArtilleryShell(
                    cell,
                    Random.IntRange(2, 3),                 // 2-3 回合后爆炸
                    1,                                     // 半径 1（3×3）
                    boost(20), boost(35),
                    this
            );
            Actor.add( bomb );    //END: 非 Mob 的 Actor 用 Actor.add
            placed++;
        }

        yell( Messages.get(this, "bombs") );
    }

    // ═══════════════════════════════════════════════
    //  位置工具
    // ═══════════════════════════════════════════════

    /** 玩家附近的一个可站立格 */
    private int nearHeroCell() {
        if (enemy == null) return -1;
        java.util.ArrayList<Integer> cand = new java.util.ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS8) {
            int c = enemy.pos + n;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            cand.add( c );
        }
        if (cand.isEmpty()) return -1;
        return cand.get( Random.Int( cand.size() ) );
    }

    /** 场地内任意可站立格 */
    private int randomFieldCell() {
        int w = Dungeon.level.width();
        int h = Dungeon.level.height();
        for (int tries = 0; tries < 30; tries++) {
            int x = Random.IntRange(1, w - 2);
            int y = Random.IntRange(1, h - 2);
            int c = y * w + x;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (c == pos) continue;
            return c;
        }
        return -1;
    }

    /** 后撤：找离玩家更远的格 */
    private boolean retreat() {
        if (enemy == null) return false;

        int best = -1;
        int bestDist = Dungeon.level.distance( pos, enemy.pos );

        for (int n : PathFinder.NEIGHBOURS8) {
            int c = pos + n;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (Actor.findChar(c) != null) continue;

            int d = Dungeon.level.distance( c, enemy.pos );
            if (d > bestDist) {
                bestDist = d;
                best = c;
            }
        }

        if (best != -1) {
            move( best );
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════
    //  近战很弱（定位是远程）
    // ═══════════════════════════════════════════════
    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 8, 14 );
    }

    public boolean attackHook(Char enemy) {
        // 近战命中时给个小 debuff，但不是主要输出
        Buff.prolong( enemy, Vulnerable.class, 3f );
        return false;
    }

    @Override
    public void die(Object cause) {
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
        bundle.put(PHASE, phase);
        bundle.put(PIERCE_CD, pierceCd);
        bundle.put(BARRAGE_CD, barrageCd);
        bundle.put(BOMB_CD, bombCd);
        bundle.put(DOOM_CD, doomCd);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase     = bundle.getInt(PHASE);
        pierceCd  = bundle.getInt(PIERCE_CD);
        barrageCd = bundle.getInt(BARRAGE_CD);
        bombCd    = bundle.getInt(BOMB_CD);
        doomCd    = bundle.getInt(DOOM_CD);
    }
}
