//END(port from Arknights): SiestaLevel_part2（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SiestaPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CoreRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CursingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.HallucinationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OblivionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OriginiumTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.watabou.noosa.Group;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class SiestaLevel_part2
extends RegularLevel {
    public SiestaLevel_part2() {
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
            this.addItemToSpawn(new ScrollOfUpgrade());
        } else {
            this.addItemToSpawn(new PotionOfStrength());
        }
        super.createItems();
    }

    @Override
    public int nMobs() {
        return super.nMobs() + 4;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_siesta2.png";
    }

    @Override
    public String waterTex() {
        return "environment/water6.png";
    }

    @Override
    protected Painter painter() {
        return new SiestaPainter().setWater(this.feeling == Level.Feeling.WATER ? 0.98f : 0.38f, 4).setGrass(this.feeling == Level.Feeling.GRASS ? 0.8f : 0.2f, 3).setTraps(this.nTraps(), this.trapClasses(), this.trapChances());
    }

    @Override
    protected Class<?>[] trapClasses() {
        return new Class[]{ShockingTrap.class, RockfallTrap.class, BurningTrap.class, ExplosiveTrap.class, OriginiumTrap.class, FlashingTrap.class, StormTrap.class, CursingTrap.class, OblivionTrap.class, GrimTrap.class, WarpingTrap.class, HallucinationTrap.class};
    }

    @Override
    protected float[] trapChances() {
        return new float[]{4.0f, 4.0f, 4.0f, 4.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.0f, 1.0f, 1.0f, 1.0f};
    }

    @Override
    protected ArrayList<Room> initRooms() {
        ArrayList<Room> rooms = super.initRooms();

        //END(修复·地形错乱·关键): 剔除会写"方舟贴图里不存在的地形"的房间。
        //RegularLevel 默认房间表可能放入 RegionDecoPatchEntranceRoom /
        //LibraryHallEntranceRoom，它们会往地图写 Terrain.REGION_DECO(33) /
        //REGION_DECO_ALT(34) / MINE_CRYSTAL(35) 等本 fork 独有的地形，
        //而这些在方舟贴图里几乎是空白 → 显示错乱。
        for (int i = rooms.size() - 1; i >= 0; i--) {
            String cn = rooms.get(i).getClass().getName();
            if (cn.contains("RegionDecoPatch")
                    || cn.contains("LibraryHall")
                    || cn.contains("RegionDeco")) {
                rooms.remove(i);
            }
        }

        rooms.add(new CoreRoom());
        rooms.add(new CoreRoom());
        return rooms;
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

	//END(port from Arknights): 方舟区域音乐（原来没有覆盖此方法 → 进关卡没音乐）
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.GAME_SIESTA2, true );
	}
}
