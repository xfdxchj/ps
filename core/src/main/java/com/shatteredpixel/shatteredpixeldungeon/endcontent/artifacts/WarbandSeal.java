package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.BloodShieldCooldown;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.RageBuff;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.RageCooldown;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.ThrowWeaponCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 终焉 · 破印·军令 (三分支 artifact)
 *  由炼金主题:原版破印已有其机制(本件保留原版,属新增独立专属物);升阶需在
 *  NONE 时右键,消耗 1 枚邪能碎片(MetalShard),从三支中选定其一;此后固定该分支。
 *  分支: 血盾 / 狂暴/ 飞掷武;  附带当前护甲 DRmax×20% 的魔减参考。
 */
public class WarbandSeal extends Artifact {

	public enum Branch { NONE, BLOOD_SHIELD, RAGE, FLYING_WEAPON }

	public Branch branch = Branch.NONE;

	public static final String AC_PICK = "WARD_PICK";
	public static final String AC_BLOOD_SHIELD = "WARD_BLOOD";
	public static final String AC_RAGE         = "WARD_RAGE";
	public static final String AC_THROW        = "WARD_THROW";

	public static final float BLOOD_DURATION = 200f;
	public static final float RAGE_DURATION  = 200f;
	public static final float THROW_DURATION = 20f;

	{
		image = com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.ARTIFACT_TALISMAN;
	}

	@Override public String name(){ return "破印·军令"; }

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> list = super.actions(hero);
		if (!isEquipped(hero)) return list;
		if (branch == Branch.NONE) list.add(AC_PICK);
		else if (branch == Branch.BLOOD_SHIELD && hero.buff(BloodShieldCooldown.class) == null)
			list.add(AC_BLOOD_SHIELD);
		else if (branch == Branch.RAGE && hero.buff(RageCooldown.class) == null)
			list.add(AC_RAGE);
		else if (branch == Branch.FLYING_WEAPON && hero.buff(ThrowWeaponCooldown.class) == null)
			list.add(AC_THROW);
		return list;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		switch (action){
			case AC_PICK: chooseBranch(hero);                       break;
			case AC_BLOOD_SHIELD:
				hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.20f));
				Buff.affect(hero, Barrier.class).setShield( Math.round(hero.HT * 0.30f) );
				Buff.affect(hero, BloodShieldCooldown.class, BLOOD_DURATION);
				updateQuickslot();
				break;
			case AC_RAGE:
				hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.50f));
				Buff.affect(hero, RageBuff.class, RageBuff.DURATION);
				Buff.affect(hero, RageCooldown.class, RAGE_DURATION);
				updateQuickslot();
				break;
			case AC_THROW:
				if (hero.belongings.weapon() != null){
					hero.spend(1f);
					Buff.affect(hero, ThrowWeaponCooldown.class, THROW_DURATION);
					updateQuickslot();
				}
				break;
		}
	}

	/** NONE：消耗 1 枚邪能(MetalShard)，弹窗选一次分支。 */
	private void chooseBranch(final Hero hero){
		if (!hasMetal(hero)){ return; }
		final String[] opts = { "血盾", "狂暴", "飞行武器" };
		GameScene.show(new WndOptions("", "消耗 1 枚邪能碎片,选择破印分支:", opts){
			@Override
			protected void onSelect(int index){
				if (index < 0 || index >= opts.length) return;
				if (!takeMetal(hero)) return;
				switch(index){
					case 0: branch = Branch.BLOOD_SHIELD; break;
					case 1: branch = Branch.RAGE;         break;
					case 2: branch = Branch.FLYING_WEAPON;break;
				}
				updateQuickslot();
			}
		});
	}

	private boolean hasMetal(Hero hero){
		return findMetal(hero) != null;
	}

	private boolean takeMetal(Hero hero){
		MetalShard shard = findMetal(hero);
		if (shard == null) return false;
		shard.quantity(shard.quantity()-1);
		return true;
	}

	private MetalShard findMetal(Hero hero){
		if (hero.belongings.backpack.items == null) return null;
		for (com.shatteredpixel.shatteredpixeldungeon.items.Item i : hero.belongings.backpack.items){
			if (i instanceof MetalShard && i.quantity() > 0) return (MetalShard)i;
		}
		return null;
	}

	/** 当前护甲 DRmax 的 20% 作为固定魔法减伤参考。 */
	public int magicDefense(Hero hero){
		Armor acc = hero.belongings.armor();
		return acc == null ? 0 : Math.round(acc.DRMax() * 0.20f);
	}

	@Override public boolean isUpgradable(){ return false; }

	@Override public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put("branch", branch);
	}
	@Override public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		if (bundle.contains("branch")) branch = bundle.getEnum("branch", Branch.class);
	}
}
