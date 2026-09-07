/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfTransfusion → 汲魂法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：注魂:获护盾时吸20%生命。
 * 实现：完整复制 onZap/onHit，在玩家获得护盾的分支额外按护盾值 20% 恢复生命。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class EvolvedWandOfTransfusion extends WandOfTransfusion {

	@Override
	public String name() {
		return "汲魂法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 14715530, 1.5f );
	}

	private boolean freeCharge = false;

	@Override
	public void onZap(Ballistica beam) {

		for (int c : beam.subPath(0, beam.dist))
			CellEmitter.center(c).burst( BloodParticle.BURST, 1 );

		int cell = beam.collisionPos;

		Char ch = Actor.findChar(cell);

		if (ch instanceof Mob){

			wandProc(ch, chargesPerCast());

			//this wand does different things depending on the target.

			//heals/shields an ally or a charmed enemy while damaging self
			if (ch.alignment == Char.Alignment.ALLY || ch.buff(Charm.class) != null){

				// 5% of max hp
				int selfDmg = Math.round(curUser.HT*0.05f);

				int healing = selfDmg + 3*buffedLvl();
				int shielding = (ch.HP + healing) - ch.HT;
				if (shielding > 0){
					healing -= shielding;
					Buff.affect(ch, Barrier.class).setShield(shielding);
				} else {
					shielding = 0;
				}

				ch.HP += healing;

				ch.sprite.emitter().burst(Speck.factory(Speck.HEALING), 2 + buffedLvl() / 2);
				if (healing > 0) {
					ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);
				}
				if (shielding > 0){
					ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shielding), FloatingText.SHIELDING);
				}

				if (!freeCharge) {
					damageHero(selfDmg);
				} else {
					freeCharge = false;
				}

			//for enemies...
			//(or for mimics which are hiding, special case)
			} else if (ch.alignment == Char.Alignment.ENEMY || ch instanceof Mimic) {

				//grant a self-shield, and...
				int shield = 5 + buffedLvl();
				Buff.affect(curUser, Barrier.class).setShield(shield);
				curUser.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);

				//END M2：获得护盾时吸取护盾值 20% 的生命
				int lifeGain = Math.max(1, Math.round(shield * 0.2f));
				curUser.HP = Math.min(curUser.HT, curUser.HP + lifeGain);
				curUser.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(lifeGain), FloatingText.HEALING);

				//charms living enemies
				if (!ch.properties().contains(Char.Property.UNDEAD)) {
					Charm charm = Buff.affect(ch, Charm.class, Charm.DURATION/2f);
					charm.object = curUser.id();
					charm.ignoreHeroAllies = true;
					ch.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 3 );

				//harms the undead
				} else {
					ch.damage(damageRoll(), this);
					ch.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10 + buffedLvl());
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				}

			}

		}

	}

	//this wand costs health too
	private void damageHero(int damage){

		curUser.damage(damage, this);

		if (!curUser.isAlive()){
			Badges.validateDeathFromFriendlyMagic();
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "ondeath") );
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		if (defender.buff(Charm.class) != null && defender.buff(Charm.class).object == attacker.id()){
			//grants a free use of the staff and shields self
			freeCharge = true;
			int shieldToGive = Math.round((2*(5 + buffedLvl()))*procChanceMultiplier(attacker));
			Buff.affect(attacker, Barrier.class).setShield(shieldToGive);
			attacker.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);

			//END M2：获得护盾时吸取护盾值 20% 的生命
			int lifeGain = Math.max(1, Math.round(shieldToGive * 0.2f));
			attacker.HP = Math.min(attacker.HT, attacker.HP + lifeGain);
			attacker.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(lifeGain), FloatingText.HEALING);

			GLog.p( Messages.get(this, "charged") );
			attacker.sprite.emitter().burst(BloodParticle.BURST, 20);
		}
	}

	private static final String FREECHARGE = "freecharge";

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		freeCharge = bundle.getBoolean( FREECHARGE );
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( FREECHARGE, freeCharge );
	}

	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}