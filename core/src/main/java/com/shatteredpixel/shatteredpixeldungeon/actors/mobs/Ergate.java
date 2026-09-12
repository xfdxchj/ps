//END(port from Arknights): Ergate
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ceylon;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ErgateSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Ergate
extends Mob {
    public Item item;

    public Ergate() {
        this.spriteClass = ErgateSprite.class;
        this.HT = 75;
        this.HP = 75;
        this.defenseSkill = 40;
        this.EXP = 15;
        this.maxLvl = 29;
        this.state = this.WANDERING;
        this.loot = new MysteryMeat();
        this.lootChance = 0.2f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(24, 35);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * 0.5f;
    }

    @Override
    public Item createLoot() {
        Item loot;
        switch (Random.Int(3)) {
            default: {
                int ofs;
                loot = new Dewdrop();
                while (Dungeon.level.solid[this.pos + (ofs = PathFinder.NEIGHBOURS8[Random.Int(8)])] && !Dungeon.level.passable[this.pos + ofs]) {
                }
                if (Dungeon.level.heaps.get(this.pos + ofs) == null) {
                    Dungeon.level.drop((Item)new Dewdrop(), (int)(this.pos + ofs)).sprite.drop(this.pos);
                    break;
                }
                Dungeon.level.drop((Item)new Dewdrop(), (int)(this.pos + ofs)).sprite.drop(this.pos + ofs);
                break;
            }
            case 2: {
                loot = Generator.random(Generator.Category.SEED);
            }
        }
        return loot;
    }

    @Override
    public int attackSkill(Char target) {
        return 35;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 6);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        damage = super.attackProc(enemy, damage);
        if (this.buff(Silence.class) == null && this.alignment == Char.Alignment.ENEMY && this.item == null && enemy instanceof Hero && Random.Int(4) < 1) {
            Hero hero = (Hero)enemy;
            KindOfWeapon weapon = hero.belongings.weapon;
            if (weapon != null && !weapon.cursed) {
                hero.belongings.weapon = null;
                Dungeon.level.drop((Item)weapon, (int)hero.pos).sprite.drop();
                GLog.w(Messages.get(this, "disarm", weapon.name()));
                Buff.affect(this, Terror.class, 20.0f);
            }
        }
        return damage;
    }

    @Override
    public void rollToDropLoot() {
        Ceylon.Quest.process(this);
        super.rollToDropLoot();
    }
}
