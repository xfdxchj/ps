/*
 * 破碎的地牢 (End fork) — 挑战 40「贷款」的债务状态
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/**
 * END(挑战 40 贷款): 一笔未偿还的债务。
 *
 * <h3>机制</h3>
 * 原表："可贷款金币，1000 回合内偿还本金 110%"。
 * <ul>
 *   <li>借入 {@code principal} 金币，立刻到手。</li>
 *   <li>{@link #REPAY_TURNS} 回合后自动扣除 {@code principal * 1.1}。</li>
 *   <li>金币不够时**扣到 0 为止**，并把差额记为"违约"——
 *       违约不清空背包（那太惩罚了），只是让玩家欠着并在日志里提示。</li>
 * </ul>
 *
 * <h3>为什么用 buff 而不是字段</h3>
 * buff 天然参与存读档、有图标显示剩余回合、也能被"驱散"类效果处理。
 * 若用 Hero 上的裸字段，这三件事都要手工重做。
 */
public class LoanDebt extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	/** 偿还期限：1000 回合。 */
	public static final float REPAY_TURNS = 1000f;

	/** 利息倍率：本金 110%。 */
	public static final float REPAY_MULT = 1.10f;

	/** 本金。 */
	private int principal = 0;

	/** 剩余回合（自己维护，不依赖 buff 的 cooldown 以便显示精确数字）。 */
	private float turnsLeft = REPAY_TURNS;

	public void setPrincipal(int amount) {
		this.principal = Math.max(0, amount);
		this.turnsLeft = REPAY_TURNS;
	}

	public int principal() {
		return principal;
	}

	/** 到期应还的总额。 */
	public int owed() {
		return Math.round(principal * REPAY_MULT);
	}

	public int turnsLeft() {
		return Math.max(0, (int) turnsLeft);
	}

	@Override
	public boolean act() {
		turnsLeft -= TICK;

		if (turnsLeft <= 0) {
			repay();
			detach();
			return true;
		}

		spend(TICK);
		return true;
	}

	/** 到期结算。 */
	private void repay() {
		int owed = owed();

		//==== END(40 贷款·修订): 允许金币扣成负数 ====
		//文档所有者要求："只有 100 块但要还 1100，就变成 -1000，这是可以的"。
		//
		//早先版本用 Math.min(owed, gold) 把扣款封顶在"手上有的钱"，
		//那样永远不会有负数 —— 但也就失去了"欠债"的实感：
		//玩家可以借钱花光，然后一分不还。
		//
		//金币变负的**安全性**已逐一核查（见下），不会崩：
		//  · WndTradeItem:156   btnBuy.enable(price <= Dungeon.gold)
		//      -> 负数时购买按钮自动禁用，安全
		//  · Shopkeeper:304     Dungeon.gold >= buyback.value()
		//      -> 同理，安全
		//  · MasterThievesArmband:265 有 Dungeon.gold > 0 的保护
		//  · CurrencyIndicator / InventoryPane / WndBag 只是显示数字
		//      -> Integer.toString 支持负数
		//  · EscapeCrystal 存读档用 int，支持负数
		Dungeon.gold -= owed;

		if (Dungeon.gold >= 0) {
			GLog.i(Messages.get(this, "repaid", owed));
		} else {
			//扣成负数：欠款转为负金币，会在界面上以负数显示
			GLog.w(Messages.get(this, "defaulted", owed, -Dungeon.gold));
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.WEAKNESS;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.85f, 0.3f);   //金色，呼应金币
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, 1f - (turnsLeft / REPAY_TURNS));
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", principal, owed(), turnsLeft());
	}

	private static final String PRINCIPAL = "principal";
	private static final String TURNS_LEFT = "turns_left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PRINCIPAL, principal);
		bundle.put(TURNS_LEFT, turnsLeft);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		principal = bundle.getInt(PRINCIPAL);
		turnsLeft = bundle.getFloat(TURNS_LEFT);
	}
}
