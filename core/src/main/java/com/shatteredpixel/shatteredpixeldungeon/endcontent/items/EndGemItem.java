/*
 * Shattered Pixel Dungeon: End  —《破碎的像素地牢：终焉扩展》(装备宝石系统 第一批实现)
 *
 * 一颗可直接使用(镶嵌)的宝石道具。
 * 玩家从背包选择 "镶嵌",再从弹出的物品选择界面选择一件自己背包/身上的
 * Weapon 或 Armor,若槽位合法(该装备此前没有已嵌的[[不同]]宝石)则把本宝石序号写入
 * ((Weapon)item).gem / ((Armor)item).gem,随后消耗这枚宝石,并让快读/背包即时刷新。
 *
 * 设计参考(嵌入范式均照抄上游既有做法):
 *   - items/scrolls/exotic/ScrollOfEnchantment.java 的
 *     WndBag.ItemSelector + GameScene.selectItem(...) 选目标结构;
 *   - items/stones/InventoryStone.java 的 actions/AC_USE/execute 一次性消耗道具范式;
 *   - Item.updateQuickslot()/Item.detach(Bag) API。
 *
 * 本类只开放很小通路(不接入世界掉落spawn);spawn 测试写法见 docs/END_ROADMAP.md。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.EndGem;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.EndGemProfile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class EndGemItem extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.STONE_AUGMENTATION; //占位图标：正式美术未接前复用现有宝石石图标
		defaultAction = AC_USE;
		stackable = false;                 //单颗使用；不同种类宝石不堆叠

		//END: 宝石天生"已鉴定"——拾取即登记进图鉴/日志(见 Item.collect→Catalog.setSeen)
		levelKnown = true;
		cursedKnown = true;
	}

	/** 本宝石所属类型；由构造/恢复时设置。缺省=攻击,便于 new EndGemItem() 断言。 */
	private EndGem gem = EndGem.ATTACK;

	public EndGemItem(){
		this( EndGem.ATTACK );
	}

	public EndGemItem( EndGem gem ){
		super();
		this.gem = gem;
	}

	/** 便捷工厂：造一枚指定类型宝石,便于测试 spawn (见 docs/END_ROADMAP.md)。 */
	public static EndGemItem of( EndGem gem ){
		return new EndGemItem(gem);
	}

	public EndGem gemType(){
		return gem;
	}

	@Override
	public boolean doPickUp( Hero hero, int pos ) {
		boolean wasSeen = Catalog.isSeen( getClass() );
		boolean picked = super.doPickUp( hero, pos );
		//END: 首次获得宝石时即时日志提示(登记已由 Item.collect→Catalog.setSeen 完成)
		if (picked && !wasSeen){
			GLog.i( "图鉴新增: " + name() );
		}
		return picked;
	}

	//——显示信息(自造中文占位,未走 messages 属性文件以优先保编译)——
	@Override
	public String name(){
		switch (gem){
			case ATTACK:   return "攻击宝石";
			case DEFENSE:  return "防御宝石";
			case ACCURACY: return "命中宝石";
			case EVASION:  return "闪避宝石";
			case MAX_HP:   return "生命宝石";
			default:       return "未定义宝石";
		}
	}

	@Override
	public String info(){
		int base = EndGemProfile.of(gem).bonusAt(0);
		return "终焉·装备宝石(第一批): " + name()
				+ "\n镶嵌于装备: 永久生效、随装备等级成长(基础"+ base +",每升级约+"+ max(1, EndGemProfile.of(gem).bonusAt(1)-EndGemProfile.of(gem).bonusAt(0)) +")。"
				+ "\n合法槽位: "
				+ (gem.isWeaponOriented() ? "武器(命中/攻击增幅)" : (gem.isArmorOriented()? "护甲(闪避/减伤)": "护甲(生命上限)"));
	}

	private int max( int a, int b ){ return a > b ? a : b; }

	@Override
	public int value() {
		return 50 * quantity;   //与 Ankh(复活十字章)同价,商店售价经统一倍率计算故两者恒同价
	}

	//——行动/动作——
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}

	//只有 USE 按钮需要本地化文案,其它(丢/投)沿用父类
	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals( AC_USE )) {
			return "镶嵌";
		}
		return super.actionName(action, hero);
	}

	@Override
	public String defaultAction(){
		return AC_USE;
	}

	//让宝石能被按类型区分,用于防止异种堆叠(虽 stackable=false,稳妥起见一并按类型判定相似)
	@Override
	public boolean isSimilar( Item item ){
		return super.isSimilar(item) && item instanceof EndGemItem
				&& ((EndGemItem) item).gem == gem;
	}

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle(bundle);
		bundle.put("gem", (gem != null) ? gem.ordinal() : EndGem.ATTACK.ordinal());
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle(bundle);
		if (bundle.contains("gem")){
			int o = bundle.getInt("gem");
			if (o >= 0 && o < EndGem.values().length) gem = EndGem.values()[o];
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		//super.execute 会设置 curUser/curItem 静态引用,同 InventoryStone 的做法
		super.execute( hero, action );
		if (action.equals( AC_USE )){
			interact( hero );
		}
	}

	//点击"镶嵌":打开背包弹选择界面
	private void interact( final Hero hero ){
		GameScene.selectItem(itemSelector);
	}

	/**
	 * 合法的目标宝石放入哪类装备:
	 * weapon-oriented(ATTACK/ACCURACY)→ 只嵌武器; armor-oriented 与 MAX_HP → 嵌护甲。
	 * (与数值接入一致: 命中/攻击宝石从 attackingWeapon 生效,其余从护甲生效,见 Hero.java 的 //END gem 注入)
	 */
	private boolean usableOnItem( Item item ){
		if (gem == null) return false;
		if (gem.isWeaponOriented()){
			return item instanceof Weapon;
		} else {
			return item instanceof Armor;
		}
	}

	private void socket( Item target ){

		if (gem.isWeaponOriented() && target instanceof Weapon){
			Weapon w = (Weapon) target;
			if (w.hasGem()){
				GLog.w("该武器已经镶嵌了「" + (w.gemType()!=null? w.gemType().name() : "") + "」宝石,无法再嵌新宝石(不可覆写)。");
				return;
			}
			w.gem = gem.ordinal();
			socketed(w);
		} else if (target instanceof Armor){
			Armor a = (Armor) target;
			if (a.hasGem()){
				GLog.w("该护甲已经镶嵌了「" + (a.gemType()!=null? a.gemType().name() : "") + "」宝石,无法再嵌新宝石(不可覆写)。");
				return;
			}
			a.gem = gem.ordinal();
			//生命宝石改变英雄最大生命: 立即让英雄重算生命上限(UpdateHT 会把当下护甲上的 MAX_HP 纳入)
			if (isEquippedOnHero(a) && gem == EndGem.MAX_HP && curUser != null){
				curUser.updateHT(true);
			}
			socketed(a);
		} else {
			GLog.w("槽位不合法: 该宝石不能嵌入这个目标装备。");
			return;
		}

		consume();
	}

	private boolean isEquippedOnHero( Armor a ){
		return curUser != null && curUser.belongings.armor == a;
	}

	//成功镶嵌的公共收尾：刷新缓存的快读图标
	private void socketed( Item target ){
		target.updateQuickslot();
	}

	//消耗本颗宝石
	private void consume(){
		if (curUser == null) return;
		this.detach(curUser.belongings.backpack);
		updateQuickslot();
	}

	//选中的目标物件经此回调被嵌入
	private WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return "为哪件装备镶嵌「" + Messages.titleCase(name()) + "」?";
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return usableOnItem( item );
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null){
				socket(item);
			}
		}
	};
}
