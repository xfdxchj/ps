//END(port from Arknights): SeaLevel_part1（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.IberiaPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CursingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.HallucinationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OriginiumTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Group;
import com.watabou.utils.Random;

public class SeaLevel_part1
extends RegularLevel {
    public SeaLevel_part1() {
        this.color1 = 4941366;
        this.color2 = 0xF2F2F2;
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) {
            return 7;
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
            this.addItemToSpawn(new PotionOfHealing());
        } else if ((akFloorIn() < 5)) {
            this.addItemToSpawn(new ScrollOfUpgrade());
        } else {
            this.addItemToSpawn(new PotionOfStrength());
        }
        this.addItemToSpawn(new SanityPotion());
        super.createItems();
    }

    @Override
    public int nMobs() {
        return super.nMobs() + 2;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_iberia.png";
    }

    @Override
    public String waterTex() {
        return "environment/water7.png";
    }

    @Override
    protected Painter painter() {
        return new IberiaPainter().setWater(this.feeling == Level.Feeling.WATER ? 0.38f : 0.18f, 4).setGrass(this.feeling == Level.Feeling.GRASS ? 0.3f : 0.1f, 3).setTraps(this.nTraps(), this.trapClasses(), this.trapChances());
    }

    @Override
    protected Class<?>[] trapClasses() {
        return new Class[]{FrostTrap.class, ToxicTrap.class, OozeTrap.class, PoisonDartTrap.class, OriginiumTrap.class, FlashingTrap.class, StormTrap.class, CursingTrap.class, GrimTrap.class, WarpingTrap.class, HallucinationTrap.class};
    }

    @Override
    protected float[] trapChances() {
        return new float[]{4.0f, 4.0f, 4.0f, 4.0f, 2.0f, 2.0f, 2.0f, 2.0f, 1.0f, 1.0f, 1.0f};
    }

    @Override
    public String tileDesc(int tile) {
        switch (tile) {
            case 7: {
                return Messages.get(CityLevel.class, "entrance_desc");
            }
            case 8: {
                return Messages.get(CityLevel.class, "exit_desc");
            }
            case 12: 
            case 20: {
                return Messages.get(CityLevel.class, "deco_desc");
            }
            case 14: {
                return Messages.get(CityLevel.class, "sp_desc");
            }
            case 25: 
            case 26: {
                return Messages.get(CityLevel.class, "statue_desc");
            }
            case 27: {
                return Messages.get(CityLevel.class, "bookshelf_desc");
            }
        }
        return super.tileDesc(tile);
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
