package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.DebuffKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 3号 · Debuff 王（区域污染）
 *
 * 核心机制：
 *   · 攻击附加随机 Debuff（7 种）
 *   · 每次生成 2 个【3×3 区域】—— 和药水碎裂的效果一样，是真正的 Blob 气体
 *   · 区域持续 10 回合；冷却 10 / 5 / 2（按阶段）
 *   · 每 5 次命中拆掉玩家的净化/护盾/无敌
 *   · 三阶段：玩家身上 10 种 Debuff → 定命
 *
 * END(用户反馈·已修): "debuff 的也没有（特效），随机区域是 3*3 的，
 * 类似药水的效果"。现在的区域完全照抄药水（PotionOfToxicGas.shatter）。
 */
public class DebuffKing extends Boss {

    {
        spriteClass = DebuffKingSprite.class;

        HT  = 1000;
        HP  = HT;           //END(修复·关键): 原来只设 HT 没设 HP，HP 默认 0
        EXP = 100;
        baseHT = HT;

        baseMin = 12;
        baseMax = 20;
        baseAcc = 30;
        baseEva = 14;
        baseMinDef = 5;
        baseMaxDef = 10;

        viewDistance = 31;  //END(修复): 视野

        properties.add( Property.BOSS );
        alignment = Alignment.ENEMY;

        loot = Generator.Category.WEP_T5;
        lootChance = 1f;

        areaCd = 10;
    }

    private int areaCd = 10;
    private int phase = 1;
    private int damageCount = 0;

    private final ArrayList<Area> areas = new ArrayList<>();

    private static final String AREA_CD      = "areaCd";
    private static final String PHASE        = "phase";
    private static final String DAMAGE_COUNT = "damageCount";

    @Override
    protected boolean act() {

        //END(修复): Boss 血条
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

    private void removeProtection(Char target) {
        Buff.detach(target, BlobImmunity.class);
        Buff.detach(target, Barrier.class);
        Buff.detach(target, Invulnerability.class);
        if (Dungeon.level != null && Dungeon.level.heroFOV[target.pos]) {
            target.sprite.flash();
        }
    }

    /**
     * END(用户反馈): "随机区域是 3*3 的，类似药水的效果"。
     * 每个区域 3x3（radius=1），用真正的 Blob 气体，和药水碎裂完全一样。
     */
    private void createTwoAreas() {
        for (int i = 0; i < 2; i++) {
            int cell = findAreaCell();
            if (cell == -1) continue;

            DebuffAreas.Kind kind;
            int r = Random.Int( phase == 1 ? 4 : (phase == 2 ? 7 : 9) );
            switch (r) {
                case 0:  kind = DebuffAreas.Kind.TOXIC;     break;
                case 1:  kind = DebuffAreas.Kind.FIRE;      break;
                case 2:  kind = DebuffAreas.Kind.CORROSIVE; break;
                case 3:  kind = DebuffAreas.Kind.PARALYTIC; break;
                case 4:  kind = DebuffAreas.Kind.CONFUSION; break;
                case 5:  kind = DebuffAreas.Kind.ACID;      break;
                case 6:  kind = DebuffAreas.Kind.BLEED;     break;
                case 7:  kind = DebuffAreas.Kind.SUMMON;    break;
                default: kind = DebuffAreas.Kind.DOOM;      break;
            }

            //★ 3x3 区域
            DebuffAreas.spawn( cell, kind, 1, this );
            areas.add( new Area( cell, kind, 10 ) );
        }
    }

    private int findAreaCell() {
        Char hero = Dungeon.hero;
        if (hero == null || !hero.isAlive()) return -1;

        ArrayList<Integer> candidates = new ArrayList<>();

        for (int n : PathFinder.NEIGHBOURS8) {
            int base = hero.pos + n;
            if (!Dungeon.level.insideMap(base)) continue;
            if (!Dungeon.level.passable[base]) continue;
            candidates.add( base );

            for (int n2 : PathFinder.NEIGHBOURS8) {
                int c2 = base + n2;
                if (!Dungeon.level.insideMap(c2)) continue;
                if (!Dungeon.level.passable[c2]) continue;
                candidates.add( c2 );
            }
        }
        if (candidates.isEmpty()) return -1;
        return candidates.get( Random.Int(candidates.size()) );
    }

    /** 每回合检查"触发式"区域（酸蚀/流血/召唤/定命）。气体类由 Blob 自己处理。 */
    private void updateAreas() {
        if (Dungeon.hero == null) return;

        for (int i = areas.size() - 1; i >= 0; i--) {
            Area area = areas.get(i);

            Char c = Actor.findChar( area.cell );
            if (c != null && c == Dungeon.hero) {
                DebuffAreas.applyTrigger( c, area.kind, this );
            }

            area.duration--;
            if (area.duration <= 0) {
                areas.remove(i);
            }
        }
    }

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
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class) != null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots.class)    != null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo.class)  != null) count++;
        if (target.buff(Daze.class)        != null) count++;
        if (target.buff(Hex.class)         != null) count++;
        if (target.buff(Blindness.class)   != null) count++;
        if (target.buff(Chill.class)       != null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison.class)   != null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class) != null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion.class)!= null) count++;
        if (target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze.class)     != null) count++;
        return count;
    }

    @Override
    public void die(Object cause) {
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

    /** 区域数据（触发式效果用） */
    private static class Area {
        int cell;
        DebuffAreas.Kind kind;
        int duration;
        Area(int cell, DebuffAreas.Kind kind, int duration) {
            this.cell = cell;
            this.kind = kind;
            this.duration = duration;
        }
    }
}
