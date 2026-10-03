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

	/** 领域：粒子随机撒满视野（每批 FX_MULT*4 个，分布在约 40 格上）。 */
	private void spawnDomainParticles(com.watabou.noosa.particles.Emitter.Factory factory){
		//END(修订): 不再所有粒子堆在同一格，改为随机分布在视野内的多格
		JujutsuFx.scatter(factory, JujutsuFx.FX_MULT * 4, 40);
	}

	/** 伏魔御厨子：在视野内角色身上刷斩击（×FX_MULT）。 */
	private void spawnSukunaSlashes(){
		if (Dungeon.level == null) return;
		java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.actors.Char> pool =
				new java.util.ArrayList<>();
		for (com.shatteredpixel.shatteredpixeldungeon.actors.Char ch
				: com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()){
			if (ch == null || ch.sprite == null) continue;
			if (!Dungeon.level.insideMap(ch.pos)) continue;
			if (!Dungeon.level.heroFOV[ch.pos]) continue;
			pool.add(ch);
		}
		if (pool.isEmpty()) return;
		for (int i = 0; i < JujutsuFx.FX_MULT; i++){
			com.shatteredpixel.shatteredpixeldungeon.actors.Char ch =
					pool.get(com.watabou.utils.Random.Int(pool.size()));
			try {
				com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(
						ch, com.watabou.utils.Random.Float() * 360f);
			} catch (Throwable ignored) {}
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
				particleTimer = 0.2f;
				spawnDomainParticles(JujutsuDomainParticle.FACTORY);
				spawnDomainParticles(JujutsuDomainParticle.FACTORY_RED);
				spawnSukunaSlashes();
			}
		} else if (player){
			//END(修订): 无量空处**不要遮罩层**，改为画面持续粒子（0.2 秒一批）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = 0.2f;
				spawnDomainParticles(JujutsuDomainParticle.FACTORY);
			}
		} else if (sukuna){
			//END(修订): 伏魔御厨子不遮罩，改为红色粒子 + 持续斩击（0.2 秒一批）
			target = 0f; color = 0xFFFFFF;
			particleTimer -= Game.elapsed;
			if (particleTimer <= 0f){
				particleTimer = 0.2f;
				spawnDomainParticles(JujutsuDomainParticle.FACTORY_RED);
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
