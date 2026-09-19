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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Shopkeeper extends NPC {

	{
		spriteClass = ShopkeeperSprite.class;

		properties.add(Property.IMMOVABLE);
	}

	public static int MAX_BUYBACK_HISTORY = 3;
	public ArrayList<Item> buybackItems = new ArrayList<>();

	private int turnsSinceHarmed = -1;

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.SHOP;
	}

	@Override
	protected boolean act() {

		if (turnsSinceHarmed >= 0){
			turnsSinceHarmed ++;
		}

		sprite.turnTo( pos, Dungeon.hero.pos );
		spend( TICK );
		return super.act();
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		processHarm();
	}
	
	@Override
	public boolean add( Buff buff ) {
		if (buff.type == Buff.buffType.NEGATIVE){
			processHarm();
		}
		return false;
	}

	public void processHarm(){

		//do nothing if the shopkeeper is out of the hero's FOV
		if (!Dungeon.level.heroFOV[pos]){
			return;
		}

		if (turnsSinceHarmed == -1){
			turnsSinceHarmed = 0;
			yell(Messages.get(this, "warn"));

			//use a new actor as we can't clear the gas while we're in the middle of processing it
			Actor.add(new Actor() {
				{
					actPriority = VFX_PRIO;
				}

				@Override
				protected boolean act() {
					//cleanses all harmful blobs in the shop
					ArrayList<Blob> blobs = new ArrayList<>();
					for (Class c : new BlobImmunity().immunities()){
						Blob b = Dungeon.level.blobs.get(c);
						if (b != null && b.volume > 0){
							blobs.add(b);
						}
					}

					PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 4 );

					for (int i=0; i < Dungeon.level.length(); i++) {
						if (PathFinder.distance[i] < Integer.MAX_VALUE) {

							boolean affected = false;
							for (Blob blob : blobs) {
								if (blob.cur[i] > 0) {
									blob.clear(i);
									affected = true;
								}
							}

							if (affected && Dungeon.level.heroFOV[i]) {
								CellEmitter.get( i ).burst( Speck.factory( Speck.DISCOVER ), 2 );
							}

						}
					}
					Actor.remove(this);
					return true;
				}
			});

		//There is a 1 turn buffer before more damage/debuffs make the shopkeeper flee
		//This is mainly to prevent stacked effects from causing an instant flee
		} else if (turnsSinceHarmed >= 1) {
			flee();
		}
	}
	
	public void flee() {
		destroy();

		Notes.remove( landmark() );
		GLog.newLine();
		GLog.n(Messages.get(this, "flee"));

		if (sprite != null) {
			sprite.killAndErase();
			CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		for (Heap heap: Dungeon.level.heaps.valueList()) {
			if (heap.type == Heap.Type.FOR_SALE) {
				if (ShatteredPixelDungeon.scene() instanceof GameScene) {
					CellEmitter.get(heap.pos).burst(ElmoParticle.FACTORY, 4);
				}
				if (heap.size() == 1) {
					heap.destroy();
				} else {
					heap.items.remove(heap.size()-1);
					heap.type = Heap.Type.HEAP;
				}
			}
		}
	}
	
	@Override
	public boolean reset() {
		return true;
	}

	//shopkeepers are greedy!
	public static int sellPrice(Item item){
		int p = item.value() * 5 * (Dungeon.effectiveDepth() / 5 + 1);
		//挑战·通货膨胀:商店售价 +50%
		if (Dungeon.isChallenged(com.shatteredpixel.shatteredpixeldungeon.Challenges.INFLATION)) p = (int)Math.ceil(p * 1.5f);
		//END(挑战 44 慷慨商人): 商店售价 ×1.25。
		//与 32 通货膨胀**可共存**（原表注明"效果重叠，可共存"），
		//叠加时价格为 1.5 × 1.25 = 1.875 倍，这是预期行为。
		float merchantMult = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.merchantPriceMultiplier();
		if (merchantMult != 1f) p = (int)Math.ceil(p * merchantMult);

		//==== END(挑战 88 拍卖行): 价格波动 + NPC 抬价 ====
		//原表："商店物品可竞价，价格波动，可低价买入或被 NPC 抬价"
		//
		//简化版：每件商品在**首次查询时**定格一个 0.5~1.8 的倍率
		//（之后不变，避免"看到的价格与结算价格不一致"），
		//外加本层可能的 35% 概率全场抬价 ×1.5。
		//未勾选 88 时原样返回。
		p = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.auctionPrice(item, p);
		return p;
	}
	
	public static WndBag sell() {
		return GameScene.selectItem( itemSelector );
	}

	public static boolean canSell(Item item){
		if (item.value() <= 0)                                              return false;
		if (item.unique && !item.stackable)                                 return false;
		if (item instanceof Armor && ((Armor) item).checkSeal() != null)    return false;
		if (item.isEquipped(Dungeon.hero) && item.cursed)                   return false;
		return true;
	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(Shopkeeper.class, "sell");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return Shopkeeper.canSell(item);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null && Dungeon.hero != null && Dungeon.hero.isAlive()) {
				WndBag parentWnd = sell();
				GameScene.show( new WndTradeItem( item, parentWnd ) );
			}
		}
	};

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero) {
			return true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				//==== END(挑战 40 贷款): 商店加"贷款"选项 ====
				//只有在勾选 40 时才显示，未勾选时选项数量与原来完全一致。
				final boolean loan = com.shatteredpixel.shatteredpixeldungeon.endcontent
						.challenge.ChallengeEffects.loanAvailable();

				int baseCount = loan ? 3 : 2;      // sell / talk / (loan)
				String[] options = new String[baseCount + buybackItems.size()];
				int maxLen = PixelScene.landscape() ? 30 : 25;
				int i = 0;
				options[i++] = Messages.get(Shopkeeper.this, "sell");
				options[i++] = Messages.get(Shopkeeper.this, "talk");
				if (loan) {
					options[i++] = Messages.get(Shopkeeper.this, "challenge_loan");
				}
				//buyback 段起始索引 —— 后面用它做判断，避免硬编码
				final int buybackStart = i;
				for (Item item : buybackItems){
					options[i] = Messages.get(Heap.class, "for_sale", item.value(), Messages.titleCase(item.title()));
					if (options[i].length() > maxLen) options[i] = options[i].substring(0, maxLen-3) + "...";
					i++;
				}
				CurrencyIndicator.showGold = true;
				GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()), description(), options){
					@Override
					protected void onSelect(int index) {
						super.onSelect(index);
						if (index == 0){
							sell();
						} else if (index == 1){
							GameScene.show(new WndTitledMessage(sprite(), Messages.titleCase(name()), chatText()));
						} else if (loan && index == 2){
							//==== END(挑战 40 贷款): 弹出金额选择 ====
							showLoanWindow();
						} else if (index >= buybackStart){
							GLog.i(Messages.get(Shopkeeper.this, "buyback"));
							Item returned = buybackItems.remove(index - buybackStart);
							Dungeon.gold -= returned.value();
							Statistics.goldCollected -= returned.value();
							if (returned instanceof MissileWeapon && returned.isUpgradable()){
								Buff.affect(Dungeon.hero, MissileWeapon.UpgradedSetTracker.class).levelThresholds.put(((MissileWeapon) returned).setID, returned.level());
							}
							if (!returned.doPickUp(Dungeon.hero)){
								Dungeon.level.drop(returned, Dungeon.hero.pos);
							}
						}
					}

					@Override
					protected boolean enabled(int index) {
						if (index >= buybackStart){
							return Dungeon.gold >= buybackItems.get(index - buybackStart).value();
						} else if (loan && index == 2) {
							//已经欠着一笔时不能再借
							return com.shatteredpixel.shatteredpixeldungeon.endcontent
									.challenge.ChallengeEffects.canTakeLoan(Dungeon.hero);
						} else {
							return super.enabled(index);
						}
					}

					@Override
					protected boolean hasIcon(int index) {
						return index >= buybackStart;
					}

					@Override
					protected Image getIcon(int index) {
						if (index >= buybackStart){
							return new ItemSprite(buybackItems.get(index - buybackStart));
						}
						return null;
					}

					@Override
					public void hide() {
						super.hide();
						CurrencyIndicator.showGold = false;
					}
				});
			}
		});
		return true;
	}

	/**
	 * END(挑战 40 贷款): 让玩家选择贷款金额。
	 *
	 * <p>用现成的 {@link WndOptions} 而不是自造输入界面 ——
	 * 本 fork 没有通用的数字输入控件，而"自选金额"用几个档位
	 * 已经完全够用（100 / 300 / 500 / 1000）。
	 *
	 * <p>每个档位都标注了到期应还的金额，避免玩家借完才发现要还 110%。
	 */
	private void showLoanWindow() {

		final int[] amounts = com.shatteredpixel.shatteredpixeldungeon.endcontent
				.challenge.ChallengeEffects.LOAN_AMOUNTS;

		String[] options = new String[amounts.length];
		for (int i = 0; i < amounts.length; i++) {
			int owed = Math.round(amounts[i]
					* com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LoanDebt.REPAY_MULT);
			options[i] = Messages.get(this, "challenge_loan_option", amounts[i], owed);
		}

		GameScene.show(new WndOptions(sprite(),
				Messages.get(this, "challenge_loan_title"),
				Messages.get(this, "challenge_loan_desc",
						(int) com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.LoanDebt.REPAY_TURNS),
				options) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index < 0 || index >= amounts.length) return;

				int got = com.shatteredpixel.shatteredpixeldungeon.endcontent
						.challenge.ChallengeEffects.takeLoan(Dungeon.hero, amounts[index]);
				if (got > 0) {
					GLog.i(Messages.get(Shopkeeper.this, "challenge_loan_taken", got));
				}
			}
		});
	}

	public String chatText(){
		if (Dungeon.hero.buff(AscensionChallenge.class) != null){
			return Messages.get(this, "talk_ascent");
		}
		switch (Dungeon.depth){
			case 6: default:
				return Messages.get(this, "talk_prison_intro") + "\n\n" + Messages.get(this, "talk_prison_" + Dungeon.hero.heroClass.name());
			case 11:
				return Messages.get(this, "talk_caves");
			case 16:
				return Messages.get(this, "talk_city");
			case 20:
				return Messages.get(this, "talk_halls");
		}
	}

	public static String BUYBACK_ITEMS = "buyback_items";

	public static String TURNS_SINCE_HARMED = "turns_since_harmed";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BUYBACK_ITEMS, buybackItems);
		bundle.put(TURNS_SINCE_HARMED, turnsSinceHarmed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		buybackItems.clear();
		if (bundle.contains(BUYBACK_ITEMS)){
			for (Bundlable i : bundle.getCollection(BUYBACK_ITEMS)){
				buybackItems.add((Item) i);
			}
		}
		turnsSinceHarmed = bundle.contains(TURNS_SINCE_HARMED) ? bundle.getInt(TURNS_SINCE_HARMED) : -1;
	}
}
