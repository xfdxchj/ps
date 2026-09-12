package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;

/**
 * ═══════════════════════════════════════════════════════════════
 *  地形调试覆盖层（排查"贴图错乱"用）
 * ═══════════════════════════════════════════════════════════════
 *
 * 开启后在屏幕左上角实时显示鼠标所指格子：
 *     · 格号与坐标
 *     · Terrain 值 + 名称
 *     · 该格实际使用的图案索引，以及它在贴图上的 (列, 行)
 *     · 周围 8 格的地形值
 *
 * 用途：用户报告"墙壁贴图错乱"时，可以精确定位是哪一格、
 * 用的哪个图案索引、这个索引在贴图上落在什么位置。
 *
 * 开关：{@link #enabled}
 */
public class TerrainDebugOverlay extends Component {

    /** 总开关。排查完地形问题后置回 false 即可。 */
    public static boolean enabled = true;

    /** 当前已挂载的实例 */
    private static TerrainDebugOverlay instance;

    private RenderedTextBlock label;

    public TerrainDebugOverlay() {
        super();
        instance = this;

        label = PixelScene.renderTextBlock( 6 );
        label.hardlight( 0xFFFF00 );
        label.maxWidth( 140 );
        add( label );
    }

    /** 当前是否已挂载 */
    public static boolean attached() {
        return instance != null && instance.parent != null;
    }

    /** 供 GameScene 在 create() 里调用。 */
    public static void attachTo( com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene scene ) {
        if (instance != null && instance.parent != null) {
            return;      // 已挂
        }
        try {
            TerrainDebugOverlay o = new TerrainDebugOverlay();
            //END(修复·关键): 必须用 UI 相机。原来写成 Camera.main（游戏世界相机），
            //UI 元素会被画到地图坐标系里 → 跑到屏幕外看不见。
            o.camera = com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera;
            scene.add( o );
        } catch (Exception e) {
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
                    "[调试] 地形覆盖层挂载失败: " + e);
        }
    }

    @Override
    public void update() {
        super.update();

        if (label == null) return;

        if (!enabled || Dungeon.level == null) {
            visible = false;
            return;
        }
        visible = true;

        try {
            PointF hover = PointerEvent.currentHoverPos();
            float sx = (hover != null) ? hover.x : Game.width / 2f;
            float sy = (hover != null) ? hover.y : Game.height / 2f;

            int cell = cellAtScreen( sx, sy );
            if (cell < 0 || cell >= Dungeon.level.length()) {
                label.text( "（指针不在有效格上）" );
                label.setPos( 4, 4 );
                return;
            }

            int terr = Dungeon.level.map[cell];
            int w = Dungeon.level.width();

            StringBuilder sb = new StringBuilder();

            sb.append( "格 " ).append( cell )
              .append( " (" ).append( cell % w ).append( "," ).append( cell / w ).append( ")\n" );

            sb.append( "Terrain " ).append( terr )
              .append( " " ).append( terrainName( terr ) ).append( "\n" );

            // directVisuals 里有没有这一项
            int direct = -1;
            try {
                Integer d = DungeonTileSheet.directVisuals.get( terr, -1 );
                if (d != null) direct = d;
            } catch (Throwable ignored) { }

            if (direct >= 0) {
                sb.append( "pattern " ).append( direct )
                  .append( " @(" ).append( direct % 16 )
                  .append( "," ).append( direct / 16 ).append( ")\n" );
            } else {
                sb.append( "pattern 走缝合(墙/门/水)\n" );
            }

            // 周围 8 格
            sb.append( "n8: " );
            for (int n : PathFinder.NEIGHBOURS8) {
                int c2 = cell + n;
                if (c2 < 0 || c2 >= Dungeon.level.length()) {
                    sb.append( "- " );
                } else {
                    sb.append( Dungeon.level.map[c2] ).append( " " );
                }
            }

            label.text( sb.toString() );
            label.setPos( 4, 4 );

        } catch (Exception e) {
            label.text( "调试异常: " + e );
            label.setPos( 4, 4 );
        }
    }

    /** 屏幕坐标 -> 格子 */
    private int cellAtScreen( float x, float y ) {
        try {
            //END(修复): 屏幕坐标 -> 地图格。用游戏世界相机做主转换。
            Camera cam = Camera.main;
            PointF world = cam.screenToCamera( (int) x, (int) y );

            int col = (int) Math.floor( world.x / DungeonTilemap.SIZE );
            int row = (int) Math.floor( world.y / DungeonTilemap.SIZE );

            if (col < 0 || row < 0
                    || col >= Dungeon.level.width()
                    || row >= Dungeon.level.height()) {
                return -1;
            }
            return row * Dungeon.level.width() + col;
        } catch (Exception e) {
            return -1;
        }
    }

    /** Terrain 值 -> 名字 */
    public static String terrainName( int t ) {
        if (t == Terrain.WALL)           return "WALL";
        if (t == Terrain.WALL_DECO)      return "WALL_DECO";
        if (t == Terrain.EMPTY)          return "EMPTY";
        if (t == Terrain.EMPTY_DECO)     return "EMPTY_DECO";
        if (t == Terrain.GRASS)          return "GRASS";
        if (t == Terrain.HIGH_GRASS)     return "HIGH_GRASS";
        if (t == Terrain.FURROWED_GRASS) return "FURROWED_GRASS";
        if (t == Terrain.DOOR)           return "DOOR";
        if (t == Terrain.OPEN_DOOR)      return "OPEN_DOOR";
        if (t == Terrain.LOCKED_DOOR)    return "LOCKED_DOOR";
        if (t == Terrain.SECRET_DOOR)    return "SECRET_DOOR";
        if (t == Terrain.CRYSTAL_DOOR)   return "CRYSTAL_DOOR";
        if (t == Terrain.ENTRANCE)       return "ENTRANCE";
        if (t == Terrain.EXIT)           return "EXIT";
        if (t == Terrain.CHASM)          return "CHASM";
        if (t == Terrain.WATER)          return "WATER";
        if (t == Terrain.TRAP)           return "TRAP";
        if (t == Terrain.SECRET_TRAP)    return "SECRET_TRAP";
        if (t == Terrain.INACTIVE_TRAP)  return "INACTIVE_TRAP";
        if (t == Terrain.EMBERS)         return "EMBERS";
        if (t == Terrain.BOOKSHELF)      return "BOOKSHELF";
        if (t == Terrain.ALCHEMY)        return "ALCHEMY";
        if (t == Terrain.STATUE)         return "STATUE";
        if (t == Terrain.STATUE_SP)      return "STATUE_SP";
        if (t == Terrain.WELL)           return "WELL";
        if (t == Terrain.EMPTY_WELL)     return "EMPTY_WELL";
        if (t == Terrain.PEDESTAL)       return "PEDESTAL";
        if (t == Terrain.BARRICADE)      return "BARRICADE";
        if (t == Terrain.REGION_DECO)    return "REGION_DECO";
        if (t == Terrain.REGION_DECO_ALT)return "REGION_DECO_ALT";
        return "?";
    }
}
