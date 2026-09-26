/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArmorEnhance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WeaponEnhance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class UpgradeDust
extends Spell {
    public UpgradeDust() {
        this.image = ItemSpriteSheet.UPGRADE_DUST;
        this.talentChance = 0.33333334f;
    }

    @Override
    protected void onCast(Hero hero) {
        Buff.affect(hero, WeaponEnhance.class).set(1 + hero.lvl / 10, 20);
        Buff.affect(hero, ArmorEnhance.class).set(1 + hero.lvl / 10, 20);
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play("sounds/evoke.mp3");
        CellEmitter.center(hero.pos).burst(Speck.factory(1), 7);
        GLog.p(Messages.get(this, "empower", new Object[0]), new Object[0]);
        this.detach(UpgradeDust.curUser.belongings.backpack);
        UpgradeDust.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(1.0f);
    }

    @Override
    public int value() {
        return Math.round(23.333334f);
    }

    @Override
    public int energyVal() {
        return (int)(8.0f * ((float)this.quantity / 3.0f));
    }

    public static class Recipe
    extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
        private static final int OUT_QUANTITY = 3;

        public Recipe() {
            this.inputs = new Class[]{StoneOfAugmentation.class, LiquidMetal.class};
            this.inQuantity = new int[]{1, 20};
            this.cost = 3;
            this.output = UpgradeDust.class;
            this.outQuantity = 3;
        }
    }
}

