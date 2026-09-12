//END(port from Arknights): Sniper
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Siesta_SniperSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Sniper
extends Mob {
    private int charge;
    private static final String SKILLCD = "charge";

    public Sniper() {
        this.spriteClass = Siesta_SniperSprite.class;
        this.HT = 120;
        this.HP = 120;
        this.defenseSkill = 22;
        this.EXP = 16;
        this.maxLvl = 30;
        this.loot = new PotionOfHealing();
        this.lootChance = 0.1f;
        this.immunities.add(Silence.class);
        this.charge = 0;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(32, 48);
    }

    @Override
    public int attackSkill(Char target) {
        return 44;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 12);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        Ballistica attack = new Ballistica(this.pos, enemy.pos, 7);
        return !Dungeon.level.adjacent(this.pos, enemy.pos) && attack.collisionPos == enemy.pos;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (this.charge >= 2) {
            Buff.affect(enemy, Paralysis.class, 1.0f);
            this.charge = 0;
        } else {
            damage = super.attackProc(enemy, damage);
            ++this.charge;
        }
        return damage;
    }

    @Override
    public void move(int step, boolean travelling) {
        this.charge = 0;
        super.move(step, travelling);
    }

    @Override
    protected boolean getCloser(int target) {
        if (this.state == this.HUNTING) {
            return this.enemySeen && this.getFurther(target);
        }
        return super.getCloser(target);
    }

    @Override
    public void rollToDropLoot() {
        this.lootChance *= (5.0f - (float)Dungeon.LimitedDrops.SNIPER_HP.count) / 5.0f;
        super.rollToDropLoot();
    }

    @Override
    public Item createLoot() {
        ++Dungeon.LimitedDrops.SNIPER_HP.count;
        return super.createLoot();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SKILLCD, this.charge);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.charge = bundle.getInt(SKILLCD);
    }
}
