/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfWarding -> 灵哨法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：哨戒:消耗额外充能直接生成高阶段哨兵。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.GameMath;

public class EvolvedWandOfWarding extends WandOfWarding {

	@Override
	public String name() {
		return "灵哨法杖";
	}

	@Override
	public String desc() {
		return "进化·灵哨法杖：发射时可消耗当前充能的一部分（30%，上限 4）一次生成更高阶的守卫哨兵；对已在场的哨兵瞄准时可在守卫能量允许内升级或治疗它，并能同时维持的哨兵总能量上限提升 30%。\n\n**继承源法杖的等级**；充能上限提升到 20。";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 9090280, 1.3f );
	}

	/** END(一.12): 当前这一发是否瞄的是已有哨位（升级/治疗只应耗 1 充能）。 */
	private boolean targetingWard = false;

	@Override
	public boolean tryToZap(Hero owner, int target) {
		targetingWard = Actor.findChar(target) instanceof WandOfWarding.Ward;
		return super.tryToZap(owner, target);
	}

	/** END(一.12): 哨位总能量上限 +30%。 */
	@Override
	protected int maxWardEnergy() {
		return (int)Math.floor(super.maxWardEnergy() * 1.3f);
	}

	//结束扩展 M2：消耗当前充能的 30%（1~4），仿照火焰法杖；充能越多，生成的哨兵阶位越高。
	//END(一.12): 对已有哨位使用时只耗 1 充能。
	@Override
	protected int chargesPerCast() {
		if (cursed || targetingWard ||
				(charger != null && charger.target != null && charger.target.buff(WildMagic.WildMagicTracker.class) != null)){
			return 1;
		}
		return (int) GameMath.gate(1, (int)Math.ceil(curCharges*0.3f), 4);
	}

	@Override
	public void onZap(Ballistica bolt) {

		int target = bolt.collisionPos;
		Char ch = Actor.findChar(target);
		if (ch != null && !(ch instanceof WandOfWarding.Ward)){
			if (bolt.dist > 1) target = bolt.path.get(bolt.dist-1);

			ch = Actor.findChar(target);
			if (ch != null && !(ch instanceof WandOfWarding.Ward)){
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(bolt.collisionPos);
				return;
			}
		}

		if (ch != null){
			if (ch instanceof WandOfWarding.Ward){
				if (wardAvailable) {
					((WandOfWarding.Ward) ch).upgrade( buffedLvl() );
				} else {
					((WandOfWarding.Ward) ch).wandHeal( buffedLvl() );
				}
				ch.sprite.emitter().burst(MagicMissile.WardParticle.UP, ((WandOfWarding.Ward) ch).tier);
			} else {
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(target);
			}

		} else if (!Dungeon.level.passable[target]){
			GLog.w( Messages.get(this, "bad_location"));
			Dungeon.level.pressCell(target);

		} else {
			//结束扩展 M2：新建哨兵时，按消耗充能数直接生成对应阶位(tier)。
			WandOfWarding.Ward ward = new WandOfWarding.Ward();
			ward.pos = target;
			ward.wandHeal( buffedLvl() );                 //tier 1：仅写入 wandLevel，不升阶
			int cpc = chargesPerCast();
			for (int i = 1; i < cpc; i++){
				ward.upgrade( buffedLvl() );               //每多消耗 1 充能，初始阶位 +1
			}
			GameScene.add(ward, 1f);
			Dungeon.level.occupyCell(ward);
			ward.sprite.emitter().burst(MagicMissile.WardParticle.UP, ward.tier);
			Dungeon.level.pressCell(target);
		}
	}

	// ---- 终焉·进化基础(统一13把)：继承源法杖等级、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}