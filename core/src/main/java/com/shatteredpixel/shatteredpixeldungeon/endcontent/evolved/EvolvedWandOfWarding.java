/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfWarding → 灵哨法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：哨戒:消耗额外充能直接生成高阶段哨兵。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
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
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 9090280, 1.3f );
	}

	//结束扩展 M2：消耗当前充能的 30%（1~3），仿照火焰法杖；充能越多，生成的哨兵阶位越高。
	@Override
	protected int chargesPerCast() {
		if (cursed ||
				(charger != null && charger.target != null && charger.target.buff(WildMagic.WildMagicTracker.class) != null)){
			return 1;
		}
		return (int) GameMath.gate(1, (int)Math.ceil(curCharges*0.3f), 3);
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
				if (wardAvailable()) {
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

	//父类 wardAvailable 为 private，跨包不可见，这里按相同规则重新计算。
	private boolean wardAvailable() {
		int currentWardEnergy = 0;
		for (Char c : Actor.chars()){
			if (c instanceof WandOfWarding.Ward){
				currentWardEnergy += ((WandOfWarding.Ward) c).tier;
			}
		}
		if (Stasis.getStasisAlly() instanceof WandOfWarding.Ward){
			currentWardEnergy += ((WandOfWarding.Ward) Stasis.getStasisAlly()).tier;
		}

		int maxWardEnergy = 0;
		for (Buff buff : curUser.buffs()){
			if (buff instanceof Wand.Charger){
				if (((Wand.Charger) buff).wand() instanceof WandOfWarding){
					maxWardEnergy += 2 + ((Wand.Charger) buff).wand().level();
				}
			}
		}

		return currentWardEnergy < maxWardEnergy;
	}
	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}