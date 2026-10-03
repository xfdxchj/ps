/* 无量空处 / 伏魔御厨子 的专属画面粒子。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public class JujutsuDomainParticle extends Image {

	private static final int SIZE = 7;

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((JujutsuDomainParticle)emitter.recycle( JujutsuDomainParticle.class )).reset( x, y );
		}
	};

	private float lifespan;
	private float left;
	private int tintColor = 0xFFFFFFFF;

	public void setTint(int color){ tintColor = color; }

	public JujutsuDomainParticle() {
		super();
		texture( Assets.Effects.JUJUTSU_DOMAIN_PARTICLE );
		origin.set( SIZE / 2f );
	}

	public void reset( float x, float y ) {
		revive();
		this.x = x;
		this.y = y;

		hardlight( tintColor );
		angle = Random.Float( 360 );
		angularSpeed = Random.Float( -40, 40 );
		scale.set( Random.Float( 0.8f, 1.4f ) );
		left = lifespan = Random.Float( 0.8f, 2.0f );
	}

	@Override
	public void update() {
		super.update();
		float p = left / lifespan;
		am = (float)Math.sqrt( (p < 0.5f ? p : 1 - p) * 0.5f );
		if ((left -= Game.elapsed) <= 0f) {
			kill();
		}
	}
}
