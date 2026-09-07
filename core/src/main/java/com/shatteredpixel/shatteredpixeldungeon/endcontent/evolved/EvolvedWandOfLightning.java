/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfLightning → 雷髓法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：闪电:受自电伤转化为等量护盾。
 * 实现：完整复制 onZap/fx/arc，将"施法者自身受到电伤"分支改为获得等量护盾。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class EvolvedWandOfLightning extends WandOfLightning {

	@Override
	public String name() {
		return "雷髓法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		// 独特的电蓝/白弧光泽
		return new ItemSprite.Glowing( 0x9FF5FF, 1.2f );
	}

	private ArrayList<Char> affected = new ArrayList<>();

	private ArrayList<Lightning.Arc> arcs = new ArrayList<>();

	@Override
	public void onZap(Ballistica bolt) {

		for (Char ch : affected.toArray(new Char[0])){
			if (ch != curUser && ch.alignment == curUser.alignment && ch.pos != bolt.collisionPos){
				affected.remove(ch);
			} else if (ch.buff(LightningCharge.class) != null){
				affected.remove(ch);
			}
		}

		//lightning deals less damage per-target, the more targets that are hit.
		float multiplier = 0.4f + (0.6f/affected.size());
		//if the main target is in water, all affected take full damage
		if (Dungeon.level.water[bolt.collisionPos]) multiplier = 1f;

		for (Char ch : affected){
			if (ch == Dungeon.hero) PixelScene.shake( 2, 0.3f );
			ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
			ch.sprite.flash();

			wandProc(ch, chargesPerCast());
			if (ch == curUser && ch.isAlive()) {
				//END M2：受自身闪电伤害转化为等量护盾
				int selfDmg = Math.round(damageRoll() * multiplier);
				int shield = Math.max(1, Math.round(selfDmg * 0.40f));
				Buff.affect(ch, Barrier.class).setShield(shield);
				ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
			} else {
				ch.damage(Math.round(damageRoll() * multiplier), this);
			}
		}
	}

	private void arc( Char ch ) {

		int dist = Dungeon.level.water[ch.pos] ? 2 : 1;

		if (curUser.buff(LightningCharge.class) != null){
			dist++;
		}

		ArrayList<Char> hitThisArc = new ArrayList<>();
		PathFinder.buildDistanceMap( ch.pos, BArray.not( Dungeon.level.solid, null ), dist );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE){
				Char n = Actor.findChar( i );
				if (n == Dungeon.hero && PathFinder.distance[i] > 1)
					//the hero is only zapped if they are adjacent
					continue;
				else if (n != null && !affected.contains( n )) {
					hitThisArc.add(n);
				}
			}
		}

		affected.addAll(hitThisArc);
		for (Char hit : hitThisArc){
			arcs.add(new Lightning.Arc(ch.sprite.center(), hit.sprite.center()));
			arc(hit);
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {

		affected.clear();
		arcs.clear();

		int cell = bolt.collisionPos;

		Char ch = Actor.findChar( cell );
		if (ch != null) {
			if (ch instanceof DwarfKing){
				Statistics.qualifiedForBossChallengeBadge = false;
			}

			affected.add( ch );
			arcs.add( new Lightning.Arc(curUser.sprite.center(), ch.sprite.center()));
			arc(ch);
		} else {
			arcs.add( new Lightning.Arc(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(bolt.collisionPos)));
			CellEmitter.center( cell ).burst( SparkParticle.FACTORY, 3 );
		}

		//don't want to wait for the effect before processing damage.
		curUser.sprite.parent.addToFront( new Lightning( arcs, null ) );
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		callback.call();
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