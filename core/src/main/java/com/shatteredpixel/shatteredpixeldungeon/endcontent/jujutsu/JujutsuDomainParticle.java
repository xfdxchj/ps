/* 无量空处 / 伏魔御厨子 的专属画面粒子。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public class JujutsuDomainParticle extends Image {

	/** 默认（无量空处）粒子工厂，使用 wuliang 贴图。 */
	public static final Emitter.Factory FACTORY = factory(0xFFFFFFFF, Assets.Effects.JUJUTSU_WULIANG);
	/** 伏魔御厨子用的红色粒子工厂，使用原星形贴图。 */
	public static final Emitter.Factory FACTORY_RED = factory(0xFFFF5555, Assets.Effects.JUJUTSU_DOMAIN_PARTICLE);

	public static Emitter.Factory factory(final int tint, final String tex){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((JujutsuDomainParticle)emitter.recycle( JujutsuDomainParticle.class ))
						.reset( x, y, tint, tex );
			}
		};
	}

	private float lifespan;
	private float left;

	public JujutsuDomainParticle() {
		super();
	}

	public void reset( float x, float y, int tintColor, String tex ) {
		texture( tex );
		origin.set( width / 2f, height / 2f );
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
