/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfCorrosion → 蚀骨法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：腐蚀:命中附加1回合缠绕(新增 Roots)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class EvolvedWandOfCorrosion extends WandOfCorrosion {

	@Override
	public String name() {
		return "蚀骨法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10215546, 1.6f );
	}

	//END M2 真机制：命中目标（3×3 毒云覆盖内）额外附加 1 回合缠绕
	@Override
	public void onZap(Ballistica bolt) {
		CorrosiveGas gas = Blob.seed(bolt.collisionPos, 50 + 10 * buffedLvl(), CorrosiveGas.class);
		CellEmitter.get(bolt.collisionPos).burst(Speck.factory(Speck.CORROSION), 10 );
		gas.setStrength(2 + buffedLvl(), getClass());
		GameScene.add(gas);
		Sample.INSTANCE.play(Assets.Sounds.GAS);

		for (int i : PathFinder.NEIGHBOURS9) {
			Char ch = Actor.findChar(bolt.collisionPos + i);
			if (ch != null) {
				wandProc(ch, chargesPerCast());
				Buff.affect(ch, Roots.class, 1f);

				if (i == 0 && ch instanceof DwarfKing){
					Statistics.qualifiedForBossChallengeBadge = false;
				}
			}
		}

		if (Actor.findChar(bolt.collisionPos) == null){
			Dungeon.level.pressCell(bolt.collisionPos);
		}
	}
	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}