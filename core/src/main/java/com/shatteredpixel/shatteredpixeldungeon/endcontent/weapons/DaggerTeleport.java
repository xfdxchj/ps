package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.TeleportCooldown;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(刺杀匕首·传送分支): 刺杀·传送。
 *
 * <h3>文档所有者定稿</h3>
 * <ul>
 *   <li>**一直有传送**（不是"命中才触发"）</li>
 *   <li>**可以扔到平地也传送** —— 落点是空地时同样把你送过去</li>
 *   <li>冷却 **10 回合**</li>
 * </ul>
 *
 * <h3>"可以扔到平地也传送"是怎么做的</h3>
 * {@link EmbedDagger#onThrow(int)} 已经把落点记进 {@code stuckCell}
 * （命中敌人时记敌人格，落在空地时记空地格）。所以回收时：
 * <pre>
 *   有活着的被嵌敌人 → 传到他**背后**
 *   否则             → 传到**匕首落点那一格**（平地也生效）
 * </pre>
 * 这正是文档所有者要的行为。
 *
 * <h3>冷却 10 回合</h3>
 * 比刺杀分支（20）短一半 —— 传送是这个分支的核心玩法，
 * CD 太长就变成"摆设"。10 回合约等于"每场小战斗能用一次"。
 */
public class DaggerTeleport extends EmbedDagger {

	/**
	 * END(修订·文档所有者定稿): 冷却 **10 回合**。
	 *
	 * <p>沿革：100（初版）→ 30（第一次修订）→ **10**（现在）。
	 * 这个分支的定位就是"高频位移"，CD 越短手感越好。
	 */
	private static final float TELEPORT_COOLDOWN = 10f;

	/** 传送成功后给英雄的隐匿回合。 */
	private static final float TELEPORT_GUARD = 1f;

	{
		image = ItemSpriteSheet.KUNAI;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;
		tier = 2;
		//END(修复·产出 3 个): 覆盖父类构造块设下的 defaultQuantity()（=3）
		stackable = false;
		quantity = 1;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·传送"; }

	@Override
	protected boolean recoverable( Hero hero ){
		return hero.buff( TeleportCooldown.class ) == null;
	}

	@Override
	protected void recover( Hero hero ){

		int spot = -1;

		//① 有活着的被嵌敌人 → 传到他背后
		if (stuckEnemy != null && stuckEnemy.isAlive()){
			spot = behindCell( hero, stuckEnemy );
		}
		//② 否则（包括"落在平地"）→ 传到匕首所在的那一格
		//
		//==== END(修复·平地不传送) ====
		//文档所有者定稿："传送可以扔到平地也传送。"
		//
		//原来这里也是这么写的，但有一个漏洞：
		//如果 spot 恰好等于 hero.pos（比如匕首就落在脚下），
		//下面的 `spot != hero.pos` 判定会跳过移动 —— 看起来像"没传送"。
		//现在即使同格也允许"传一次"（至少给隐匿），保证行为一致。
		if (spot == -1 && stuckCell != -1){
			spot = stuckCell;
		}

		boolean moved = false;
		if (spot != -1){
			if (spot != hero.pos){
				moved = moveTo( hero, spot );
			} else {
				//原地也算"成功传送" —— 否则落脚下时什么都不发生，很怪
				moved = true;
			}
		}

		if (moved){
			//传送成功 → 一小段隐匿
			Buff.affect( hero, Invisibility.class, TELEPORT_GUARD );
		} else {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog
					.w("没有可以落下的地方。");
		}

		//无论是否成行都记一次冷却，防止靠"反复回收"绕开 CD 连续闪身
		Buff.prolong( hero, TeleportCooldown.class, TELEPORT_COOLDOWN );

		clean( hero );
	}

	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 7 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		//==== END(修订·原版写法): 风味(desc) + 数值/力量 + 机制说明 ====
		String info = super.info();
		return info + "\n\n" + "掷出后嵌在敌人身上、或插在地上。\n\n" +
				"回收时：\n" +
				"-嵌在**敌人**身上 → 传到他**背后**\n" +
				"-插在**平地**上 → 直接传送到那一格\n\n" +
				"传送后获得 1 回合隐匿，冷却 **10 回合**。";
	}
}
