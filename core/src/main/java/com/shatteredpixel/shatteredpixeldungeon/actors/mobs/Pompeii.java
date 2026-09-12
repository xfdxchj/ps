//END(port from Arknights): Pompeii
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LavaSlug;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Talu_BlackSnake;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PompeiiSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.BArray;
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

public class Pompeii
extends Mob {
    private int phase;
    private int blastcooldown;
    private int summoncooldown;
    private int barriercooldown;
    private int volcanocooldown;
    private int volcanotime;
    private int restorecooldown;
    private ArrayList<Integer> targetedCells;
    private static final String PHASE = "phase";
    private static final String BLAST_CD = "blastcooldown";
    private static final String BARRIER_CD = "barriercooldown";
    private static final String VOCAL_CD = "volcanocooldown";
    private static final String VOCAL_TIME = "volcanotime";
    private static final String SUMMON_CD = "summoncooldown";
    private static final String RESTORE_CD = "restorecooldown";
    private static final String TARGETED_CELLS = "targeted_cells";

    public Pompeii() {
        this.spriteClass = PompeiiSprite.class;
        this.HT = 2300;
        this.HP = 2300;
        this.defenseSkill = 25;
        this.EXP = 100;
        this.state = this.HUNTING;
        this.baseSpeed = 1.0f;
        this.viewDistance = 12;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.FIERY);
        this.properties.add(Char.Property.INFECTED);
        this.immunities.add(Amok.class);
        this.immunities.add(ParalyticGas.class);
        this.immunities.add(Terror.class);
        this.immunities.add(Silence.class);
        this.immunities.add(Blindness.class);
        this.immunities.add(Frost.class);
        this.immunities.add(Freezing.class);
        this.immunities.add(ScrollOfPsionicBlast.class);
        this.phase = 0;
        this.blastcooldown = 0;
        this.summoncooldown = 6;
        this.barriercooldown = 4;
        this.volcanocooldown = 9;
        this.volcanotime = 0;
        this.restorecooldown = 10;
        this.targetedCells = new ArrayList();
    }

    @Override
    public int damageRoll() {
        if (this.phase == 3) {
            return Random.NormalIntRange(55, 65);
        }
        return Random.NormalIntRange(45, 55);
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 24);
    }

    @Override
    public int attackSkill(Char target) {
        return 48;
    }

    @Override
    public void damage(int dmg, Object src) {
        LockedFloor lock;
        if (this.phase == 2) {
            this.sprite.showStatus(0xFFFF00, Messages.get(Talu_BlackSnake.class, "invincibility"));
            return;
        }
        if (this.buff(RestorBuff.class) != null) {
            this.HP = Math.min(this.HP + dmg / 2, this.HT);
            int bufftime = Math.min(3, dmg / 50);
            Buff.affect(this, Adrenaline.class, bufftime);
            this.sprite.emitter().burst(Speck.factory(0), 3);
            this.sprite.showStatus(65280, "+%dHP", dmg / 2);
            return;
        }
        if (this.buff(Barrier.class) != null) {
            dmg /= 4;
        } else if (this.volcanotime > 0) {
            dmg /= 8;
        }
        if (dmg > 300) {
            int thedamage;
            dmg = thedamage = 300 + dmg / 10;
        }
        super.damage(dmg, src);
        if (this.phase == 1 && this.HP < 1700) {
            this.HP = 1700;
            this.phase = 2;
            Buff.detach(this, Barrier.class);
            this.summoncooldown = 1;
            this.blastcooldown = 1;
            this.barriercooldown = 4;
            this.volcanocooldown = 7;
            this.restorecooldown = 10;
            GameScene.flash(-2130771968);
        }
        if ((lock = Dungeon.hero.buff(LockedFloor.class)) != null) {
            lock.addTime((float)dmg * 0.3f);
        }
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (this.buff(RestorBuff.class) != null && !(enemy instanceof Hero)) {
            damage = 0;
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    protected boolean act() {
        if (this.phase == 3 && this.HP < 1) {
            Dungeon.hero.HP = Dungeon.hero.HT;
            Badges.validateVictory();
            Badges.validateChampion(Challenges.activeChallenges());
            Badges.validateChampion_char(Challenges.activeChallenges());
            Badges.saveGlobal();
            Certificate.specialEndingBouns();
            Badges.silentValidateHappyEnd();
            Badges.validatesiesta2();
            Dungeon.win(Amulet.class);
            Dungeon.deleteGame(GamesInProgress.curSlot, true);
            Game.switchScene(SurfaceScene.class);
        } else {
            if (Dungeon.level.map[this.pos] == 29 && this.buff(RestorBuff.class) == null) {
                if (this.buff(Barrier.class) != null) {
                    this.damage(200, this);
                    Level.set(this.pos, 1);
                    GameScene.updateMap(this.pos);
                    CellEmitter.get(this.pos).burst(Speck.factory(13), 10);
                } else {
                    this.damage(6, this);
                }
            }
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
            if (this.UseAbility()) {
                return true;
            }
            if (this.phase == 2) {
                this.spend(1.0f);
                if (this.summoncooldown > 0) {
                    --this.summoncooldown;
                }
                return true;
            }
            if (this.phase == 1 || this.phase == 3) {
                if (this.blastcooldown > 0) {
                    --this.blastcooldown;
                }
                if (this.barriercooldown > 0) {
                    --this.barriercooldown;
                }
                if (this.volcanocooldown > 0) {
                    --this.volcanocooldown;
                }
                if (this.summoncooldown > 0) {
                    --this.summoncooldown;
                }
                if (this.restorecooldown > 0) {
                    --this.restorecooldown;
                }
            }
        }
        return super.act();
    }

    private boolean UseAbility() {
        if (this.FireBlast()) {
            return true;
        }
        if (this.summoncooldown <= 0) {
            BossSlug summon = new BossSlug();
            int spawnPos = -1;
            for (int i : PathFinder.NEIGHBOURS8) {
                if (Actor.findChar(this.pos + i) != null || Dungeon.level.solid[this.pos + i] || spawnPos != -1 && !(Dungeon.level.trueDistance(Dungeon.hero.pos, spawnPos) > Dungeon.level.trueDistance(Dungeon.hero.pos, this.pos + i))) continue;
                spawnPos = this.pos + i;
            }
            if (spawnPos != -1) {
                summon.pos = spawnPos;
                GameScene.add(summon);
                Actor.addDelayed(new Pushing(summon, this.pos, summon.pos), -1.0f);
                summon.beckon(Dungeon.hero.pos);
                if (this.phase == 3) {
                    this.summoncooldown = 7;
                } else if (this.phase == 2) {
                    int dmg = 50 - Statistics.coreAlive * 3;
                    this.HP -= dmg;
                    this.sprite.showStatus(0xFF8800, "" + dmg);
                    this.summoncooldown = 1;
                    if (this.HP < 1200) {
                        this.HP = 1200;
                        this.phase = 3;
                        GameScene.flash(-2130771968);
                    }
                } else {
                    this.summoncooldown = 10;
                }
                this.spend(1.0f);
                return true;
            }
            this.spend(1.0f);
            return true;
        }
        if (this.restorecooldown <= 0 && this.phase == 3) {
            Buff.affect(this, RestorBuff.class, 5.0f);
            GLog.w(Messages.get(this, "skill"));
            this.restorecooldown = 35;
        }
        if (this.barriercooldown <= 0) {
            if (this.phase == 3) {
                Buff.affect(this, Barrier.class).setShield(150);
            } else {
                Buff.affect(this, Barrier.class).setShield(100);
            }
            CellEmitter.center(this.pos).burst(FlameParticle.FACTORY, 4);
            Sample.INSTANCE.play("sounds/burning.mp3", 2.0f);
            this.barriercooldown = 25 - Statistics.coreAlive;
            return true;
        }
        if (this.volcanocooldown <= 0) {
            PathFinder.buildDistanceMap(this.pos, BArray.not(Dungeon.level.solid, null), 3);
            if (this.volcanotime < 3) {
                this.sprite.parent.addToBack(new TargetedCell(this.pos, 0xFF0000));
                if (this.volcanotime == 0 || this.volcanotime == 2) {
                    for (int i = 0; i < PathFinder.distance.length; ++i) {
                        int vol;
                        if (PathFinder.distance[i] >= Integer.MAX_VALUE || (vol = Fire.volumeAt(i, Fire.class)) >= 4) continue;
                        this.sprite.parent.addToBack(new TargetedCell(i, 0xFF0000));
                    }
                }
                ++this.volcanotime;
                this.spend(GameMath.gate(1.0f, Dungeon.hero.cooldown(), 2.0f));
                return true;
            }
            boolean isHit = false;
            for (int i = 0; i < PathFinder.distance.length; ++i) {
                if (PathFinder.distance[i] >= Integer.MAX_VALUE) continue;
                Char ch = Actor.findChar(i);
                int vol = Fire.volumeAt(i, Fire.class);
                if (vol < 4) {
                    CellEmitter.center(i).burst(BlastParticle.FACTORY, 1);
                }
                if (ch == null || isHit || ch.alignment == this.alignment && !(ch instanceof Bee)) continue;
                if (this.phase == 3) {
                    ch.damage(Random.NormalIntRange(120, 180), new Volcano());
                } else {
                    ch.damage(Random.NormalIntRange(80, 120), new Volcano());
                }
                if (ch.isAlive()) {
                    Buff.affect(ch, Blindness.class, 10.0f);
                } else if (!ch.isAlive() && ch == Dungeon.hero) {
                    Dungeon.fail(this.getClass());
                    GLog.n(Messages.get(Char.class, "kill", this.name()));
                }
                isHit = true;
            }
            Camera.main.shake(2.0f, 0.5f);
            Sample.INSTANCE.play("sounds/blast.mp3", 2.0f);
            Sample.INSTANCE.play("sounds/burning.mp3", 3.0f);
            Buff.affect(this, Stamina.class, 2.0f);
            this.volcanotime = 0;
            this.volcanocooldown = 10 - Statistics.coreAlive / 2;
            this.spend(1.0f);
            return true;
        }
        return false;
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
                    ch.damage(Random.NormalIntRange(60, 70), new Blast());
                } else {
                    ch.damage(Random.NormalIntRange(45, 55), new Blast());
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
        if (this.blastcooldown <= 0) {
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
                    CellEmitter.center(p).burst(FlameParticle.FACTORY, 14);
                    affectedCells.add(p);
                }
            }
            Dungeon.hero.interrupt();
            this.blastcooldown = this.phase == 3 ? 4 : 6;
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
        bundle.put(BLAST_CD, this.blastcooldown);
        bundle.put(BARRIER_CD, this.barriercooldown);
        bundle.put(VOCAL_CD, this.volcanocooldown);
        bundle.put(VOCAL_TIME, this.volcanotime);
        bundle.put(SUMMON_CD, this.summoncooldown);
        bundle.put(RESTORE_CD, this.restorecooldown);
        int[] bundleArr = new int[this.targetedCells.size()];
        for (int i = 0; i < this.targetedCells.size(); ++i) {
            bundleArr[i] = this.targetedCells.get(i);
        }
        bundle.put(TARGETED_CELLS, bundleArr);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.blastcooldown = bundle.getInt(BLAST_CD);
        this.barriercooldown = bundle.getInt(BARRIER_CD);
        this.summoncooldown = bundle.getInt(SUMMON_CD);
        this.volcanocooldown = bundle.getInt(VOCAL_CD);
        this.volcanotime = bundle.getInt(VOCAL_TIME);
        this.restorecooldown = bundle.getInt(RESTORE_CD);
        if (this.phase != 0) {
            BossHealthBar.assignBoss(this);
        }
        for (int i : bundle.getIntArray(TARGETED_CELLS)) {
            this.targetedCells.add(i);
        }
    }

    public static class RestorBuff
    extends FlavourBuff {
        public RestorBuff() {
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

    public class BossSlug
    extends LavaSlug {
        public BossSlug() {
            this.HT = 65;
            this.HP = 65;
            this.maxLvl = -5;
        }
    }

    public class Volcano {
    }

    public class Blast {
    }
}
