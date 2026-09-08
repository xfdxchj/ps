/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * B2 湮解进化重新设计（取代原"看破视野/落空省充能"的 M2 描述）：
 *   湮解法杖 → 拥有人手可选的两种发射形态（在背包-法杖详情窗口切换 / 见 EndModeWand）：
 *
 *   形态 0·湮解·单线(default)：耗 1 充能。沿瞄准单线投出一束解离柱，
 *      可穿透沿途所有单位、轰开易燃地形；本形态伤害 ×1.2。
 *   形态 1·湮解·分裂：耗 1 充能。以瞄准方向为中心，另向对称 ±45° 各投一束，
 *      共 3 束(中心 + 左右)。三束各沿各自弹道穿透命中，本形态伤害不额外提升。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.HashSet;

public class EvolvedWandOfDisintegration extends WandOfDisintegration implements EndModeWand {

	private static final int MODE_LINE  = 0;
	private static final int MODE_SPLIT = 1;

	/** 分裂形态左右两侧相比瞄准偏转的角度(度)。 */
	private static final float SIDE_DEG = 45f;

	/** 当前形态(0=湮解·单线,1=湮解·分裂)。 */
	private int mode = MODE_LINE;

	@Override
	public String name() {
		return "湮解法杖";
	}

	@Override
	public String desc() {
		return "进化·湮解法杖（源：解离法杖）：拥有两种发射形态，可在背包-法杖窗口切换。\n\n"
				+ "▍形态 0·湮解·单线（默认，耗 1 充）：一条穿透直射柱，越穿越痛，伤害 ×1.2。\n"
				+ "▍形态 1·湮解·分裂（耗 1 充）：在瞄准方向双侧各偏转 45° 补两条，共 3 条穿多段覆盖多目标。\n\n"
				+ "两形态弹壁均可摧毁易燃地形；射程随 buffedLvl ×2 + 6；充能上限 20、真实等级 +8。";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10477823, 1.1f );
	}

	/* ---------------- EndModeWand ---------------- */
	@Override
	public int modeCount() {
		return 2;
	}
	@Override
	public int modeIndex() {
		return mode;
	}
	@Override
	public void setModeIndex( int index ) {
		if (index < 0 || index >= modeCount()) index = 0;
		this.mode = index;
	}
	@Override
	public String modeName( int index ) {
		switch (index){
			case MODE_LINE:  return "湮解·单线";
			case MODE_SPLIT: return "湮解·分裂";
			default:         return "";
		}
	}

	/* ---------------- 充能/伤害 ---------------- */

	/**
	 * 充能消耗：单线与分裂形态都只耗 1 点（分裂不再额外 +1）。
	 * 取舍由伤害倍率体现（见 damageScale）。
	 */
	@Override
	protected int chargesPerCast() {
		return 1;
	}

	/** 伤害倍率：普通·单线 +20%；分裂(三束)不提升(收益在多目标覆盖)。 */
	private float damageScale() {
		return mode == MODE_LINE ? 1.2f : 1f;
	}

	private int distance() {
		return buffedLvl()*2 + 6;
	}

	/* ---------------- 三束/单束 ---------------- */

	/** 当前形态应实际射出的光束们。单线只有瞄准线；分裂再加两条对称 ±SIDE_DEG 的旋转束。 */
	private ArrayList<Ballistica> beams( Ballistica aim ){

		ArrayList<Ballistica> list = new ArrayList<>();
		list.add( aim );

		if (mode == MODE_SPLIT){
			Ballistica left  = rotatedBeam( aim, aim.sourcePos, -SIDE_DEG );
			Ballistica right = rotatedBeam( aim, aim.sourcePos, +SIDE_DEG );
			if (left  != null) list.add( left );
			if (right != null) list.add( right );
		}
		return list;
	}

	/**
	 * 把「施法者 -> 瞄准落点」这条核心方向绕施法者旋转 deg 度，再以同射程打一条新的穿透弹道。
	 * 返回 null 当落点格被夹回地图后与施法者重叠(几乎不会)。
	 */
	private Ballistica rotatedBeam( Ballistica aim, int from, float deg ){

		PointF fromP = centre( from );
		PointF toP   = centre( aim.collisionPos );

		float base = PointF.angle( fromP, toP );
		float ang  = base + deg * PointF.G2R;

		float reach = distance() + 0.5f;
		PointF scan = new PointF( fromP.x + (float)Math.cos(ang)*reach,
		                          fromP.y + (float)Math.sin(ang)*reach );

		int nx = (int)GameMath.gate( 0, (int)Math.floor( scan.x ), Dungeon.level.width()-1 );
		int ny = (int)GameMath.gate( 0, (int)Math.floor( scan.y ), Dungeon.level.height()-1 );
		int cell = Dungeon.level.pointToCell( new Point( nx, ny ) );

		return new Ballistica( from, cell, Ballistica.WONT_STOP );
	}

	private static PointF centre( int cell ){
		PointF p = new PointF( Dungeon.level.cellToPoint( cell ) );
		p.x += 0.5f; p.y += 0.5f;
		return p;
	}

	/* ---------------- 施放 ---------------- */

	@Override
	public void onZap(Ballistica beam) {

		boolean terrainAffected = false;
		HashSet<Integer> destroyed = new HashSet<>();   //同一易燃格只摧毁一次

		//横跨全部光束去重收集要吃的角色
		ArrayList<Char> affected = new ArrayList<>();

		for (Ballistica b : beams( beam )){

			int maxD = Math.min( distance(), b.dist );

			for (int c : b.subPath( 1, maxD )) {

				Char ch = Actor.findChar( c );
				if (ch != null && c != beam.sourcePos){
					// 别伤害未被发现且处于被动状态的怪物
					boolean hiddenPassive = (ch instanceof Mob)
							&& ((Mob)ch).state == ((Mob)ch).PASSIVE
							&& !(Dungeon.level.mapped[c] || Dungeon.level.visited[c]);
					if (!hiddenPassive && !containsAffected( ch, affected )){
						affected.add( ch );
					}
				}

				if (Dungeon.level.flamable[c] && !destroyed.contains( c )) {
					Dungeon.level.destroy( c );
					GameScene.updateMap( c );
					destroyed.add( c );
					terrainAffected = true;
				}

				CellEmitter.center( c ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
			}
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		if (!affected.isEmpty()) {
			float scale = damageScale();
			//每多命中一个单位，本发伤害等级随之提高(继承父类"越穿越痛"基调)；每只各自独立roll。
			for (int i = 0; i < affected.size(); i++) {
				Char ch = affected.get( i );
				int lvl = buffedLvl() + i;
				wandProc( ch, chargesPerCast() );
				ch.damage( Math.round( damageRoll( lvl ) * scale ), this );
				ch.sprite.centerEmitter().burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
				ch.sprite.flash();
			}
		}

	}

	private static boolean containsAffected( Char ch, ArrayList<Char> list ){
		int id = ch.id();
		for (Char c : list) if (c.id() == id) return true;
		return false;
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {

		for (Ballistica b : beams( beam )){
			int cell = b.path.get( Math.min( b.dist, distance() ) );
			curUser.sprite.parent.add(
					new Beam.DeathRay( curUser.sprite.center(),
							DungeonTilemap.raisedTileCenterToWorld( cell ) ) );
		}

		Sample.INSTANCE.play( Assets.Sounds.RAY );
		callback.call();
	}

	// ---- 形态持久化 ----
	private static final String MODE = "end_mode";
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MODE, mode );
	}
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		mode = bundle.getInt( MODE );
		if (mode < 0 || mode >= modeCount()) mode = 0;
	}

	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}
