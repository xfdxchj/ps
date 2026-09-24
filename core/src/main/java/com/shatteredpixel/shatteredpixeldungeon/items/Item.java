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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable {

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 1.0f;
	
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
	
	protected String defaultAction;
	public boolean usesTargeting;

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
	protected int quantity = 1;
	public boolean dropsDownHeap = false;
	
	private int level = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	// Unique items persist through revival
	public boolean unique = false;

	// These items are preserved even if the hero's inventory is lost via unblessed ankh
	// this is largely set by the resurrection window, items can override this to always be kept
	public boolean keptThoughLostInvent = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;

	public int customNoteID = -1;
	
	public static final Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<>();

		//==== END(挑战 56 装备绑定): 绑定的装备不能丢弃/投掷 ====
		//原表："装备获得后自动绑定，无法丢弃、出售或交换，通过使用驱邪可以取下"。
		//
		//"驱邪"= ScrollOfRemoveCurse，它会调 unbind()（见该类），
		//所以这里只要把 DROP / THROW 从可选操作里去掉即可，
		//不需要额外阻止出售（不能丢出的物品在商店里也不会被列出来）。
		if (isBound()) {
			return actions;      //空列表：玩家在背包里对它没有任何操作
		}

		//==== END(修复·160 氪金大佬没有入口) ====
		//文档所有者反馈："氪金大佬没有效果，无法升级。"
		//
		//根因：ChallengeEffects 里的 whaleUpgrade()/whaleCanUpgrade()
		//逻辑全都写好了，但**没有任何 UI 入口**去调用它 ——
		//玩家在背包里对物品只有"丢弃/投掷"两个选项，自然点了没反应。
		//
		//修法：这里给可升级的物品加一个"金币强化"动作。
		//只有勾选了 160 且金币够时才出现 —— 没勾这条规则时列表与原来完全一致。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.whaleCanUpgrade(this)) {
			actions.add( AC_WHALE );
		}

		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}

	//==== END(160 氪金大佬): 用金币强化的动作名 ====

	/** 背包动作：花金币强化（见 ChallengeEffects.whaleUpgrade）。 */
	public static final String AC_WHALE = "WHALE";

	//==== END(挑战 56 装备绑定) ====

	/** 该物品是否已被绑定（不可丢弃/投掷）。 */
	public boolean boundForChallenge = false;

	public boolean isBound() {
		return boundForChallenge;
	}

	/** 解绑（驱邪卷轴调用）。 */
	public void unbind() {
		boundForChallenge = false;
	}

	/**
	 * END(挑战 56 装备绑定): 该物品获得时是否应自动绑定。
	 *
	 * <p>只对**装备**生效（武器/护甲/戒指/法杖/神器），
	 * 消耗品与金币不绑定 —— 否则连药水都丢不掉了。
	 */
	public boolean shouldAutoBind() {
		return com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.equipmentBindingEnabled()
				&& isBindableEquipment();
	}

	/** 是否属于"会被绑定的装备类别"。 */
	private boolean isBindableEquipment() {
		return this instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon
			|| this instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
			|| this instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring
			|| this instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
			|| this instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts
					.Artifact;
	}

	public String actionName(String action, Hero hero){
		//==== END(修复·160 氪金大佬动作名显示 nofound) ====
		//文档所有者反馈："金大佬的额外升级 UI 是 nofound。"
		//根因：Messages 查表时会把 key 转小写，而 AC_WHALE 原值是 "WHALE_UPGRADE"，
		//拼出来是 ac_whale_upgrade，properties 里只有 ac_whale，查不到就显示 nofound。
		//改成 "WHALE"（与 AC_DROP/AC_THROW 命名一致），并把费用显示在按钮上。
		if (AC_WHALE.equals(action)) {
			return Messages.get(this, "ac_whale") + "  "
				+ com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.whaleUpgradeCost(this) + "G";
		}
		return Messages.get(this, "ac_" + action);
	}

	public final boolean doPickUp( Hero hero ) {
		return doPickUp( hero, hero.pos );
	}

	public boolean doPickUp(Hero hero, int pos) {
		//==== END(挑战 56 装备绑定): 获得时自动绑定 ====
		//放在 collect 之前：一旦进了背包就已经是"获得后"的状态，
		//这里先打标记，避免中间出现"能丢一下"的短暂窗口。
		if (shouldAutoBind()) {
			boundForChallenge = true;
		}

		if (collect( hero.belongings.backpack )) {
			
			GameScene.pickUp( this, pos );
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext( pickupDelay() );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		hero.spendAndNext(TIME_TO_DROP);
		int pos = hero.pos;

		//==== END(修复·丰饶刷物品) ====
		//文档所有者反馈："丰饶可以刷物品，原理扔地上有概率翻倍。"
		//
		//根因：Level.drop() 是所有掉落的公共入口，勾了 35 丰饶之后
		//连**玩家主动丢弃**的东西也会被追加一份 —— 丢 1 瓶捡回 2 瓶，无限刷。
		//
		//修法：走这条"玩家丢弃"路径时把抑制标记置位，
		//让 Level.drop() 跳过额外掉落那一段。用 try/finally 保证一定复位
		//（否则一次异常就会让后续所有掉落都不再吃丰饶）。
		try {
			com.shatteredpixel.shatteredpixeldungeon.levels.Level.suppressDropBonus = true;
			Dungeon.level.drop(detachAll(hero.belongings.backpack), pos).sprite.drop(pos);
		} finally {
			com.shatteredpixel.shatteredpixeldungeon.levels.Level.suppressDropBonus = false;
		}
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		keptThoughLostInvent = false;
	}

	public boolean keptThroughLostInventory(){
		return keptThoughLostInvent;
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}
	
	public void execute( Hero hero, String action ) {

		GameScene.cancel();
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doDrop(hero);
			}
			
		} else if (action.equals( AC_THROW )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doThrow(hero);
			}
			
		} else if (action.equals( AC_WHALE )) {
			
			//==== END(修复·160 氪金大佬没有入口) ====
			//花金币强化。真正的扣钱与 upgrade() 在 whaleUpgrade() 里，
			//这里只负责"从背包动作触发它"以及给玩家反馈。
			//
			//用 RedButton 那种二次确认？—— 不必：
			//动作列表里这一项本来就写着费用（见下方的 name()），
			//玩家点它就是要花这个钱；而且升级是**不可逆的正向操作**，
			//没有"点错了亏了"的风险（顶多是花钱花早了）。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.whaleUpgrade(this)) {
				//==== END(修复·160 氪金升级后无法打人 / 无限过回合) ====
				//文档所有者反馈："氪金升级时无法打人、无限过回合，切屏才能解决。"
				//根因：原来是 hero.spend(1) 与 hero.busy() 分开调用的。
				//Hero.busy() 只把 ready 置 false，没有任何地方再调 ready()，
				//精灵停在 BUSY、回合也没推进 —— 于是人物卡死；切屏会强制刷新状态。
				//原版所有背包动作统一走 spendAndNext()（= busy + spend + next），照抄即可。
				hero.spendAndNext( 1f );
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog
						.p("金币化作了力量。");
			} else {
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog
						.w("金币不够。");
			}
		}
	}

	//can be overridden if default action is variable
	public String defaultAction(){
		return defaultAction;
	}
	
	public void execute( Hero hero ) {
		String action = defaultAction();
		if (action != null) {
			execute(hero, defaultAction());
		}
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}
	
	//takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other )){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}
	
	public boolean collect( Bag container ) {

		if (quantity <= 0){
			return true;
		}

		ArrayList<Item> items = container.items;

		if (items.contains( this )) {
			return true;
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).canHold( this )) {
				if (collect( (Bag)item )){
					return true;
				}
			}
		}

		if (!container.canHold(this)){
			return false;
		}
		
		if (stackable) {
			for (Item item:items) {
				if (isSimilar( item )) {
					item.merge( this );
					item.updateQuickslot();
					if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
						Badges.validateItemLevelAquired( this );
						Talent.onItemCollected(Dungeon.hero, item);
						if (isIdentified()) {
							Catalog.setSeen(getClass());
							Statistics.itemTypesDiscovered.add(getClass());
						}
					}
					if (TippedDart.lostDarts > 0){
						Dart d = new Dart();
						d.quantity(TippedDart.lostDarts);
						TippedDart.lostDarts = 0;
						if (!d.collect()){
							//have to handle this in an actor as we can't manipulate the heap during pickup
							Actor.add(new Actor() {
								{ actPriority = VFX_PRIO; }
								@Override
								protected boolean act() {
									Dungeon.level.drop(d, Dungeon.hero.pos).sprite.drop();
									Actor.remove(this);
									return true;
								}
							});
						}
					}
					return true;
				}
			}
		}

		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Badges.validateItemLevelAquired( this );
			Talent.onItemCollected( Dungeon.hero, this );
			if (isIdentified()){
				Catalog.setSeen(getClass());
				Statistics.itemTypesDiscovered.add(getClass());
			}
		}

		items.add( this );
		Dungeon.quickslot.replacePlaceholder(this);
		Collections.sort( items, itemComparator );
		updateQuickslot();
		return true;

	}
	
	public final boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}
	
	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			//pssh, who needs copy constructors?
			Item split = Reflection.newInstance(getClass());
			
			if (split == null){
				return null;
			}
			
			Bundle copy = new Bundle();
			this.storeInBundle(copy);
			split.restoreFromBundle(copy);
			split.quantity(amount);
			quantity -= amount;
			
			return split;
		}
	}

	public Item duplicate(){
		Item dupe = Reflection.newInstance(getClass());
		if (dupe == null){
			return null;
		}
		Bundle copy = new Bundle();
		this.storeInBundle(copy);
		dupe.restoreFromBundle(copy);
		return dupe;
	}
	
	public final Item detach( Bag container ) {
		
		if (quantity <= 0) {
			
			return null;
			
		} else
		if (quantity == 1) {

			if (stackable){
				Dungeon.quickslot.convertToPlaceholder(this);
			}

			return detachAll( container );
			
		} else {
			
			
			Item detached = split(1);
			updateQuickslot();
			if (detached != null) detached.onDetach( );
			return detached;
			
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				container.grabItems(); //try to put more items into the bag as it now has free space
				updateQuickslot();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		updateQuickslot();
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return getClass() == item.getClass();
	}

	protected void onDetach(){}

	//returns the true level of the item, ignoring all modifiers aside from upgrades
	public final int trueLevel(){
		return level;
	}

	//returns the persistant level of the item, only affected by modifiers which are persistent (e.g. curse infusion)
	public int level(){
		return level;
	}
	
	//returns the level of the item, after it may have been modified by temporary boosts/reductions
	//note that not all item properties should care about buffs/debuffs! (e.g. str requirement)
	public int buffedLvl(){
		//only the hero can be affected by Degradation
		if (Dungeon.hero != null && Dungeon.hero.buff( Degrade.class ) != null
			&& (isEquipped( Dungeon.hero ) || Dungeon.hero.belongings.contains( this ))) {
			return Degrade.reduceLevel(level());
		} else {
			return level();
		}
	}

	public void level( int value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	public int visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}

	public int buffedVisiblyUpgraded() {
		return levelKnown ? buffedLvl() : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}

	public final Item identify(){
		return identify(true);
	}

	public Item identify( boolean byHero ) {

		if (byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(getClass());
			Statistics.itemTypesDiscovered.add(getClass());
		}

		levelKnown = true;
		cursedKnown = true;
		Item.updateQuickslot();
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}
	
	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	public String title() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;

	}
	
	public String name() {
		return trueName();
	}
	
	public final String trueName() {
		return Messages.get(this, "name");
	}
	
	public int image() {
		return image;
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {

		if (Dungeon.hero != null) {
			Notes.CustomRecord note = Notes.findCustomRecord(customNoteID);
			if (note != null) {
				//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
				return Messages.get(this, "custom_note", note.title().replace('_', 'ˍ')) + "\n\n" + desc();
			} else {
				note = Notes.findCustomRecord(getClass());
				if (note != null) {
					//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
					return Messages.get(this, "custom_note_type", note.title().replace('_', 'ˍ')) + "\n\n" + desc();
				}
			}
		}

		return desc();
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public int quantity() {
		return quantity;
	}
	
	public Item quantity( int value ) {
		quantity = value;
		return this;
	}

	//item's value in gold coins
	public int value() {
		return 0;
	}

	//item's value in energy crystals
	public int energyVal() {
		return 0;
	}
	
	public Item virtual(){
		Item item = Reflection.newInstance(getClass());
		if (item == null) return null;
		
		item.quantity = 0;
		item.level = level;
		return item;
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}

	public static void updateQuickslot() {
		GameScene.updateItemDisplays = true;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String KEPT_LOST       = "kept_lost";
	private static final String CUSTOM_NOTE_ID = "custom_note_id";
	/** END(挑战 56 装备绑定): 绑定标记的存档键。 */
	private static final String BOUND_CHALLENGE  = "bound_challenge";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
		bundle.put( KEPT_LOST, keptThoughLostInvent );
		if (customNoteID != -1)     bundle.put(CUSTOM_NOTE_ID, customNoteID);
		//END(挑战 56): 只在已绑定时写，保持旧存档的字段集合不变
		if (boundForChallenge)      bundle.put( BOUND_CHALLENGE, true );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getInt( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slots when restoring belongings
		if (Belongings.bundleRestoring) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}

		keptThoughLostInvent = bundle.getBoolean( KEPT_LOST );
		//END(挑战 56): 旧存档没有这个键 → 默认 false（未绑定）
		boundForChallenge = bundle.getBoolean( BOUND_CHALLENGE );
		if (bundle.contains(CUSTOM_NOTE_ID))    customNoteID = bundle.getInt(CUSTOM_NOTE_ID);
	}

	public int targetingPos( Hero user, int dst ){
		return throwPos( user, dst );
	}

	public int throwPos( Hero user, int dst){
		return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

	public void throwSound(){
		Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		throwSound();

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);
		
		final float delay = castDelay(user, cell);

		if (enemy != null) {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							enemy.sprite,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							if (i != null) i.onThrow(cell);
							if (curUser.hasTalent(Talent.IMPROVISED_PROJECTILES)
									&& !(Item.this instanceof MissileWeapon)
									&& curUser.buff(Talent.ImprovisedProjectileCooldown.class) == null){
								if (enemy != null && enemy.alignment != curUser.alignment){
									Sample.INSTANCE.play(Assets.Sounds.HIT);
									Buff.affect(enemy, Blindness.class, 1f + curUser.pointsInTalent(Talent.IMPROVISED_PROJECTILES));
									Buff.affect(curUser, Talent.ImprovisedProjectileCooldown.class, 50f);
								}
							}
							if (user.buff(Talent.LethalMomentumTracker.class) != null){
								user.buff(Talent.LethalMomentumTracker.class).detach();
								user.next();
							} else if (Item.this instanceof com.shatteredpixel.shatteredpixeldungeon
									.endcontent.grimm.SilverGun
									&& com.shatteredpixel.shatteredpixeldungeon.endcontent
											.grimm.SilverGun.shouldBeFree(user)) {
								//==== END(挑战 125 银色短铳): 投掷不消耗回合 ====
								//原表："可以不消耗回合发出远程攻击"。
								//这里复用原版 LethalMomentumTracker 的"免费出手"路径：
								//直接 user.next() 推进时间轴但**不 spend**，于是玩家
								//的回合没有被消耗掉，可以继续行动。
								//
								//次数由 SilverGun.shouldBeFree 限制（每回合 2 次），
								//否则一回合能清空整层。
								user.next();
							} else {
								user.spendAndNext(delay);
							}
						}
					});
		} else {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							user.spend(delay);
							if (i != null) i.onThrow(cell);
							user.next();
						}
					});
		}
	}
	
	public float castDelay( Char user, int cell ){
		return TIME_TO_THROW;
	}

	public float pickupDelay(){
		return TIME_TO_PICK_UP;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	public void setCurrent( Hero hero ){
		curUser = hero;
		curItem = this;
	}

	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};

	//END(移植自魔绫): 传奇武器标记接口（无成员）
	public interface LengedsItem { }
}