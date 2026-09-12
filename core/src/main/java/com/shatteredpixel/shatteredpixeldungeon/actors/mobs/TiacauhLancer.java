//END(port from Arknights): TiacauhLancer
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_lancerSprite;
import com.watabou.utils.Random;

public class TiacauhLancer
extends Mob {
    public TiacauhLancer() {
        this.spriteClass = Tiacauh_lancerSprite.class;
        this.HT = 80;
        this.HP = 80;
        this.defenseSkill = 13;
        this.EXP = 14;
        this.maxLvl = 30;
        this.immunities.add(Silence.class);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (super.canAttack(enemy)) {
            return true;
        }
        return this.fieldOfView[enemy.pos] && Dungeon.level.distance(this.pos, enemy.pos) <= 3;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(30, 38);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 0.5f;
    }

    @Override
    public int attackSkill(Char target) {
        return 36;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }
}
