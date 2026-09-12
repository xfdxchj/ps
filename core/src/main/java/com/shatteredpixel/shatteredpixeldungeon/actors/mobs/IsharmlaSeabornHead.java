//END(port from Arknights): IsharmlaSeabornHead
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornBody;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornTail;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WaterParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Mula_1Sprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossMultiHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IsharmlaSeabornHead
extends Mob {
    private boolean isDead;
    private int laserCooldown;
    private static boolean isAngry = false;
    private static boolean isEnraged = false;
    private static boolean isHeadEnraged = false;
    private static int enrageDuration = 20;
    private int waveCooldown;
    private static final String IS_DEAD_HEAD = "isDeadHead";
    private static final String LASER_COOLDOWN = "laserCooldown";
    private static final String IS_ANGRY = "isAngry";
    private static final String IS_ENRAGED = "isEnraged";
    private static final String IS_HEAD_ENRAGED = "isHeadEnraged";
    private static final String ENRAGE_DURATION = "enrageDuration";
    private static final String WAVE_COOLDOWN = "waveCooldown";

    public IsharmlaSeabornHead() {
        this.spriteClass = Mula_1Sprite.class;
        this.HT = 1000;
        this.HP = 1000;
        this.defenseSkill = 20;
        this.actPriority = -21;
        this.properties.add(Char.Property.SEA);
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.IMMOVABLE);
        this.state = new Hunting();
        this.isDead = false;
        this.laserCooldown = 6;
        this.waveCooldown = 1;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 70);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public void notice() {
        BossMultiHealthBar.assignBoss(this);
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (this.isDead) {
            return INFINITE_EVASION;
        }
        if (enemy instanceof Hero && Dungeon.level.map[enemy.pos] == 1) {
            return INFINITE_EVASION;
        }
        return super.defenseSkill(enemy);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return !this.isDead && this.fieldOfView[enemy.pos] && Dungeon.level.distance(this.pos, enemy.pos) <= 2;
    }

    @Override
    protected boolean act() {
        this.sprite.turnTo(this.pos, 999999);
        this.rooted = true;
        if (this.isDead) {
            if (Dungeon.mulaCount == 3 || this.allBodyPartsDead()) {
                Badges.validateVictory();
                Badges.validateChampion(Challenges.activeChallenges());
                Badges.validateChampion_char(Challenges.activeChallenges());
                Badges.saveGlobal();
                Certificate.specialEndingBouns();
                Badges.silentValidateHappyEnd();
                Badges.validateiberia2();
                Dungeon.win(Amulet.class);
                Dungeon.deleteGame(GamesInProgress.curSlot, true);
                Game.switchScene(SurfaceScene.class);
            }
            this.alerted = false;
            return super.act();
        }
        if (this.laserCooldown <= 0) {
            boolean terrainAffected = false;
            HashSet<Char> affected = new HashSet<Char>();
            int targetPos = Dungeon.hero.pos;
            Ballistica b = new Ballistica(this.pos, targetPos, 0);
            this.sprite.parent.add(new Beam.WaterRay(this.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
            for (int p : b.path) {
                Platform platform;
                Char ch = Actor.findChar(p);
                if (ch != null && (ch.alignment != this.alignment || ch instanceof Bee)) {
                    affected.add(ch);
                }
                if (Dungeon.level.flamable[p]) {
                    Dungeon.level.destroy(p);
                    GameScene.updateMap(p);
                    terrainAffected = true;
                }
                if ((platform = (Platform)Dungeon.level.platforms.get(p)) == null) continue;
                platform.destroy();
                GameScene.updateMap(p);
                terrainAffected = true;
            }
            if (terrainAffected) {
                Dungeon.observe();
            }
            int dmg = Random.NormalIntRange(12, 34);
            for (Char ch : affected) {
                ch.damage(dmg, this);
                if (Dungeon.level.heroFOV[this.pos]) {
                    ch.sprite.flash();
                    CellEmitter.center(this.pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
                }
                if (ch.isAlive() || ch != Dungeon.hero) continue;
                Dungeon.fail(this.getClass());
                GLog.n(Messages.get(Char.class, "kill", this.name()));
            }
            this.laserCooldown = Dungeon.isChallenged(512) ? 5 : 6;
        } else {
            --this.laserCooldown;
        }
        if (isEnraged) {
            this.specialAttack();
        } else if (isAngry) {
            this.specialAttack();
            if (--enrageDuration <= 0) {
                isAngry = false;
            }
        }
        return super.act();
    }

    @Override
    public void damage(int dmg, Object src) {
        if (this.isDead) {
            return;
        }
        int heroTile = Dungeon.level.map[Dungeon.hero.pos];
        if (heroTile == 1 || heroTile == 20) {
            return;
        }
        float resistance = Dungeon.isChallenged(512) ? 0.5f : 0.33f;
        dmg = (int)((double)dmg * (1.0 - (double)(resistance * (float)(2 - Dungeon.mulaCount))));
        int hpThreshold = this.HT / 2;
        super.damage(dmg, src);
        if (this.HP < hpThreshold && !isHeadEnraged) {
            this.HP = hpThreshold;
            isHeadEnraged = true;
            IsharmlaSeabornHead.triggerAnger();
        } else if (this.HP < 1) {
            this.isDead = true;
            Buff.affect(this, Doom.class);
            ++Dungeon.mulaCount;
        }
    }

    @Override
    public void die(Object cause) {
    }

    @Override
    public boolean isAlive() {
        return !this.isDead;
    }

    private boolean allBodyPartsDead() {
        for (Mob mob : Dungeon.level.mobs) {
            if (!(mob instanceof IsharmlaSeabornHead) && !(mob instanceof IsharmlaSeabornBody) && !(mob instanceof IsharmlaSeabornTail) || !mob.isAlive()) continue;
            return false;
        }
        return true;
    }

    public static void triggerAnger() {
        if (isHeadEnraged) {
            isEnraged = true;
            enrageDuration = 999;
        } else {
            int newDuration = (Dungeon.isChallenged(512) ? 20 : 15) * Dungeon.mulaCount;
            if (!isAngry || newDuration > enrageDuration) {
                enrageDuration = newDuration;
            }
            isAngry = true;
        }
    }

    public static void resetBoss() {
        isAngry = false;
        isEnraged = false;
        isHeadEnraged = false;
        enrageDuration = 20;
    }

    public void specialAttack() {
        if (this.waveCooldown > 0) {
            --this.waveCooldown;
        } else {
            this.waveCooldown = Dungeon.isChallenged(512) || isEnraged ? 2 : 3;
            IsharmlaSeabornHead.sendWaves(this);
        }
    }

    public static void sendWaves(Char thrower) {
        WaveAbility waveAbility = Buff.append(thrower, WaveAbility.class);
        waveAbility.width = isHeadEnraged ? 7 : 3 + 2 * Dungeon.mulaCount;
        waveAbility.setStartPos();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(IS_DEAD_HEAD, this.isDead);
        bundle.put(IS_ANGRY, isAngry);
        bundle.put(IS_ENRAGED, isEnraged);
        bundle.put(IS_HEAD_ENRAGED, isHeadEnraged);
        bundle.put(LASER_COOLDOWN, this.laserCooldown);
        bundle.put(ENRAGE_DURATION, enrageDuration);
        bundle.put(WAVE_COOLDOWN, this.waveCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.isDead = bundle.getBoolean(IS_DEAD_HEAD);
        isAngry = bundle.getBoolean(IS_ANGRY);
        isEnraged = bundle.getBoolean(IS_ENRAGED);
        isHeadEnraged = bundle.getBoolean(IS_HEAD_ENRAGED);
        this.laserCooldown = bundle.getInt(LASER_COOLDOWN);
        enrageDuration = bundle.getInt(ENRAGE_DURATION);
        this.waveCooldown = bundle.getInt(WAVE_COOLDOWN);
    }
    public boolean isDead() {
        return this.isDead;
    }

    protected class Hunting
    implements Mob.AiState {
        protected Hunting() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            IsharmlaSeabornHead.this.enemySeen = enemyInFOV;
            if (enemyInFOV && !IsharmlaSeabornHead.this.isCharmedBy(IsharmlaSeabornHead.this.enemy) && IsharmlaSeabornHead.this.canAttack(IsharmlaSeabornHead.this.enemy)) {
                IsharmlaSeabornHead.this.target = IsharmlaSeabornHead.this.enemy.pos;
                return IsharmlaSeabornHead.this.doAttack(IsharmlaSeabornHead.this.enemy);
            }
            IsharmlaSeabornHead.this.spend(1.0f);
            return true;
        }
    }

    public static class WaveAbility
    extends Buff {
        public int start;
        public int width;
        public int previousStart = -1;
        private int[] curCells;
        HashSet<Integer> toCells = new HashSet();
        private static final String START = "start";
        private static final String WIDTH = "width";
        private static final String CUR_CELLS = "cur_cells";
        private static final String PREVIOUS_START = "previousStart";

        @Override
        public boolean act() {
            Integer c;
            int n;
            int n2;
            int[] nArray;
            if (this.target instanceof IsharmlaSeabornHead && ((IsharmlaSeabornHead)this.target).isDead) {
                this.detach();
                return true;
            }
            this.toCells.clear();
            if (this.curCells == null) {
                this.curCells = this.initialCells(this.start);
                this.spreadFromCells(this.curCells);
            } else {
                nArray = this.curCells;
                n2 = nArray.length;
                for (n = 0; n < n2; ++n) {
                    c = nArray[n];
                    if (WaterBlob.volumeAt(c, WaterBlob.class) <= 0) continue;
                    this.spreadFromCell(c);
                }
            }
            nArray = this.curCells;
            n2 = nArray.length;
            for (n = 0; n < n2; ++n) {
                c = nArray[n];
                this.toCells.remove(c);
            }
            if (this.toCells.isEmpty()) {
                this.detach();
            } else {
                this.curCells = new int[this.toCells.size()];
                int i = 0;
                for (Integer c2 : this.toCells) {
                    GameScene.add(Blob.seed(c2, 2, WaterBlob.class));
                    this.curCells[i] = c2;
                    ++i;
                }
            }
            this.spend(1.0f);
            return true;
        }

        private int[] initialCells(int cell) {
            HashSet<Integer> cells = new HashSet<Integer>();
            cells.add(cell);
            this.addLeft(cell, this.width / 2, cells);
            this.addRight(cell, this.width / 2, cells);
            return this.convertToArray(cells);
        }

        private int[] convertToArray(Set<Integer> cells) {
            int[] outArr = new int[cells.size()];
            int index = 0;
            Iterator<Integer> iterator = cells.iterator();
            while (iterator.hasNext()) {
                int cell;
                outArr[index] = cell = iterator.next().intValue();
                ++index;
            }
            return outArr;
        }

        private void addLeft(int cell, int width, Set<Integer> cells) {
            for (int i = 1; i <= width; ++i) {
                if (Dungeon.level.solid[cell - i]) continue;
                cells.add(cell - i);
            }
        }

        private void addRight(int cell, int width, Set<Integer> cells) {
            for (int i = 1; i <= width; ++i) {
                if (Dungeon.level.solid[cell + i]) continue;
                cells.add(cell + i);
            }
        }

        private void spreadFromCells(int[] cells) {
            for (int cell : cells) {
                this.spreadFromCell(cell);
            }
        }

        private void spreadFromCell(int cell) {
            if (!Dungeon.level.solid[cell + PathFinder.NEIGHBOURS4[3]]) {
                this.toCells.add(cell + PathFinder.NEIGHBOURS4[3]);
            }
        }

        private void setStartPos() {
            int newStart = 199;
            for (int i = 0; i < 10 && this.previousStart == (newStart = Random.Int(169 + this.width / 2, 187 - this.width / 2)); ++i) {
            }
            this.start = newStart;
            this.previousStart = newStart;
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(START, this.start);
            bundle.put(WIDTH, this.width);
            if (this.curCells != null) {
                bundle.put(CUR_CELLS, this.curCells);
            }
            bundle.put(PREVIOUS_START, this.previousStart);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            this.start = bundle.getInt(START);
            this.width = bundle.getInt(WIDTH);
            if (bundle.contains(CUR_CELLS)) {
                this.curCells = bundle.getIntArray(CUR_CELLS);
            }
            this.previousStart = bundle.getInt(PREVIOUS_START);
        }

        public static class WaterBlob
        extends Blob {
            public WaterBlob() {
                this.actPriority = -31;
                this.alwaysVisible = true;
            }

            @Override
            protected void evolve() {
                boolean burned = false;
                for (int i = this.area.left; i < this.area.right; ++i) {
                    for (int j = this.area.top; j < this.area.bottom; ++j) {
                        int cell = i + j * Dungeon.level.width();
                        int n = this.off[cell] = this.cur[cell] > 0 ? this.cur[cell] - 1 : 0;
                        if (this.off[cell] > 0) {
                            this.volume += this.off[cell];
                        }
                        if (this.cur[cell] <= 0 || this.off[cell] != 0) continue;
                        Char ch = Actor.findChar(cell);
                        if (!(ch == null || ch instanceof IsharmlaSeabornHead || ch instanceof IsharmlaSeabornBody || ch instanceof IsharmlaSeabornTail)) {
                            Buff.prolong(ch, Blindness.class, 3.0f);
                            Buff.affect(ch, NervousImpairment.class).sum(40.0f);
                            ch.damage(Random.Int(25, 55), this);
                        }
                        burned = true;
                        CellEmitter.get(cell).start(WaterParticle.SPLASHING, 0.07f, 10);
                    }
                }
                if (burned) {
                    Sample.INSTANCE.play("sounds/splash.mp3");
                }
            }

            @Override
            public void use(BlobEmitter emitter) {
                super.use(emitter);
                emitter.y -= 6.4f;
                emitter.height *= 0.4f;
                emitter.pour(WaterParticle.FALLING, 0.2f);
            }

            @Override
            public String tileDesc() {
                return Messages.get(this, "desc");
            }
        }
    }
}
