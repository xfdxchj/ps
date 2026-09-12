package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HalomethaneBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.DebuffKingSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  3号 · Debuff 王（区域污染）
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · 攻击附加随机 Debuff（7 种）
 *   · 【真地形区域】：每次召唤 2 个区域，把格子改成 Terrain.TRAP
 *     并在地面生成对应的 Blob（毒气/火焰/腐蚀/麻痹/混乱）
 *   · 区域持续 10 回合；冷却 10 / 5 / 2（按阶段）
 *   · 每 5 次命中拆掉玩家的净化/护盾
 *   · 三阶段：玩家身上 10 种 Debuff → 定命
 *
 * 与草稿版的差异（关键）：
 *   【真地形】区域落地时：
 *     1) Level.set(cell, Terrain.TRAP)  + GameScene.updateMap(cell)  —— 视觉上变成陷阱格
 *     2) Blob.seed(cell, amount, 类)                                  —— 地面实际产生毒气/火焰
 *     3) 玩家踩上去由 Blob 自动施加效果（不用手写 applyArea）
 *   这样"看得见、踩得到、会掉血"，而不是草稿版那种纯逻辑区域。
 */
public class DebuffKing extends Boss {

    {
        spriteClass = DebuffKingSprite.class;

        HT  = 1000;
        EXP = 100;
        baseHT = HT;
        HP = HT;   //END(修复·关键): 原来只设了 HT 没设 HP，HP 默认 0 → 阶段判定/死亡判定立刻成立

        baseMin = 12;
        baseMax = 20;
        baseAcc = 30;
        baseEva = 14;
        baseMinDef = 5;
        baseMaxDef = 10;

        //END(修复): 默认视野太小 → "离开一格就看不见"

        viewDistance = 31;

        

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.WEP_T5;
        lootChance = 1f;

        areaCd = 10;
    }

    private int areaCd = 10;
    private int phase = 1;
    /** 玩家累计被直接命中次数，每 5 次拆保护 */
    private int damageCount = 0;

    /** 当前存在的区域 */
    private final ArrayList<Area> areas = new ArrayList<>();

    private static final String AREA_CD      = "areaCd";
    private static final String PHASE        = "phase";
    private static final String DAMAGE_COUNT = "damageCount";

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        //END(修复): 没有 assignBoss → Boss 血条不显示。"法术王没有血条"
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss( this );
        }

        updatePhase();
        updateAreas();

        if (areaCd-- <= 0) {
            createTwoAreas();

            if (phase == 1)      areaCd = 10;
            else if (phase == 2) areaCd = 5;
            else                 areaCd = 2;

            spend( TICK );
            return true;
        }

        return super.act();
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

    // ═══════════════════════════════════════════════
    //  攻击附加随机 Debuff
    // ═══════════════════════════════════════════════
    public boolean attackHook(Char enemy) {
        if (enemy == null) return false;

        applyRandomDebuff( enemy );

        damageCount++;
        if (damageCount >= 5) {
            damageCount = 0;
            removeProtection( enemy );
        }
        return false;
    }

    private void applyRandomDebuff(Char target) {
        switch (Random.Int(7)) {
            case 0: Buff.prolong(target, Weakness.class, 15f);   break;
            case 1: Buff.prolong(target, Vulnerable.class, 15f); break;
            case 2: Buff.prolong(target, Cripple.class, 15f);    break;
            case 3: Buff.prolong(target, Slow.class, 15f);       break;
            case 4: Buff.prolong(target, Hex.class, 15f);        break;
            case 5: Buff.prolong(target, Daze.class, 15f);       break;
            default: Buff.prolong(target, Chill.class, 15f);     break;
        }
    }

    /** 每 5 次命中：拆掉玩家的净化/护盾/无敌 */
    private void removeProtection(Char target) {
        Buff.detach(target, BlobImmunity.class);
        Buff.detach(target, Barrier.class);
        Buff.detach(target, Invulnerability.class);
        if (Dungeon.level.heroFOV[target.pos]) {
            target.sprite.flash();
        }
    }

    // ═══════════════════════════════════════════════
    //  ★ 真地形区域系统 ★
    // ═══════════════════════════════════════════════

    /** 区域类型 */
    public enum AreaType {
        TOXIC_GAS,      // 毒气
        FIRE,           // 火焰
        CORROSIVE,      // 腐蚀气体
        PARALYTIC,      // 麻痹气体
        CONFUSION,      // 混乱气体
        ACID_TRAP,      // 酸蚀陷阱
        BLEED_TRAP,     // 流血陷阱
        SUMMON_TRAP,    // 召唤陷阱（踩到召小怪）
        DOOM_TRAP       // 定命陷阱（三阶段专用）
    }

    /** 每次生成两个区域 */
    private void createTwoAreas() {
        for (int i = 0; i < 2; i++) {
            int cell = findAreaCell();
            if (cell == -1) continue;

            AreaType type;
            int r = Random.Int( phase == 1 ? 4 : (phase == 2 ? 7 : 9) );
            switch (r) {
                case 0:  type = AreaType.TOXIC_GAS;   break;
                case 1:  type = AreaType.FIRE;        break;
                case 2:  type = AreaType.CORROSIVE;   break;
                case 3:  type = AreaType.PARALYTIC;   break;
                case 4:  type = AreaType.CONFUSION;   break;
                case 5:  type = AreaType.ACID_TRAP;   break;
                case 6:  type = AreaType.BLEED_TRAP;  break;
                case 7:  type = AreaType.SUMMON_TRAP; break;
                default: type = AreaType.DOOM_TRAP;   break;
            }

            spawnArea( cell, type );
        }
    }

    /** 找一个玩家附近、可站立的格子（比 randomRespawnCell 更可控） */
    private int findAreaCell() {
        Char hero = Dungeon.hero;
        if (hero == null || !hero.isAlive()) return -1;

        // 优先选玩家周围 2-5 格内的空地
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS8) {
            int base = hero.pos + n;
            if (base < 0 || base >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[base]) continue;
            if (Dungeon.level.map[base] == Terrain.TRAP) continue;   // 已经有区域了
            candidates.add( base );

            // 再往外扩一层
            for (int n2 : PathFinder.NEIGHBOURS8) {
                int c2 = base + n2;
                if (c2 < 0 || c2 >= Dungeon.level.length()) continue;
                if (!Dungeon.level.passable[c2]) continue;
                if (Dungeon.level.map[c2] == Terrain.TRAP) continue;
                candidates.add( c2 );
            }
        }
        if (candidates.isEmpty()) return -1;
        return candidates.get( Random.Int(candidates.size()) );
    }

    /** ★ 在指定格生成区域：改地形 + 种 Blob */
    private void spawnArea(int cell, AreaType type) {

        // ── 1) 把格子变成陷阱地形（玩家能看见）──
        if (type == AreaType.TOXIC_GAS || type == AreaType.FIRE
                || type == AreaType.CORROSIVE || type == AreaType.PARALYTIC
                || type == AreaType.CONFUSION) {
            // 气体类：不改地形（保持可走），只种 Blob
        } else {
            // 陷阱类：改地形
            Level_setTrap( cell );
        }

        // ── 2) 种 Blob / 施加效果 ──
        switch (type) {
            case TOXIC_GAS:
                Blob.seed(cell, 40, ToxicGas.class);
                break;
            case FIRE:
                Blob.seed(cell, 6, Fire.class);
                break;
            case CORROSIVE:
                Blob.seed(cell, 40, CorrosiveGas.class);
                break;
            case PARALYTIC:
                Blob.seed(cell, 40, ParalyticGas.class);
                break;
            case CONFUSION:
                Blob.seed(cell, 40, ConfusionGas.class);
                break;
            case ACID_TRAP:
                Blob.seed(cell, 25, CorrosiveGas.class);
                break;
            case BLEED_TRAP:
                // 陷阱格：踩到掉血（这里做成"站立即受伤"）
                break;
            case SUMMON_TRAP:
                // 踩到会召小怪（在 updateAreas 里处理）
                break;
            case DOOM_TRAP:
                // 三阶段：踩到上定命
                break;
        }

        areas.add( new Area(cell, type, 10) );

        // 视觉提示
        GameScene.updateMap( cell );
        if (Dungeon.level.heroFOV[cell]) {
            com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(cell)
                    .burst( com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle.UP, 8 );
        }
    }

    /** 把格子设为陷阱地形 */
    private void Level_setTrap(int cell) {
        //END(真地形): 用本 fork 的 Level.set 改地形，并刷新地图
        com.shatteredpixel.shatteredpixeldungeon.levels.Level.set( cell, Terrain.TRAP );
        GameScene.updateMap( cell );
    }

    /** 每回合检查区域：对站在上面的玩家施加效果；到期的还原地形 */
    private void updateAreas() {
        for (int i = areas.size() - 1; i >= 0; i--) {
            Area area = areas.get(i);

            // 只对玩家生效
            Char c = Actor.findChar( area.cell );
            if (c != null && c == Dungeon.hero) {
                applyAreaEffect( c, area.type );
            }

            area.duration--;
            if (area.duration <= 0) {
                // 还原地形
                if (area.type != AreaType.TOXIC_GAS && area.type != AreaType.FIRE
                        && area.type != AreaType.CORROSIVE && area.type != AreaType.PARALYTIC
                        && area.type != AreaType.CONFUSION) {
                    com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(
                            area.cell, Terrain.EMPTY );
                    GameScene.updateMap( area.cell );
                }
                areas.remove(i);
            }
        }
    }

    /** 区域对玩家的效果（气体类由 Blob 自动处理，这里处理陷阱类） */
    private void applyAreaEffect(Char target, AreaType type) {
        switch (type) {
            case ACID_TRAP:
                target.damage( Random.IntRange(3, 6), this );
                Buff.affect(target, Corrosion.class).set( 5f, 3 );
                break;
            case BLEED_TRAP:
                target.damage( Random.IntRange(4, 8), this );
                Buff.affect(target, Bleeding.class).set( 8f );
                break;
            case SUMMON_TRAP:
                if (Random.Int(3) == 0) summonMinion( target.pos );
                break;
            case DOOM_TRAP:
                if (phase == 3) Buff.affect( target, Doom.class );
                break;
            default:
                break;   // 气体类交给 Blob
        }
    }

    /** 召唤一个小怪 */
    private void summonMinion(int near) {
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m =
                new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        int cell = -1;
        for (int n : PathFinder.NEIGHBOURS8) {
            int c = near + n;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (Actor.findChar(c) != null) continue;
            cell = c;
            break;
        }
        if (cell == -1) return;
        m.pos = cell;
        GameScene.add( m );
        m.state = m.HUNTING;
    }

    // ═══════════════════════════════════════════════
    //  三阶段：10 种 Debuff → 定命
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        if (phase == 3 && enemy == Dungeon.hero && countDebuffs(enemy) >= 10) {
            Buff.affect( enemy, Doom.class );
        }
        return super.defenseProc(enemy, damage);
    }

    private int countDebuffs(Char target) {
        int count = 0;
        if (target.buff(Weakness.class)    != null) count++;
        if (target.buff(Vulnerable.class)  != null) count++;
        if (target.buff(Cripple.class)     != null) count++;
        if (target.buff(Slow.class)        != null) count++;
        if (target.buff(Paralysis.class)   != null) count++;
        if (target.buff(Roots.class)       != null) count++;
        if (target.buff(Vertigo.class)     != null) count++;
        if (target.buff(Daze.class)        != null) count++;
        if (target.buff(Hex.class)         != null) count++;
        if (target.buff(Blindness.class)   != null) count++;
        if (target.buff(Chill.class)       != null) count++;
        if (target.buff(Poison.class)      != null) count++;
        if (target.buff(Bleeding.class)    != null) count++;
        if (target.buff(Corrosion.class)   != null) count++;
        if (target.buff(Ooze.class)        != null) count++;
        if (target.buff(HalomethaneBurning.class) != null) count++;
        return count;
    }

    @Override
    public void die(Object cause) {
        // 清理所有区域地形
        for (Area a : areas) {
            if (a.type != AreaType.TOXIC_GAS && a.type != AreaType.FIRE
                    && a.type != AreaType.CORROSIVE && a.type != AreaType.PARALYTIC
                    && a.type != AreaType.CONFUSION) {
                com.shatteredpixel.shatteredpixeldungeon.levels.Level.set( a.cell, Terrain.EMPTY );
                GameScene.updateMap( a.cell );
            }
        }
        areas.clear();

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
        bundle.put(AREA_CD, areaCd);
        bundle.put(PHASE, phase);
        bundle.put(DAMAGE_COUNT, damageCount);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        areaCd      = bundle.getInt(AREA_CD);
        phase       = bundle.getInt(PHASE);
        damageCount = bundle.getInt(DAMAGE_COUNT);
    }

    /** 区域数据 */
    private static class Area {
        int cell;
        AreaType type;
        int duration;
        Area(int cell, AreaType type, int duration) {
            this.cell = cell;
            this.type = type;
            this.duration = duration;
        }
    }
}
