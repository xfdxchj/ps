# 宝石系统 M1 —— 盲编译审清单 (给免费AI)

> 当好"静态编译器,严格按 Java + 此工程上游 API"逐字节把关下面改动。
> 交付格式见 handoff.md 第四节；期望你要给出 <文件:行> 级的不符点或"无问题"。
> 编译无法本地跑，此审查是我们的首道关卡，务必从严（尤其 import/签名/visibility/空指针）。

待审文件（相对 E:\破碎的地牢\_EndShatteredBuild\）：
1. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent/EndGem.java
2. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent/EndGemProfile.java
3. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/endcontent/items/EndGemItem.java
4. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java   （新增：字段 gem/hasGem/gemType + bundle end_gem）
5. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/Armor.java     （同上）
6. core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java      （updateHT 生命宝石 / attackSkill 命中 / defenseSkill 闪避 / drRoll 减伤 / 近战伤害加攻击）

重点核对问题：
- EndGemItem 用了 `Item.defaultAction` 字段与 `defaultAction()` override 是否并存正确；`updateQuickslot()` 为 static 其被以实例调用是否合法；`detach`、`curUser`、`Messages.titleCase(String)`、`hero.belongings.armor` 字段、`GameScene.selectItem(WndBag.ItemSelector<Item>)` 签名是否对。
- 与我确认 Hero 相关方法实签名：`public void updateHT(boolean boostHP)`、attackSkill/defenseSkill/drRoll/damageRoll 返回值与局部变量类型(acc 改 int、evasion float?)。
- `Weapon.hasGem/gemType`、`Armor.hasGem/gemType` 定义是否与本批注释前后一致(尤其 gem/ gem的序数与 EndGem enum 序号映射)。
输出针对每文件是否"能过静态编译/有无可修复风险"。若有疑问标 UNKNOWN,不要瞎给结论。