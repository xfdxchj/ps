/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」999 层的爱丽丝 NPC
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AliceSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * END(挑战 129 心爱的少女): 999 层的爱丽丝。
 *
 * <h3>设定</h3>
 * 按文档所有者提供的《BLACK SOULS》人物设定：
 * "爱丽丝终于发觉了。这个不可思议的世界是伪造出来的。
 *   一切都不不过是被谁所创造出来的妄想罢了……自己也是如此。"
 *
 * <p>所以她的对话不谈战斗、不谈奖励 —— 她只是**告诉你真相**，
 * 然后把黑兔戒指给你，让你回去。
 *
 * <h3>行为</h3>
 * <ul>
 *   <li>**无敌**：不可被攻击，也不主动攻击</li>
 *   <li>**不动**：永远停在原地</li>
 *   <li>**点击对话**：靠近后点击她触发（走 WndOptions）</li>
 * </ul>
 */
public class Alice extends Mob {

	{
		spriteClass = AliceSprite.class;

		HP = HT = 1;                 //她不是用来打的
		EXP = 0;
		defenseSkill = 0;
		alignment = Alignment.NEUTRAL;

		state = PASSIVE;             //永远不主动行动
	}

	@Override
	public String name() {
		return "爱丽丝";
	}

	/** 免疫一切伤害 —— 她是"作者"，不该被剧中人伤到。 */
	@Override
	public void damage(int dmg, Object src) {
		//完全无视
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	/** 不参与寻路，永远站着。 */
	@Override
	protected boolean act() {
		spend(TICK);
		return true;
	}

	/**
	 * END(129): 玩家点击 / 靠近时触发对话。
	 *
	 * <p>用 {@code WndOptions} 而不是自定义窗口：对话是**单次**的
	 * （说完就给戒指并送走），不需要复杂的分支 UI。
	 */
	@Override
	public boolean interact(com.shatteredpixel.shatteredpixeldungeon.actors.Char c) {
		if (!(c instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) {
			return false;
		}
		talk((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) c);
		return true;
	}

	/** END(129): 爱丽丝的对话。 */
	public static void talk(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (hero == null) return;

		GameScene.show(new WndOptions(
				"爱丽丝",
				"爱丽丝终于发觉了。\n" +
				"这个不可思议的世界是伪造出来的。\n\n" +
				"一切都不过是被谁所创造出来的妄想罢了……\n" +
				"自己也是如此。\n\n" +
				"「你也发现了吧？那本书里的字，是别人写的。」",
				"「……是的。」",
				"「我不知道你在说什么。」"
		) {
			@Override
			protected void onSelect(int index) {
				//两个选项都给戒指 —— 承认与否不影响结局，
				//这本身就是"世界是伪造的"这个设定的体现。
				giveRingAndReturn();
			}
		});
	}

	/** END(129): 给出黑兔戒指，然后把玩家送回原位。 */
	private static void giveRingAndReturn() {
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero = Dungeon.hero;
		if (hero == null) return;

		//---- 给黑兔戒指 ----
		//"若已持有则转为等价魂" —— 本 fork 没有"魂"这个资源，
		//所以改为给等值的经验值作为替代（原表提到的 126 黑之魂系统尚未实装）。
		boolean alreadyHas = false;
		try {
			alreadyHas = hero.belongings.getItem(RabbitRing.class) != null;
		} catch (Throwable ignored) { }

		if (!alreadyHas) {
			RabbitRing ring = new RabbitRing();
			ring.identify();
			if (ring.collect()) {
				GLog.i("爱丽丝把一枚黑色的戒指放进了你手里。");
			} else {
				Dungeon.level.drop(ring, hero.pos).sprite.drop(hero.pos);
				GLog.i("爱丽丝把一枚黑色的戒指放在了地上。");
			}
		} else {
			//已持有 → 折算成经验
			hero.earnExp(Math.max(1, hero.lvl), Alice.class);
			GLog.i("你已经有那枚戒指了。爱丽丝把它化作了一点别的东西。");
		}

		//---- 送回原位 ----
		returnToOrigin(hero);
	}

	/** END(129): 把玩家送回使用《心爱的少女》之前的位置。 */
	public static void returnToOrigin(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		if (hero == null) return;

		final int depth = BelovedGirl.AliceReturn.returnDepth();
		final int branch = BelovedGirl.AliceReturn.returnBranch();
		final int pos = BelovedGirl.AliceReturn.returnPos();

		GLog.i("书页合上了。你回到了原来的地方。");

		//用 WndMessage 做一次确认，关闭时才真正切场景 ——
		//否则会在对话窗口还开着的时候直接换场景，视觉上很突兀。
		GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage(
				"书页合上了。")
		{
			@Override
			public void onBackPressed() {
				super.onBackPressed();

				//RETURN 模式 + returnDepth/Branch/Pos —— 这是原版
				//"从特殊层返回原位"的标准写法（参考 InterlevelScene 的字段定义）。
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
						com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.RETURN;
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnDepth = depth;
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnBranch = branch;
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.returnPos = pos;
				com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition = null;

				com.watabou.noosa.Game.switchScene(
						com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class);
			}
		});
	}
}
