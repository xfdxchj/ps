/*
 * Shattered Pixel Dungeon: End  —《破碎的像素地牢：终焉扩展》
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * 一颗已镶嵌宝石随<b>当前装备等级</b>产生的数值 (design doc 装备宝石矩阵)。
 *
 * <p>数值为线性成长，由本体方法叠加到攻击/护甲结算位置上。</p>
 * <p>五种宝石：攻击 ATTACK(伤害)、防御 DEFENSE(护甲减伤)、命中 ACCURACY(命中)、
 * 闪避 EVASION(闪避)、生命 MAX_HP(最大生命)。任一槽由装备方按需读取。
 */
public final class EndGemProfile {

    private final EndGem gem;
    /** 等级偏移敏感的基础量。 */
    private final float base;
    /** 每级成长(设计稿"成长1~2"等在此落数值)。 */
    private final float perLevel;

    private EndGemProfile(EndGem gem, float base, float perLevel){
        this.gem = gem;
        this.base = base;
        this.perLevel = perLevel;
    }

    public float atLevel(int equipmentLevel){
        return base + perLevel * equipmentLevel;
    }

    public int bonusAt(int equipmentLevel){
        return Math.round(atLevel(equipmentLevel));
    }

    public EndGem gem(){ return gem; }
    public float base(){ return base; }
    public float perLevel(){ return perLevel; }

    /** 是否与"已镶嵌且正在该装备上"的宝石配套，用于各处守卫。 */
    public static boolean attached(int gem){
        return gem >= 0 && gem < EndGem.values().length;
    }

    /** 单值(最简单的整数值)，给定已装备宝石序号与装备等级，取该宝石在当前等级的贡献。 */
    public static int value(int gemOrdinal, int equipmentLevel){
        if (!attached(gemOrdinal)) return 0;
        EndGem g = EndGem.values()[gemOrdinal];
        return of(g).bonusAt(equipmentLevel);
    }

    /** 设计稿默认成长矩阵(可按系统数值平衡调整；数值语义在生产时随接入位置统一)。 */
    public static EndGemProfile of(EndGem gem){
        switch (gem){
            case ATTACK:   return new EndGemProfile(EndGem.ATTACK,   1f, 1f); // 成长 1~2/级
            case DEFENSE:  return new EndGemProfile(EndGem.DEFENSE,  1f, 1f); // 成长 1~2/级
            case ACCURACY: return new EndGemProfile(EndGem.ACCURACY, 2f, 1f); // 命中点
            case EVASION:  return new EndGemProfile(EndGem.EVASION,  2f, 1f); // 闪避点
            case MAX_HP:   return new EndGemProfile(EndGem.MAX_HP,   5f, 2f); // 生命上限
            default:       return new EndGemProfile(gem, 0f, 0f);
        }
    }
}
