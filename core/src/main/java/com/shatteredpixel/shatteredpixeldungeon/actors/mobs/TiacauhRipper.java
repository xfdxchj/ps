//END(port from Arknights): TiacauhRipper
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_RipperSprite;
import com.watabou.utils.Random;

public class TiacauhRipper
extends Mob {
    public TiacauhRipper() {
        this.spriteClass = Tiacauh_RipperSprite.class;
        this.HT = 65;
        this.HP = 65;
        this.defenseSkill = 38;
        this.EXP = 16;
        this.maxLvl = 34;
        this.immunities.add(Silence.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(24, 38);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 0.4f;
    }

    @Override
    public int attackSkill(Char target) {
        return 38;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        int dmgbouns = enemy.drRoll() / 4;
        dmgbouns = Math.min(dmgbouns, 9);
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
