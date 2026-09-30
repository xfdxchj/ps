package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

/**
 * END(冰天雪地): 冰蓝色气体粒子。
 *
 * <p>照原版毒气/酸蚀气做：用特效图集里的 STEAM 软点，粒子小、
 * 原地缓慢旋转、淡入淡出并逐渐放大，寿命 1~3 秒。
 */
public class SnowflakeParticle extends Image {

	private static final int SIZE = 7;
	private static TextureFilm film;

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((SnowflakeParticle)emitter.recycle( SnowflakeParticle.class )).reset( x, y );
		}
	};

	private float lifespan;
	private float left;

	public SnowflakeParticle() {
		super();
		texture( Assets.Effects.SPECKS );
		if (film == null) {
			film = new TextureFilm( texture, SIZE, SIZE );
		}
		frame( film.get( Speck.STEAM ) );
		origin.set( SIZE / 2f );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;

		hardlight( 0xA8E6FF );
		angle = Random.Float( 360 );
		angularSpeed = 30;
		scale.set( 1f );
		left = lifespan = Random.Float( 1f, 3f );
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = (float)Math.sqrt( (p < 0.5f ? p : 1 - p) * 0.5f );
		scale.set( 1 + p );
		if ((left -= Game.elapsed) <= 0f) {
			kill();
		}
	}
}
