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

	/** 展开瞬间的黑->白/红过渡计时。 */
	private static float castTimer = 0f;
	private static boolean castPlayer = true;
	private static final float CAST_TIME = 0.9f;
	private float particleTimer = 0f;

	/** 领域展开瞬间的黑->白/红过渡。 */
	public static void castFlash(boolean player){
		castPlayer = player;
		castTimer = CAST_TIME;
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

	/** 无量空处：在视野内随机格撒粒子，铺满画面。 */
	private void spawnDomainParticles(){
		if (Dungeon.level == null) return;
		for (int i = 0; i < 4; i++){
			int cell = com.watabou.utils.Random.Int(Dungeon.level.length());
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.heroFOV[cell]) continue;
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(cell)
					.burst(com.shatteredpixel.shatteredpixeldungeon.effects.Speck
							.factory(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STAR), 1);
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

		//展开瞬间：先黑，再白/红，然后落到领域底色
		if (castTimer > 0f){
			castTimer -= Game.elapsed;
			float p = 1f - Math.max(0f, castTimer) / CAST_TIME;
			int c; float a;
			if (p < 0.35f){
				c = 0xFF000000; a = p / 0.35f;
			} else if (p < 0.65f){
				c = 0xFFFFFFFF; a = 0.85f;
			} else {
				c = castPlayer ? 0xFF08182E : 0xFF520808;
				a = 0.85f * (1f - (p - 0.65f) / 0.35f);
			}
			tint(c); alpha(Math.min(1f, Math.max(0f, a)));
			return;
		}

		boolean player = Jujutsu.playerDomainTurns > 0;
		boolean sukuna = Jujutsu.sukunaDomainTurns > 0;
		boolean clash = player && sukuna;

		float target;
		int color;
		if (clash){
			target = 0f; color = 0xFFFFFF;   //对撞也不遮罩
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){ particleTimer = 0.2f; spawnDomainParticles(); }
		} else if (player){
			//END(修订): 无量空处**不要遮罩层**，改为画面持续粒子（0.2 秒一批）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = 0.2f;
				spawnDomainParticles();
			}
		} else if (sukuna){
			//END(修订): 伏魔御厨子也不遮罩，改为持续刺客斩击特效（0.2 秒）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = 0.2f;
				if (Dungeon.hero != null){
					try {
						com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(
								Dungeon.hero, com.watabou.utils.Random.Float() * 360f);
					} catch (Throwable ignored) {}
				}
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
