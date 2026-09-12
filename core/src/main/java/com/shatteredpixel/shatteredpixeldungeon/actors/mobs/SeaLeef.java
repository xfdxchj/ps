//END(port from Arknights): SeaLeef
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_LeefSprite;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SeaLeef
extends Mob {
    public SeaLeef() {
        this.spriteClass = Sea_LeefSprite.class;
        this.HT = 135;
        this.HP = 135;
        this.EXP = 18;
        this.maxLvl = 37;
        this.defenseSkill = 15;
        this.loot = Gold.class;
        this.lootChance = 0.28f;
        this.properties.add(Char.Property.SEA);
    }

    @Override
    public int damageRoll() {
        int bonus = 0;
        DamageRampUp ramp = this.buff(DamageRampUp.class);
        if (ramp != null) {
            bonus = ramp.getBonus();
        }
        return Random.NormalIntRange(16 + bonus / 2, 24 + bonus);
    }

    @Override
    public int attackSkill(Char target) {
        return 37;
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 0.5f;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 10);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        Buff.affect(this, DamageRampUp.class).addBonus();
        return super.attackProc(enemy, damage);
    }

    @Override
    public void activateSeaTerror() {
        if (this.buff(Camouflage.class) == null) {
            Buff.affect(this, Camouflage.class, 1.0f);
        } else if (this.buff(Camouflage.class) != null) {
            Buff.prolong(this, Camouflage.class, 1.0f);
        }
    }

    public static class DamageRampUp
    extends Buff {
        private int bonus = 0;
        private int turnsWithoutAttack = 0;
        private static final String BONUS = "bonus";
        private static final String TURNS_NO_ATK = "turnsWithoutAttack";

        public DamageRampUp() {
            this.type = Buff.buffType.POSITIVE;
            this.announced = true;
        }

        public void addBonus() {
            this.bonus = Math.min(this.bonus + 3, 60);
            this.turnsWithoutAttack = 0;
        }

        public int getBonus() {
            return this.bonus;
        }

        private void removeBonus() {
            this.bonus -= 6;
            if (this.bonus <= 0) {
                this.detach();
            } else {
                this.spend(1.0f);
            }
        }

        @Override
        public boolean act() {
            ++this.turnsWithoutAttack;
            if (this.turnsWithoutAttack >= 3) {
                this.removeBonus();
            } else {
                this.spend(1.0f);
            }
            return true;
        }

        @Override
        public int icon() {
            return 50;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.2f, 1.5f, 0.5f);
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", this.bonus);
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BONUS, this.bonus);
            bundle.put(TURNS_NO_ATK, this.turnsWithoutAttack);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            this.bonus = bundle.getInt(BONUS);
            this.turnsWithoutAttack = bundle.getInt(TURNS_NO_ATK);
        }
    }
}
