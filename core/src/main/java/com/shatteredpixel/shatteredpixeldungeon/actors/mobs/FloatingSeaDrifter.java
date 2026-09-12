//END(port from Arknights): FloatingSeaDrifter
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dario;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
//END(暂缓): GunWeapon 未搬
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_DrifterSprite;
import com.watabou.utils.Random;

public class FloatingSeaDrifter
extends Mob {
    public FloatingSeaDrifter() {
        this.spriteClass = Sea_DrifterSprite.class;
        this.HT = 65;
        this.HP = 65;
        this.defenseSkill = 50;
        this.EXP = 14;
        this.maxLvl = 29;
        this.flying = true;
        this.loot = new SanityPotion();
        this.lootChance = 0.1f;
        this.properties.add(Char.Property.SEA);
        this.immunities.add(Paralysis.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(25, 33);
    }

    @Override
    public int attackSkill(Char target) {
        return 32;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 14);
    }

    @Override
    public int defenseSkill(Char enemy) {
        KindOfWeapon weapon;
        if (enemy instanceof Hero && (weapon = Dungeon.hero.belongings.attackingWeapon()) != null && (weapon instanceof MissileWeapon || false /*END(暂缓): 枪械未搬*/)) {
            return 0;
        }
        return super.defenseSkill(enemy);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (enemy.alignment == Char.Alignment.ALLY) {
            Buff.affect(enemy, NervousImpairment.class).sum(10.0f);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Dario.Quest.process();
    }

}
