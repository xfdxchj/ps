package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 破印·狂战士 「飞行武器」掷出的回返刀刃。
 * 直接继承真 回旋镖 HeavyBoomerang——即拥有其“扔出仍回手/自动再掷”的真实返回机制。
 * (数值/图标即军令版。)
 */
public class FlyingBlade extends HeavyBoomerang {

	{
		image = ItemSpriteSheet.TOMAHAWK;
		tier = 4;
		baseUses = 6;
	}

	@Override public String name(){ return "军令·飞行刃"; }

	@Override public String info(){
		return "消耗邪能金属炼成的回旋刃，掷出后仍会自己飞回你的手中。";
	}
}
