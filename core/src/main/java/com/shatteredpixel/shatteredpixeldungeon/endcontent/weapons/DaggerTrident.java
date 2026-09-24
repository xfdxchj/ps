package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.TeleportCooldown;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(刺杀匕首·三叉戟分支): 刺杀·三叉戟。
 *
 * <h3>文档所有者定稿</h3>
 * <ul>
 *   <li>**数值变为三叉戟**（比基底匕首重且狠）</li>
 *   <li>**也有传送功能**（三个分支都有）</li>
 *   <li>冷却 **20 回合**</li>
 * </ul>
 *
 * <h3>与另外两个分支的区别</h3>
 * <pre>
 *   刺杀·传送   CD 10   纯位移，数值最轻
 *   刺杀·三叉戟 CD 20   **位移 + 高数值**（一掷就是重击）
 *   刺杀·处决   CD 20   位移 + 斩杀残血
 * </pre>
 * 三叉戟是"靠面板取胜"的那一支：没有斩杀那样的花活，
 * 但每一掷的伤害都比另两个高一截，而且同样能传送。
 *
 * <h3>为什么改成继承 EmbedDagger</h3>
 * 原来它继承 {@code MissileWeapon} —— 那是**普通投掷武器**，
 * 掷出后不会"嵌住"，自然也就没有"回收"动作，无法传送。
 * 改继承 {@link EmbedDagger} 之后它就自动获得了
 * "投掷 → 嵌住 → 回收"这一整套（传送正是挂在回收上的）。
 */
public class DaggerTrident extends EmbedDagger {

	/**
	 * END(修订·文档所有者定稿): 冷却 **20 回合**。
	 *
	 * <p>与刺杀分支同档 —— 两者都是"传送 + 额外收益"，
	 * 只有纯传送的 DaggerTeleport 是 10。
	 */
	private static final float TELEPORT_COOLDOWN = 20f;

	/** 传送后给的隐匿回合。 */
	private static final float TELEPORT_GUARD = 1f;

	{
		image = ItemSpriteSheet.TRIDENT;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		stackable = false;
		//END(修复·产出 3 个): MissileWeapon 的构造块会把 quantity 设成
		//defaultQuantity()（原版投掷武器 = 3）。这里显式设成 1。
		quantity = 1;
		bones = false;
		tier = 3;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·三叉戟"; }

	//==== 数值：三叉戟那套（比基底匕首重且狠）====
	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 14 + 4*lvl; }
	@Override public int STRReq(int lvl){ return 12; }

	//无限耐久
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	//==================================================================
	//传送（三个分支都有）
	//==================================================================

	@Override
	protected boolean recoverable( Hero hero ){
		return hero.buff( TeleportCooldown.class ) == null;
	}

	@Override
	protected void recover( Hero hero ){

		int spot = -1;
		if (stuckEnemy != null && stuckEnemy.isAlive()){
			spot = behindCell( hero, stuckEnemy );   //敌人背面优先
		}
		if (spot == -1 && stuckCell != -1){
			spot = stuckCell;                        //敌人离场 / 落平地 → 落到那一格
		}

		boolean moved = false;
		if (spot != -1){
			moved = (spot == hero.pos) || moveTo( hero, spot );
		}

		if (moved){
			Buff.affect( hero, Invisibility.class, TELEPORT_GUARD );
		}

		//被嵌住的敌人：回收时再补一记重击（三叉戟的"力道"体现）
		Char enemy = stuckEnemy;
		if (enemy != null && enemy.isAlive()){
			enemy.damage( Math.max( 1, damageRoll( hero ) ), this );
		}

		Buff.prolong( hero, TeleportCooldown.class, TELEPORT_COOLDOWN );
		clean( hero );
	}

	@Override public String info(){
		//==== END(修订·原版写法): 风味(desc) + 数值/力量 + 机制说明 ====
		String info = super.info();
		return info + "\n\n" + "铸造精悍的三叉戟：出手厚重，一掷就是重击。\n\n" +
				"回收时：\n" +
				"-**传送到敌人背后**（或匕首落点）\n" +
				"-并对被嵌住的目标**再补一记伤害**\n\n" +
				"传送后获得 1 回合隐匿，冷却 **20 回合**。";
	}
}
