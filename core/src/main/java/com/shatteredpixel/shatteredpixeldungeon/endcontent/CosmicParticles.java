/*
 * 破碎的地牢 (End fork) — cosmic 粒子接入「无尽装备」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 *
 * <p><b>资源约定</b>：assets/cosmic/cosmic0~9.png 是 <b>10 个各自独立
 * 的粒子动画</b>——每张宽 16，高是 16 的倍数（48~112），内部自上而下
 * 叠着若干 16×16 子帧。也就是：0~9 不是一条动画的 10 帧，
 * 而是 10 个完整的、可单独循环的粒子素材。
 *
 * <p>发射时<u>随机挑一张</u>，让该粒子的子帧按顺序循环播放。
 * 不做横向拼帧、不做全局连续 0→9 动画。
 *
 * <p>接入方式：在 4 件顶级装备里 override {@code emitter()}，
 * 返回 {@link #equipmentEmitter()}。原版 {@code ItemSprite.view(Item)}
 * 拿到 emitter 后会 {@code pos(this)} 并 {@code parent.add(emitter)}，
 * 粒子的发射位置会逐帧跟随物品图标，无需自维护跟随逻辑。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public final class CosmicParticles {

	private CosmicParticles() {}

	/** 10 个独立粒子素材（相对 assets 的路径）。 */
	private static final String[] SPRITES = {
			"cosmic/cosmic0.png",
			"cosmic/cosmic1.png",
			"cosmic/cosmic2.png",
			"cosmic/cosmic3.png",
			"cosmic/cosmic4.png",
			"cosmic/cosmic5.png",
			"cosmic/cosmic6.png",
			"cosmic/cosmic7.png",
			"cosmic/cosmic8.png",
			"cosmic/cosmic9.png",
	};

	/** 单帧边长（每个 16×16 子帧）。 */
	public static final int FRAME = 16;

	/** 发射间隔（秒）：隔 0.5s 冒一个，构成持续漂浮的效果。 */
	public static final float INTERVAL = 0.5f;

	public static final Emitter.Factory FACTORY = new Emitter.Factory() {
		@Override
		public void emit(Emitter emitter, int index, float x, float y) {
			CosmicParticle p = (CosmicParticle) emitter.recycle(CosmicParticle.class);
			p.reset(x, y);
		}

		@Override
		public boolean lightMode() {
			//粒子是黑底+亮色，用加法混合把黑底当透明
			return true;
		}
	};

	/**
	 * 构造一个挂在装备图标上持续冒 cosmic 粒子的发射器。
	 *
	 * <p>返回的 Emitter 会以物品 Icon 中心为发射点位（fillTarget=true，
	 * 由父类 {@code ItemSprite.view()} 注入 target，位置自动跟随）。
	 */
	public static Emitter equipmentEmitter() {
		Emitter e = new Emitter();
		e.pos(ItemSprite.SIZE / 2f - FRAME / 2f, 2f, FRAME, FRAME);
		e.pour(FACTORY, INTERVAL);
		return e;
	}

	/**
	 * 一个 cosmic 粒子：播放某一张素材的 16×16 子帧动画，缓缓上浮后消失。
	 */
	public static class CosmicParticle extends MovieClip {

		private float lifespan;
		private float left;

		public CosmicParticle() {
			origin.set(FRAME / 2f);
		}

		public void reset(float x, float y) {
			revive();

			this.x = x - FRAME / 2f;
			this.y = y - FRAME / 2f;

			//随机挑一个粒子素材
			int idx = Random.Int(SPRITES.length);
			String path = SPRITES[idx];

			com.watabou.gltextures.SmartTexture tx = TextureCache.get(path);
			texture(tx);

			//播放该素材自己的子帧循环
			int frames = tx.height / FRAME;
			TextureFilm film = new TextureFilm(tx, FRAME, FRAME);
			Integer[] keys = new Integer[frames];
			for (int i = 0; i < frames; i++) keys[i] = i;
			MovieClip.Animation anim =
					new MovieClip.Animation(12, true).frames(film, (Object[]) keys);
			play(anim);

			//寿命约 1.5 秒，向上漂
			this.lifespan = this.left = 1.5f;
			speed.set(0, -20);
			acc.set(0, 0);

			//粒子尺寸 16×16；可微调让它比原尺寸略小些更精致
			scale.set(0.9f);
			angularSpeed = Random.Float(-30, 30);
		}

		@Override
		public void update() {
			super.update();

			left -= Game.elapsed;
			if (left <= 0) {
				killAndErase();
			}
		}
	}
}