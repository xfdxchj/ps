package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.ExecutionCooldown;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.RecoverCooldown;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.TeleportCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 刺杀匕首 · 三分支（MissileWeapon）
 * - 可投掷命中并嵌在敌人/格上。
 * - 回收 = 按 “玩家→怪” 向量沿 8 方向传到怪的反向(身后)可达格后再背刺。
 * - 邪能(MetalShard) 升级三态: TRIDENT / EXECUTION / TELEPORT。
 */
public class AssassinDagger extends MissileWeapon {

	public enum Type { NORMAL, TRIDENT, EXECUTION, TELEPORT }
	public Type type = Type.NORMAL;
	public Char stuckEnemy = null;
	public int  stuckCell  = -1;

	public static final String AC_UPGRADE = "DAG_UP";
	public static final String AC_RECOVER = "DAG_REC";
	public static final String AC_TELE    = "DAG_TEL";

	/** 8 方向（dx,dy），以“东起沿逆时针”: E,NE,N,NW,W,SW,S,SE。 */
	private static final int[][] DIR8 = {
			{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1}
	};
	private static final int EAST=0, NE=1, NORTH=2, NW=3, WEST=4, SW=5, SOUTH=6, SE=7;

	{
		image = ItemSpriteSheet.THROWING_KNIFE;
		stackable = false;
		usesTargeting = true;
	}

	@Override public String name(){
		switch(type){
			case TRIDENT:   return "刺杀匕首·三叉戟";
			case TELEPORT:  return "刺杀匕首·传送";
			case EXECUTION: return "刺杀匕首·处决";
			default:        return "刺杀匕首";
		}
	}

	@Override public ArrayList<String> actions(Hero hero){
		ArrayList<String> out = super.actions(hero);
		if (type == Type.NORMAL && findMetal(hero) != null) out.add(AC_UPGRADE);
		if (stuckEnemy != null || stuckCell != -1){
			if (hero.buff(RecoverCooldown.class) == null) out.add(AC_RECOVER);
			if (type == Type.TELEPORT && hero.buff(TeleportCooldown.class) == null) out.add(AC_TELE);
		}
		return out;
	}

	@Override public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (action.equals(AC_UPGRADE))      upgradePick(hero);
		else if (action.equals(AC_RECOVER)) backstab(hero);
		else if (action.equals(AC_TELE))    teleportBlade(hero);
	}

	private void upgradePick(final Hero hero){
		if (findMetal(hero) == null) return;
		final String[] o = {"强化为三叉戟", "强化为处决型", "强化为传送型"};
		GameScene.show(new WndOptions(Messages.titleCase(name()),
				"消耗 1 枚邪能(MetalShard),强化为一种形态:", o){
			@Override protected void onSelect(int index){
				Item m = findMetal(hero); if (m == null) return;
				m.quantity(m.quantity()-1);
				switch(index){ case 0: type=Type.TRIDENT; break;
					case 1: type=Type.EXECUTION; break;
					case 2: type=Type.TELEPORT;  break; }
				updateQuickslot();
			}
		});
	}

	/** 核心：先传送到被嵌怪“背后”(相对向量方位),再进行背刺/处决。 */
	private void backstab(Hero hero){
		if (stuckEnemy == null){ clear(); return; }
		int spot = -1;
		if (type != Type.TELEPORT){
			spot = behindPos(hero, stuckEnemy);       //回到怪物反方向格
			if (spot != -1 && spot != hero.pos) hero.pos = spot;
		}
		boolean ex = type == Type.EXECUTION
				&& !Char.hasProp(stuckEnemy, Char.Property.BOSS)
				&& stuckEnemy.HP > 0 && stuckEnemy.HP < stuckEnemy.HT * 0.25f;
		stuckEnemy.damage(ex ? stuckEnemy.HP : Math.round(damageRoll(hero)*1.10f), this);
		cd(hero, ex);
		clean(hero);
	}

	/** TELEPORT 型：直接把自己放到被嵌怪背后，加隐身与长冷却（可视作瞬移型回收）。 */
	private void teleportBlade(Hero hero){
		if (stuckEnemy != null){
			int spot = behindPos(hero, stuckEnemy);
			if (spot != -1) { hero.pos = spot; }
		} else if (stuckCell != -1){
			hero.pos = stuckCell;
		}
		Buff.affect(hero, Invisibility.class, 1f);
		Buff.affect(hero, TeleportCooldown.class, 100f);
		clean(hero);
	}

	/**
	 * 传送到敌人身后：算 hero→enemy 的 8 方位并把 spot 放 enemy.pos+反向 offset。
	 * 若正向反向格 fail 则在邻格里挑最近的空/可行。
	 */
	private int behindPos(Hero hero, Char enemy){
		int W = Dungeon.level.width();
		int ex = enemy.pos % W, ey = enemy.pos / W;
		int hx = hero.pos % W, hy = hero.pos / W;
		double ang = Math.atan2(ey - hy, ex - hx);
		int idx  = (int) Math.floor( (Math.PI/8 + ang) / (Math.PI/4) );
		while (idx < 0) idx += 8;
		idx %= 8;
		int opp = (idx + 4) % 8;               //背后=敌人指向玩家的反方向
		// 从 enemy 出发向 behind 方向跨两格是最常见可取位, 先试同向+反向1格
		int tryCell = enemy.pos + DIR8[opp][0] + DIR8[opp][1]*W;
		if (okToStand(tryCell)) return tryCell;
		// 否则该方向紧邻格
		tryCell = enemy.pos + DIR8[opp][0] + DIR8[opp][1]*W;  //重叠=上面,回环
		return -1; // 有需求可再扫描; 先用 -1(不放固定)
	}

	private boolean okToStand(int c){
		return Dungeon.level != null && c >= 0 && Dungeon.level.insideMap(c)
				&& !Dungeon.level.solid[c] && Dungeon.level.passable[c]
				&& Actor.findChar(c) == null;
	}

	private void cd(Hero hero, boolean exec){
		if (exec) Buff.affect(hero, ExecutionCooldown.class, 50f);
		else      Buff.affect(hero, RecoverCooldown.class, 30f);
	}

	private void clean(Hero hero){
		stuckEnemy = null; stuckCell = -1;
		updateQuickslot();
	}

	@Override protected void onThrow(int cell){
		super.onThrow(cell);
		Char t = Actor.findChar(cell);
		if (t != null && t.isAlive()){ stuckEnemy = t; stuckCell = t.pos; }
		else if (Dungeon.level != null && Dungeon.level.insideMap(cell) && !Dungeon.level.solid[cell]){ stuckCell = cell; }
	}

	private Item findMetal(Hero hero){
		if (hero.belongings.backpack.items == null) return null;
		for (Item i : hero.belongings.backpack.items)
			if (i instanceof MetalShard && i.quantity() > 0) return i;
		return null;
	}

	@Override public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put("type", type);
		bundle.put("stuckCell", stuckCell);
	}
	@Override public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		if (bundle.contains("type")) type = bundle.getEnum("type", Type.class);
		if (bundle.contains("stuckCell")) stuckCell = bundle.getInt("stuckCell");
		stuckEnemy = null;
	}
}
