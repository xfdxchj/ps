/*
 * 破碎的地牢 (End fork) — 后缀卷轴。
 *
 * 炼金：升级卷轴 + 任意符石 + 邪能碎片 -> 后缀卷轴。
 * 使用时选择一把武器，随机附加 1 个后缀技能。
 * 若该武器已有后缀，会先询问是否替换。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.WeaponSuffix;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class SuffixScroll extends Scroll {

	{
		image = ItemSpriteSheet.SCROLL_KAUNAN; //临时图标
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "后缀卷轴"; }
	@Override public String info(){
		return "阅读并选择一把武器，随机附加 1 个后缀技能。"
				+ "若该武器已有后缀，会询问是否替换。\n\n炼金：**升级卷轴 + 任意符石 + 邪能碎片**。";
	}
	@Override public String desc(){ return info(); }
	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public void doRead(){
		GameScene.selectItem( itemSelector );
	}

	/** END: 真正把随机后缀写到武器上（含替换）。 */
	private void applySuffix( Weapon w ){
		if (w == null) return;

		detach( curUser.belongings.backpack );
		curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );
		Sample.INSTANCE.play( Assets.Sounds.READ );

		WeaponSuffix s = WeaponSuffix.random();
		boolean replace = w.suffix != null;
		w.suffix = s.name();

		if (replace){
			GLog.i( "后缀已替换为：" + s.label );
		} else {
			GLog.i( "武器获得后缀：" + s.label );
		}

		readAnimation();
		identify();
		Item.updateQuickslot();
	}

	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt(){
			return "选择要附加后缀的武器：";
		}

		@Override
		public boolean itemSelectable( Item item ){
			return item instanceof Weapon && item.isIdentified() && !item.cursed;
		}

		@Override
		public void onSelect( Item item ){
			if (!(item instanceof Weapon)) return;
			final Weapon w = (Weapon) item;

			if (w.suffix != null){
				WeaponSuffix old = WeaponSuffix.byName( w.suffix );
				GameScene.show( new WndOptions(
						new ItemSprite( new SuffixScroll() ),
						"替换后缀",
						"该武器已有后缀「" + (old == null ? w.suffix : old.label) + "」，\n要替换成新的随机后缀吗？",
						"替换", "取消" ) {
					@Override
					protected void onSelect( int index ){
						if (index == 0) applySuffix( w );
					}
				} );
			} else {
				applySuffix( w );
			}
		}
	};

	/** 炼金：升级卷轴 + 任意符石 + 邪能碎片。 */
	public static class Recipe3 extends Recipe {
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.suffixEnabled()) return false;
			if (ingredients.size() != 3) return false;
			boolean scroll = false, stone = false, shard = false;
			for (Item it : ingredients){
				if (it instanceof ScrollOfUpgrade) scroll = true;
				else if (it instanceof Runestone) stone = true;
				else if (it instanceof MetalShard) shard = true;
				else return false;
			}
			return scroll && stone && shard;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) { return 0; }

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			for (Item i : ingredients) i.quantity(i.quantity() - 1);
			return new SuffixScroll();
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new SuffixScroll();
		}
	}
}
