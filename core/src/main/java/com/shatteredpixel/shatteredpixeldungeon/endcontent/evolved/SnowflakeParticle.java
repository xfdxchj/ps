package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

/**
 * END(冰天雪地): 纯白雪花粒子。
 *
 * <p>原版 SnowParticle 是半透明、偏淡的，这里单独做一个更明显、
 * 冰蓝色的雪点，用于凝霜法杖「冰天雪地」形态的气体。
 */
public class SnowflakeParticle extends PixelParticle {

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((SnowflakeParticle)emitter.recycle( SnowflakeParticle.class )).reset( x, y );
		}
	};

	public SnowflakeParticle() {
		super();
		color( 0xA8E6FF ); //冰蓝色
		size( 2f );
		lifespan = 1.4f;
		speed.set( Random.Float( -2f, 2f ), Random.Float( 4f, 8f ) );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;
		left = lifespan;
		speed.set( Random.Float( -2f, 2f ), Random.Float( 4f, 8f ) );
	}

	@Override
	public void update() {
		super.update();
		//白色雪花保持较高不透明度，只在消散时淡出
		am = Math.min( 1f, left / lifespan * 1.5f );
	}
}
