package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.ExecutionCooldown;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(刺杀匕首·刺杀分支): 刺杀·处决。
 *
 * <h3>文档所有者定稿</h3>
 * <ul>
 *   <li>**传送斩杀 25% 生命以下的小怪**（两个效果都有）</li>
 *   <li>冷却 **20 回合**</li>
 * </ul>
 *
 * <h3>与"刺杀·传送"的区别</h3>
 * <pre>
 *   刺杀·传送   CD 10   传送之后什么都不做（纯位移）
 *   刺杀·处决   CD 20   传送之后**顺手斩杀**残血小怪
 * </pre>
 * 所以处决是"位移 + 收割"的合体，CD 翻倍是合理的代价。
 *
 * <h3>斩杀条件</h3>
 * <ul>
 *   <li>被嵌住的敌人**还活着**</li>
 *   <li>**不是 Boss / mini-Boss**（文档所有者说"小怪"）</li>
 *   <li>生命 **低于上限的 25%**</li>
 * </ul>
 * 不满足就只做传送 + 一次普通伤害，不浪费这一刀。
 */
public class DaggerExecution extends EmbedDagger {

	/** 处决阈值: 生命低于上限 25%。 */
	private static final float EXECUTE_FRACTION = 0.25f;

	/**
	 * END(修订·文档所有者定稿): 冷却 **20 回合**。
	 *
	 * <p>沿革：50（初版）→ **20**（现在）。
	 */
	private static final float EXECUTION_COOLDOWN = 20f;

	/** 传送后给的隐匿回合。 */
	private static final float TELEPORT_GUARD = 1f;

	{
		image = ItemSpriteSheet.THROWING_KNIFE;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.95f;
		tier = 2;
		//END(修复·产出 3 个): 覆盖父类构造块设下的 defaultQuantity()（=3）
		stackable = false;
		quantity = 1;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·处决"; }

	@Override
	protected boolean recoverable( Hero hero ){
		return hero.buff( ExecutionCooldown.class ) == null;
	}

	@Override
	protected void recover( Hero hero ){

		//==== END(修复·补上传送) ====
		//文档所有者定稿："（三个分支）都有传送功能。"
		//
		//原来这个类**完全不做位移** —— 只判定处决。
		//于是它名义上是"刺杀匕首的进阶"，实际却失去了刺杀匕首最核心的传送。
		//
		//现在：先传送（与 DaggerTeleport 同一套逻辑），再判定处决。
		int spot = -1;
		if (stuckEnemy != null && stuckEnemy.isAlive()){
			spot = behindCell( hero, stuckEnemy );   //敌人背面优先
		}
		if (spot == -1 && stuckCell != -1){
			spot = stuckCell;                        //敌人已离场/落平地 → 落到那一格
		}

		boolean moved = false;
		if (spot != -1){
			moved = (spot == hero.pos) || moveTo( hero, spot );
		}
		if (moved){
			Buff.affect( hero, Invisibility.class, TELEPORT_GUARD );
		}

		//---- 传送之后再判定处决 ----
		Char enemy = stuckEnemy;
		if (enemy == null || !enemy.isAlive()){
			//没有可斩的目标 → 只完成传送
			Buff.prolong( hero, ExecutionCooldown.class, EXECUTION_COOLDOWN );
			clean( hero );
			return;
		}

		//仅对易受处决的残血且非 Boss 目标直接处决
		boolean execute = !Char.hasProp( enemy, Char.Property.BOSS )
				&& !Char.hasProp( enemy, Char.Property.MINIBOSS )
				&& enemy.HP > 0
				&& enemy.HP < enemy.HT * EXECUTE_FRACTION;

		if (execute){
			//沿用 SPD 处决既有的样板(见 Preparation 杀伤路径): 置零生命并走标准死亡流程
			enemy.HP = 0;

			if (enemy.buff( Brute.BruteRage.class ) != null){
				enemy.buff( Brute.BruteRage.class ).detach();
			}

			if (!enemy.isAlive()){
				enemy.die( hero );
			} else {
				//helps with triggering any on-damage effects that need to activate
				enemy.damage( -1, hero );
				DeathMark.processFearTheReaper( enemy );
			}

			com.shatteredpixel.shatteredpixeldungeon.utils.GLog
					.p("刺杀。");
		} else {
			//未到阈值则收刀时给一下普通伤害(不漏刀), 不强杀
			int dmg = Math.round( damageRoll( hero ) + enemy.HT * 0.1f );
			enemy.damage( Math.max( 1, dmg ), this );
		}

		//写冷却, 收口连续处决
		Buff.prolong( hero, ExecutionCooldown.class, EXECUTION_COOLDOWN );

		clean( hero );
	}

	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 8 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		//==== END(修订·原版写法): 风味(desc) + 数值/力量 + 机制说明 ====
		String info = super.info();
		return info + "\n\n" + "掷出后嵌在敌人身上、或插在地上。\n\n" +
				"回收时：\n" +
				"-**传送到敌人背后**（或匕首落点）\n" +
				"-若目标**生命低于 25%** 且不是 Boss，**直接斩杀**\n\n" +
				"传送后获得 1 回合隐匿，冷却 **20 回合**。\n" +
				"适合收残血，也适合强行切入。";
	}
}
