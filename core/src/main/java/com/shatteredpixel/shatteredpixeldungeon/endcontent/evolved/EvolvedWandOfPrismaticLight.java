/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * B4 棱光进化重新设计（取代原"+30% 伤害"的 M2 描述）：
 *   棱辉法杖 → 「灵光光束」，拥有人手可选的两种发射形态（在背包-法杖详情窗口切换）：
 *
 *   形态 0·普攻直射(default)：耗 1 充能。退回父类的单目标直射——照亮地形浅层，
 *      只命中瞄准落点上的一个敌对单位，附带致盲/对亡灵·恶魔增伤（基础 affectTarget 语义）。
 *   形态 1·灵光光束：耗 3 充能。向瞄准方向打出一道 2 格宽、可穿透的强光束
 *      (ConeAOE 窄发散角 + 不因单位停下)，对光束所经的每一个敌对单位真正独立结算
 *      affectTarget(致盲与增伤)，并照亮点亮更宽范围的地形。
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
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.noosa.audio.Sample;

public class EvolvedWandOfPrismaticLight extends WandOfPrismaticLight implements EndModeWand {

	private static final int MODE_BASIC = 0;
	private static final int MODE_BEAM  = 1;

	/** 灵光光束的"宽度"(名义发散角,度数)。小值≈2格宽的准直光束。 */
	private static final float BEAM_DEGREES = 8f;

	/** 当前形态(0=普攻直射,1=灵光光束)。 */
	private int mode = MODE_BASIC;

	/** 本发施放实际展开的光带(仅灵光形态)。 */
	private transient ConeAOE cone;

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
	 * 按所选形态决定每次施放消耗：
	 * 普攻 1 点；灵光光束 3 点。
	 * Wand 的施放门槛(tryToZap)与扣充(wandUsed)都走这一处，因此若充能不足 3 却开着
	 * 灵光光束，本次就无法施放(会 fizzle)，直到攒满 3 点——与"灵光光束耗 3"匹配。
	 */
	@Override
	protected int chargesPerCast() {
		return mode == MODE_BEAM ? 3 : 1;
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

		if (cone == null){
			//防御：fx 一定在 onZap 前调用把 cone 建好；若没有则补一块。
			cone = new ConeAOE( beam, distance(), BEAM_DEGREES,
					Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID );
		}

		boolean discovered = false;
		for (int cell : cone.cells) {

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
			// 灵光光束：打一束窄发散可穿透光带并画出多条光束示意宽度。
			cone = new ConeAOE( beam, distance(), BEAM_DEGREES,
					Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID );

			// 主光轴
			curUser.sprite.parent.add(
					new Beam.LightRay( curUser.sprite.center(),
							DungeonTilemap.raisedTileCenterToWorld( rayEndCone( cone.coreRay ) ) ) );
			// 外沿两道光,示意"2格宽"
			for (Ballistica r : cone.outerRays){
				curUser.sprite.parent.add(
						new Beam.LightRay( curUser.sprite.center(),
								DungeonTilemap.raisedTileCenterToWorld( rayEndCone( r ) ) ) );
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

	private int rayEndCone( Ballistica ray ){
		if (ray == null) return curUser.pos;
		return ray.path.get( Math.min( ray.dist, distance() ) );
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
