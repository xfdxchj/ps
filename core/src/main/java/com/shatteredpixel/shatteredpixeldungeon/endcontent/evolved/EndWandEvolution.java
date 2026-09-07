/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 法杖蜕变(进化)外层框架。
 * 目标：法杖到达蜕变线(>= +8,见 isEligible)时,可用材料(后续接入锻造/UI)把它“变成一个新物品”,
 * 即换成某个 EvolvedXxx/高阶法杖类,保留充能/等级并携带独特附魔光泽。
 *
 * 当前已为全部 13 种法杖注册进化形态(魔弹/爆炎/闪电/冲击波/腐蚀/腐化/解离/冰霜/活体大地/棱光/再生/注魂/哨戒)。
 * 每把进化即一支“新法杖”、带独特进阶附魔光泽;本类仅描述“x 源法杖 -> y 进化法杖”映射与换装,机制微调在各 Evolved 子类。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;

import java.util.HashMap;
import java.util.Map;

public final class EndWandEvolution {

    /** 蜕变所需的原始法杖最低强化等级。 */
    public static final int MIN_EVOLUTION_LEVEL = 8;

    /** 由“来源法杖类型”索引到“其进化后所成新法杖”。 */
    private static final Map<Class<? extends Wand>, Class<? extends Wand>> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put(WandOfMagicMissile.class, EvolvedWandOfMagicMissile.class);
        REGISTRY.put(WandOfFireblast.class,    EvolvedWandOfFireblast.class);
        REGISTRY.put(WandOfLightning.class,    EvolvedWandOfLightning.class);
        REGISTRY.put(WandOfBlastWave.class,      EvolvedWandOfBlastWave.class);
        REGISTRY.put(WandOfCorrosion.class,      EvolvedWandOfCorrosion.class);
        REGISTRY.put(WandOfCorruption.class,     EvolvedWandOfCorruption.class);
        REGISTRY.put(WandOfDisintegration.class, EvolvedWandOfDisintegration.class);
        REGISTRY.put(WandOfFrost.class,          EvolvedWandOfFrost.class);
        REGISTRY.put(WandOfLivingEarth.class,    EvolvedWandOfLivingEarth.class);
        REGISTRY.put(WandOfPrismaticLight.class, EvolvedWandOfPrismaticLight.class);
        REGISTRY.put(WandOfRegrowth.class,       EvolvedWandOfRegrowth.class);
        REGISTRY.put(WandOfTransfusion.class,    EvolvedWandOfTransfusion.class);
        REGISTRY.put(WandOfWarding.class,        EvolvedWandOfWarding.class);
    }

    public static boolean hasEvolution( Wand wand ){
        return wand != null && REGISTRY.containsKey( wand.getClass() );
    }

    /** 一支法杖当前是否达到/超过蜕变线。 */
    public static boolean isEligible( Wand wand ){
        return hasEvolution(wand) && wand.buffedLvl() >= MIN_EVOLUTION_LEVEL;
    }

    /**
     * 把一支(达标)源法杖“蜕变”成进化版新法杖。
     * @return 构造出的进化法杖,等级/剩余充能尽量保留(等级归零与否的设计见开发.txt,若走 M2 的二段归零,
     *         在锻造流程处自行 level(0) 清除即可)；若源法杖无进化项或不足 8 级则返回 null。
     */
    public static Wand evolve( Wand source ){
        if (source == null || !isEligible(source)) return null;
        Class<? extends Wand> target = REGISTRY.get(source.getClass());
        if (target == null) return null;

        Wand evolved;
        try {
            evolved = target.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null; // 防御：注册的进化类都应有无参构造
        }
        // 终焉·进化基础：真实等级+8(保留原属性，level()/buffedLvl() 全按8级)；充能上限20 由子类覆写
        evolved.level( 8 );
        evolved.updateLevel();
        evolved.curCharges = Math.min( evolved.maxCharges, source.curCharges );
        return evolved;
    }

    /** 便捷：法杖类型的进化版类型(供 UI 文案/图鉴用),未注册返回 null。 */
    public static Class<?> evolutionFor( Wand wand ){
        return REGISTRY.get( wand.getClass() );
    }
}
