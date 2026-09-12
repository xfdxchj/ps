//END(port from Arknights): SeaLevel_part2（地形阶段）
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.miniboss.TheEndspeaker;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaPlatform;
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

public class SeaLevel_part2
extends RegularLevel {
    public SeaLevel_part2() {
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
    public void create() {
        this.addItemToSpawn(new SeaPlatform.LittleHandy());
        this.addItemToSpawn(new SeaPlatform.LittleHandy());
        this.addItemToSpawn(new SeaPlatform.LittleHandy());
        super.create();
    }

    @Override
    protected void createItems() {
        this.addItemToSpawn(new SanityPotion());
        super.createItems();
    }

    @Override
    protected void createMobs() {
        super.createMobs();
        TheEndspeaker.Status.spawnAspects(this);
    }

    @Override
    public int nMobs() {
        return super.nMobs() + 2;
    }

    @Override
    public String tilesTex() {
        return "environment/tiles_iberia2.png";
    }

    @Override
    public String waterTex() {
        return "environment/water7.png";
    }

    @Override
    protected Painter painter() {
        return new IberiaPainter().setWater(this.feeling == Level.Feeling.WATER ? 0.33f : 0.1f, 4).setGrass(this.feeling == Level.Feeling.GRASS ? 0.05f : 0.03f, 3).setTraps(this.nTraps(), this.trapClasses(), this.trapChances());
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

	//END(port from Arknights): 方舟区域音乐（原来没有覆盖此方法 → 进关卡没音乐）
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.GAME_IBERIA2, true );
	}
}
