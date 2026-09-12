//END(port from Arknights): GavialLevel（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.GavialPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.HallucinationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OblivionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OriginiumTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.watabou.noosa.Group;
import com.watabou.utils.Random;

public class GavialLevel
extends RegularLevel {
    public GavialLevel() {
        this.color1 = 4941366;
        this.color2 = 0xF2F2F2;
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) {
            return 10;
        }
        return 7 + Random.chances(new float[]{3.0f, 2.0f, 1.0f});
    }

    @Override
    protected int specialRooms(boolean forceMax) {
        if (forceMax) {
            return 3;
        }
        return 1 + Random.chances(new float[]{1.0f, 1.0f});
    }

    @Override
    protected void createItems() {
        if ((akFloorIn() >= 5)) {
            this.addItemToSpawn(new PotionOfStrength());
        } else if ((akFloorIn() < 5)) {
            this.addItemToSpawn(new ScrollOfUpgrade());
        }
        super.createItems();
    }

    @Override
    public int nMobs() {
        return super.nMobs() + 2;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_sargon.png";
    }

    @Override
    public String waterTex() {
        return "environment/water0.png";
    }

    @Override
    protected Painter painter() {
        return new GavialPainter().setWater(this.feeling == Level.Feeling.WATER ? 0.38f : 0.18f, 4).setGrass(this.feeling == Level.Feeling.GRASS ? 0.99f : 0.3f, 3).setTraps(this.nTraps(), this.trapClasses(), this.trapChances());
    }

    @Override
    protected Class<?>[] trapClasses() {
        return new Class[]{FrostTrap.class, ToxicTrap.class, OozeTrap.class, PoisonDartTrap.class, BurningTrap.class, OriginiumTrap.class, FlashingTrap.class, StormTrap.class, HallucinationTrap.class, OblivionTrap.class, GrimTrap.class, WarpingTrap.class};
    }

    @Override
    protected float[] trapChances() {
        return new float[]{4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 1.0f, 1.0f};
    }

    @Override
    protected void createMobs() {
        super.createMobs();
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        return this.visuals;
    }

	//END(修复·挑战区): 方舟原版用绝对层号(31-40F)决定该层给什么奖励，
	//但本 MOD 的挑战区布局是 26F 起的连续 10 层 → 原条件永不成立，Boss 层拿不到奖励。
	//这里改用【区内偏移 floorIn】（0-3 第1章 / 4 Boss1 / 5-8 第2章 / 9 Boss2）。
	private int akFloorIn() {
		int[] info = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
				.areaAtDepth(com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth);
		return (info != null) ? info[1] : 0;
	}
}
