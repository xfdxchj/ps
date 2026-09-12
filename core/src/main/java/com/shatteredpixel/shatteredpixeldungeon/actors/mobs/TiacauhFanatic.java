//END(port from Arknights): TiacauhFanatic
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_fanaticSprite;
import com.watabou.utils.Random;

public class TiacauhFanatic
extends Mob {
    public TiacauhFanatic() {
        this.spriteClass = Tiacauh_fanaticSprite.class;
        this.HT = 55;
        this.HP = 55;
        this.defenseSkill = 34;
        this.EXP = 14;
        this.maxLvl = 29;
        this.loot = Gold.class;
        this.lootChance = 0.35f;
        this.immunities.add(Silence.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 32);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 0.4f;
    }

    @Override
    public int attackSkill(Char target) {
        return 36;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 14);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        int dmgbouns = enemy.drRoll() / 4;
        dmgbouns = Math.min(dmgbouns, 8);
        return super.attackProc(enemy, damage += dmgbouns);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }
}
