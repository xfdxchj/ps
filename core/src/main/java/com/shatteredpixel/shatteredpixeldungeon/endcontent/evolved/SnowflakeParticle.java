package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

/**
 * END(冰天雪地): 冰蓝色雪花粒子。
 *
 * <p>用特效图集里的 STAR 帧当雪花（六角星形），比原来的方形像素更大、
 * 更明显；下落时缓慢旋转。
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
		frame( film.get( Speck.STAR ) );
		origin.set( SIZE / 2f );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y - Random.Float( 8f, 24f );

		hardlight( 0xA8E6FF );
		angle = Random.Float( 360 );
		angularSpeed = Random.Float( -120f, 120f );
		speed.set( Random.Float( -3f, 3f ), Random.Float( 3f, 6f ) );
		scale.set( Random.Float( 1.6f, 2.6f ) );

		left = lifespan = Random.Float( 1.4f, 2.4f );
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = Math.min( 1f, p * 2f );
		if ((left -= Game.elapsed) <= 0f) {
			kill();
		}
	}
}
