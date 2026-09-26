/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.InventorySpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Evolution
extends InventorySpell {
    public Evolution() {
        this.image = ItemSpriteSheet.EVOLUTION;
        this.unique = true;
    }

    @Override
    protected boolean usableOnItem(Item item) {
        return item instanceof MeleeWeapon;
    }

    @Override
    protected void onItemSelected(Item item) {
        Item result = Evolution.changeItem(item);
        if (result == null) {
            GLog.n(Messages.get(this, "nothing", new Object[0]), new Object[0]);
            curItem.collect(Evolution.curUser.belongings.backpack);
        } else {
            if (result != item) {
                int slot = Dungeon.quickslot.getSlot(item);
                if (item.isEquipped(Dungeon.hero)) {
                    item.cursed = false;
                    if (item instanceof Artifact && result instanceof Ring) {
                        ((EquipableItem)item).doUnequip(Dungeon.hero, false);
                        if (!result.collect()) {
                            Dungeon.level.drop((Item)result, (int)Evolution.curUser.pos).sprite.drop();
                        }
                    } else if (item instanceof KindOfWeapon && Dungeon.hero.belongings.secondWep() == item) {
                        ((EquipableItem)item).doUnequip(Dungeon.hero, false);
                        ((KindOfWeapon)result).equipSecondary(Dungeon.hero);
                    } else {
                        ((EquipableItem)item).doUnequip(Dungeon.hero, false);
                        ((EquipableItem)result).doEquip(Dungeon.hero);
                    }
                    Dungeon.hero.spend(-Dungeon.hero.cooldown());
                } else {
                    item.detach(Dungeon.hero.belongings.backpack);
                    if (!result.collect()) {
                        Dungeon.level.drop((Item)result, (int)Evolution.curUser.pos).sprite.drop();
                    } else if (Dungeon.hero.belongings.getSimilar(result) != null) {
                        result = Dungeon.hero.belongings.getSimilar(result);
                    }
                }
                if (slot != -1 && result.defaultAction() != null && !Dungeon.quickslot.isNonePlaceholder(slot).booleanValue() && Dungeon.hero.belongings.contains(result)) {
                    Dungeon.quickslot.setSlot(slot, result);
                }
            }
            if (result.isIdentified()) {
                Catalog.setSeen(result.getClass());
            }
            Transmuting.show((Char)curUser, item, result);
            Evolution.curUser.sprite.emitter().start(Speck.factory(10), 0.2f, 10);
            GLog.p(Messages.get(this, "evolve", new Object[0]), new Object[0]);
        }
    }

    public static Item changeItem(Item item) {
        if (item instanceof MeleeWeapon) {
            return Evolution.changeWeapon((Weapon)item);
        }
        return null;
    }

    private static Weapon changeWeapon(Weapon w) {
        Weapon n;
        Generator.Category c = Random.Float() < 0.25f ? (((MeleeWeapon)w).tier >= 4 ? Generator.wepTiers[4] : Generator.wepTiers[((MeleeWeapon)w).tier]) : (((MeleeWeapon)w).tier >= 5 ? Generator.wepTiers[4] : Generator.wepTiers[((MeleeWeapon)w).tier - 1]);
        while (Challenges.isItemBlocked(n = (Weapon)Reflection.newInstance(c.classes[Random.chances(c.probs)])) || n.getClass() == w.getClass()) {
        }
        n.level(0);
        n.quantity(1);
        int level = w.trueLevel();
        if (level > 0) {
            n.upgrade(level);
        } else if (level < 0) {
            n.degrade(-level);
        }
        n.enchantment = w.enchantment;
        n.curseInfusionBonus = w.curseInfusionBonus;
        n.masteryPotionBonus = w.masteryPotionBonus;
        n.levelKnown = w.levelKnown;
        n.cursedKnown = w.cursedKnown;
        n.cursed = w.cursed;
        n.augment = w.augment;
        return n;
    }

    @Override
    public int value() {
        return Math.round(90.0f * ((float)this.quantity / 1.0f));
    }

    @Override
    public int energyVal() {
        return (int)(22.0f * ((float)this.quantity / 1.0f));
    }

    public static class Recipe
    extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
        private static final int OUT_QUANTITY = 1;

        public Recipe() {
            this.inputs = new Class[]{ScrollOfTransmutation.class, UnstableSpell.class};
            this.inQuantity = new int[]{1, 1};
            this.cost = 4;
            this.output = Evolution.class;
            this.outQuantity = 1;
        }
    }
}

