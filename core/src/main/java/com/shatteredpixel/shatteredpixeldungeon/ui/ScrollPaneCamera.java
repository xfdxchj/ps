/*
 * 破碎的地牢 (End fork) — ScrollPane 相机修正工具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.noosa.Camera;

/**
 * END(通用修复·ScrollPane 右下偏移): 修正 ScrollPane 内部裁剪相机的位置。
 *
 * <h3>问题现象</h3>
 * 带 {@link ScrollPane} 的窗口里，内容整体**向右下偏移** ——
 * 表现为列表不贴左边缘、点击位置与视觉位置对不上。
 *
 * <h3>根因</h3>
 * {@code ScrollPane.layout()} 里用：
 * <pre>
 *   Point p = camera().cameraToScreen( x, y );
 *   cs.x = p.x;  cs.y = p.y;
 * </pre>
 * 而 {@code Gizmo.camera()} 是**向上查找并缓存**的：
 * <pre>
 *   if (camera != null) return camera;
 *   else if (parent != null) return camera = parent.camera();
 *   else return null;
 * </pre>
 * 于是它算出来的坐标依赖"当时父链解析到了哪个相机"。
 * 在 {@code add()} 之前或父链尚未接好时调用 {@code layout()}，
 * 算出的位置就会偏（实测是 {@code uiCamera} 的中心坐标）。
 *
 * <h3>解法</h3>
 * 两道保险，缺一不可：
 * <ol>
 *   <li>{@link #bindCamera} —— 把 {@code pane.camera} 显式绑到窗口相机，
 *       让 {@code camera()} 不会再解析到 {@code uiCamera}</li>
 *   <li>{@link #placeContentCamera} —— 在窗口的 {@code update()} 里
 *       **每帧**重算内部相机的屏幕位置，覆盖 {@code layout()} 可能算错的</li>
 * </ol>
 */
public final class ScrollPaneCamera {

	/** 本类是纯工具，不允许实例化。 */
	private ScrollPaneCamera() {}

	/**
	 * END(通用修复): 把 ScrollPane 的父级相机显式绑定到窗口相机。
	 *
	 * <p>必须在 {@code add(pane)} 之后调用 —— 那时 {@code window.camera} 才有值。
	 */
	public static void bindCamera(ScrollPane pane, Window window) {
		if (pane == null || window == null) return;
		if (pane.camera != window.camera) {
			pane.camera = window.camera;
		}
		if (window.camera != null && pane.content() != null
				&& pane.content().camera != null) {
			//内部裁剪相机也要跟着（它的 zoom 必须与窗口一致，否则内容会缩放）
			pane.content().camera.zoom = window.camera.zoom;
		}
	}

	/**
	 * END(通用修复): 把内部裁剪相机摆到窗口里的正确屏幕位置。
	 *
	 * <p>在窗口的 {@code update()} 里**每帧**调用（开销只是几次赋值）。
	 *
	 * @param pane   要修正的 ScrollPane
	 * @param window 它所在的窗口（用来取相机与坐标基准）
	 * @param height 可视区高度 —— 传 {@code pane.height()} 即可
	 */
	public static void placeContentCamera(ScrollPane pane, Window window, float height) {
		if (pane == null || window == null) return;
		if (pane.content() == null) return;

		Camera inner = pane.content().camera;
		if (inner == null) return;

		Camera outer = window.camera;
		if (outer == null) return;

		//内容点 (cx,cy) 相对窗口左上角；窗口相机把窗口坐标映射到屏幕，
		//所以内部裁剪相机的屏幕位置就是"窗口内偏移经相机变换后的结果"。
		inner.x = (int) ((pane.left() - outer.scroll.x) * outer.zoom + outer.x);
		inner.y = (int) ((pane.top()  - outer.scroll.y) * outer.zoom + outer.y);
		inner.zoom = outer.zoom;
		inner.resize(Math.max(1, (int) pane.width()), Math.max(1, (int) height));
	}
}
