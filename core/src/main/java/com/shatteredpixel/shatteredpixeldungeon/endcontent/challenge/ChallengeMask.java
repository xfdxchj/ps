/*
 * 破碎的地牢 (End fork) — 挑战规则框架
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge;

/**
 * END(挑战框架): 任意位宽的挑战规则掩码。
 *
 * <h3>为什么需要它</h3>
 * 原版 {@code Dungeon.challenges} 是 {@code int}，只能装 32 位
 * （实际被 {@code Challenges.MAX_VALUE = 4095} 夹到 12 位）。
 * 而权威清单有 <b>108 条</b>规则（表 ID 最大 138），{@code int} 和
 * {@code long} 都不够，因此改用 {@code long[]}。
 *
 * <h3>关键设计：位号 = 表 ID，不是数组下标</h3>
 * 原表编号有断层（50/51/53/66/82–85/89/91–94/98/99/102/105–107 是空号），
 * 所以<b>不能用数组下标当位号</b>——否则以后插入新规则会让所有旧存档错位。
 * 这里直接把表 ID 当位号：{@code bit(ID)}，最大需要 139 位 → 3 个 {@code long}。
 *
 * <h3>存档兼容</h3>
 * 用 {@code long[]} 存储，{@code Bundle} 原生支持。
 * 旧的 int 掩码（原版 12 条挑战）通过 {@link #fromLegacyInt(int)} 迁移到低 12 位。
 *
 * <p>本类**不可变**（immutable），所有修改操作返回新实例，避免引用共享导致的串档 bug。
 */
public final class ChallengeMask {

	/** 需要的 long 数量：表 ID 最大 138 → 139 位 → ceil(139/64) = 3。 */
	private static final int WORDS = 3;

	/** 全零。 */
	public static final ChallengeMask NONE = new ChallengeMask(new long[WORDS]);

	private final long[] bits;

	private ChallengeMask(long[] bits) {
		this.bits = bits;
	}

	//==== 构造 ====

	/** 空掩码。 */
	public static ChallengeMask empty() {
		return NONE;
	}

	/** 从原始 long[] 构造（会复制并裁剪到 WORDS）。 */
	public static ChallengeMask of(long[] raw) {
		long[] b = new long[WORDS];
		if (raw != null) {
			for (int i = 0; i < Math.min(raw.length, WORDS); i++) {
				b[i] = raw[i];
			}
		}
		return new ChallengeMask(b);
	}

	/**
	 * END(存档兼容): 从原版 int 掩码迁移。
	 *
	 * <p>原版 12 条挑战占用 bit 0..11（{@code NO_FOOD=1} … {@code INFLATION=2048}，
	 * 以及越界的 {@code DHXD=8192} 会被丢弃）。直接放到低 12 位。
	 */
	public static ChallengeMask fromLegacyInt(int legacy) {
		long[] b = new long[WORDS];
		b[0] = legacy & 0xFFFFFFFFL;
		return new ChallengeMask(b);
	}

	/** 导出为 long[]（存盘用）。 */
	public long[] toLongArray() {
		long[] copy = new long[WORDS];
		System.arraycopy(bits, 0, copy, 0, WORDS);
		return copy;
	}

	/**
	 * END(存档兼容): 导出为原版 int 掩码（只取低 12 位）。
	 * <p>供 {@code Dungeon.challenges} 与旧 {@code Challenges.isItemBlocked} 等继续使用。
	 */
	public int toLegacyInt() {
		return (int) (bits[0] & 0xFFFL);   // 只保留原版 12 位
	}

	//==== 位运算 ====

	/** 表 ID 是否已启用。 */
	public boolean has(int id) {
		if (id < 0) return false;
		int w = id >>> 6;              // id / 64
		if (w >= WORDS) return false;
		return (bits[w] & (1L << (id & 63))) != 0;
	}

	/** 启用某条规则，返回新掩码。 */
	public ChallengeMask with(int id) {
		if (id < 0) return this;
		int w = id >>> 6;
		if (w >= WORDS) return this;   // 超出容量，忽略（防御）
		long[] b = copyBits();
		b[w] |= (1L << (id & 63));
		return new ChallengeMask(b);
	}

	/** 停用某条规则，返回新掩码。 */
	public ChallengeMask without(int id) {
		if (id < 0) return this;
		int w = id >>> 6;
		if (w >= WORDS) return this;
		long[] b = copyBits();
		b[w] &= ~(1L << (id & 63));
		return new ChallengeMask(b);
	}

	/** 切换某条规则，返回新掩码。 */
	public ChallengeMask toggle(int id) {
		return has(id) ? without(id) : with(id);
	}

	/** 是否为空（一条都没选）。 */
	public boolean isEmpty() {
		for (long w : bits) {
			if (w != 0) return false;
		}
		return true;
	}

	private long[] copyBits() {
		long[] b = new long[WORDS];
		System.arraycopy(bits, 0, b, 0, WORDS);
		return b;
	}

	//==== 与注册表联动的便捷方法 ====

	/** 某条规则是否启用（按定义对象）。 */
	public boolean has(ChallengeDef def) {
		return def != null && has(def.id);
	}

	/** 启用/停用一条规则（按定义对象）。 */
	public ChallengeMask set(ChallengeDef def, boolean on) {
		if (def == null) return this;
		return on ? with(def.id) : without(def.id);
	}

	/** 启用/停用一条规则（按定义对象）。 */
	public ChallengeMask toggle(ChallengeDef def) {
		return def == null ? this : toggle(def.id);
	}

	/**
	 * 统计已启用的**正式**规则数量。
	 *
	 * <p>排除测试/娱乐类（{@link ChallengeDef#countsForLevel} 为 false 的项，
	 * 例如「便利测试包」），用于随机挑战骰子与 UI 计数。
	 */
	public int activeCount() {
		int n = 0;
		for (ChallengeDef def : ChallengeRegistry.ALL) {
			if (has(def.id) && def.countsForLevel) n++;
		}
		return n;
	}

	/**
	 * END(通过等级): 计算通过等级。
	 *
	 * <p>权威清单定义：{@code 通过等级 = ∑ (启用规则的等级数值)}。
	 * 只累加 {@link ChallengeDef#countsForLevel} 为 true 的规则。
	 */
	public int passLevel() {
		int sum = 0;
		for (ChallengeDef def : ChallengeRegistry.ALL) {
			if (has(def.id) && def.countsForLevel) {
				sum += def.level;
			}
		}
		return sum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChallengeMask)) return false;
		ChallengeMask other = (ChallengeMask) o;
		for (int i = 0; i < WORDS; i++) {
			if (bits[i] != other.bits[i]) return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int h = 1;
		for (long w : bits) {
			h = 31 * h + (int) (w ^ (w >>> 32));
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ChallengeMask[");
		boolean first = true;
		for (ChallengeDef def : ChallengeRegistry.ALL) {
			if (has(def.id)) {
				if (!first) sb.append(", ");
				sb.append(def.id).append(':').append(def.name);
				first = false;
			}
		}
		return sb.append(']').toString();
	}
}
