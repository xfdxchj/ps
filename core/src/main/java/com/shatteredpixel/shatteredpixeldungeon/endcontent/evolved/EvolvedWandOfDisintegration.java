/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfDisintegration → 湮解法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：解离:命中得目标视野 2 回合 / 未命中 50% 概率不消耗充能。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class EvolvedWandOfDisintegration extends WandOfDisintegration {

	@Override
	public String name() {
		return "湮解法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10477823, 1.1f );
	}

	//END M2 真机制：命中得目标视野、未命中 50% 概率不耗充能
	@Override
	public void onZap(Ballistica beam) {

		boolean terrainAffected = false;

		int level = buffedLvl();

		int maxDistance = Math.min(distance(), beam.dist);

		ArrayList<Char> chars = new ArrayList<>();

		Blob web = Dungeon.level.blobs.get(Web.class);

		int terrainPassed = 2, terrainBonus = 0;
		for (int c : beam.subPath(1, maxDistance)) {

			Char ch;
			if ((ch = Actor.findChar( c )) != null) {

				//we don't want to count passed terrain after the last enemy hit.
				terrainBonus += terrainPassed/3;
				terrainPassed = terrainPassed%3;

				if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
						&& !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])){
					//avoid harming undiscovered passive chars
				} else {
					chars.add(ch);
				}
			}

			if (Dungeon.level.solid[c]) {
				terrainPassed++;
			}

			if (Dungeon.level.flamable[c]) {

				Dungeon.level.destroy( c );
				GameScene.updateMap( c );
				terrainAffected = true;

			}

			CellEmitter.center( c ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		int lvl = level + (chars.size()-1) + terrainBonus;
		for (Char ch : chars) {
			wandProc(ch, chargesPerCast());
			ch.damage( damageRoll(lvl), this );
			ch.sprite.centerEmitter().burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
			ch.sprite.flash();
		}

		//END M2：命中则获得目标视野 2 回合
		if (!chars.isEmpty()) {
			for (Char ch : chars) {
				Buff.append(Dungeon.hero, TalismanOfForesight.CharAwareness.class, 2).charID = ch.id();
			}
		} else {
			//END M2：未命中则有 50% 概率不消耗充能（返还 1 点充能）
			if (Random.Float() < 0.5f){
				partialCharge += 1f;
			}
		}
	}

	private int distance() {
		return buffedLvl()*2 + 6;
	}
	// ---- 终焉·进化基础(统一13把)：等级归零后强度锚定+8并成长+20%、最大充能20 ----
	@Override
	public int buffedLvl() {
		return 8 + Math.round(super.buffedLvl() * 1.2f);
	}

	@Override
	public void updateLevel() {
		maxCharges = 20;
		curCharges = Math.min(curCharges, maxCharges);
	}
}