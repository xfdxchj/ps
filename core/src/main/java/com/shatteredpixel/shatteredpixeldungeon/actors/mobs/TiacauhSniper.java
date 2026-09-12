//END(port from Arknights): TiacauhSniper
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhLancer;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_ImpalerSprite;
import com.watabou.utils.Random;

public class TiacauhSniper
extends TiacauhLancer {
    public TiacauhSniper() {
        this.spriteClass = Tiacauh_ImpalerSprite.class;
        this.HT = 105;
        this.HP = 105;
        this.defenseSkill = 16;
        this.EXP = 17;
        this.maxLvl = 30;
        this.immunities.add(Silence.class);
        this.loot = new PotionOfHealing();
        this.lootChance = 1.0f;
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (super.canAttack(enemy)) {
            return true;
        }
        return this.fieldOfView[enemy.pos] && Dungeon.level.distance(this.pos, enemy.pos) <= 4;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(30, 42);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }
}
