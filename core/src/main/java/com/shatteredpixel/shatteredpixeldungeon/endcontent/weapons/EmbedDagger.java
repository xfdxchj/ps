package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 「嵌入刺杀」回收模型基类（整条匕首家族共享的 embed→recover 状态机）。
 * <p>仿照 AssassinDagger 原始恢复模型：投掷命中后记录到底嵌了谁/落在哪格
 * ({@link #stuckEnemy} / {@link #stuckCell}); 玩家随后从物品的 AC_RECOVER 动作
 * 触发“回收/拔出”分支效果, 形同把刀从目标身上抽回来进行结算。
 * <p>回收动作是否可点由派生类各自的 FlavourBuff 冷却决定:
 * <ul><li>DaggerTeleport  回收→把英雄传送到被嵌敌背后 + 隐匿 + TeleportCooldown;</li>
 *     <li>DaggerExecution 回收→处决残血非 Boss 目标 + ExecutionCooldown。</li></ul>
 * 本类为抽象公共基：具体成品均 CONCRETE / 无参构造, 并在派生里给出各自的 name/info。
 */
public abstract class EmbedDagger extends MissileWeapon {

	//回收动作键。与原始匕首一致, 由各成品物品的 actions/execute 提供。
	public static final String AC_RECOVER = "DAG_REC";

	//嵌住的目标 / 落点。每次实例独立存放（基础类的实力字段）。
	public Char stuckEnemy = null;
	public int  stuckCell  = -1;

	{
		stackable = false;
		bones = false;
		tier = 2;
		baseUses = 5;
	}

	/** 派生类需要实现：真正“回收结算”的内容（传送或处决），执行后应调用 {@link #clean(Hero)}。 */
	protected abstract void recover( Hero hero );

	/** 该匕首当前是否处于可被“回收”的状态（冷却是否允许再次触发）。 */
	protected abstract boolean recoverable( Hero hero );

	@Override
	public ArrayList<String> actions( Hero hero ){
		ArrayList<String> out = super.actions( hero );
		if ((stuckEnemy != null || stuckCell != -1) && recoverable( hero )){
			out.add( AC_RECOVER );
		}
		return out;
	}

	@Override
	public void execute( Hero hero, String action ){
		super.execute( hero, action );
		if (action.equals( AC_RECOVER ) && recoverable( hero )){
			recover( hero );
		}
	}

	@Override
	protected void onThrow( int cell ){
		super.onThrow( cell );
		Char t = Actor.findChar( cell );
		if (t != null && t.isAlive()){
			stuckEnemy = t;
			stuckCell  = t.pos;
		} else if (Dungeon.level != null
				&& Dungeon.level.insideMap( cell )
				&& !Dungeon.level.solid[cell]){
			stuckEnemy = null;
			stuckCell  = cell;
		}
	}

	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 6 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	//无限耐久: 与基底匕首相同, 投掷后不因耐久损坏, 便于重复“投掷→回收”循环
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	/** 清空嵌住状态。 */
	protected void clean( Hero hero ){
		stuckEnemy = null;
		stuckCell  = -1;
		//物品在背包里可能需要刷新它的动作面板
		updateQuickslot();
	}

	/**
	 * “正背后”落点：扫 target 周边可站空格, 挑距 origin 最远的一格（近似敌人背后）。
	 * @return 空格的 cell; 若找不到可移位则返回 -1。
	 */
	protected int behindCell( Char origin, Char target ){

		int width  = Dungeon.level.width();
		int height = Dungeon.level.height();

		int best = -1;
		int bestDist = -1;

		int tx = target.pos % width;
		int ty = target.pos / width;
		int ox = origin.pos % width;
		int oy = origin.pos / width;

		for (int dy = -1; dy <= 1; dy++){
			for (int dx = -1; dx <= 1; dx++){
				if (dx == 0 && dy == 0) continue;

				int x = tx + dx;
				int y = ty + dy;
				if (x < 0 || x >= width || y < 0 || y >= height) continue;

				int cell = x + y * width;
				if (!Dungeon.level.insideMap( cell )) continue;
				if (Dungeon.level.solid[cell] || !Dungeon.level.passable[cell]) continue;
				if (Actor.findChar( cell ) != null) continue;

				int d = (x - ox)*(x - ox) + (y - oy)*(y - oy);
				if (d > bestDist){
					bestDist = d;
					best = cell;
				}
			}
		}
		return best;
	}

	/** 把被指定对象传送到某格(安全可行地), 失败返回 false；成帧移动/特效由魔法刷新。 */
	protected boolean moveTo( Char who, int cell ){
		if (!Dungeon.level.insideMap( cell )
				|| Dungeon.level.solid[cell] || !Dungeon.level.passable[cell]
				|| (Actor.findChar( cell ) != null && Actor.findChar( cell ) != who)){
			return false;
		}
		//真实可见传送: 走 ScrollOfTeleportation 的 appear（会处理移动 + 精灵落位）, 再刷新电平/迷雾。
		ScrollOfTeleportation.appear( who, cell );
		Dungeon.level.occupyCell( who );
		if (who == Dungeon.hero){
			Dungeon.observe();
			GameScene.updateFog();
		}
		return true;
	}

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		bundle.put( "stuckCell", stuckCell );
	}
	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		if (bundle.contains( "stuckCell" )) stuckCell = bundle.getInt( "stuckCell" );
		//无法对已保存的 Char 引用持久化，载档后以一格坐标兜底
		stuckEnemy = null;
	}
}
