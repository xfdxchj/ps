//END(port from Arknights): CatGun
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CustomeSet;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMistress;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Mon3terSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class CatGun
extends MeleeWeapon {
    public static final String AC_ZAP = "ZAP";

    public CatGun() {
        this.image = ItemSpriteSheet.CATGUN;
        this.hitSound = "sounds/atk_spiritbow.mp3";
        this.hitSoundPitch = 1.0f;
        this.defaultAction = AC_ZAP;
        this.tier = 5;
        this.RCH = 2;
    }

    @Override
    public int max(int lvl) {
        return 3 * (this.tier + 1) + lvl * (this.tier - 1);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        this.SPCharge_cat(Random.IntRange(2, 3));
        return super.proc(attacker, defender, damage);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    public void SPCharge_cat(int value) {
        this.charge = Math.min(this.charge + value, this.chargeCap);
        CatGun.updateQuickslot();
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_ZAP)) {
            if (this.charge >= this.chargeCap) {
                ArrayList<Integer> respawnPoints = new ArrayList<Integer>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; ++i) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar(p) != null || !Dungeon.level.passable[p]) continue;
                    respawnPoints.add(p);
                }
                for (int spawnd = 0; respawnPoints.size() > 0 && spawnd == 0; ++spawnd) {
                    int index = Random.index(respawnPoints);
                    Mon3tr tr = new Mon3tr();
                    tr.setting(this.buffedLvl());
                    GameScene.add(tr);
                    ScrollOfTeleportation.appear(tr, (Integer)respawnPoints.get(index));
                    respawnPoints.remove(index);
                    Sample.INSTANCE.play("sounds/skill_mon1.mp3");
                }
                this.charge = 0;
            } else if (this.charge < this.chargeCap && CatGun.catsetbouns()) {
                for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (!(mob instanceof Mon3tr)) continue;
                    int gaincharge = mob.HT / 2 > mob.HP ? 25 : (mob.HT / 4 > mob.HP ? 15 : 40);
                    mob.die(this);
                    this.charge = Math.min(this.charge + gaincharge, this.chargeCap);
                    CatGun.updateQuickslot();
                }
            }
        }
    }

    @Override
    public String status() {
        if (!this.isIdentified() || this.cursed) {
            return null;
        }
        if (this.chargeCap == 100) {
            return Messages.format("%d%%", this.charge);
        }
        return null;
    }

    @Override
    public String desc() {
        if (CatGun.catsetbouns()) {
            Object info = Messages.get(this, "desc_sp");
            info = (String)info + "\n\n" + Messages.get(CatGun.class, "setbouns");
            return String.valueOf(info);
        }
        String info = Messages.get(this, "desc");
        return String.valueOf(info);
    }

    public static boolean catsetbouns() {
        //END(修复): 图鉴(WndJournal)会调用 Item.info() → desc()，
        //那时 Dungeon.hero 为 null → 原来直接访问 belongings 会 NPE。
        if (Dungeon.hero == null || Dungeon.hero.belongings == null) {
            return false;
        }

        if (!(Dungeon.hero.belongings.weapon instanceof CatGun)) {
            return false;
        }
        return Dungeon.hero.belongings.getItem(RingOfMistress.class) != null && Dungeon.hero.belongings.getItem(ChaliceOfBlood.class) != null && Dungeon.hero.belongings.getItem(UnstableSpellbook.class) != null && Dungeon.hero.belongings.getItem(RingOfMistress.class).isEquipped(Dungeon.hero) && Dungeon.hero.belongings.getItem(ChaliceOfBlood.class).isEquipped(Dungeon.hero) && Dungeon.hero.belongings.getItem(UnstableSpellbook.class).isEquipped(Dungeon.hero);
    }

    public static class Mon3tr
    extends NPC {
        private int blinkCooldown;
        private static final String BLINK = "blinkcooldown";

        public Mon3tr() {
            this.spriteClass = Mon3terSprite.class;
            this.baseSpeed = 3.0f;
            this.state = this.HUNTING;
            this.immunities.add(Silence.class);
            this.alignment = Char.Alignment.ALLY;
            this.WANDERING = new Wandering();
            this.blinkCooldown = 0;
        }

        @Override
        protected boolean getCloser(int target) {
            if (this.fieldOfView[target] && Dungeon.level.distance(this.pos, target) > 2 && this.blinkCooldown <= 0 && target != Dungeon.hero.pos) {
                this.blink(target);
                this.spend(-1.0f / this.speed());
                return true;
            }
            --this.blinkCooldown;
            return super.getCloser(target);
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
                    this.blinkCooldown = 1;
                    return;
                }
            }
            ScrollOfTeleportation.appear(this, cell);
            Sample.INSTANCE.play("sounds/skill_mon2.mp3");
            this.blinkCooldown = 1;
        }

        @Override
        protected boolean act() {
            if (this.buff(StoneOfAggression.Aggression.class) == null) {
                Buff.prolong(this, StoneOfAggression.Aggression.class, 20.0f);
            }
            CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
            if (this.isAlive() || this.HP <= 1) {
                if (setBuff != null) {
                    int adddamage = this.HT / (50 + setBuff.itemLevel() * 3);
                    if (adddamage < 1) {
                        adddamage = 1;
                    }
                    this.HP = this.state == this.WANDERING && adddamage > 1 ? (this.HP -= adddamage / 2) : (this.HP -= adddamage);
                } else {
                    int adddamage = this.HT / 50;
                    if (adddamage < 1) {
                        adddamage = 1;
                    }
                    this.HP = this.state == this.WANDERING && adddamage > 1 ? (this.HP -= adddamage / 2) : (this.HP -= adddamage);
                }
            }
            if (this.HP < 1) {
                this.die(this);
                return true;
            }
            return super.act();
        }

        @Override
        public int attackProc(Char enemy, int damage) {
            CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
            if (setBuff != null) {
                int n = 20 + setBuff.itemLevel() * 3;
                this.HP -= this.HT / n;
            } else {
                this.HP -= this.HT / 20;
            }
            if (CatGun.catsetbouns()) {
                damage = (int)((float)damage * 1.3f);
                //END(移植调整): 本 fork 的 ArtifactRecharge 只有 set(amount)，没有 prolong()。
                Buff.affect((Char)Dungeon.hero, ArtifactRecharge.class).set(2f);
                Buff.affect(Dungeon.hero, Bless.class, 2.0f);
            }
            return super.attackProc(enemy, damage);
        }

        @Override
        public void die(Object cause) {
            if (CatGun.catsetbouns()) {
                for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (!Dungeon.level.adjacent(mob.pos, this.pos) || mob.alignment == Char.Alignment.ALLY) continue;
                    int dmg = this.damageRoll();
                    CellEmitter.get(mob.pos).burst(BlastParticle.FACTORY, 6);
                    mob.damage(dmg, this);
                }
            }
            super.die(cause);
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(18 + this.maxLvl * 4, 24 + this.maxLvl * 6);
        }

        @Override
        public int attackSkill(Char target) {
            return 25 + this.maxLvl * 3;
        }

        @Override
        public int drRoll() {
            return Random.NormalIntRange(this.maxLvl / 2, 2 + this.maxLvl);
        }

        public void setting(int setlvl) {
            CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
            int itembuff = 0;
            if (setBuff != null) {
                itembuff = setBuff.itemLevel();
            }
            this.HP = this.HT = 120 + setlvl * 20;
            this.defenseSkill = 10 + setlvl * 2;
            this.maxLvl = setlvl + itembuff / 2;
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BLINK, this.blinkCooldown);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            this.blinkCooldown = bundle.getInt(BLINK);
            this.enemySeen = true;
        }

        private class Wandering
        extends Mob.Wandering {
            private Wandering() {
            }

            @Override
            public boolean act(boolean enemyInFOV, boolean justAlerted) {
                if (enemyInFOV) {
                    Mon3tr.this.enemySeen = true;
                    Mon3tr.this.notice();
                    Mon3tr.this.alerted = true;
                    Mon3tr.this.state = Mon3tr.this.HUNTING;
                    Mon3tr.this.target = ((Mon3tr)Mon3tr.this).enemy.pos;
                } else {
                    Mon3tr.this.enemySeen = false;
                    int oldPos = Mon3tr.this.pos;
                    Mon3tr.this.target = Dungeon.hero.pos;
                    if (Mon3tr.this.getCloser(Mon3tr.this.target)) {
                        if (!Dungeon.level.adjacent(Mon3tr.this.target, Mon3tr.this.pos)) {
                            Mon3tr.this.getCloser(Mon3tr.this.target);
                        }
                        Mon3tr.this.spend(1.0f / Mon3tr.this.speed());
                        return Mon3tr.this.moveSprite(oldPos, Mon3tr.this.pos);
                    }
                    Mon3tr.this.spend(1.0f);
                }
                return true;
            }
        }
    }
}
