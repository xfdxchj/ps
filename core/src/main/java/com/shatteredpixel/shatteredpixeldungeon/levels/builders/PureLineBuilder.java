/*
 * 破碎的地牢 (End fork) — 挑战「线性地牢」：所有房间排成一条线。
 *
 * 复用原版 placeRoom：从入口出发，方向固定，把全部房间（含商店、
 * 特殊房、连接房）依次首尾相接排成一条直线，不再产生分叉。
 * 入口在一端，出口在另一端。
 */
package com.shatteredpixel.shatteredpixeldungeon.levels.builders;

import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class PureLineBuilder extends RegularBuilder {

	@Override
	public ArrayList<Room> build(ArrayList<Room> rooms) {

		setupRooms(rooms);

		if (entrance == null){
			return null;
		}

		entrance.setSize();
		entrance.setPos(0, 0);

		ArrayList<Room> rest = new ArrayList<>();
		if (shop != null) rest.add(shop);
		rest.addAll(mainPathRooms);
		rest.addAll(multiConnections);
		rest.addAll(singleConnections);
		Random.shuffle(rest);
		//出口放最后，保证线性的两端是入口 / 出口
		if (exit != null) rest.add(exit);

		final float direction = 90f; //0 = 上，90 = 右
		Room prev = entrance;
		for (Room r : rest){
			boolean ok = false;
			for (int t = 0; t < 16 && !ok; t++){
				float a = (t == 0) ? direction : direction + Random.Float(-25f, 25f);
				float ang = placeRoom(rooms, prev, r, a);
				ok = ang != -1;
			}
			if (!ok){
				return null;
			}
			prev = r;
		}

		findNeighbours(rooms);
		return rooms;
	}
}
