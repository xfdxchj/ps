/* 领域展开的全屏覆盖层（无量空处 / 伏魔御厨子 / 领域对撞）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class JujutsuOverlay extends Image {

	private static JujutsuOverlay instance;

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

	@Override
	public void update(){
		super.update();

		//全屏跟随相机
		if (Camera.main != null){
			x = Camera.main.x;
			y = Camera.main.y;
			scale.set(Camera.main.width, Camera.main.height);
		}

		boolean player = Jujutsu.playerDomainTurns > 0;
		boolean sukuna = Jujutsu.sukunaDomainTurns > 0;
		boolean clash = player && sukuna;

		float target;
		int color;
		if (clash){
			target = 0.38f; color = 0xFF6A20B0;   //对撞：紫
		} else if (player){
			target = 0.42f; color = 0xFF08182E;   //无量空处：深海蓝黑
		} else if (sukuna){
			target = 0.45f; color = 0xFF520808;   //伏魔御厨子：血红
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
