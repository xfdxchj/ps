//END(port from Arknights): SeaRunner
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_RunnerSprite;
import com.watabou.utils.Random;

public class SeaRunner
extends Mob {
    public SeaRunner() {
        this.spriteClass = Sea_RunnerSprite.class;
        this.HT = 90;
        this.HP = 90;
        this.EXP = 13;
        this.maxLvl = 29;
        this.defenseSkill = 18;
        this.loot = new MysteryMeat();
        this.lootChance = 0.12f;
        this.loot = new SanityPotion();
        this.lootChance = 0.1f;
        this.properties.add(Char.Property.SEA);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(26, 42);
    }

    @Override
    public int attackSkill(Char target) {
        return 33;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 10);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (Random.Int(3) == 0) {
            Buff.affect(enemy, Hex.class, 3.0f);
        } else if (Random.Int(2) == 0) {
            Buff.prolong(enemy, Chill.class, 3.0f);
        }
        return super.attackProc(enemy, damage);
    }
}
