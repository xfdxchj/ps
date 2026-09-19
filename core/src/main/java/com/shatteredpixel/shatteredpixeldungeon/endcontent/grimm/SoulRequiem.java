/*
 * 破碎的地牢 (End fork) — 挑战 128「格林之术」的专属道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * END(挑战 128 格林之术): 镇魂歌。
 *
 * <h3>原表效果</h3>
 * "使用后可以获得 3 回合不死（生命值到了 0 也不死，buff 结束后死亡）"
 *
 * <h3>语义要点</h3>
 * <ul>
 *   <li>使用后挂 {@link SoulRequiem} buff，持续 {@link #DURATION} 回合</li>
 *   <li>buff 期间**任何致命伤害都会把生命压到 1**，而不是真的死</li>
 *   <li>buff **结束时**，如果此时生命为 1 且本来该死了 —— 那就是"还债"的时刻</li>
 * </ul>
 *
 * <h3>为什么用"结束时死亡"而不是"结束时不死"</h3>
 * 原表写的是"buff 结束后死亡"。所以它**不是**保命道具，
 * 而是"换取 3 回合的行动机会"—— 3 回合后该死还是得死。
 *
 * <p>实现上：buff 期间被致命伤打到时，把 {@code pendingDeath} 标记为 true；
 * buff 到期 detach 时若该标记为 true，就让英雄真正死亡。
 * 这样"3 回合内没挨过致命伤"的玩家不会莫名其妙死掉。
 */
public class SoulRequiem extends Item {

	/** buff 持续回合数。 */
	public static final int DURATION = 3;

	{
		image = ItemSpriteSheet.GRIMM_REQUIEM;
		stackable = false;
		bones = false;
	}

	@Override public String name(){ return "镇魂歌"; }

	@Override public String info(){
		return "一段写在羊皮上的、没有旋律的歌词。\n\n" +
				"- 使用后获得 **" + DURATION + " 回合**的不死状态\n" +
				"- 期间生命值降到 0 也不会死（会停在 1）\n" +
				"- **状态结束时若已欠下死亡，仍会死去**\n\n" +
				"它不是护身符，只是把判决往后推了三个回合。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	public static final String AC_USE = "USE";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_USE) || hero == null) return;

		Buff.affect(hero, SoulRequiemBuff.class, (float) DURATION);
		GLog.i("歌声响起。你暂时不会死去。");
		hero.spendAndNext(1f);
	}

	//==================================================================
	//buff
	//==================================================================

	/**
	 * END(128): 不死状态。
	 *
	 * <p>必须继承 {@code FlavourBuff}：{@code Buff.affect(Char, Class, float)}
	 * 这个三参重载只接受它的子类（原版限制）。
	 */
	public static class SoulRequiemBuff extends FlavourBuff {
		{
			type = buffType.POSITIVE;
			announced = true;
		}

		/**
		 * 是否已经"欠下死亡"。
		 *
		 * <p>buff 期间被致命伤打到就置 true；buff 结束时若仍为 true，
		 * 说明这 3 回合本该死一次 —— 现在补上。
		 */
		public boolean pendingDeath = false;

		@Override public int icon(){ return BuffIndicator.HEALING; }

		/**
		 * END(128): buff 结束（到期或被驱散）时的结算。
		 *
		 * <p>**在这里而不是 act() 里判死**：{@code detach()} 是"状态消失"的
		 * 唯一收口点。放在 act() 里会漏掉"被驱散"的情况。
		 */
		/**
		 * END(修订): 结束**不结算**期间的死亡。
		 *
		 * <p>文档所有者定稿："镇魂结束应该是不会结算期间的死亡"。
		 *
		 * <p>所以 {@code pendingDeath} 只作为一个统计标记保留，
		 * **不再触发真正的死亡** —— 镇魂歌就是纯粹的"3 回合无敌"，
		 * 那 3 回合里挨的致命伤一笔勾销。
		 */
		@Override
		public void detach(){
			super.detach();
			//不再调用 target.die() —— 见上方说明
		}

		private static final String PENDING = "pending_death";

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(PENDING, pendingDeath);
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle){
			super.restoreFromBundle(bundle);
			pendingDeath = bundle.getBoolean(PENDING);
		}
	}

	/**
	 * END(128): 致命伤拦截 —— 有镇魂歌时不死，把生命压到 1。
	 *
	 * <p>调用点：{@code Char.damage()} 里、真正扣血之前
	 * （与 65 及时雨、104 命悬一线并列）。
	 *
	 * @param ch  受击者
	 * @param dmg 即将造成的伤害
	 * @return true 表示本次致命伤被镇魂歌挡下
	 */
	public static boolean surviveFatal(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch,
									   int dmg){
		if (ch == null) return false;
		if (dmg < ch.HP) return false;                 //不是致命伤，不管

		SoulRequiemBuff b = ch.buff(SoulRequiemBuff.class);
		if (b == null) return false;

		//记下"欠了一条命"，3 回合后还
		b.pendingDeath = true;

		//把血留在 1，而不是真的扣到 0 以下
		ch.HP = 1;
		if (ch.sprite != null) {
			ch.sprite.showStatus(
					com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
					"镇魂");
		}
		return true;
	}
}
