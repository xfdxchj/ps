//END(port from Arknights): Isharmla
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornBody;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornHead;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornTail;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SeaLeef;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SeaRunner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Sea_Octo;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Skadi_mulaSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.Camera;
import com.watabou.utils.Random;
import java.util.HashSet;

public class Isharmla
extends Mob {
    int summonCooldown;
    int shieldCooldown;
    int shieldAmount;

    public Isharmla() {
        this.spriteClass = Skadi_mulaSprite.class;
        this.HT = 1500;
        this.HP = 1500;
        this.defenseSkill = 60;
        this.actPriority = -21;
        this.WANDERING = new Wandering();
        this.HUNTING = new Hunting();
        this.state = this.WANDERING;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.IMMOVABLE);
        this.properties.add(Char.Property.STATIC);
        this.summonCooldown = 5;
        this.shieldCooldown = 8;
        this.shieldAmount = Dungeon.isChallenged(512) ? 30 : 15;
    }

    @Override
    public int defenseSkill(Char enemy) {
        return INFINITE_EVASION;
    }

    @Override
    public void notice() {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
        }
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return false;
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src != this) {
            dmg = 0;
        }
        super.damage(dmg, src);
    }

    private HashSet<Mob> getSubjects() {
        HashSet<Mob> subjects = new HashSet<Mob>();
        for (Mob m : Dungeon.level.mobs) {
            if (m.alignment != this.alignment || !(m instanceof SummonRunner) && !(m instanceof SummonLeef) && !(m instanceof SummonOcto)) continue;
            subjects.add(m);
        }
        return subjects;
    }

    public void detach() {
        Bestiary.skipCountingEncounters = true;
        for (Mob m : this.getSubjects()) {
            m.die(null);
        }
        Bestiary.skipCountingEncounters = false;
        Ballistica trajectory = new Ballistica(this.pos, Dungeon.hero.pos, 1);
        trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), 7);
        WandOfBlastWave.throwChar(Dungeon.hero, trajectory, 6, true, true, null);
        IsharmlaSeabornHead.resetBoss();
        IsharmlaSeabornHead boss1 = new IsharmlaSeabornHead();
        boss1.pos = 197;
        boss1.notice();
        GameScene.add(boss1);
        IsharmlaSeabornBody boss2 = new IsharmlaSeabornBody();
        boss2.pos = 199;
        boss2.notice();
        GameScene.add(boss2);
        IsharmlaSeabornTail boss3 = new IsharmlaSeabornTail();
        boss3.pos = 201;
        boss3.notice();
        GameScene.add(boss3);
        this.updateTerrain();
        GameScene.flash(-2130706433);
        Camera.main.shake(2.0f, 2.0f);
        Dungeon.observe();
        GameScene.updateFog();
    }

    private void updateTerrain() {
        int[] positions;
        for (int pos : positions = new int[]{197, 198, 199, 200, 201}) {
            Platform platform = (Platform)Dungeon.level.platforms.get(pos);
            if (platform != null) {
                platform.destroy();
            }
            Level.set(pos, 24);
            GameScene.updateMap(pos);
        }
    }

    @Override
    protected boolean act() {
        this.rooted = true;
        if (this.state == this.WANDERING) {
            return super.act();
        }
        if (this.summonCooldown <= 0) {
            this.damage(250, this);
            if (!this.isAlive()) {
                return super.act();
            }
            this.SummonEnemy();
        } else {
            --this.summonCooldown;
        }
        if (this.shieldCooldown <= 0) {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (!mob.isAlive() || !(mob instanceof SummonRunner) && !(mob instanceof SummonLeef) && !(mob instanceof SummonOcto)) continue;
                Buff.affect(mob, Barrier.class).setShield(this.shieldAmount);
            }
            this.shieldCooldown = Dungeon.isChallenged(512) ? 8 : 12;
        } else {
            --this.shieldCooldown;
        }
        return super.act();
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        this.detach();
    }

    private void SummonEnemy() {
        int summonpos1 = 169;
        Mob summonEnemy1 = Random.Int(4) != 0 ? new SummonRunner() : new SummonOcto();
        int summonpos2 = 187;
        Mob summonEnemy2 = Random.Int(4) != 0 ? new SummonRunner() : new SummonLeef();
        int summonpos3 = 196;
        Mob summonEnemy3 = Random.Int(4) != 0 ? new SummonRunner() : new SummonLeef();
        int summonpos4 = 192;
        Mob summonEnemy4 = Random.Int(4) != 0 ? new SummonRunner() : new SummonOcto();
        summonEnemy1.pos = summonpos1;
        GameScene.add(summonEnemy1, 1.0f);
        if (summonpos1 == Dungeon.hero.pos) {
            ScrollOfTeleportation.teleportChar_unobstructed(summonEnemy1);
        }
        summonEnemy2.pos = summonpos2;
        GameScene.add(summonEnemy2, 1.0f);
        if (summonpos2 == Dungeon.hero.pos) {
            ScrollOfTeleportation.teleportChar_unobstructed(summonEnemy2);
        }
        summonEnemy3.pos = summonpos3;
        GameScene.add(summonEnemy3, 1.0f);
        if (summonpos3 == Dungeon.hero.pos) {
            ScrollOfTeleportation.teleportChar_unobstructed(summonEnemy3);
        }
        summonEnemy4.pos = summonpos4;
        GameScene.add(summonEnemy4, 1.0f);
        if (summonpos4 == Dungeon.hero.pos) {
            ScrollOfTeleportation.teleportChar_unobstructed(summonEnemy4);
        }
        for (Mob mob : Dungeon.level.mobs) {
            mob.beckon(Dungeon.hero.pos);
        }
        this.summonCooldown = 10;
    }

    protected class Wandering
    implements Mob.AiState {
        public static final String TAG = "PASSIVE";

        protected Wandering() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            Isharmla.this.enemySeen = enemyInFOV;
            if (Isharmla.this.enemySeen) {
                Isharmla.this.notice();
                Isharmla.this.state = Isharmla.this.HUNTING;
            }
            Isharmla.this.spend(1.0f);
            return true;
        }
    }

    protected class Hunting
    implements Mob.AiState {
        public static final String TAG = "PASSIVE";

        protected Hunting() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            Isharmla.this.spend(1.0f);
            return true;
        }
    }

    public static class SummonRunner
    extends SeaRunner {
        public SummonRunner() {
            this.state = this.HUNTING;
            this.immunities.add(Drowsy.class);
            this.immunities.add(MagicalSleep.class);
            this.immunities.add(AllyBuff.class);
            this.maxLvl = -5;
        }
    }

    public static class SummonLeef
    extends SeaLeef {
        public SummonLeef() {
            this.state = this.HUNTING;
            this.immunities.add(Drowsy.class);
            this.immunities.add(MagicalSleep.class);
            this.immunities.add(AllyBuff.class);
            this.maxLvl = -5;
        }
    }

    public static class SummonOcto
    extends Sea_Octo {
        public SummonOcto() {
            this.state = this.HUNTING;
            this.immunities.add(Drowsy.class);
            this.immunities.add(MagicalSleep.class);
            this.immunities.add(AllyBuff.class);
            this.maxLvl = -5;
        }
    }
}
