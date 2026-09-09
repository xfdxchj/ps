package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.ThrowWeaponCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Callback;

/**
 * 破印·进阶 「飞掷武器」。
 * 贴到护甲并穿戴后，护甲右键出现“飞掷武器”技能键：
 * 消耗 1 枚邪能碎片(MetalShard)，把你装备的近战武器作为飞弹单程掷向指定敌人,造成其 80% 面板伤害。
 */
public class FlyWeaponSeal extends BrokenSeal {

	public static final String KEY = "FLY_WEAPON";
	public static final float COOLDOWN = 20f;

	@Override public String name(){ return "破印·飞掷"; }

	@Override public String armorSkillKey(){ return KEY; }

	@Override public boolean armorSkillUsable(Hero hero){
		if (hero == null) return false;
		if (hero.buff(ThrowWeaponCooldown.class) != null) return false;
		//需要有可投的装备中近战武器 + 至少 1 枚邪能碎片
		return hero.belongings.weapon() instanceof Weapon
				&& hasMetal(hero);
	}

	@Override public void armorSkillEffect(final Hero hero){
		if (hero == null || !armorSkillUsable(hero)) return;
		GameScene.selectCell(new CellSelector.Listener() {
			@Override public String prompt(){ return "选择飞掷目标…"; }
			@Override public void onSelect(Integer cell){
				if (cell == null || cell == -1) return;
				Char ch = Actor.findChar(cell);
				Weapon wp = hero.belongings.weapon() instanceof Weapon ? (Weapon) hero.belongings.weapon() : null;
				if (wp == null || ch == null || !ch.isAlive()) return;
				if (!takeMetal(hero)) return;
				MissileSprite flying = (MissileSprite) hero.sprite.parent.recycle(MissileSprite.class);
				flying.reset(hero.pos, ch.pos, wp, new Callback() {
					@Override public void call(){
						Hero cur = Dungeon.hero != null ? Dungeon.hero : hero;
						if (ch.isAlive()){
							int dmg = Math.round(wp.damageRoll(cur) * 0.80f);
							ch.damage(Math.max(1, dmg), wp);
						}
					}
				});
				hero.spend(1f);
				Buff.affect(hero, ThrowWeaponCooldown.class, COOLDOWN);
				Item.updateQuickslot();
			}
		});
	}

	/** 是否在背包里有 ≥1 枚邪能碎片。 */
	private static boolean hasMetal(Hero hero){
		if (hero.belongings.backpack.items == null) return false;
		for (Item i : hero.belongings.backpack.items){
			if (i instanceof MetalShard && i.quantity() > 0) return true;
		}
		return false;
	}

	private static boolean takeMetal(Hero hero){
		if (hero.belongings.backpack.items == null) return false;
		for (Item i : hero.belongings.backpack.items){
			if (i instanceof MetalShard && i.quantity() > 0){
				i.quantity(i.quantity() - 1);
				return true;
			}
		}
		return false;
	}

	@Override public String info(){
		return "念力附刃：消耗 1 枚邪能碎片,把当前装备的近战武器单程掷向目标,造成其 80% 伤害。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
