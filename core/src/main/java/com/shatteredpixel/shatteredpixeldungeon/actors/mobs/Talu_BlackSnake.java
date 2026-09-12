//END(port from Arknights): Talu_BlackSnake
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FistSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.BArray;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class Talu_BlackSnake
extends Mob {
    private static final String[] LINE_KEYS = new String[]{"invincibility1", "invincibility2", "invincibility3"};
    private int phase;
    private int IgniteCooldown;
    private int BurstCooldown;
    private int OverwhelmCooldown;
    private int InvincibilityCooldown;
    private int InvincibilityTime;
    private int BurstPos;
    private int BurstTime;
    private int drup;
    private boolean fx;
    private static final String PHASE = "phase";
    private static final String SKILL1CD = "IgniteCooldown";
    private static final String SKILL2CD = "BurstCooldown";
    private static final String SKILL2POS = "BurstPos";
    private static final String SKILL2TIME = "BurstTime";
    private static final String SKILL3CD = "OverwhelmCooldown";
    private static final String SKILL4CD = "InvincibilityCooldown";
    private static final String SKILL4TIME = "InvincibilityTime";
    private static final String DRUPTIME = "drup";

    public Talu_BlackSnake() {
        this.spriteClass = FistSprite.Burning.class;
        this.HT = 2500;
        this.HP = 2500;
        this.defenseSkill = 32;
        this.EXP = 100;
        this.state = this.HUNTING;
        this.viewDistance = 12;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.INFECTED);
        this.properties.add(Char.Property.SARKAZ);
        this.immunities.add(Amok.class);
        this.immunities.add(Terror.class);
        this.immunities.add(Paralysis.class);
        this.immunities.add(Vertigo.class);
        this.immunities.add(Silence.class);
        this.immunities.add(Blindness.class);
        this.properties.add(Char.Property.FIERY);
        this.phase = 0;
        this.IgniteCooldown = 0;
        this.BurstCooldown = 0;
        this.OverwhelmCooldown = 0;
        this.InvincibilityCooldown = 0;
        this.InvincibilityTime = 0;
        this.BurstPos = -1;
        this.BurstTime = 0;
        this.drup = 0;
        this.fx = false;
    }

    @Override
    public int damageRoll() {
        if (this.InvincibilityTime > 0) {
            return Random.NormalIntRange(55, 75);
        }
        return Random.NormalIntRange(40, 50);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int defenseSkill(Char enemy) {
        this.defenseSkill = Dungeon.level.map[this.pos] == 29 ? 16 : 32;
        return super.defenseSkill(enemy);
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 20);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (this.InvincibilityTime > 0) {
            this.sprite.showStatus(0xFFFF00, Messages.get(this, "invincibility"));
            Sample.INSTANCE.play("sounds/hit_parry.mp3", 1.0f, Random.Float(0.96f, 1.05f));
            return;
        }
        if (this.drup > 0) {
            dmg /= 2;
        }
        super.damage(dmg, src);
        if (this.phase == 1 && this.HP < 2200) {
            this.HP = 2200;
            this.phase = 2;
            this.drup += 3;
            GameScene.flash(-2130771968);
            this.yell(Messages.get(this, "phase2"));
        } else if (this.phase == 2 && this.HP < 1700) {
            this.HP = 1700;
            this.phase = 3;
            this.drup += 6;
            GameScene.flash(-2130771968);
            this.yell(Messages.get(this, "phase3"));
        } else if (this.phase == 3 && this.HP < 1300) {
            this.HP = 1300;
            this.phase = 4;
            GameScene.flash(-2130771968);
            this.yell(Messages.get(this, "phase4"));
        } else if (this.phase == 4 && this.HP < 500) {
            this.HP = 500;
            this.phase = 5;
            this.drup += 3;
            GameScene.flash(-2130771968);
            this.yell(Messages.get(this, "phase5"));
        }
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Badges.validateVictory();
        Badges.validateChampion(Challenges.activeChallenges());
        Badges.validateChampion_char(Challenges.activeChallenges());
        Badges.saveGlobal();
        Dungeon.level.drop((Item)new Certificate((int)25), (int)this.pos).sprite.drop(this.pos);
        Certificate.specialEndingBouns();
        Badges.silentValidateHappyEnd();
        Badges.validatewill();
        Dungeon.win(Amulet.class);
        Dungeon.deleteGame(GamesInProgress.curSlot, true);
        Game.switchScene(SurfaceScene.class);
    }

    @Override
    protected boolean act() {
        if (this.phase == 5 && this.HP <= 0) {
            this.die(this);
        } else if (this.HP <= 0) {
            this.phase = Math.min(5, this.phase + 1);
        }
        if (this.phase > 3 && !this.fx) {
            //END(移植调整): 本 fork 无 TALU_BOSS 状态
        this.sprite.flash();
            this.fx = true;
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
        this.UseAbility();
        if (Dungeon.level.map[this.pos] == 29) {
            this.damage(8, this);
        }
        if (this.InvincibilityTime > 0) {
            int evaporatedTiles = Random.chances(new float[]{0.0f, 2.0f, 1.0f, 1.0f});
            for (int i = 0; i < evaporatedTiles; ++i) {
                int cell = this.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
                if (Dungeon.level.map[cell] != 29) continue;
                Level.set(cell, 1);
                GameScene.updateMap(cell);
                CellEmitter.get(cell).burst(Speck.factory(13), 10);
            }
            for (int i : PathFinder.NEIGHBOURS9) {
                int vol = Fire.volumeAt(this.pos + i, Fire.class);
                if (vol >= 4 || Dungeon.level.water[this.pos + i] || Dungeon.level.solid[this.pos + i]) continue;
                GameScene.add(Blob.seed(this.pos + i, 4 - vol, Fire.class));
            }
        }
        if (this.IgniteCooldown > 0) {
            --this.IgniteCooldown;
        }
        if (this.BurstCooldown > 0) {
            --this.BurstCooldown;
        }
        if (this.OverwhelmCooldown > 0) {
            --this.OverwhelmCooldown;
        }
        if (this.InvincibilityCooldown > 0) {
            --this.InvincibilityCooldown;
        }
        if (this.InvincibilityTime > 0) {
            --this.InvincibilityTime;
        }
        if (this.drup > 0) {
            --this.drup;
        }
        return super.act();
    }

    private boolean UseAbility() {
        if (this.BurstCooldown <= 0) {
            if (this.BurstPos == -1) {
                this.BurstPos = Dungeon.hero.pos;
                this.sprite.parent.addToBack(new TargetedCell(this.BurstPos, 0xFF0000));
                for (int i : PathFinder.NEIGHBOURS9) {
                    int vol = Fire.volumeAt(this.BurstPos + i, Fire.class);
                    if (vol >= 4) continue;
                    this.sprite.parent.addToBack(new TargetedCell(this.BurstPos + i, 0xFF0000));
                }
                Sample.INSTANCE.play("sounds/burning.mp3");
                ++this.BurstTime;
                return true;
            }
            if (this.BurstTime == 1) {
                for (int i : PathFinder.NEIGHBOURS9) {
                    int vol = Fire.volumeAt(this.BurstPos + i, Fire.class);
                    if (vol >= 4) continue;
                    GameScene.add(Blob.seed(this.BurstPos + i, 4 - vol, Fire.class));
                }
                Sample.INSTANCE.play("sounds/burning.mp3");
                ++this.BurstTime;
                return true;
            }
            if (this.BurstTime == 2) {
                PathFinder.buildDistanceMap(this.BurstPos, BArray.not(Dungeon.level.solid, null), 1);
                for (int cell = 0; cell < PathFinder.distance.length; ++cell) {
                    Char ch;
                    if (PathFinder.distance[cell] >= Integer.MAX_VALUE) continue;
                    if (Dungeon.level.map[cell] == 29) {
                        Level.set(cell, 1);
                        GameScene.updateMap(cell);
                        CellEmitter.get(cell).burst(Speck.factory(13), 10);
                    }
                    if ((ch = Actor.findChar(cell)) == null || ch instanceof Talu_BlackSnake) continue;
                    ch.damage(Random.NormalIntRange(48, 72), this);
                    if (!(ch instanceof Hero)) continue;
                    GameScene.flash(-2130771968);
                }
                Camera.main.shake(2.0f, 0.5f);
                this.BurstPos = -1;
                this.BurstTime = 0;
                this.BurstCooldown = this.phase == 5 ? Random.NormalIntRange(4, 5) : Random.NormalIntRange(8, 10);
                Sample.INSTANCE.play("sounds/blast.mp3", 2.5f, 1.21f);
                return true;
            }
        } else {
            if (this.InvincibilityCooldown <= 0 && this.phase >= 4) {
                this.InvincibilityTime = this.phase == 5 ? 8 : 5;
                this.InvincibilityCooldown = 22;
                this.yell(Messages.get(this, Random.element(LINE_KEYS)));
                Sample.INSTANCE.play("sounds/blast.mp3", 3.0f, 0.25f);
                Camera.main.shake(3.0f, 0.5f);
                return true;
            }
            if (this.OverwhelmCooldown <= 0 && this.phase >= 2) {
                this.blink(Dungeon.hero.pos);
                Ballistica trajectory = new Ballistica(this.pos, Dungeon.hero.pos, 1);
                trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), 7);
                WandOfBlastWave.throwChar(Dungeon.hero, trajectory, 1, true, true, null);
                if (this.phase == 5) {
                    Buff.affect(this, Barrier.class).incShield(40);
                }
                this.OverwhelmCooldown = this.phase == 5 ? Random.NormalIntRange(7, 11) : Random.NormalIntRange(12, 16);
                return true;
            }
            if (this.IgniteCooldown <= 0) {
                Dungeon.hero.sprite.emitter().burst(ElmoParticle.FACTORY, 5);
                Dungeon.hero.damage(Random.NormalIntRange(6, 12), this);
                Buff.affect(Dungeon.hero, Burning.class).reignite(Dungeon.hero);
                if (this.phase == 5) {
                    Level.set(Dungeon.hero.pos, 1);
                    GameScene.updateMap(Dungeon.hero.pos);
                    CellEmitter.get(Dungeon.hero.pos).burst(Speck.factory(13), 10);
                    this.IgniteCooldown = 3;
                } else {
                    this.IgniteCooldown = Random.NormalIntRange(5, 6);
                }
                Sample.INSTANCE.play("sounds/burning.mp3");
                return true;
            }
        }
        return true;
    }

    private void blink(int target) {
        Ballistica route = new Ballistica(this.pos, target, 7);
        int cell = route.collisionPos;
        if (Actor.findChar(cell) != null && cell != this.pos) {
            cell = route.path.get(route.dist - 1);
        }
        if (Dungeon.level.avoid[cell]) {
            ArrayList<Integer> candidates = new ArrayList<Integer>();
            for (int n : PathFinder.NEIGHBOURS8) {
                cell = route.collisionPos + n;
                if (!Dungeon.level.passable[cell] || Actor.findChar(cell) != null) continue;
                candidates.add(cell);
            }
            if (candidates.size() > 0) {
                cell = (Integer)Random.element(candidates);
            } else {
                return;
            }
        }
        ScrollOfTeleportation.appear(this, cell);
        Sample.INSTANCE.play("sounds/cursed.mp3");
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        enemy.damage(8, this);
        if (Dungeon.level.map[enemy.pos] == 29) {
            damage = (int)((float)damage * 0.7f);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void notice() {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            this.yell(Messages.get(this, "notice"));
            for (Char ch : Actor.chars()) {
                if (!(ch instanceof DriedRose.GhostHero)) continue;
                ((DriedRose.GhostHero)ch).sayBoss();
            }
            if (this.phase == 0) {
                this.phase = 1;
            }
        }
    }

    @Override
    public boolean isAlive() {
        return this.HP > 0 || this.phase < 5;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.phase);
        bundle.put(SKILL1CD, this.IgniteCooldown);
        bundle.put(SKILL2CD, this.BurstCooldown);
        bundle.put(SKILL2POS, this.BurstPos);
        bundle.put(SKILL2TIME, this.BurstTime);
        bundle.put(SKILL3CD, this.OverwhelmCooldown);
        bundle.put(SKILL4CD, this.InvincibilityCooldown);
        bundle.put(SKILL4TIME, this.InvincibilityTime);
        bundle.put(DRUPTIME, this.drup);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.IgniteCooldown = bundle.getInt(SKILL1CD);
        this.BurstCooldown = bundle.getInt(SKILL2CD);
        this.BurstPos = bundle.getInt(SKILL2POS);
        this.BurstTime = bundle.getInt(SKILL2TIME);
        this.OverwhelmCooldown = bundle.getInt(SKILL3CD);
        this.InvincibilityCooldown = bundle.getInt(SKILL4CD);
        this.InvincibilityTime = bundle.getInt(SKILL4TIME);
        this.drup = bundle.getInt(DRUPTIME);
        this.fx = this.phase > 3;
        BossHealthBar.assignBoss(this);
    }
}
