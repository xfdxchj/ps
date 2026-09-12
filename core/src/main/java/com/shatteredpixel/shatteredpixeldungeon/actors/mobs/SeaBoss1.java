//END(port from Arknights): SeaBoss1
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SeaObject;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.First_talkSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HandclapSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SeaBoss1
extends Mob {
    boolean SkillActive;
    int phase;
    int skillCD;
    int skillAttackPoint1;
    int skillProcVaule;
    private static final String PHASE = "phase";
    private static final String SKILL_ACTIVE = "skillActive";
    private static final String SKILL_CD = "skillCD";
    private static final String SKILL_POINT = "skillAttackPoint1";
    private static final String SKILL_PROC = "skillProcVaule";

    public SeaBoss1() {
        this.spriteClass = First_talkSprite.class;
        this.HT = 1000;
        this.HP = 1000;
        this.EXP = 40;
        this.defenseSkill = 25;
        this.state = this.HUNTING;
        this.properties.add(Char.Property.SEA);
        this.properties.add(Char.Property.BOSS);
        this.SkillActive = false;
        this.phase = 0;
        this.skillCD = 7;
        this.skillAttackPoint1 = 0;
        this.skillProcVaule = 0;
    }

    @Override
    public int damageRoll() {
        if (this.HP <= 500) {
            return Random.NormalIntRange(45, 60);
        }
        return Random.NormalIntRange(30, 50);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (enemy.alignment == Char.Alignment.ALLY) {
            Buff.affect(enemy, NervousImpairment.class).sum(10.0f);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (this.SkillActive) {
            dmg /= 3;
        }
        super.damage(dmg, src);
        if (this.phase == 1 && this.HP <= 600) {
            this.HP = 600;
            this.phase = 2;
            GameScene.flash(-2130771968);
            Buff.affect(Dungeon.hero, Silence.class, 15.0f);
        }
    }

    @Override
    protected boolean act() {
        if (this.skillCD > 0) {
            --this.skillCD;
        } else {
            this.Skill();
            this.SkillActive = true;
            this.spend(1.0f);
            return true;
        }
        if (this.phase == 2) {
            int healpoint = 4;
            if (Dungeon.isChallenged(512)) {
                healpoint = 8;
            }
            this.HP = Math.min(this.HT, this.HP + healpoint);
            this.sprite.emitter().burst(Speck.factory(0), 1);
        }
        return super.act();
    }

    boolean Skill() {
        switch (this.skillProcVaule) {
            case 0: {
                this.skillAttackPoint1 = Dungeon.hero.pos;
                this.sprite.parent.addToBack(new TargetedCell(this.skillAttackPoint1, 0xFF0000));
                Camera.main.shake(2.0f, 0.5f);
                break;
            }
            case 1: {
                this.skillAttackPoint1 = Dungeon.hero.pos;
                this.sprite.parent.addToBack(new TargetedCell(this.skillAttackPoint1, 0xFF0000));
                break;
            }
            case 2: {
                this.skillAttackPoint1 = Dungeon.hero.pos;
                this.sprite.parent.addToBack(new TargetedCell(this.skillAttackPoint1, 0xFF0000));
                break;
            }
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: {
                this.SkillAttack(this.skillAttackPoint1);
            }
        }
        ++this.skillProcVaule;
        if (this.skillProcVaule == 10) {
            this.skillProcVaule = 0;
            this.skillCD = 8;
            this.SkillActive = false;
        }
        return true;
    }

    void SkillAttack(int point) {
        PathFinder.buildDistanceMap(point, BArray.not(Dungeon.level.solid, null), 1);
        for (int cell = 0; cell < PathFinder.distance.length; ++cell) {
            if (PathFinder.distance[cell] >= Integer.MAX_VALUE) continue;
            CellEmitter.center(cell).burst(HandclapSprite.GooParticle.FACTORY, 10);
            Char ch = Actor.findChar(cell);
            if (ch == null) continue;
            int damage = Random.IntRange(13, 19);
            if (ch != Dungeon.hero) {
                damage /= 4;
            }
            if (ch == this) {
                damage = 0;
            }
            if (ch instanceof SeaObject) {
                damage = Random.IntRange(95, 110);
            }
            ch.damage(damage, SeaBoss_SkillAttack.class);
            if (ch.alignment == Char.Alignment.ALLY) {
                Buff.affect(ch, NervousImpairment.class).sum(20.0f);
            }
            if (ch.isAlive() || ch != Dungeon.hero) continue;
            Dungeon.fail(this.getClass());
            GLog.n(Messages.get(this, "destroy"));
        }
    }

    @Override
    public void die(Object cause) {
        Badges.validateiberia1();
        GameScene.bossSlain();
        Dungeon.level.unseal();
        super.die(cause);
    }

    @Override
    public void notice() {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            if (this.phase == 0) {
                this.phase = 1;
            }
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.phase);
        bundle.put(SKILL_ACTIVE, this.SkillActive);
        bundle.put(SKILL_CD, this.skillCD);
        bundle.put(SKILL_POINT, this.skillAttackPoint1);
        bundle.put(SKILL_PROC, this.skillProcVaule);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.SkillActive = bundle.getBoolean(SKILL_ACTIVE);
        if (bundle.contains(SKILL_CD)) {
            this.skillCD = bundle.getInt(SKILL_CD);
        }
        this.skillAttackPoint1 = bundle.getInt(SKILL_POINT);
        this.skillProcVaule = bundle.getInt(SKILL_PROC);
        BossHealthBar.assignBoss(this);
    }

    public static class SeaBoss_SkillAttack {
    }
}
