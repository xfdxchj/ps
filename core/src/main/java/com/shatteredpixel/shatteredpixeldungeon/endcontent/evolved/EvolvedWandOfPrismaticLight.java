/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * B4 棱光进化重新设计（取代原"+30% 伤害"的 M2 描述）：
 *   棱辉法杖 → 「灵光光束」，拥有人手可选的两种发射形态（在背包-法杖详情窗口切换）：
 *
 *   形态 0·普攻直射(default)：耗 1 充能。退回父类的单目标直射——照亮地形浅层，
 *      只命中瞄准落点上的一个敌对单位，附带致盲/对亡灵·恶魔增伤（基础 affectTarget 语义）。
 *   形态 1·灵光光束：耗 3 充能。沿瞄准方向打出一道 3 格宽、可穿透的矩形光带
 *      (核心束 + 左右两条垂直偏移平行束)，对光带内每一个敌对单位独立结算
 *      affectTarget(致盲与增伤)，并照亮点亮光带地形。
 *
 * 上述是哪一种由 modeIndex 决定（非按剩余充能自动判断），方便战斗中按需手动切换。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.HashSet;

public class EvolvedWandOfPrismaticLight extends WandOfPrismaticLight implements EndModeWand {

	private static final int MODE_BASIC = 0;
	private static final int MODE_BEAM  = 1;

	/** 灵光光束名义"宽度"格数(3格宽矩形光带：核心束+左右各1条垂直偏移束)。 */
	private static final int BEAM_WIDTH = 3;

	/** 当前形态(0=普攻直射,1=灵光光束)。 */
	private int mode = MODE_BASIC;

	/** 本发施放实际展开的光带格集合(仅灵光形态)。 */
	private transient HashSet<Integer> beamCells;

	@Override
	public String name() {
		return "棱辉法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 16762598, 1.2f );
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
			case MODE_BASIC: return "普攻·直射";
			case MODE_BEAM:  return "灵光光束";
			default:         return "";
		}
	}

	/* ---------------- 充能/形态 ---------------- */

	/**
	 * 棱光每次施放统一耗 1 点充能（普攻与灵光光束都 1）。
	 * Wand 的施放门槛(tryToZap)与扣充(wandUsed)都走这一处。
	 */
	@Override
	protected int chargesPerCast() {
		return 1;
	}

	/** 灵光光束形态瞄准需能穿过单位(只有实墙才停)，否则退回父类的魔弹式单点停止瞄准。 */
	@Override
	public int collisionProperties( int target ){
		if (mode == MODE_BEAM){
			return Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID;
		}
		return super.collisionProperties( target );
	}

	/* ---------------- 施放 ---------------- */

	@Override
	public void onZap(Ballistica beam) {

		if (mode == MODE_BEAM) {
			onZapBeam( beam );
		} else {
			onZapBasic( beam );
		}

		if (Dungeon.level.viewDistance < 6 ) {
			if (Dungeon.isChallenged(Challenges.DARKNESS)){
				Buff.prolong( curUser, Light.class, 2f + buffedLvl());
			} else {
				Buff.prolong( curUser, Light.class, 10f+buffedLvl()*5);
			}
		}
	}

	/** 普攻直射：命中瞄准落点一个敌对单位，并照亮落点 3x3 邻域（不穿透/不扩散）。 */
	private void onZapBasic( Ballistica beam ){
		// 照亮瞄准落点 3x3 邻接(基础 affectMap 同款照点亮开）。
		revealAround( beam.collisionPos );

		Char ch = Actor.findChar( beam.collisionPos );
		if (ch != null && ch != curUser) {
			wandProc( ch, chargesPerCast() );
			hitTarget( ch );
		}
		CellEmitter.center( beam.collisionPos ).burst( RainbowParticle.BURST, Random.IntRange( 1, 2 ) );
	}

	private void onZapBeam( Ballistica beam ){

		if (beamCells == null){
			//防御：fx 一定在 onZap 前调用把光带建好；若没有则补建。
			beamCells = buildBeamCells( beam );
		}

		boolean discovered = false;
		for (int cell : beamCells) {

			if (cell == beam.sourcePos) continue;

			discovered |= revealAround( cell );
			CellEmitter.center( cell ).burst( RainbowParticle.BURST, Random.IntRange( 1, 2 ) );

			//灵光光束：光带内每一个敌对单位都独立结算命中(穿透多目标)。
			Char ch = Actor.findChar( cell );
			if (ch != null && ch != curUser && ch.alignment != Char.Alignment.ALLY){
				wandProc( ch, chargesPerCast() );
				hitTarget( ch );
			}
		}

		if (discovered) {
			Sample.INSTANCE.play( Assets.Sounds.SECRET );
		}
		GameScene.updateFog();
	}

	/**
	 * 把一条瞄准束扩成"3格宽"的矩形光带格集合：
	 * 沿主束路径，每一格再向垂直主方向的两侧各扩 (BEAM_WIDTH-1)/2 格。
	 * 用与湮解分裂同款的"整格垂直邻居法"沿 path 推进，避免发散成锥形。
	 */
	private HashSet<Integer> buildBeamCells( Ballistica beam ){

		HashSet<Integer> result = new HashSet<>();
		//主路径上每一格
		ArrayList<Integer> path = new ArrayList<>( beam.subPath( 1, Math.min( beam.dist, distance() ) ) );
		if (path.isEmpty()){
			result.add( beam.collisionPos );
			return result;
		}

		//垂直扩展半宽(3格宽 → 每侧1格)
		int half = (BEAM_WIDTH - 1) / 2;

		for (int i = 0; i < path.size(); i++){
			int cell = path.get( i );

			//推进方向 = 当前格-上一格 的方向；垂直向量 = (-dirY, dirX)
			int dirX = 0, dirY = 0;
			Point prevP = (i >= 1)
					? Dungeon.level.cellToPoint( path.get( i-1 ) )
					: Dungeon.level.cellToPoint( beam.sourcePos );
			Point curP  = Dungeon.level.cellToPoint( cell );
			dirX = curP.x - prevP.x;
			dirY = curP.y - prevP.y;

			result.add( cell );

			//两侧扩展 half 格
			for (int s = 1; s <= half; s++){
				int perpX = -dirY, perpY = dirX;
				Point base = Dungeon.level.cellToPoint( cell );
				int cellLeft  = Dungeon.level.pointToCell( new Point(
						base.x + perpX * s, base.y + perpY * s ) );
				int cellRight = Dungeon.level.pointToCell( new Point(
						base.x - perpX * s, base.y - perpY * s ) );

				//只收录地图内的格
				if (Dungeon.level.insideMap( cellLeft )){
					result.add( cellLeft );
				}
				if (Dungeon.level.insideMap( cellRight )){
					result.add( cellRight );
				}
			}
		}
		return result;
	}

	/** 照亮某个 cell 及其 3x3 邻域；若翻到秘密地形返回 true。 */
	private boolean revealAround( int c ){
		boolean noticed = false;
		for (int n : PathFinder.NEIGHBOURS9) {
			int cell = c + n;
			if (!Dungeon.level.insideMap( cell )) continue;

			if (Dungeon.level.discoverable[cell])
				Dungeon.level.mapped[cell] = true;

			int terr = Dungeon.level.map[cell];
			if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
				Dungeon.level.discover( cell );
				GameScene.discoverTile( cell, terr );
				noticed = true;
			}
		}
		return noticed;
	}

	/** 对单个目标结算"灵光直击"真实效果(致盲 + 对亡灵/恶魔 43% 增伤)。 */
	private void hitTarget( Char ch ){
		int dmg = damageRoll();

		if (Random.Int( 5 + buffedLvl() ) >= 3) {
			Buff.prolong( ch, Blindness.class, 2f + (buffedLvl() * 0.333f) );
			ch.sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 6 );
		}

		if (ch.properties().contains( Char.Property.DEMONIC )
				|| ch.properties().contains( Char.Property.UNDEAD )){
			ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10+buffedLvl() );
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
			ch.damage( Math.round( dmg * 1.333f ), this );
		} else {
			ch.sprite.centerEmitter().burst( RainbowParticle.BURST, 10+buffedLvl() );
			ch.damage( dmg, this );
		}
	}

	private int distance() {
		return buffedLvl()*2 + 6;
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {

		if (mode == MODE_BEAM) {
			// 灵光光束：构建 3 格宽矩形光带，并沿主束末端画 3 条平行射线示意宽度。
			beamCells = buildBeamCells( beam );

			int endCell = beam.path.get( Math.min( beam.dist, distance() ) );
			Point endP = Dungeon.level.cellToPoint( endCell );

			//主束末端方向(取最后一段)决定垂直偏移方向
			int lastIdx = Math.min( beam.dist, distance() ) - 1;
			Point beforeP = (lastIdx > 0)
					? Dungeon.level.cellToPoint( beam.path.get( lastIdx - 1 ) )
					: Dungeon.level.cellToPoint( beam.sourcePos );
			int dirX = endP.x - beforeP.x;
			int dirY = endP.y - beforeP.y;
			//垂直向量
			int perpX = -dirY, perpY = dirX;

			for (int s = -1; s <= 1; s++){   //s=-1,0,1 → 三条平行射线
				int tx = endP.x + perpX * s;
				int ty = endP.y + perpY * s;
				if (tx < 0 || ty < 0 || tx >= Dungeon.level.width() || ty >= Dungeon.level.height()){
					continue;
				}
				int targetCell = Dungeon.level.pointToCell( new Point( tx, ty ) );
				curUser.sprite.parent.add(
						new Beam.LightRay( curUser.sprite.center(),
								DungeonTilemap.raisedTileCenterToWorld( targetCell ) ) );
			}
			Sample.INSTANCE.play( Assets.Sounds.RAY );
		} else {
			// 普攻：单道光束打落点。
			int end = beam.path.get( Math.min( beam.dist, distance() ) );
			curUser.sprite.parent.add(
					new Beam.LightRay( curUser.sprite.center(),
							DungeonTilemap.raisedTileCenterToWorld( end ) ) );
			Sample.INSTANCE.play( Assets.Sounds.RAY );
		}

		callback.call();
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//cripples enemy
		Buff.prolong( defender, Cripple.class, Math.round((1+staff.buffedLvl())*procChanceMultiplier(attacker)));
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( Random.Int( 0x1000000 ) );
		particle.am = 0.5f;
		particle.setLifespan(1f);
		particle.speed.polar(Random.Float(PointF.PI2), 2f);
		particle.setSize( 1f, 2f);
		particle.radiateXY( 0.5f);
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
