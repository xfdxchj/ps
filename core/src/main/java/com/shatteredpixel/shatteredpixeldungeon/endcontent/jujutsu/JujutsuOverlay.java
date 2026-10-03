/* 领域展开的全屏覆盖层（无量空处 / 伏魔御厨子 / 领域对撞）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class JujutsuOverlay extends Image {

	private static JujutsuOverlay instance;

	/** END(修订): 刷新/消失都加快 —— 原 0.2s 一波、斩击 1s 才消失，太慢。 */
	private static final float DOMAIN_TICK = 0.08f;
	private static final int PARTICLES_PER_TICK = 80;
	private static final int SLASH_PER_TICK = 16;
	private static final float SLASH_FADE = 0.3f;

	/** 展开瞬间的黑->白/红过渡计时。 */
	private static float castTimer = 0f;
	private static boolean castPlayer = true;
	private static final float CAST_TIME = 0.9f;
	private float particleTimer = 0f;

	/** 领域展开瞬间的黑->白/红过渡。 */
	public static void castFlash(boolean player){
		castPlayer = player;
		ensure();
	}

	/** 确保场上有一层覆盖（领域开始/对撞时调用）。 */
	public static void ensure(){
		if (instance == null || !instance.exists){
			instance = new JujutsuOverlay();
			GameScene.effectOverFog(instance);
		}
	}

	public JujutsuOverlay(){
		super(Assets.Interfaces.WHITE_RECT);
		alpha(0f);
	}

	/** 领域：粒子撒在**当前屏幕画面**上（不按格子）。 */
	private void spawnDomainParticles(com.watabou.noosa.particles.Emitter.Factory factory){
		//END(修订): 改为按屏幕随机分布，不再按格子
		JujutsuFx.scatterScreen(factory, PARTICLES_PER_TICK);
	}

	/** 伏魔御厨子：在视野内随机多格刷斩击（不再堆在单个角色身上）。 */
	private void spawnSukunaSlashes(){
		if (Dungeon.level == null) return;
		int placed = 0;
		int attempts = 0;
		while (placed < SLASH_PER_TICK && attempts < SLASH_PER_TICK * 8){
			attempts++;
			int cell = com.watabou.utils.Random.Int(Dungeon.level.length());
			if (!Dungeon.level.insideMap(cell) || !Dungeon.level.heroFOV[cell]) continue;
			try {
				com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(
						cell, com.watabou.utils.Random.Float() * 360f, SLASH_FADE);
			} catch (Throwable ignored) {}
			placed++;
		}
	}

	@Override
	public void update(){
		super.update();

		//全屏跟随相机
		if (Camera.main != null){
			x = Camera.main.x;
			y = Camera.main.y;
			scale.set(Camera.main.width / width, Camera.main.height / height);
		}

		boolean player = Jujutsu.playerDomainTurns > 0;
		boolean sukuna = Jujutsu.sukunaDomainTurns > 0;
		boolean clash = player && sukuna;

		float target;
		int color;
		if (clash){
			target = 0f; color = 0xFFFFFF;   //对撞也不遮罩
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = DOMAIN_TICK;
				spawnDomainParticles(JujutsuDomainParticle.FACTORY);
				spawnSukunaSlashes();
			}
		} else if (player){
			//END(修订): 无量空处**不要遮罩层**，改为画面持续粒子（0.2 秒一批）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = DOMAIN_TICK;
				spawnDomainParticles(JujutsuDomainParticle.FACTORY);
			}
		} else if (sukuna){
			//END(修订): 伏魔御厨子不遮罩，改为持续红色斩击（不撒星形粒子）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = DOMAIN_TICK;
				spawnSukunaSlashes();
			}
		} else {
			target = 0f; color = 0xFFFFFF;
		}
		tint(color);

		float a = alpha();
		float step = Game.elapsed * 1.8f;
		if (a < target) a = Math.min(target, a + step);
		else a = Math.max(target, a - step);
		alpha(a);

		if (!player && !sukuna && a <= 0.01f){
			instance = null;
			killAndErase();
		}
	}
}
