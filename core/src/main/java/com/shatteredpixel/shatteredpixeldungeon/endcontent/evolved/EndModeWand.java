/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 供「可在背包-法杖详情窗口里手动选择形态」的进化法杖实现的模式接口。
 *
 * 让 EvolvedWandOfPrismaticLight(棱辉法杖) / EvolvedWandOfDisintegration(湮解法杖)
 * 这类拥有“多种射击形态”的法杖，能在局内背包点开它弹出的 WndUseItem 窗口里，
 * 通过一组按钮切换当前发射形态(模式)，并把选择持久化到该法杖物品上。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

/**
 * 可切换模式的进化法杖契约。
 * modeIndex 为该法杖当前选择的形态编号(从 0 起)；
 * WandUseItem 等 UI 据此渲染一组“形态选择”按钮。
 */
public interface EndModeWand {

    /** 该法杖共有多少种可选形态。 */
    int modeCount();

    /** 当前形态编号(0-based)。 */
    int modeIndex();

    /** 设置当前形态编号(由 UI 点击触发)。 */
    void setModeIndex( int index );

    /** 第 index 种形态的展示名(按钮文案)。 */
    String modeName( int index );
}
