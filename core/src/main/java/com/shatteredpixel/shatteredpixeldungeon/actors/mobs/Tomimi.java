//END(port from Arknights): Tomimi
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Talu_BlackSnake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhFanatic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhLancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhWarrior;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tomimi_BossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tomimi_towerSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.HashSet;

public class Tomimi
extends Mob {
    private int phase;
    private int enemyspawncooldown;
    private int BurstPos;
    private int BurstTime;
    public static int tomimitower = 0;
    private static final String PHASE = "phase";
    private static final String SKILL2POS = "BurstPos";
    private static final String SKILL2TIME = "BurstTime";
    private static final String SUMMON = "enemyspawncooldown";
    private static final String TOWER = "tomimitower";

    public Tomimi() {
        this.spriteClass = Tomimi_BossSprite.class;
        this.HT = 1500;
        this.HP = 1500;
        this.defenseSkill = 20;
        this.state = this.HUNTING;
        this.EXP = 40;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.INFECTED);
        this.immunities.add(Amok.class);
        this.immunities.add(Terror.class);
        this.immunities.add(Silence.class);
        this.phase = 0;
        this.enemyspawncooldown = 2;
        this.BurstPos = -1;
        this.BurstTime = 0;
    }

    @Override
    public int attackSkill(Char target) {
        return 35;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(36, 48);
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 18);
    }

    @Override
    public void damage(int dmg, Object src) {
        LockedFloor lock;
        if (this.phase == 2) {
            this.sprite.showStatus(0xFFFF00, Messages.get(Talu_BlackSnake.class, "invincibility"));
            if (src == Dungeon.hero) {
                ScrollOfTeleportation.appear(Dungeon.hero, Dungeon.level.entrance());
            }
            return;
        }
        super.damage(dmg, src);
        if (this.phase == 1 && this.HP <= 750) {
            this.HP = 750;
            this.phase = 2;
            GameScene.flash(-2130771968);
            this.yell(Messages.get(this, "phase2"));
            this.boss_tel(52);
            ScrollOfTeleportation.appear(Dungeon.hero, Dungeon.level.entrance());
            this.summonSubject(47, new TomimiTower());
            this.summonSubject(57, new TomimiTower());
            tomimitower = 2;
        }
        if ((lock = Dungeon.hero.buff(LockedFloor.class)) != null) {
            lock.addTime((float)dmg * 0.3f);
        }
    }

    @Override
    public void notice() {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            this.yell(Messages.get(this, "notice"));
            if (this.phase == 0) {
                this.phase = 1;
            }
        }
    }

    @Override
    protected boolean act() {
        if (this.phase == 0) {
            if (Dungeon.hero.viewDistance >= Dungeon.level.distance(this.pos, Dungeon.hero.pos)) {
                Dungeon.observe();
            }
            if (Dungeon.level.heroFOV[this.pos]) {
                this.notice();
            }
        }
        if (this.phase == 0) {
            this.spend(1.0f);
            return true;
        }
        this.enemyspawn();
        if (this.phase == 2) {
            if (tomimitower == 0) {
                this.HP = 350;
                this.phase = 3;
                GameScene.flash(-2130771968);
                this.yell(Messages.get(this, "phase3"));
                this.boss_tel(241);
                for (Mob m : this.getSubjects()) {
                    m.destroy();
                    m.sprite.killAndErase();
                }
            }
            this.RPG_SHOT(Dungeon.hero.pos);
        }
        if (this.enemyspawncooldown >= 1) {
            --this.enemyspawncooldown;
        }
        if (this.phase == 3) {
            this.spend(1.0f);
            return true;
        }
        return super.act();
    }

    protected void boss_tel(int movepos) {
        this.sprite.move(this.pos, movepos);
        this.pos = movepos;
    }

    protected void RPG_SHOT(int targetpos) {
        if (this.BurstPos == -1) {
            this.BurstPos = Dungeon.hero.pos;
            this.sprite.parent.addToBack(new TargetedCell(this.BurstPos, 0xFF0000));
            for (int i : PathFinder.NEIGHBOURS9) {
                int vol = Fire.volumeAt(this.BurstPos + i, Fire.class);
                if (vol >= 4) continue;
                this.sprite.parent.addToBack(new TargetedCell(this.BurstPos + i, 0xFF0000));
            }
            Sample.INSTANCE.play("sounds/zap_gun.mp3");
            ++this.BurstTime;
            return;
        }
        if (this.BurstTime == 1) {
            ++this.BurstTime;
            return;
        }
        if (this.BurstTime == 2) {
            PathFinder.buildDistanceMap(this.BurstPos, BArray.not(Dungeon.level.solid, null), 1);
            for (int cell = 0; cell < PathFinder.distance.length; ++cell) {
                if (PathFinder.distance[cell] >= Integer.MAX_VALUE) continue;
                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                Char ch = Actor.findChar(cell);
                if (ch == null) continue;
                int damage = Random.NormalIntRange(65, 90);
                if (ch != Dungeon.hero) {
                    damage *= 2;
                }
                ch.damage(damage - ch.drRoll(), RPG7.class);
                if (ch.isAlive() || ch != Dungeon.hero) continue;
                Dungeon.fail(this.getClass());
                GLog.n(Messages.get(this, "destroy"));
            }
            Camera.main.shake(2.0f, 0.5f);
            ++this.BurstTime;
            Sample.INSTANCE.play("sounds/burning.mp3");
            return;
        }
        this.BurstPos = -1;
        this.BurstTime = 0;
    }

    protected void enemyspawn() {
        if (this.enemyspawncooldown <= 0) {
            if (Random.Int(7) == 0) {
                this.summonSubject(401, new TomimiLancer());
                this.summonSubject(417, new TomimiFanatic());
            } else {
                this.summonSubject(401, new TomimiWarrior());
                this.summonSubject(417, new TomimiFanatic());
            }
            this.enemyspawncooldown = this.phase == 1 ? 9 : (this.phase == 2 ? 5 : 3);
        }
    }

    private void summonSubject(int pos, Mob enemy) {
        enemy.pos = pos;
        if (Actor.findChar(enemy.pos) != null) {
            int pushPos = pos;
            for (int c : PathFinder.NEIGHBOURS8) {
                if (Actor.findChar(enemy.pos + c) != null || !Dungeon.level.passable[enemy.pos + c] || !Dungeon.level.openSpace[enemy.pos + c] && Tomimi.hasProp(Actor.findChar(enemy.pos), Char.Property.LARGE) || !(Dungeon.level.trueDistance(pos, enemy.pos + c) > Dungeon.level.trueDistance(pos, pushPos))) continue;
                pushPos = enemy.pos + c;
            }
            if (pushPos != pos) {
                Char ch = Actor.findChar(enemy.pos);
                Actor.addDelayed(new Pushing(ch, ch.pos, pushPos), -1.0f);
                ch.pos = pushPos;
                Dungeon.level.occupyCell(ch);
            } else {
                Char blocker = Actor.findChar(enemy.pos);
                if (blocker.alignment != this.alignment) {
                    blocker.damage(Random.NormalIntRange(22, 30), this);
                }
            }
        }
        GameScene.add(enemy);
    }

    private HashSet<Mob> getSubjects() {
        HashSet<Mob> subjects = new HashSet<Mob>();
        for (Mob m : Dungeon.level.mobs) {
            if (m.alignment != this.alignment || !(m instanceof TiacauhWarrior) && !(m instanceof TiacauhLancer) && !(m instanceof TiacauhFanatic)) continue;
            subjects.add(m);
        }
        return subjects;
    }

    @Override
    public void die(Object cause) {
        this.yell(Messages.get(this, "die"));
        Bestiary.skipCountingEncounters = true;
        for (Mob m : this.getSubjects()) {
            m.die(null);
        }
        Bestiary.skipCountingEncounters = false;
        Dungeon.level.drop((Item)new Certificate((int)40), (int)this.pos).sprite.drop(this.pos);
        GameScene.bossSlain();
        Dungeon.level.unseal();
        Badges.validategavial1();
        super.die(cause);
    }

    @Override
    public boolean isAlive() {
        return this.HP > 0 || this.phase < 3;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.phase);
        bundle.put(SKILL2POS, this.BurstPos);
        bundle.put(SKILL2TIME, this.BurstTime);
        bundle.put(SUMMON, this.enemyspawncooldown);
        bundle.put(TOWER, tomimitower);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.BurstPos = bundle.getInt(SKILL2POS);
        this.BurstTime = bundle.getInt(SKILL2TIME);
        this.enemyspawncooldown = bundle.getInt(SUMMON);
        tomimitower = bundle.getInt(TOWER);
        BossHealthBar.assignBoss(this);
    }

    public static class TomimiTower
    extends Mob {
        public TomimiTower() {
            this.spriteClass = Tomimi_towerSprite.class;
            this.HP = 150;
            this.HT = 150;
            this.state = this.PASSIVE;
            this.properties.add(Char.Property.IMMOVABLE);
            this.properties.add(Char.Property.MINIBOSS);
            this.immunities.add(Paralysis.class);
            this.immunities.add(Amok.class);
            this.immunities.add(Sleep.class);
            this.immunities.add(Terror.class);
            this.immunities.add(Vertigo.class);
        }

        @Override
        public void beckon(int cell) {
        }

        @Override
        public void damage(int dmg, Object src) {
            if (src != RPG7.class) {
                this.sprite.showStatus(0xFFFF00, Messages.get(Talu_BlackSnake.class, "invincibility"));
                return;
            }
            dmg = 50;
            super.damage(dmg, src);
        }

        @Override
        public void die(Object cause) {
            --tomimitower;
            super.die(cause);
        }
    }

    public static class RPG7 {
    }

    public static class TomimiLancer
    extends TiacauhLancer {
        public TomimiLancer() {
            this.state = this.HUNTING;
            this.maxLvl = -1;
        }
    }

    public static class TomimiFanatic
    extends TiacauhFanatic {
        public TomimiFanatic() {
            this.state = this.HUNTING;
            this.maxLvl = -1;
        }
    }

    public static class TomimiWarrior
    extends TiacauhWarrior {
        public TomimiWarrior() {
            this.state = this.HUNTING;
            this.maxLvl = -1;
        }
    }
}
