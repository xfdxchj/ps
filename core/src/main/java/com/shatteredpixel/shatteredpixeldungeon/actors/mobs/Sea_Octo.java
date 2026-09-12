//END(port from Arknights): Sea_Octo
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_SpewerSprite;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Sea_Octo
extends Mob {
    private boolean terrorSpawned;
    private static final String VAL = "firstTEEROR";

    public Sea_Octo() {
        this.spriteClass = Sea_SpewerSprite.class;
        this.HT = 125;
        this.HP = 125;
        this.defenseSkill = 12;
        this.EXP = 17;
        this.maxLvl = 36;
        this.loot = Generator.Category.SEED;
        this.lootChance = 0.3f;
        this.properties.add(Char.Property.SEA);
        this.terrorSpawned = false;
    }

    @Override
    protected boolean act() {
        if (!this.terrorSpawned) {
            SeaTerror seaTerror = Dungeon.level.addSeaTerror(this.pos);
            seaTerror.activate();
            GameScene.updateMap(this.pos);
            this.terrorSpawned = true;
        }
        return super.act();
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (super.canAttack(enemy)) {
            return true;
        }
        if (this.buff(ExtendedRange.class) != null) {
            return this.fieldOfView[enemy.pos] && Dungeon.level.distance(this.pos, enemy.pos) <= 8;
        }
        return this.fieldOfView[enemy.pos] && Dungeon.level.distance(this.pos, enemy.pos) <= 1;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 42);
    }

    @Override
    public int attackSkill(Char target) {
        return 36;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 18);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        int ndamage = 8;
        if (Dungeon.isChallenged(1024)) {
            ndamage = 16;
        }
        if (Dungeon.depth == 39) {
            ndamage /= 2;
        }
        if (enemy instanceof Hero || enemy instanceof DriedRose.GhostHero) {
            Buff.affect(enemy, NervousImpairment.class).sum(ndamage);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(VAL, this.terrorSpawned);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.terrorSpawned = bundle.getBoolean(VAL);
    }

    @Override
    public void activateSeaTerror() {
        if (this.buff(ExtendedRange.class) == null) {
            Buff.affect(this, ExtendedRange.class);
        }
    }

    public static class ExtendedRange
    extends Buff {
        public ExtendedRange() {
            this.type = Buff.buffType.POSITIVE;
            this.announced = false;
        }

        @Override
        public boolean act() {
            if (Dungeon.level.seaTerrors.get(this.target.pos) == null) {
                this.detach();
            } else {
                this.spend(1.0f);
            }
            return true;
        }

        @Override
        public int icon() {
            return 45;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.25f, 1.5f, 1.0f);
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
    }
}
