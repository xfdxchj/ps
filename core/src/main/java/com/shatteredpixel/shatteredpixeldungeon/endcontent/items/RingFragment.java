package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * END(无尽炼金): 戒指碎片。
 * 6 枚**不同种类**的原版戒指 → 1 个碎片；碎片记录用过的戒指种类。
 * 2 个互不重复的碎片 + 2 无尽锭 + 戒指核心 → 轮回噬灭之戒。
 */
public class RingFragment extends Item {

	public final HashSet<String> rings = new HashSet<>();

	{
		image = ItemSpriteSheet.GRIMM_CORE_RING;
		stackable = false;
		bones = false;
	}

	@Override public String name(){ return "戒指碎片"; }

	@Override
	public String desc(){
		StringBuilder sb = new StringBuilder("由六枚不同的原版戒指熔成的碎片。\n\n包含：");
		for (String s : rings) sb.append("\n· ").append(s);
		return sb.toString();
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	private static final String RINGS = "rings";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(RINGS, rings.toArray(new String[0]));
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		rings.clear();
		String[] a = bundle.getStringArray(RINGS);
		if (a != null) for (String s : a) rings.add(s);
	}

	/** 原版十二戒指（排除本 fork 新增的 RingOfMistress）。 */
	public static final Class<? extends Ring>[] VANILLA_RINGS = new Class[]{
			RingOfAccuracy.class, RingOfArcana.class, RingOfElements.class,
			RingOfEnergy.class, RingOfEvasion.class, RingOfForce.class,
			RingOfFuror.class, RingOfHaste.class, RingOfMight.class,
			RingOfSharpshooting.class, RingOfTenacity.class, RingOfWealth.class
	};

	public static String ringLabel(Class<?> c){
		return c.getSimpleName().replace("RingOf", "");
	}

	/** 固定配方两组，各 6 枚，互不重复。 */
	public static final Class<? extends Ring>[] GROUP_A = new Class[]{
			RingOfAccuracy.class, RingOfArcana.class, RingOfElements.class,
			RingOfEnergy.class, RingOfEvasion.class, RingOfForce.class
	};
	public static final Class<? extends Ring>[] GROUP_B = new Class[]{
			RingOfFuror.class, RingOfHaste.class, RingOfMight.class,
			RingOfSharpshooting.class, RingOfTenacity.class, RingOfWealth.class
	};

	private static boolean matchesGroup(ArrayList<Item> ingredients, Class<? extends Ring>[] group){
		if (ingredients.size() != group.length) return false;
		boolean[] used = new boolean[group.length];
		for (Item it : ingredients){
			if (!(it instanceof Ring) || it.cursed || !it.isIdentified()) return false;
			int idx = -1;
			for (int i = 0; i < group.length; i++){
				if (it.getClass() == group[i]){ idx = i; break; }
			}
			if (idx < 0 || used[idx]) return false;
			used[idx] = true;
		}
		return true;
	}

	/** 炼金：恰好 6 枚不同种类的原版戒指 → 1 碎片。 */
	public static class FragmentRecipe extends Recipe {
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients){
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled()) return false;
			return matchesGroup(ingredients, GROUP_A) || matchesGroup(ingredients, GROUP_B);
		}

		@Override public int cost(ArrayList<Item> ingredients){ return 0; }

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;
			RingFragment f = new RingFragment();
			for (Item it : ingredients){
				if (it instanceof Ring){
					f.rings.add(ringLabel(it.getClass()));
					com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume.remove(it);
				}
			}
			return f;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;
			RingFragment f = new RingFragment();
			for (Item it : ingredients){
				if (it instanceof Ring) f.rings.add(ringLabel(it.getClass()));
			}
			return f;
		}
	}
}
