/*
 * Shattered Pixel Dungeon: End  —《破碎的像素地牢：终焉扩展》
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

/**
 * 装备宝石的类型词典（设计文档 §"装备宝石系统"）。
 *
 * <p>每种宝石是一份"可装备在武器或护甲上的成长属性"(仅在对应装备身上生效)，
 * 属性随装备等级(<code>Weapon/Armor.level()</code>)成长。</p>
 *
 * <p><b>集成规划(见 docs/END_ROADMAP.md → M1)</b>：这些常量仅作为权威词典，
 * 类型需从 <code>items.Weapon</code> / <code>items.Armor</code> 的持久化字段
 * (仿 enchantment/glyph 的 store 与 bundle)引用，并在数值产出点(如 Weapon.damageFactor /
 * Armor.defenseFactor / evasionFactor / Char 生命上限)按需应用，而不动主流程判定。</p>
 */
public enum EndGem {

    /** 攻击宝石：提升武器造成伤害的能力。 */
    ATTACK,
    /** 防御宝石：提升护甲的减伤能力。 */
    DEFENSE,
    /** 命中宝石：提升攻击命中率。 */
    ACCURACY,
    /** 闪避宝石：提升闪避能力。 */
    EVASION,
    /** 生命宝石：提升最大生命值。 */
    MAX_HP;

    public boolean isWeaponOriented() {
        return this == ATTACK || this == ACCURACY;
    }

    public boolean isArmorOriented() {
        return this == DEFENSE || this == EVASION;
    }

    /** 单颗宝石给某装备带来的"成长量"轴向；供后续数值实现使用。 */
    public String growthAxis() {
        switch (this) {
            case ATTACK:  return "damage";
            case DEFENSE: return "defense";
            case ACCURACY:return "accuracy";
            case EVASION: return "evasion";
            case MAX_HP:  return "maxHp";
            default:      return "none";
        }
    }
}
