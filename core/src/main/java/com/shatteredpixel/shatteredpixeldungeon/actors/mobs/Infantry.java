//END(port from Arknights): Infantry
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ceylon;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Siesta_InfantrySprite;
import com.watabou.utils.Random;

public class Infantry
extends Mob {
    public Infantry() {
        this.spriteClass = Siesta_InfantrySprite.class;
        this.HT = 125;
        this.HP = 125;
        this.defenseSkill = 25;
        this.EXP = 15;
        this.maxLvl = 28;
        this.loot = Gold.class;
        this.lootChance = 0.28f;
        this.immunities.add(Silence.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(33, 45);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 20);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (this.HP == this.HT) {
            dmg = Math.min(this.HT - 1, dmg);
        }
        super.damage(dmg, src);
    }

    @Override
    public void rollToDropLoot() {
        Ceylon.Quest.process(this);
        super.rollToDropLoot();
    }
}
