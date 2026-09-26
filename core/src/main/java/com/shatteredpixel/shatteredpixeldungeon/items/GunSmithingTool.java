/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.Locale;

/**
 * END(ReReARPD 枪械移植): 枪械改造工具。
 * 对一把枪使用后可改 6 类部件：枪管/弹匣/子弹/重量/附件/附魔。
 */
public class GunSmithingTool extends Item {

	public static final String AC_USE = "USE";

	protected Class<? extends Bag> preferredBag = Belongings.Backpack.class;

	public GunSmithingTool() {
		image = ItemSpriteSheet.GUNSMITHING_TOOL;
		defaultAction = AC_USE;
		stackable = true;
		unique = true;
		bones = false;
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
			GameScene.selectItem(new WndBag.ItemSelector() {
				@Override
				public String textPrompt() {
					return Messages.get(GunSmithingTool.class, "inv_title");
				}

				@Override
				public Class<? extends Bag> preferredBag() {
					return preferredBag;
				}

				@Override
				public boolean itemSelectable(Item item) {
					return item instanceof Gun;
				}

				@Override
				public void onSelect(Item item) {
					if (!(Item.curItem instanceof GunSmithingTool)) return;
					if (item instanceof Gun) {
						GameScene.show(new WndModSelect((Gun)item));
					}
				}
			});
		}
	}

	protected static void onItemSelected() {
		curUser.spend(1f);
		curUser.busy();
		curUser.sprite.operate(curUser.pos);
		Sample.INSTANCE.play("sounds/evoke.mp3");
		CellEmitter.center(curUser.pos).burst(Speck.factory(1), 7);
		Invisibility.dispel();
		updateQuickslot();
		Catalog.countUse(GunSmithingTool.class);
		GunSmithingTool tool = Dungeon.hero.belongings.getItem(GunSmithingTool.class);
		if (tool != null) {
			tool.detach(Dungeon.hero.belongings.backpack);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 50 * quantity;
	}

	public static class ToolRecipe extends Recipe.SimpleRecipe {
		public ToolRecipe() {
			inputs = new Class[]{StoneOfAugmentation.class, LiquidMetal.class};
			inQuantity = new int[]{1, 20};
			cost = 3;
			output = GunSmithingTool.class;
			outQuantity = 1;
		}
	}

	public static class WndMod extends Window {

		private static final int WIDTH_P = 120;
		private static final int WIDTH_L = 144;
		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 20;
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		public WndMod(final Gun toMod, String key) {
			IconTitle titlebar = new IconTitle(toMod);
			titlebar.setRect(0, 0, width, 0);
			add(titlebar);
			RenderedTextBlock tfMessage = PixelScene.renderTextBlock(
					Messages.get(GunSmithingTool.class, key + "_desc"), 6);
			tfMessage.maxWidth(width - 4);
			tfMessage.setPos(2, titlebar.bottom() + 2);
			add(tfMessage);
			float pos = tfMessage.top() + tfMessage.height();

			switch (key) {
				case "barrel":
					for (final Gun.BarrelMod mod : Gun.BarrelMod.values()) {
						if (toMod.barrelMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.barrelMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "magazine":
					for (final Gun.MagazineMod mod : Gun.MagazineMod.values()) {
						if (toMod.magazineMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.magazineMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "bullet":
					for (final Gun.BulletMod mod : Gun.BulletMod.values()) {
						if (toMod.bulletMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.bulletMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "weight":
					for (final Gun.WeightMod mod : Gun.WeightMod.values()) {
						if (toMod.weightMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.weightMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "attach":
					for (final Gun.AttachMod mod : Gun.AttachMod.values()) {
						if (toMod.attachMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.attachMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "enchant":
					for (final Gun.EnchantMod mod : Gun.EnchantMod.values()) {
						if (toMod.enchantMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.enchantMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
				case "inscribe":
					for (final Gun.InscribeMod mod : Gun.InscribeMod.values()) {
						if (toMod.inscribeMod == mod) continue;
						RedButton btn = new RedButton(Messages.get(WndMod.class, mod.name().toLowerCase(Locale.ENGLISH))) {
							@Override
							protected void onClick() {
								hide();
								toMod.inscribeMod = mod;
								onItemSelected();
							}
						};
						btn.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
						add(btn);
						pos = btn.bottom();
					}
					break;
			}

			RedButton btnCancel = new RedButton(Messages.get(GunSmithingTool.class, "cancel")) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setRect(2, pos + 2, width - 4, BUTTON_HEIGHT);
			add(btnCancel);
			resize(width, (int)btnCancel.bottom() + 2);
		}
	}

	public static class WndModSelect extends WndOptions {

		private static Gun gun;
		private static final String[] mods = {"barrel", "magazine", "bullet", "weight", "attach", "enchant", "inscribe"};

		public WndModSelect(Gun gun) {
			super(new ItemSprite(new GunSmithingTool()),
					Messages.titleCase(new GunSmithingTool().name()),
					Messages.get(GunSmithingTool.class, "mod_select"),
					Messages.get(GunSmithingTool.class, mods[0]),
					Messages.get(GunSmithingTool.class, mods[1]),
					Messages.get(GunSmithingTool.class, mods[2]),
					Messages.get(GunSmithingTool.class, mods[3]),
					Messages.get(GunSmithingTool.class, mods[4]),
					Messages.get(GunSmithingTool.class, mods[5]),
					Messages.get(GunSmithingTool.class, mods[6]),
					Messages.get(GunSmithingTool.class, "cancel"));
			WndModSelect.gun = gun;
		}

		@Override
		protected void onSelect(int index) {
			if (index < mods.length) {
				GameScene.show(new WndMod(gun, mods[index]));
			} else {
				hide();
			}
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < mods.length;
		}

		@Override
		protected void onInfo(int index) {
			GameScene.show(new WndTitledMessage(Icons.get(Icons.INFO),
					Messages.titleCase(Messages.get(GunSmithingTool.class, mods[index])),
					Messages.get(GunSmithingTool.class, mods[index] + "_desc")));
		}
	}
}
