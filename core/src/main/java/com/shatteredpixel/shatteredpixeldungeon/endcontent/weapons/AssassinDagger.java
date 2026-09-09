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
 * 刺杀匕首 · 三分支 (MissileWeapon)
 * - 可投掷命中并“嵌”在敌人/格上，便于回收触发 处决/背刺/传送。
 * - 邪能(MetalShard)在炼金/详情里改三态 NORMAL→(三叉戟/处决/传送)。
 */
public class AssassinDagger extends MissileWeapon {

	public enum Type { NORMAL, TRIDENT, EXECUTION, TELEPORT }

	public Type type = Type.NORMAL;
	public Char stuckEnemy = null;
	public int  stuckCell  = -1;

	public static final String AC_UPGRADE = "DAG_UP";
	public static final String AC_RECOVER = "DAG_REC";
	public static final String AC_TELE    = "DAG_TEL";

	{
		image = ItemSpriteSheet.DART_TIPPED;
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
		else if (action.equals(AC_TELE))    teleportTo(hero);
	}

	private void upgradePick(final Hero hero){
		if (findMetal(hero) == null) return;
		String[] opts = { "强化为三叉戟", "强化为处决型", "强化为传送型" };
		GameScene.show(new WndOptions(Messages.titleCase(name()),
				"消耗 1 枚邪能(MetalShard)，强化为一种形态:", opts){
			@Override protected void onSelect(int index){
				Item m = findMetal(hero);
				if (m == null) return;
				m.quantity(m.quantity()-1);
				switch(index){
					case 0: type = Type.TRIDENT;   break;
					case 1: type = Type.EXECUTION; break;
					case 2: type = Type.TELEPORT;  break;
				}
				updateQuickslot();
			}
		});
	}

	private void backstab(Hero hero){
		if (stuckEnemy != null){
			boolean ex = type == Type.EXECUTION
					&& !Char.hasProp(stuckEnemy, Char.Property.BOSS)
					&& stuckEnemy.HP > 0 && stuckEnemy.HP < stuckEnemy.HT*0.25f;
			stuckEnemy.damage(ex ? stuckEnemy.HP : Math.round(damageRoll(hero)*1.10f), this);
			cd(hero, ex);
		}
		clear();
	}

	private void teleportTo(Hero hero){
		int dest = stuckEnemy != null ? stuckEnemy.pos : stuckCell;
		if (dest >= 0 && Dungeon.level.insideMap(dest) && !Dungeon.level.solid[dest]){
			hero.pos = dest;
			Buff.affect(hero, Invisibility.class, 1f);
			Buff.affect(hero, TeleportCooldown.class, 100f);
		} else {
			cd(hero, false);
		}
		clear();
	}

	private void cd(Hero hero, boolean exec){
		Buff.affect(hero, exec ? ExecutionCooldown.class : RecoverCooldown.class,
				exec ? 50f : 30f);
	}

	private void clear(){
		stuckEnemy = null;
		stuckCell  = -1;
	}

	@Override protected void onThrow(int cell){
		super.onThrow(cell);
		Char t = Actor.findChar(cell);
		if (t != null && t.isAlive()){ stuckEnemy = t; stuckCell = t.pos; }
		else if (Dungeon.level.insideMap(cell) && !Dungeon.level.solid[cell]){ stuckCell = cell; }
	}

	private Item findMetal(Hero hero){
		if (hero.belongings.backpack.items == null) return null;
		for (Item i : hero.belongings.backpack.items){
			if (i instanceof MetalShard && i.quantity() > 0) return i;
		}
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
