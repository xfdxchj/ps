/*
 * 破碎的地牢 (End fork) — 挑战 125「格林之器」的专属武器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END(挑战 125 格林之器): 银色短铳。
 *
 * <h3>原表效果</h3>
 * "可以不消耗回合发出远程攻击"。
 *
 * <h3>实现（按文档所有者定稿）</h3>
 * <ul>
 *   <li>开火时若**不在冷却中** → 本次出手不消耗回合，并进入
 *       {@link #FREE_CD} 回合的冷却</li>
 *   <li>冷却期间开火照常消耗回合（等于普通远程武器）</li>
 *   <li>{@link RabbitWatch 兔子怀表} 可以把这条冷却**直接归零**</li>
 * </ul>
 *
 * <h3>END(修复·像弓不像投掷物)</h3>
 * <p>原实现继承自 {@code MissileWeapon}，导致它走"投掷"路径：
 * 玩家背包里点击会进入目标选择 → 投出去，UI 体验上完全是投掷武器。
 * 用户要求"像弓不像投掷物"。
 *
 * <p>参考 {@link com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow}
 * 的实现模式：本类改为继承 {@link Weapon}，加 {@link #AC_SHOOT} 动作，
 * 实际投射物由内部类 {@link SilverBullet}（一次性 MissileWeapon）承担。
 * 玩家点击物品 → {@code AC_SHOOT} → 目标选择 → 发射 SilverBullet，
 * 整个体验和灵能弓完全一致。
 */
public class SilverGun extends Weapon {

	public static final String AC_SHOOT = "SHOOT";

	/** 免费出手后的冷却回合数。 */
	public static final int FREE_CD = 10;

	{
		image = ItemSpriteSheet.GRIMM_SILVERGUN;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.1f;
		unique = true;
		bones = false;
		defaultAction = AC_SHOOT;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);        //像灵能弓：不可装备，按 SHOOT 触发
		actions.add(AC_SHOOT);
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute(hero, action);
		if (action.equals(AC_SHOOT)) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell(shooter);
		}
	}

	/**
	 * 面板：**固定伤害** 2 + 0.5×lv。
	 *
	 * <p>原表（文档所有者定稿）："银色短从的子弹伤害为 2+0.5lv"，
	 * 且明确是**固定值**（min == max），不是随机区间。
	 *
	 * <p>用 {@code Math.round} 而不是截断：0.5 级时应当进位（lv1 → 2.5 → 3）。
	 */
	@Override public int min(int lvl){ return Math.round(2 + 0.5f * lvl); }
	@Override public int max(int lvl){ return Math.round(2 + 0.5f * lvl); }

	@Override public int STRReq(int lvl){ return 13; }

	/** 短铳不可升级（等级只随玩家等级走，见 Weapon.level() 默认实现）。 */
	@Override public boolean isUpgradable(){ return false; }

	@Override
	public String info(){
		//END(修复): 先取父类文本 —— 那里面才有伤害面板/力量需求/等级。
		//原先直接 return 自定义文本，于是物品描述里完全看不到数值。
		return super.info() + "\n\n" +
				"一把镀银的短铳，扣下扳机几乎不占时间。\n\n" +
				"- 子弹伤害固定为 **2 + 0.5×等级**（不掷骰）\n" +
				"- 出手时**不消耗回合**，随后进入 " + FREE_CD + " 回合冷却\n" +
				"- 冷却期间攻击照常消耗回合\n" +
				"- 无限耐久\n\n" +
				"它几乎打不死人 —— 价值全在节奏上。与**兔子怀表**配合时，\n" +
				"怀表可以立刻重置这条冷却。";
	}

	//==================================================================
	//子弹（一次性投射物，参照 SpiritBow.SpiritArrow 的模式）
	//==================================================================

	/**
	 * 每次开火产生一发子弹。子弹本身不可堆叠、不可拆分、无任何动作 ——
	 * 它只是把"短铳的伤害/附魔/proc"通过 MissileSprite 投到目标格上。
	 */
	public SilverBullet knockBullet(){
		return new SilverBullet();
	}

	public class SilverBullet extends MissileWeapon {

		{
			image = ItemSpriteSheet.GRIMM_SILVERGUN;
			hitSound = Assets.Sounds.HIT_CRUSH;
			hitSoundPitch = 1.1f;
			setID = 0;
		}

		@Override
		public ArrayList<String> actions( Hero hero ) { return new ArrayList<>(); }

		@Override
		public String defaultAction() { return null; }

		@Override
		public int defaultQuantity() { return 1; }

		@Override
		public Item split( int amount ) { return null; }

		@Override
		public int damageRoll( Char owner ) {
			return SilverGun.this.damageRoll(owner);
		}

		@Override
		public boolean hasEnchant( Class<? extends Enchantment> type, Char owner ) {
			return SilverGun.this.hasEnchant(type, owner);
		}

		@Override
		public int proc( Char attacker, Char defender, int damage ) {
			return SilverGun.this.proc(attacker, defender, damage);
		}

		@Override
		public float delayFactor( Char user ) {
			return SilverGun.this.delayFactor(user);
		}

		@Override
		public float accuracyFactor( Char owner, Char target ) {
			return super.accuracyFactor(owner, target);
		}

		@Override
		public int STRReq( int lvl ) {
			return SilverGun.this.STRReq();
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xCCCCCC, 1 );
			} else {
				if (!curUser.shoot( enemy, this )) {
					Splash.at(cell, 0xCCCCCC, 1);
				}
			}
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 1, Random.Float(0.87f, 1.15f) );
		}

		/**
		 * END(125): 自定义 cast —— 把命中后是否"不消耗回合"的逻辑挪到这里。
		 *
		 * <p>原实现走 {@code MissileWeapon.cast} → {@code Item.cast} 的命中回调，
		 * 在 {@code Item.java} 里特判 {@code SilverGun.shouldBeFree}；
		 * 改成 Weapon 后不再走那条路径，所以必须在 Bullet 自己的 cast 里收尾。
		 *
		 * <p>逻辑与原版一致：命中后判 {@link SilverGun#shouldBeFree(Hero)}，
		 * 返回 true 时 user.next() 不消耗回合（同时挂上冷却）。
		 */
		@Override
		public void cast( final Hero user, final int dst ) {
			final int cell = throwPos( user, dst );

			user.busy();
			throwSound();
			user.sprite.zap( cell );

			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class))
					.reset( user.sprite, cell, this, new Callback() {
				@Override
				public void call() {
					curUser = user;
					onThrow( cell );

					if ( shouldBeFree(user) ) {
						//免费出手：推进时间轴但不 spend
						user.next();
					} else {
						user.spendAndNext( castDelay(user, cell) );
					}
				}
			});
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				knockBullet().cast(curUser, target);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(SilverGun.class, "prompt");
		}
	};

	//==================================================================
	//冷却
	//==================================================================

	/**
	 * END(125): 银色短铳的冷却。
	 *
	 * <p>用 {@code FlavourBuff} 的时长字段当冷却计时器 ——
	 * 它每回合自动递减，减到 0 就自动 detach，不需要自己写计时逻辑。
	 */
	public static class SilverGunCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		@Override public int icon(){ return BuffIndicator.NONE; }

		/** 剩余回合数（用于界面提示）。 */
		public int turnsLeft(){
			return Math.max(0, (int) Math.ceil(visualcooldown()));
		}
	}

	/**
	 * END(125): 本次开火是否应该"不消耗回合"。
	 *
	 * <p>返回 true 时会**顺便**挂上冷却，所以调用方不需要再做别的。
	 *
	 * @param hero 投掷者
	 * @return true 表示本次出手不应消耗回合
	 */
	public static boolean shouldBeFree( Hero hero ) {
		if (hero == null) return false;

		//冷却中 → 照常消耗回合
		if (hero.buff(SilverGunCooldown.class) != null) return false;

		//不在冷却 → 免费出手，并进入冷却
		Buff.affect(hero, SilverGunCooldown.class, (float) FREE_CD);
		return true;
	}

	/**
	 * END(125): 把冷却直接归零（兔子怀表调用）。
	 *
	 * @param hero 目标
	 * @return true 表示确实清掉了一层冷却
	 */
	public static boolean resetCooldown( Hero hero ) {
		if (hero == null) return false;
		SilverGunCooldown cd = hero.buff(SilverGunCooldown.class);
		if (cd == null) return false;
		cd.detach();
		return true;
	}
}
