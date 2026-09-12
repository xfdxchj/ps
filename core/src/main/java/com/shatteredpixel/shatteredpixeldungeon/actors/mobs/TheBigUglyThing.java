//END(port from Arknights): TheBigUglyThing
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhBrave;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhRipper;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.items.PortableCover;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Big_UglySprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class TheBigUglyThing
extends Mob {
    private int phase;
    private int beamcooldown;
    private int summoncooldown;
    private int ragecooldown;
    private int firecooldown;
    private int firetime;
    private ArrayList<Integer> targetedCells;
    private static final String PHASE = "phase";
    private static final String BEAM = "beamcooldown";
    private static final String SUMMONCD = "summoncooldown";
    private static final String RAGECD = "ragecooldown";
    private static final String FIRECD = "firecooldown";
    private static final String FIRETIME = "firetime";

    public TheBigUglyThing() {
        this.spriteClass = Big_UglySprite.class;
        this.HP = 1700;
        this.HT = 1700;
        this.defenseSkill = 25;
        this.EXP = 100;
        this.state = this.HUNTING;
        this.properties.add(Char.Property.BOSS);
        this.immunities.add(Poison.class);
        this.immunities.add(Paralysis.class);
        this.immunities.add(Silence.class);
        this.phase = 0;
        this.beamcooldown = 0;
        this.summoncooldown = 4;
        this.ragecooldown = 5;
        this.firecooldown = 1;
        this.firetime = 0;
        this.targetedCells = new ArrayList();
    }

    @Override
    public int damageRoll() {
        if (this.buff(rageBuff.class) != null) {
            Random.NormalIntRange(120, 150);
        }
        return Random.NormalIntRange(50, 65);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 20);
    }

    @Override
    public void damage(int dmg, Object src) {
        LockedFloor lock;
        if (this.buff(rageBuff.class) != null || this.firetime != 0) {
            dmg /= 5;
        }
        if (dmg > 500) {
            int thedamage;
            dmg = thedamage = 500 + dmg / 10;
        }
        super.damage(dmg, src);
        if (this.phase == 1 && this.HP <= 500) {
            this.HP = 600;
            this.phase = 2;
            this.firetime = 0;
            Buff.detach(this, rageBuff.class);
            GameScene.flash(-2130771968);
            Buff.affect(this, Barrier.class).setShield(1000);
        }
        if ((lock = Dungeon.hero.buff(LockedFloor.class)) != null) {
            lock.addTime((float)dmg * 0.3f);
        }
    }

    @Override
    protected boolean act() {
        if (this.phase == 0) {
            this.phase = 1;
            BossHealthBar.assignBoss(this);
        }
        if (this.phase == 3 && this.HP < 1) {
            Dungeon.hero.HP = Dungeon.hero.HT;
            Badges.validateVictory();
            Badges.validateChampion(Challenges.activeChallenges());
            Badges.validateChampion_char(Challenges.activeChallenges());
            Badges.saveGlobal();
            Certificate.specialEndingBouns();
            Badges.silentValidateHappyEnd();
            Badges.validategavial2();
            Dungeon.win(Amulet.class);
            Dungeon.deleteGame(GamesInProgress.curSlot, true);
            Game.switchScene(SurfaceScene.class);
        }
        if (this.buff(Barrier.class) != null) {
            this.HP = Math.min(this.HP + 20, this.HT);
        } else if (this.buff(Barrier.class) == null && this.phase == 2) {
            this.HP = Math.max(this.HP, 800);
            this.phase = 3;
            GameScene.flash(-2130771968);
        }
        if (this.phase == 2) {
            this.FireBlast();
            this.spend(1.0f);
            if (this.beamcooldown >= 1) {
                --this.beamcooldown;
            }
            return true;
        }
        this.UseAbility();
        if (this.phase != 2) {
            if (this.summoncooldown >= 1) {
                --this.summoncooldown;
            }
            if (this.ragecooldown >= 1) {
                --this.ragecooldown;
            }
            if (this.firecooldown >= 1) {
                --this.firecooldown;
            }
        }
        return super.act();
    }

    protected boolean UseAbility() {
        if (this.buff(rageBuff.class) != null) {
            return true;
        }
        if (this.ragecooldown <= 0 && this.firetime == 0) {
            Buff.affect(this, rageBuff.class, 4.0f);
            GameScene.flash(-2130771968);
            GLog.w(Messages.get(this, "rage"));
            this.spend(1.0f);
            this.ragecooldown = 12;
            Sample.INSTANCE.play("sounds/burning.mp3", 1.3f);
            return true;
        }
        if (this.firecooldown <= 0) {
            if (this.firetime == 0) {
                GLog.w(Messages.get(this, "fire_ready"));
                this.sprite.parent.addToBack(new TargetedCell(Dungeon.hero.pos, 0xFF0000));
                ++this.firetime;
                return true;
            }
            Hero Target2 = Dungeon.hero;
            int damage = Random.IntRange(60, 90);
            damage -= ((Char)Target2).drRoll();
            if (Target2.buff(PortableCover.CoverBuff.class) == null) {
                damage = this.phase == 1 ? (damage += Target2.HT / 4) : (damage += Target2.HT / 2);
                ((Char)Target2).damage(damage, this);
                if (((Char)Target2).isAlive()) {
                    Buff.affect(Target2, Paralysis.class, 1.0f);
                }
            } else {
                ((Char)Target2).damage(damage /= 2, this);
            }
            Sample.INSTANCE.play("sounds/zap_gun.mp3", 1.3f);
            CellEmitter.center(Dungeon.hero.pos).burst(BlastParticle.FACTORY, 3);
            Camera.main.shake(2.0f, 0.5f);
            this.firetime = 0;
            this.firecooldown = this.phase == 1 ? 18 : 7;
            this.spend(1.0f);
            return true;
        }
        if (this.summoncooldown <= 0) {
            Mob summonEnemy = this.phase == 1 ? new BossRipper() : new BossBrave();
            int summonpos = Dungeon.hero.pos;
            Ballistica trajectory = new Ballistica(this.pos, Dungeon.hero.pos, 1);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), 7);
            WandOfBlastWave.throwChar(Dungeon.hero, trajectory, 3, true, true, null);
            summonEnemy.pos = summonpos;
            GameScene.add(summonEnemy, 1.0f);
            if (summonpos == Dungeon.hero.pos) {
                ScrollOfTeleportation.teleportChar_unobstructed(summonEnemy);
            }
            this.summoncooldown = 10;
            return true;
        }
        return true;
    }

    private boolean FireBlast() {
        boolean terrainAffected = false;
        HashSet<Char> affected = new HashSet<Char>();
        if (!Dungeon.hero.rooted) {
            for (int i : this.targetedCells) {
                Ballistica b = new Ballistica(this.pos, i, 0);
                for (int p : b.path) {
                    CellEmitter.center(p).burst(BlastParticle.FACTORY, 7);
                    Char ch = Actor.findChar(p);
                    if (ch != null && (ch.alignment != this.alignment || ch instanceof Bee)) {
                        affected.add(ch);
                    }
                    if (!Dungeon.level.flamable[p]) continue;
                    Dungeon.level.destroy(p);
                    GameScene.updateMap(p);
                    terrainAffected = true;
                }
                Sample.INSTANCE.play("sounds/blast.mp3", 2.0f);
                Camera.main.shake(2.0f, 0.5f);
            }
            if (terrainAffected) {
                Dungeon.observe();
            }
            for (Char ch : affected) {
                if (this.phase == 3) {
                    ch.damage(Random.NormalIntRange(60, 70), new Beam());
                } else {
                    ch.damage(Random.NormalIntRange(45, 55), new Beam());
                }
                if (ch.isAlive()) {
                    Buff.affect(ch, Paralysis.class, 1.0f);
                }
                if (Dungeon.level.heroFOV[this.pos]) {
                    ch.sprite.flash();
                    CellEmitter.center(this.pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
                }
                if (ch.isAlive() || ch != Dungeon.hero) continue;
                Dungeon.fail(this.getClass());
                GLog.n(Messages.get(Char.class, "kill", this.name()));
            }
            this.targetedCells.clear();
        }
        if (this.beamcooldown <= 0) {
            int beams = 3;
            HashSet<Integer> affectedCells = new HashSet<Integer>();
            for (int i = 0; i < beams; ++i) {
                int targetPos = Dungeon.hero.pos;
                if (i != 0) {
                    do {
                        targetPos = Dungeon.hero.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
                    } while (Dungeon.level.trueDistance(this.pos, Dungeon.hero.pos) > Dungeon.level.trueDistance(this.pos, targetPos));
                }
                this.targetedCells.add(targetPos);
                Ballistica b = new Ballistica(this.pos, targetPos, 0);
                affectedCells.addAll(b.path);
            }
            boolean allAdjTargeted = true;
            for (int i : PathFinder.NEIGHBOURS9) {
                if (affectedCells.contains(Dungeon.hero.pos + i) || !Dungeon.level.passable[Dungeon.hero.pos + i]) continue;
                allAdjTargeted = false;
                break;
            }
            if (allAdjTargeted) {
                this.targetedCells.remove(this.targetedCells.size() - 1);
            }
            java.util.Iterator<Integer> object = this.targetedCells.iterator();
            while (object.hasNext()) {
                int i = object.next();
                Ballistica b = new Ballistica(this.pos, i, 0);
                for (int p : b.path) {
                    this.sprite.parent.add(new TargetedCell(p, 0xFF0000));
                    affectedCells.add(p);
                }
            }
            Dungeon.hero.interrupt();
            this.beamcooldown = 3;
            this.spend(GameMath.gate(1.0f, Dungeon.hero.cooldown(), 2.0f));
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.phase);
        bundle.put(BEAM, this.beamcooldown);
        bundle.put(SUMMONCD, this.summoncooldown);
        bundle.put(RAGECD, this.ragecooldown);
        bundle.put(FIRECD, this.firecooldown);
        bundle.put(FIRETIME, this.firetime);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.beamcooldown = bundle.getInt(BEAM);
        this.summoncooldown = bundle.getInt(SUMMONCD);
        this.ragecooldown = bundle.getInt(RAGECD);
        this.firecooldown = bundle.getInt(FIRECD);
        this.firetime = bundle.getInt(FIRETIME);
        BossHealthBar.assignBoss(this);
    }

    public static class rageBuff
    extends FlavourBuff {
        public rageBuff() {
            this.immunities.add(ToxicGas.class);
            this.immunities.add(CorrosiveGas.class);
        }

        @Override
        public void fx(boolean on) {
            if (on) {
                //END(移植调整): 本 fork 无 HIKARI 状态
                this.target.sprite.flash();
            } else {
                //END(移植调整): 本 fork 无 HIKARI 状态
            }
        }
    }

    public static class BossRipper
    extends TiacauhRipper {
        public BossRipper() {
            this.state = this.HUNTING;
            this.maxLvl = -1;
        }
    }

    public static class BossBrave
    extends TiacauhBrave {
        public BossBrave() {
            this.state = this.HUNTING;
            this.maxLvl = -1;
        }
    }

    public class Beam {
    }
}
