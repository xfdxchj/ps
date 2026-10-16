/*
 * 破碎的地牢 (End fork) — 挑战「线性地牢」。
 *
 * 直接用原版 LineBuilder，只把它配置成"直线主路"：
 * 全部多连接房进主路、方向偏差 0、无隧道、无额外连接。
 * 不再自己手摆房间（曾把房间摆到地图外 + 让 RegularLevel.build 死循环）。
 *
 * 注意：RegularLevel.build() 是 while (rooms == null)，本类不能返回 null。
 * LineBuilder 自带随机性，外层重试能收敛；极端情况下再退回 LoopBuilder。
 */
package com.shatteredpixel.shatteredpixeldungeon.levels.builders;

import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

import java.util.ArrayList;

public class PureLineBuilder extends LineBuilder {

	@Override
	public ArrayList<Room> build(ArrayList<Room> rooms) {
		setPathLength(1f, new float[]{0, 0, 0, 1});
		setPathVariance(0f);
		setTunnelLength(new float[]{0, 0, 0}, new float[]{0, 0, 0});
		setExtraConnectionChance(0f);

		ArrayList<Room> result = super.build(rooms);
		if (result != null) return result;

		//最后兜底：原版 LoopBuilder（正常地图，非直线）
		return new LoopBuilder()
				.setLoopShape(2, 0.5f, 0.3f)
				.build(rooms);
	}
}
