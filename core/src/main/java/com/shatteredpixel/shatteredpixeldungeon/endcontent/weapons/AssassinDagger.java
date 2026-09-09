package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
 * 刺杀匕首 · 三态 (endcontent 新增)
 * MissileWeapon,可投掷留在敌人处,再绕后回收/背刺。
 * type: NORMAL→(炼金消耗邪能 MetalShard)三选一:
 *   TRIDENT  - 属性按三叉戟成长
 *   TELEPORT - 可传送到刃处+1 回隐
 *   EXECUTION- 保留 投→背后→收 回收处决非Boss(NOT teleport)
 */
public class AssassinDagger extends MissileWeapon {

	public enum Type { NORMAL, TRIDENT, TELEPORT, EXECUTION }

	public Type type = Type.NORMAL;
	public Char stuckEnemy = null;
	public int  stuckCell  = -1;

	public static final String AC_UPGRADE = "DAG_UP";
	public static final String AC_RECOVER = "DAG_REC";
	public static final String AC_TELE    = "DAG_TEL";

	public static final String TIERKIND = "kind";

	{
		image = ItemSpriteSheet.SHURIKEN;        //占位刀图,内部描述一致
		stackable = false;
		durability = 0;                          //无限耐久
		identify();
	}

	@Override public String name(){
		switch(type){
			case TRIDENT:   return "刺杀匕首·三叉戟";
			case TELEPORT:  return "刺杀匕首·传送";
			case EXECUTION: return "刺杀匕首·处决";
			default:        return "刺杀匕首";
		}
	}

	//成长
	@Override public int min(){ return (type==Type.TRIDENT)? 3+2*level() : 2+level(); }
	@Override public int max(){ return (type==Type.TRIDENT)? 15+5*level(): 10+2*level(); }

	@Override public ArrayList<String> actions(Hero hero){
		ArrayList<String> out = super.actions(hero);
		if (type == Type.NORMAL && findMetal(hero)!=null) out.add(AC_UPGRADE);
		if ((stuckEnemy!=null || stuckCell!=-1)){
			if (type==Type.TELEPORT && hero.buff(CustomCooldown.class)==null)      out.add(AC_TELE);
			if (hero.buff(CustomCooldown.class)==null)                            out.add(AC_RECOVER);
		}
		return out;
	}

	@Override public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (action.equals(AC_UPGRADE))      upgradePick(hero);
		else if (action.equals(AC_RECOVER)) backstab(hero);
		else if (action.equals(AC_TELE))    teleportTo(hero);
	}

	private void upgradePick(final Hero hero){
		String[] opts = {"强化为三叉戟","强化为处决型","强化为传送型"};
		GameScene.show(new WndOptions(name(), "消耗1枚邪能(MetalShard):", opts){
			@Override protected void onSelect(int idx){
				Item m = findMetal(hero);
				if (m==null) return;
				m.quantity(m.quantity()-1);
				switch(idx){
					case 0: type=Type.TRIDENT;   break;
					case 1: type=Type.EXECUTION; break;
					case 2: type=Type.TELEPORT;  break;
				}
				updateQuickslot();
			}
		});
	}

	private void backstab(Hero hero){
		if (stuckEnemy != null){
			boolean ex = type==Type.EXECUTION && !Char.hasProp(stuckEnemy, Char.Property.BOSS)
					&& stuckEnemy.HP>0 && stuckEnemy.HP < stuckEnemy.HT*0.25f;
			if (ex){
				stuckEnemy.damage(stuckEnemy.HP, this);
			} else {
				int dmg = Math.round(damageRoll(hero)*1.10f);
				stuckEnemy.damage(dmg, this);
			}
			Buff.affect(hero, CustomCooldown.class, ex ? 50f : 30f);
		}
		clearStuck(hero);
	}

	private void teleportTo(Hero hero){
		int dest = stuckEnemy!=null? stuckEnemy.pos : stuckCell;
		if (dest!=-1 && Dungeon.level.insideMap(dest) && !Dungeon.level.solid[dest]){
			Scroll.move(hero, dest);     //轻移;丰富动画另行(不深扩 Hero)
			Buff.affect(hero, Invisibility.class, 1f);
		}
		Buff.affect(hero, CustomCooldown.class, 100f);
		clearStuck(hero);
	}

	private void clearStuck(Hero hero){
		stuckEnemy=null; stuckCell=-1;
		if (!hero.belongings.backpack.contains(this)){
			collect();
		}
	}

	/** 命中落点记录“嵌敌/嵌格”,用于回收。 */
	@Override protected void onThrow(int cell){
		Char t = Actor.findChar(cell);
		super.onThrow(cell);
		if (t!=null && t.isAlive()){ stuckEnemy=t; stuckCell=t.pos; }
		else if (Dungeon.level.insideMap(cell) && !Dungeon.level.solid[cell]){ stuckCell=cell; }
	}

	private Item findMetal(Hero hero){
		if (hero.belongings.backpack.items==null) return null;
		for (Item i : hero.belongings.backpack.items) if (i instanceof MetalShard && i.quantity()>0) return i;
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
		stuckCell = bundle.getInt("stuckCell");
		stuckEnemy = null;
	}
}
