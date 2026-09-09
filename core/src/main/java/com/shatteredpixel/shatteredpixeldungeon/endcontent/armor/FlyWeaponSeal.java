package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.ThrowWeaponCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

/**
 * 破印·进阶 「飞掷武器」。
 * 贴到护甲并穿戴后，护甲右键出现“飞掷武器”技能键（键无论在冷却或资源不足时都保留，
 * 点击若不可用只提示，不消失）。<br>
 * 不消耗邪能碎片：用一次冷却 20 回合；作用＝把你装备的近战武器单程掷向指定敌人,造成其 80% 面板伤害。
 */
public class FlyWeaponSeal extends BrokenSeal {

	public static final String KEY = "FLY_WEAPON";
	public static final float COOLDOWN = 20f;

	@Override public String name(){ return "破印·飞掷"; }

	@Override public String armorSkillKey(){ return KEY; }

	/** 是否“真正可用”：只需不在飞掷冷却 + 当前装备着近战武器(不耗邪能)。 */
	@Override public boolean armorSkillUsable(Hero hero){
		if (hero == null) return false;
		if (hero.buff(ThrowWeaponCooldown.class) != null) return false;
		return hero.belongings.weapon() instanceof Weapon;
	}

	@Override public void armorSkillEffect(final Hero hero){
		if (hero == null) return;
		//处于冷却：不执行,只提示仍剩几回合
		FlavourBuff cd = hero.buff(ThrowWeaponCooldown.class);
		if (cd != null){
			GLog.w("飞掷仍在冷却，" + Math.round(cd.visualcooldown()) + " 回合后才可再使用。");
			return;
		}
		GameScene.selectCell(new CellSelector.Listener() {
			@Override public String prompt(){ return "选择飞掷目标…"; }
			@Override public void onSelect(Integer cell){
				if (cell == null || cell == -1) return;
				Char ch = Actor.findChar(cell);
				Weapon wp = hero.belongings.weapon() instanceof Weapon ? (Weapon) hero.belongings.weapon() : null;
				if (ch == null || !ch.isAlive()) return;
				if (wp == null){ GLog.w("需要先装备一件近战武器才能飞掷。"); return; }
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

	@Override public String info(){
		return "念力附刃：把当前装备的近战武器单程掷向目标,造成其 80% 伤害。" +
				"\n不消耗邪能碎片;冷却 20 回合。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
