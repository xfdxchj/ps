package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.TeleportCooldown;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 刺杀·传送（成品，嵌入→回收 model）。
 * <p>投掷后匕首嵌在敌人身上或落在格上。通过物品的“回收(DAG_REC)”动作拔出时:
 * <ul><li>若能换到位, 将英雄传送到底被嵌敌人的背后(或当其已离场, 传至嵌着它的方格);</li>
 *     <li>传送成功后奖励一段隐匿;</li>
 *     <li>随即写入长的 TeleportCooldown, 并清空嵌住状态。</li></ul>
 * 数值如普通投掷匕首：非堆叠、非骨、力量 10、无限耐久。
 */
public class DaggerTeleport extends EmbedDagger {

	//冷却(回合)：两次“回收传送”之间需隔这么久
	private static final float TELEPORT_COOLDOWN = 100f;
	//传送成功后给英雄的隐匿回合
	private static final float TELEPORT_GUARD = 1f;

	{
		image = ItemSpriteSheet.KUNAI;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;
		tier = 2;
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
		if (stuckEnemy != null && stuckEnemy.isAlive()){
			spot = behindCell( hero, stuckEnemy );   //敌人背面 > any, 让其落稳
		}
		if (spot == -1 && stuckCell != -1){
			spot = stuckCell;                         //敌人已离场时, 直接回到嵌住它的方格
		}

		boolean moved = false;
		if (spot != -1 && spot != hero.pos){
			moved = moveTo( hero, spot );
		}

		if (moved){
			//真正到了背后, 才给英雄一小段隐匿；落空就不给（避免无谓 bug）。
			Buff.affect( hero, Invisibility.class, TELEPORT_GUARD );
		}

		//无论是否成行都记一次冷却, 防止每次“回收”都可以绕开传送 CD 连续闪身。
		Buff.affect( hero, TeleportCooldown.class, TELEPORT_COOLDOWN );

		clean( hero );
	}

	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 7 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "掷出并嵌在敌人身上的传送刃。拔出(回收)时把你传送到它背后再退入暗影，" +
				"\n两次传送之间受‘传送冷却’约束, 每次都得想好避开哪一侧。";
	}
}
