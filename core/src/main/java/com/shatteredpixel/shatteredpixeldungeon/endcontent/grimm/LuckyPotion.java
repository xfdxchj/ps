/*
 * 破碎的地牢 (End fork) — 挑战 198「幸运药水」的道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LuckyPotionBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * END(挑战 198 幸运药水): 幸运药水。
 *
 * <h3>效果（文档所有者定稿）</h3>
 * "增加物品，提升财富戒/幸运附魔 50% 效果，50 回合，
 *  获得方式财富/幸运掉落。"
 *
 * <h3>为什么继承 Potion 而不是 Item</h3>
 * 它是"药水" —— 继承 {@code Potion} 可以自动获得：
 * <ul>
 *   <li>鉴定系统（未鉴定时显示为随机颜色）</li>
 *   <li>投掷/泼洒逻辑</li>
 *   <li>与 45/46/47/48/195/196/197/199/201 等药剂类挑战的联动</li>
 * </ul>
 *
 * <p>反过来说，它也**会**受 199 混合药水影响（刷新时被换成紊乱魔药）——
 * 那是符合预期的：199 说的是"刷新的药水全部变为紊乱药水"。
 */
public class LuckyPotion extends Potion {

	{
		//暂时复用黄金蜂蜜酒的图标（幸运的主题色一致）
		image = ItemSpriteSheet.GRIMM_MEAD;
	}

	@Override public String name(){ return "幸运药水"; }

	@Override
	public String info(){
		return super.info() + "\n\n" +
				"瓶底沉着一层金粉。\n\n" +
				"- **50 回合**内，财富戒指的掉落率 **+50%**\n" +
				"- **幸运附魔**的触发概率 **+50%**\n" +
				"- 获取方式：财富/幸运掉落";
	}

	@Override public boolean isKnown(){ return true; }
	@Override public boolean isIdentified(){ return true; }

	/** END(198): 喝下后的效果。 */
	@Override
	public void apply(Hero hero) {
		super.apply(hero);

		Buff.affect(hero, LuckyPotionBuff.class, LuckyPotionBuff.DURATION);
		GLog.p("金粉在喉咙里散开。接下来的路会顺一些。");
	}
}
