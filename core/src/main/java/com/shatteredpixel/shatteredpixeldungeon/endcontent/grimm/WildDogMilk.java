/*
 * 破碎的地牢 (End fork) — 挑战 124「野生狗奶」的专属道具
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
 * END(挑战 124 野生狗奶): 野生狗奶。
 *
 * <h3>原表效果</h3>
 * "使用后全属性降低 75%，效果持续期间无法死亡（生命值最低为 1）"
 *
 * <h3>这是一条"以弱换不死"的规则</h3>
 * <ul>
 *   <li>**全属性 ×0.25**（攻击、命中、闪避、护甲全部降到四分之一）</li>
 *   <li>**期间不会死**：任何致命伤都只把生命压到 1</li>
 * </ul>
 *
 * <p>所以它的用法是"明知打不过，先喝一口保命" ——
 * 代价是你几乎打不动任何东西，只能靠走位和道具撑过去。
 */
public class WildDogMilk extends Item {

	/** 属性倍率（降低 75% → 乘 0.25）。 */
	public static final float STAT_MULT = 0.25f;

	/**
	 * 持续时间。
	 *
	 * <p>END(修订): 原表只写"效果持续期间无法死亡"，**没有给时长**。
	 * 我最初擅自写成 30 回合 —— 那是错的。文档所有者指定为**永久**。
	 *
	 * <p>用一个很大的数值表示永久（原版没有"无期限 buff"的机制，
	 * 所有 FlavourBuff 都会自然递减）。99999 回合远超一局游戏的长度，
	 * 实际效果等同永久。
	 */
	public static final float DURATION = 99999f;

	{
		//暂时复用镇魂歌的图标（同属"续命"主题）
		image = ItemSpriteSheet.GRIMM_REQUIEM;
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "野生狗奶"; }

	@Override
	public String info(){
		return "不知道是什么动物的奶。闻起来有股土腥味。\n\n" +
				"- 喝下后 **全属性降低 75%**（攻击/命中/闪避全部只剩四分之一）\n" +
				"- 但**期间不会死亡**（生命值最低保留 1 点）\n" +
				"- **永久生效**（不会随时间消失）\n\n" +
				"换命的道具 —— 代价是你几乎打不动任何东西。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_DRINK = "DRINK";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_DRINK);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_DRINK) || hero == null) return;

		Buff.affect(hero, DogMilkBuff.class, DURATION);
		GLog.w("腥味冲上鼻腔。你的身体变得迟钝，但暂时死不了。");

		//消耗一瓶
		detach(hero.belongings.backpack);
		hero.spendAndNext(1f);
	}

	@Override
	public int value(){ return 0; }

	//==================================================================
	//buff
	//==================================================================

	/**
	 * END(124): 野生狗奶的状态。
	 *
	 * <p>两个效果：
	 * <ol>
	 *   <li>属性 ×0.25 —— 由 {@code ChallengeEffects.speedModifier} 之外的
	 *       伤害/命中管线查询（见静态方法 {@link #isActive}）</li>
	 *   <li>不会死 —— 由 {@code Char.damage()} 的致命伤拦截处理</li>
	 * </ol>
	 */
	public static class DogMilkBuff extends FlavourBuff {
		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override public int icon(){ return BuffIndicator.WEAKNESS; }

		@Override
		public void tintIcon(com.watabou.noosa.Image icon) {
			//偏灰白的色调 —— 与"奶"呼应
			icon.hardlight(0.85f, 0.85f, 0.75f);
		}

		/**
		 * 永久状态 → 图标不显示"剩余时间"的渐变。
		 *
		 * <p>原实现按 {@code (DURATION - cooldown)/DURATION} 算，
		 * 而 DURATION 是 99999 时那个值永远是 1.0 —— 图标会一直满格，
		 * 看起来像"没有在倒计时"但又不明确。直接返回 1 语义更清楚。
		 */
		@Override
		public float iconFadePercent() {
			return 1f;
		}
	}

	/** END(124): 该角色是否处于"狗奶"状态。 */
	public static boolean isActive(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch) {
		return ch != null && ch.buff(DogMilkBuff.class) != null;
	}

	/**
	 * END(124): 致命伤拦截 —— 狗奶期间不会死。
	 *
	 * <p>调用点：{@code Char.damage()} 的致命伤处理处。
	 *
	 * @return true 表示本次致命伤被拦下（生命压到 1）
	 */
	public static boolean surviveFatal(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch,
									   int dmg) {
		if (ch == null) return false;
		if (dmg < ch.HP) return false;                  //不是致命伤
		if (!isActive(ch)) return false;

		ch.HP = 1;
		if (ch.sprite != null) {
			ch.sprite.showStatus(
					com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
					"苟活");
		}
		return true;
	}
}
