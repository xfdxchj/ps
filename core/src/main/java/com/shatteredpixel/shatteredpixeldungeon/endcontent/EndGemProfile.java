/*
 * Shattered Pixel Dungeon: End  —《破碎的像素地牢：终焉扩展》
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * 一个被镶嵌在某件装备上的宝石实例在"当前装备等级"下的实际数值方案
 * (design doc 装备宝石矩阵)。数值采用 <b>与装备等级耦合的线性成长</b>。
 *
 * <p>该模块<b>不引用任何游戏运行库</b>，便于在 <code>desktop</code> 编译前
 * 用纯 JVM(<code>javac</code>) 做快速单测/数值平衡，随后再由 gameplay 集成方
 * (见 {@link EndGem}) 叠加到武器的伤害因子 & 护甲减伤等产出点。</p>
 */
public final class EndGemProfile {

    private final EndGem gem;

    // 每种宝石一个基准点随等级成长
    /** 基础量(等级0时的贡献)。 */
    private final float base;
    /** 每级成长量(设计稿"成长1~2"等在此落数值)。 */
    private final float perLevel;

    private EndGemProfile(EndGem gem, float base, float perLevel){
        this.gem = gem;
        this.base = base;
        this.perLevel = perLevel;
    }

    /** 在给定装备等级下该宝石贡献的线性数值。 */
    public float atLevel(int equipmentLevel){
        return base + perLevel * equipmentLevel;
    }

    /** 在给定装备等级下该宝石贡献的整数加成（用于攻击/防御等按整数结算的项）。 */
    public int bonusAt(int equipmentLevel){
        return Math.round(atLevel(equipmentLevel));
    }

    public EndGem gem(){ return gem; }
    public float base(){ return base; }
    public float perLevel(){ return perLevel; }

    /** 设计稿默认成长矩阵(可按系统数值平衡调整)。 */
    public static EndGemProfile of(EndGem gem){
        switch (gem){
            case ATTACK:  return new EndGemProfile(EndGem.ATTACK,  1f,  1f); // 成长 1~2
            case DEFENSE: return new EndGemProfile(EndGem.DEFENSE, 1f,  1f); // 成长 1~2
            case ACCURACY:return new EndGemProfile(EndGem.ACCURACY,0f, 0);
            case EVASION: return new EndGemProfile(EndGem.EVASION, 0f, 0);
            case MAX_HP:  return new EndGemProfile(EndGem.MAX_HP,  5f, 0); // 固定 +5/级递增可另行调
            default:      return new EndGemProfile(gem, 0f, 0f);
        }
    }
}
