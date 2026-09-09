package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.ExecutionCooldown;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 刺杀·处决（成品，嵌入→回收 model）。
 * <p>投掷后匕首嵌进敌人/落地。从物品的“回收(DAG_REC)”动作拔出时:
 * 若被嵌住的敌人仍存活、非 Boss / mini-Boss 且生命已跌破上限 25%,
 * 即对其执行处决(沿用 SPD 处决样板: 置零并走标准死亡流程), 并写 ExecutionCooldown。
 * 否则不强杀、只结算普通一击并收刀。不进行任何位移。
 * <p>数值如普通投掷匕首：非堆叠、非骨、力量 10、无限耐久。
 */
public class DaggerExecution extends EmbedDagger {

	//处决阈值: 生命低于上限 25%
	private static final float EXECUTE_FRACTION = 0.25f;
	//处决冷却(回合)
	private static final float EXECUTION_COOLDOWN = 50f;

	{
		image = ItemSpriteSheet.THROWING_KNIFE;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.95f;
		tier = 2;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·处决"; }

	@Override
	protected boolean recoverable( Hero hero ){
		return hero.buff( ExecutionCooldown.class ) == null;
	}

	@Override
	protected void recover( Hero hero ){

		Char enemy = stuckEnemy;
		if (enemy == null || !enemy.isAlive()){
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

			//写处决冷却, 收口连续处决
			Buff.affect( hero, ExecutionCooldown.class, EXECUTION_COOLDOWN );
		} else {
			//未到阈值则收刀时给一下普通伤害(不漏刀), 不强杀, 同样记一次处决冷却
			int dmg = Math.round( damageRoll( hero ) + enemy.HT * 0.1f );
			enemy.damage( Math.max( 1, dmg ), this );
			Buff.affect( hero, ExecutionCooldown.class, EXECUTION_COOLDOWN );
		}

		clean( hero );
	}

	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 8 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "嵌在敌人身上的处决刃: 拔出(回收)时, 若它残血跌破 25% 便将其直接处决。" +
				"\n对 Boss / mini-Boss 无效; 每次处决受‘处决冷却’约束, 适合用来收割而非拔刀起手。";
	}
}
