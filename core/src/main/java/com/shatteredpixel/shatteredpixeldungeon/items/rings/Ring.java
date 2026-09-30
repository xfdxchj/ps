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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.SpiritForm;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.RingAffix;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemStatusHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Ring extends KindofMisc {
	
	protected Buff buff;
	protected Class<? extends RingBuff> buffClass;

	private static final LinkedHashMap<String, Integer> gems = new LinkedHashMap<String, Integer>() {
		{
			put("garnet",ItemSpriteSheet.RING_GARNET);
			put("ruby",ItemSpriteSheet.RING_RUBY);
			put("topaz",ItemSpriteSheet.RING_TOPAZ);
			put("emerald",ItemSpriteSheet.RING_EMERALD);
			put("onyx",ItemSpriteSheet.RING_ONYX);
			put("opal",ItemSpriteSheet.RING_OPAL);
			put("tourmaline",ItemSpriteSheet.RING_TOURMALINE);
			put("sapphire",ItemSpriteSheet.RING_SAPPHIRE);
			put("amethyst",ItemSpriteSheet.RING_AMETHYST);
			put("quartz",ItemSpriteSheet.RING_QUARTZ);
			put("agate",ItemSpriteSheet.RING_AGATE);
			put("diamond",ItemSpriteSheet.RING_DIAMOND);
		}
	};
	
	private static ItemStatusHandler<Ring> handler;
	
	private String gem;
	
	//rings cannot be 'used' like other equipment, so they ID purely based on exp
	private float levelsToID = 1;
	
	@SuppressWarnings("unchecked")
	public static void initGems() {
		handler = new ItemStatusHandler<>( (Class<? extends Ring>[])Generator.Category.RING.classes, gems );
	}

	public static void clearGems(){
		handler = null;
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void saveSelectively( Bundle bundle, ArrayList<Item> items ) {
		handler.saveSelectively( bundle, items );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>( (Class<? extends Ring>[])Generator.Category.RING.classes, gems, bundle );
	}
	
	//END(230 原神地牢): 戒指词条
	public RingAffix[] affixes = new RingAffix[0];
	//END(二.13 词条升级): 每条词条的升级次数与累计倍率（与 affixes 同长）
	public int[] affixUps = new int[0];
	public float[] affixMult = new float[0];

	/** 按当前强化等级补齐词条数量（每 3 级 1 条，最多 3 条）。 */
	public void ensureAffixes(){
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.genshinEnabled()){
			return;
		}
		int want = RingAffix.countFor(level());
		if (affixes == null) affixes = new RingAffix[0];
		if (affixUps == null) affixUps = new int[0];
		if (affixMult == null) affixMult = new float[0];
		if (affixes.length == want && affixUps.length == want && affixMult.length == want) return;
		RingAffix[] next = new RingAffix[want];
		int[] nextUps = new int[want];
		float[] nextMult = new float[want];
		int keep = Math.min(affixes.length, want);
		System.arraycopy(affixes, 0, next, 0, keep);
		System.arraycopy(affixUps, 0, nextUps, 0, Math.min(affixUps.length, keep));
		float[] oldMult = (affixMult.length >= keep) ? affixMult : new float[keep];
		for (int i = 0; i < keep; i++){
			nextMult[i] = (oldMult.length > i && oldMult[i] > 0f) ? oldMult[i] : 1f;
		}
		for (int i = affixes.length; i < want; i++){
			next[i] = RingAffix.randomAffix();
			nextMult[i] = 1f;
		}
		affixes = next;
		affixUps = nextUps;
		affixMult = nextMult;
	}

	/** END(230): 洗练 —— 全部词条重掷，同时清空升级。 */
	public void rerollAffixes(){
		affixes = RingAffix.roll(RingAffix.countFor(level()));
		affixUps = new int[affixes.length];
		affixMult = new float[affixes.length];
		for (int i = 0; i < affixMult.length; i++) affixMult[i] = 1f;
	}

	/** END(二.13): 第 i 条词条的实际数值（基础值 × 升级倍率）。 */
	public float affixValue(int i){
		if (affixes == null || i < 0 || i >= affixes.length) return 0f;
		float m = (affixMult != null && i < affixMult.length && affixMult[i] > 0f) ? affixMult[i] : 1f;
		return affixes[i].value * m;
	}

	/** END(二.13): 第 i 条词条已升级次数。 */
	public int affixUpgrades(int i){
		if (affixUps == null || i < 0 || i >= affixUps.length) return 0;
		return affixUps[i];
	}

	/** END(二.13): 对第 i 条词条应用一次升级；已达 3 次返回 false。 */
	public boolean upgradeAffix(int i, float bonus){
		if (affixes == null || i < 0 || i >= affixes.length) return false;
		if (affixUps == null || affixUps.length != affixes.length){
			int[] tmp = new int[affixes.length];
			if (affixUps != null) System.arraycopy(affixUps, 0, tmp, 0, Math.min(affixUps.length, affixes.length));
			affixUps = tmp;
		}
		if (affixMult == null || affixMult.length != affixes.length){
			float[] tmp = new float[affixes.length];
			for (int k = 0; k < tmp.length; k++) tmp[k] = 1f;
			if (affixMult != null) System.arraycopy(affixMult, 0, tmp, 0, Math.min(affixMult.length, affixes.length));
			affixMult = tmp;
		}
		if (affixUps[i] >= 3) return false;
		affixUps[i]++;
		affixMult[i] *= (1f + bonus);
		return true;
	}

	public Ring() {
		super();
		reset();
	}

	//anonymous rings are always IDed, do not affect ID status,
	//and their sprite is replaced by a placeholder if they are not known,
	//useful for items that appear in UIs, or which are only spawned for their effects
	protected boolean anonymous = false;
	public void anonymize(){
		if (!isKnown()) image = ItemSpriteSheet.RING_HOLDER;
		anonymous = true;
	}
	
	public void reset() {
		super.reset();
		levelsToID = 1;
		if (handler != null && handler.contains(this)){
			image = handler.image(this);
			gem = handler.label(this);
		} else {
			image = ItemSpriteSheet.RING_GARNET;
			gem = "garnet";
		}
	}
	
	public void activate( Char ch ) {
		if (buff != null){
			buff.detach();
			buff = null;
		}
		buff = buff();
		buff.attachTo( ch );
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			if (buff != null) {
				buff.detach();
				buff = null;
			}

			return true;

		} else {

			return false;

		}
	}
	
	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( this ));
	}
	
	public void setKnown() {
		if (!anonymous) {
			if (!isKnown()) {
				handler.know(this);
			}

			if (Dungeon.hero.isAlive()) {
				Catalog.setSeen(getClass());
				Statistics.itemTypesDiscovered.add(getClass());
			}
		}
	}
	
	@Override
	public String name() {
		return isKnown() ? super.name() : Messages.get(Ring.class, gem);
	}

	@Override
	public String desc() {
		return isKnown() ? super.desc() : Messages.get(this, "unknown_desc");
	}
	
	@Override
	public String info(){

		//skip custom notes if anonymized and un-Ided
		String desc;
		if (anonymous && (handler == null || !handler.isKnown( this ))){
			desc = desc();

		} else {
			desc = super.info();
		}

		if (cursed && isEquipped( Dungeon.hero )) {
			desc += "\n\n" + Messages.get(Ring.class, "cursed_worn");
			
		} else if (cursed && cursedKnown) {
			desc += "\n\n" + Messages.get(Ring.class, "curse_known");
			
		} else if (!isIdentified() && cursedKnown){
			desc += "\n\n" + Messages.get(Ring.class, "not_cursed");
			
		}
		
		if (isKnown()) {
			desc += "\n\n" + statsInfo();
			String ai = affixInfo();
			if (!ai.isEmpty()){
				desc += "\n\n" + ai;
			}
		}
		
		return desc;
	}

	/** END(二.13): 词条一览，供物品信息面板展示。 */
	public String affixInfo(){
		if (affixes == null || affixes.length == 0) return "";
		StringBuilder sb = new StringBuilder("戒指词条：");
		for (int i = 0; i < affixes.length; i++){
			RingAffix a = affixes[i];
			float v = affixValue(i);
			sb.append("\n• ").append(a.label).append(" ");
			if (a == RingAffix.REGEN){
				sb.append("每回合 ").append(v == Math.round(v) ? Integer.toString(Math.round(v)) : String.format("%.1f", v));
			} else {
				sb.append("+").append(Math.round(v * 100f)).append("%");
			}
			int ups = affixUpgrades(i);
			if (ups > 0){
				sb.append("（强化 ").append(ups).append("/3）");
			}
		}
		return sb.toString();
	}
	
	protected String statsInfo(){
		return "";
	}

	public String upgradeStat1(int level){
		return null;
	}

	public String upgradeStat2(int level){
		return null;
	}

	public String upgradeStat3(int level){
		return null;
	}
	
	@Override
	public Item upgrade() {
		super.upgrade();
		
		if (Random.Int(3) == 0) {
			cursed = false;
		}
		ensureAffixes();
		
		return this;
	}
	
	@Override
	public boolean isIdentified() {
		return super.isIdentified() && isKnown();
	}
	
	@Override
	public Item identify( boolean byHero ) {
		setKnown();
		levelsToID = 0;
		return super.identify(byHero);
	}

	public void setIDReady(){
		levelsToID = -1;
	}

	public boolean readyToIdentify(){
		return !isIdentified() && levelsToID <= 0;
	}
	
	@Override
	public Item random() {
		//+0: 66.67% (2/3)
		//+1: 26.67% (4/15)
		//+2: 6.67%  (1/15)
		int n = 0;
		if (Random.Int(3) == 0) {
			n++;
			if (Random.Int(5) == 0){
				n++;
			}
		}
		level(n);
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}
		ensureAffixes();
		
		return this;
	}
	
	public static HashSet<Class<? extends Ring>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Ring>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler != null && handler.known().size() == Generator.Category.RING.classes.length;
	}
	
	@Override
	public int value() {
		int price = 75;
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	protected RingBuff buff() {
		return null;
	}

	private static final String LEVELS_TO_ID    = "levels_to_ID";
	private static final String AFFIXES = "ring_affixes";
	private static final String AFFIX_UPS = "ring_affix_ups";
	private static final String AFFIX_MULT = "ring_affix_mult";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVELS_TO_ID, levelsToID );
		if (affixes != null && affixes.length > 0){
			int[] ids = new int[affixes.length];
			for (int i = 0; i < affixes.length; i++) ids[i] = affixes[i].ordinal();
			bundle.put( AFFIXES, ids );
			if (affixUps != null && affixUps.length == affixes.length){
				bundle.put( AFFIX_UPS, affixUps );
			}
			if (affixMult != null && affixMult.length == affixes.length){
				bundle.put( AFFIX_MULT, affixMult );
			}
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		levelsToID = bundle.getFloat( LEVELS_TO_ID );
		int[] ids = bundle.getIntArray( AFFIXES );
		if (ids != null){
			RingAffix[] vals = RingAffix.values();
			affixes = new RingAffix[ids.length];
			for (int i = 0; i < ids.length; i++){
				affixes[i] = (ids[i] >= 0 && ids[i] < vals.length) ? vals[ids[i]] : RingAffix.randomAffix();
			}
			affixUps = bundle.getIntArray( AFFIX_UPS );
			if (affixUps == null || affixUps.length != affixes.length){
				affixUps = new int[affixes.length];
			}
			affixMult = bundle.getFloatArray( AFFIX_MULT );
			if (affixMult == null || affixMult.length != affixes.length){
				affixMult = new float[affixes.length];
			}
			for (int i = 0; i < affixMult.length; i++){
				if (affixMult[i] <= 0f) affixMult[i] = 1f;
			}
		}
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		if (isIdentified() || !isEquipped(hero)) return;
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		//becomes IDed after 1 level
		levelsToID -= levelPercent;
		if (levelsToID <= 0){
			if (ShardOfOblivion.passiveIDDisabled()){
				if (levelsToID > -1){
					GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
				}
				setIDReady();
			} else {
				identify();
				GLog.p(Messages.get(Ring.class, "identify"));
				Badges.validateItemLevelAquired(this);
			}
		}
	}

	@Override
	public int buffedLvl() {
		int lvl = super.buffedLvl();
		if (Dungeon.hero.buff(EnhancedRings.class) != null){
			lvl++;
		}
		return lvl;
	}

	public static int getBonus(Char target, Class<?extends RingBuff> type){
		if (target.buff(MagicImmune.class) != null) return 0;
		int bonus = 0;
		for (RingBuff buff : target.buffs(type)) {
			bonus += buff.level();
		}
		SpiritForm.SpiritFormBuff spiritForm = target.buff(SpiritForm.SpiritFormBuff.class);
		if (bonus == 0
				&& spiritForm != null
				&& spiritForm.ring() != null
				&& spiritForm.ring().buffClass == type){
			bonus += spiritForm.ring().soloBonus();
		}
		return bonus;
	}

	public static int getBuffedBonus(Char target, Class<?extends RingBuff> type){
		if (target.buff(MagicImmune.class) != null) return 0;
		int bonus = 0;
		for (RingBuff buff : target.buffs(type)) {
			bonus += buff.buffedLvl();
		}
		if (bonus == 0
				&& target.buff(SpiritForm.SpiritFormBuff.class) != null
				&& target.buff(SpiritForm.SpiritFormBuff.class).ring() != null
				&& target.buff(SpiritForm.SpiritFormBuff.class).ring().buffClass == type){
			bonus += target.buff(SpiritForm.SpiritFormBuff.class).ring().soloBuffedBonus();
		}

		//==== END(顶级装备·轮回噬灭之戒): 提供所有戒指的效果 ====
		//文档所有者定稿："拥有所有戒指的效果，同时效果提升 100%，
		//升级效果提升 50%"。
		//
		//**为什么改在这一处**：这是所有戒指效果的**公共入口** ——
		//12 种戒指的 15 个查询方法（accuracyMultiplier / evasionMultiplier /
		//strengthBonus / attackSpeedMultiplier…）最终都调这里。
		//改这一处就等于"同时戴上所有戒指"，不必去动那 12 个文件。
		int reincarnation = reincarnationBonus(target);
		if (reincarnation > 0) {
			bonus += reincarnation;
		}

		return bonus;
	}

	/**
	 * END(轮回噬灭之戒): 这枚戒指为**任意一种**戒指效果提供多少等级。
	 *
	 * <p>公式：
	 * <pre>
	 *   基础份 = (等级 + 1) × 2      //"效果提升 100%"：按 2 倍计入
	 *   升级份 = 等级 × 0.5          //"升级效果提升 50%"
	 *   合计   = round(基础份 + 升级份)
	 * </pre>
	 *
	 * <p>举例：
	 * <pre>
	 *   +0  → (0+1)×2  + 0    =  2
	 *   +3  → (3+1)×2  + 1.5  = 10    （原版 +3 戒指只给 4 级效果）
	 *   +10 → (10+1)×2 + 5    = 27
	 * </pre>
	 *
	 * @return 提供的等级；没戴轮回戒时返回 0
	 */
	private static int reincarnationBonus(Char target) {
		if (target == null) return 0;
		if (target.buff(MagicImmune.class) != null) return 0;
		if (!(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) {
			return 0;                       //只有玩家能戴
		}

		com.shatteredpixel.shatteredpixeldungeon.items.Item it =
				((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) target)
						.belongings.getItem(
								com.shatteredpixel.shatteredpixeldungeon.endcontent.items
										.ReincarnationRing.class);
		if (it == null) return 0;

		int lvl = Math.max(0, it.buffedLvl());
		float base = (lvl + 1) * 2f;        //效果 +100%
		float upgrade = lvl * 0.5f;         //升级效果 +50%
		return Math.max(1, Math.round(base + upgrade));
	}

	//just used for ring descriptions
	public int soloBonus(){
		if (cursed){
			return Math.min( 0, Ring.this.level()-2 );
		} else {
			return Ring.this.level()+1;
		}
	}

	//just used for ring descriptions
	public int soloBuffedBonus(){
		if (cursed){
			return Math.min( 0, Ring.this.buffedLvl()-2 );
		} else {
			return Ring.this.buffedLvl()+1;
		}
	}

	//just used for ring descriptions
	public int combinedBonus(Hero hero){
		int bonus = 0;
		if (hero.belongings.ring() != null && hero.belongings.ring().getClass() == getClass()){
			bonus += hero.belongings.ring().soloBonus();
		}
		if (hero.belongings.misc() != null && hero.belongings.misc().getClass() == getClass()){
			bonus += ((Ring)hero.belongings.misc()).soloBonus();
		}
		return bonus;
	}

	//just used for ring descriptions
	public int combinedBuffedBonus(Hero hero){
		int bonus = 0;
		if (hero.belongings.ring() != null && hero.belongings.ring().getClass() == getClass()){
			bonus += hero.belongings.ring().soloBuffedBonus();
		}
		if (hero.belongings.misc() != null && hero.belongings.misc().getClass() == getClass()){
			bonus += ((Ring)hero.belongings.misc()).soloBuffedBonus();
		}
		return bonus;
	}

	public class RingBuff extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (target instanceof Hero && Dungeon.hero == null && cooldown() == 0 && target.cooldown() > 0) {
					spend(TICK);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean act() {
			spend( TICK );
			return true;
		}

		public int level(){
			return Ring.this.soloBonus();
		}

		public int buffedLvl(){
			return Ring.this.soloBuffedBonus();
		}

	}
}
