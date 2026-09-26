/*
 * END(ReReARPD gun port): 枪械蓝图，改编自 ReReARPD 的 BluePrint，只保留枪械部分。
 * 炼金锅用 T5 枪 + 升级之尘 + 进化法术制作；对对应 T5 枪使用后变成 T6 战术型。
 */
package com.shatteredpixel.shatteredpixeldungeon.items.changer;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UpgradeDust;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.AR.AR_T5;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.GL.GL_T5;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.HG.HG_T5;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.RL.RL_T5;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.SR.SR_T5;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class GunBlueprint extends Item {

	private static final String AC_USE = "USE";
	private static final String NEW_WEAPON = "newWeapon";

	private Class<? extends Gun> newWeapon;
	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(GunBlueprint.class, "inv_title");
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Gun;
		}

		@Override
		public void onSelect(Item item) {
			if (!(curItem instanceof GunBlueprint)) return;
			if (item instanceof Gun) onItemSelected(item);
		}
	};

	public GunBlueprint(Class<? extends Gun> wep) {
		image = ItemSpriteSheet.BLUEPRINT;
		defaultAction = AC_USE;
		stackable = false;
		levelKnown = true;
		unique = true;
		bones = false;
		newWeapon = wep;
	}

	public GunBlueprint() {
		this(null);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(NEW_WEAPON, newWeapon == null ? "" : newWeapon.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		String name = bundle.getString(NEW_WEAPON);
		if (!name.isEmpty()) {
			try {
				newWeapon = (Class<? extends Gun>)Class.forName(name);
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_USE)) {
			GameScene.selectItem(itemSelector);
		}
	}

	private void onItemSelected(Item item) {
		if (!(item instanceof Gun) || newWeapon == null) {
			GLog.n(Messages.get(this, "nothing"));
			return;
		}
		Gun result = convert((Gun)item);

		if (result != item) {
			int slot = Dungeon.quickslot.getSlot(item);
			if (item.isEquipped(Dungeon.hero)) {
				item.cursed = false;
				if (Dungeon.hero.belongings.secondWep() == item) {
					((EquipableItem)item).doUnequip(Dungeon.hero, false);
					result.equipSecondary(Dungeon.hero);
				} else {
					((EquipableItem)item).doUnequip(Dungeon.hero, false);
					result.doEquip(Dungeon.hero);
				}
				Dungeon.hero.spend(-Dungeon.hero.cooldown());
			} else {
				item.detach(Dungeon.hero.belongings.backpack);
				if (!result.collect()) {
					Dungeon.level.drop(result, curUser.pos).sprite.drop();
				}
			}
			if (slot != -1 && result.defaultAction() != null
					&& !Dungeon.quickslot.isNonePlaceholder(slot)
					&& Dungeon.hero.belongings.contains(result)) {
				Dungeon.quickslot.setSlot(slot, result);
			}
		}
		Catalog.setSeen(result.getClass());
		Sample.INSTANCE.play("sounds/read.mp3");
		Dungeon.hero.spendAndNext(1f);
		Transmuting.show(curUser, item, result);
		curUser.sprite.emitter().start(Speck.factory(10), 0.2f, 10);
		GLog.p(Messages.get(this, "morph"));

		detach(Dungeon.hero.belongings.backpack);
		Catalog.countUse(getClass());
	}

	private Gun convert(Gun wep) {
		Gun result = Reflection.newInstance(newWeapon);
		result.level(0);
		result.quantity(1);
		int level = wep.trueLevel();
		if (level > 0) {
			result.upgrade(level);
		} else if (level < 0) {
			result.degrade(-level);
		}
		result.barrelMod = wep.barrelMod;
		result.magazineMod = wep.magazineMod;
		result.bulletMod = wep.bulletMod;
		result.weightMod = wep.weightMod;
		result.attachMod = wep.attachMod;
		result.enchantMod = wep.enchantMod;
		result.inscribeMod = wep.inscribeMod;
		result.enchantment = wep.enchantment;
		result.curseInfusionBonus = wep.curseInfusionBonus;
		result.masteryPotionBonus = wep.masteryPotionBonus;
		result.levelKnown = wep.levelKnown;
		result.cursedKnown = wep.cursedKnown;
		result.cursed = wep.cursed;
		result.augment = wep.augment;
		return result;
	}

	@Override
	public String desc() {
		if (newWeapon != null) {
			return super.desc() + "\n\n" + Messages.get(this, "item_desc", Messages.titleCase(Reflection.newInstance(newWeapon).name()));
		}
		return super.desc();
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return -1;
	}

	/** 炼金：T5 枪 + 升级之尘 + 进化法术 -> 枪械蓝图。 */
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		private static final Class<?>[][] MAP = new Class<?>[][]{
				{AR_T5.class, com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.AR_T6.class},
				{SR_T5.class, com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.SR_T6.class},
				{HG_T5.class, com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.HG_T6.class},
				{GL_T5.class, com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.GL_T6.class},
				{RL_T5.class, com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy.RL_T6.class}
		};

		private static Class<? extends Gun> match(ArrayList<Item> ingredients) {
			if (ingredients.size() != 3) return null;
			boolean dust = false;
			boolean evo = false;
			Class<?> gunCls = null;
			for (Item it : ingredients) {
				if (it instanceof UpgradeDust) dust = true;
				else if (it instanceof Evolution) evo = true;
				else if (it instanceof Gun) gunCls = it.getClass();
				else return null;
			}
			if (!dust || !evo || gunCls == null) return null;
			for (Class<?>[] pair : MAP) {
				if (pair[0] == gunCls) return (Class<? extends Gun>)pair[1];
			}
			return null;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return match(ingredients) != null;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 0;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			Class<? extends Gun> target = match(ingredients);
			if (target == null) return null;
			for (Item it : ingredients) {
				it.quantity(it.quantity() - 1);
			}
			return new GunBlueprint(target);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			Class<? extends Gun> target = match(ingredients);
			return target == null ? null : new GunBlueprint(target);
		}
	}
}
