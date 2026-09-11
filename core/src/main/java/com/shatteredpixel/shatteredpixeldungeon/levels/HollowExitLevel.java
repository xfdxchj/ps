package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CUSTOM_DECO;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CUSTOM_DECO_EMPTY;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_SP;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.ENTRANCE;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.HIGH_GRASS;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL_DECO;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FrostFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Halo;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

/**
 * END(移植自魔绫·挑战区): 空洞遗迹入口层(26F)。
 * 楼梯 {@code BRANCH_EXIT} 会下到 depth+1（即 27F 的 HollowLevel）。
 * 移植调整：消息键由魔绫的 {@code NewLastLevel} 改为本 fork 已有的 {@code LastLevel}。
 */
public class HollowExitLevel extends Level {

    {
        color1 = 0x801500;
        color2 = 0xa68521;

        Statistics.NoTime = false;

        viewDistance = Math.min(5, viewDistance);
    }

    private static final int WIDTH = 13;
    private static final int HEIGHT = 23;

    private static final int W = WALL;

    private static final int E = WALL_DECO;

    private static final int O = EMPTY_SP;

    private static final int S = CUSTOM_DECO_EMPTY;   //原魔绫 SIGN_SP(可通行记号)
    private static final int K = CUSTOM_DECO;         //原魔绫 SIGN(墙上记号)
    private static final int X = CUSTOM_DECO;

    private static final int G = HIGH_GRASS;

    private static final int M = ENTRANCE;

    private static final int[] code_map = {
            K,K,K,K,K,K,K,K,K,K,K,K,K,
            W,K,K,K,K,K,O,K,K,K,K,K,W,
            W,K,O,O,S,O,O,O,S,O,O,K,W,
            E,E,O,O,O,O,O,O,O,O,O,E,E,
            E,O,X,O,O,O,O,O,O,O,X,O,E,
            E,O,O,O,O,O,O,O,O,O,O,O,E,
            K,K,K,K,K,O,O,O,K,K,K,K,K,
            K,K,K,K,K,O,O,O,K,K,K,K,K,
            E,O,O,K,K,O,O,O,K,K,O,O,E,
            E,O,O,O,K,O,O,O,K,O,O,O,E,
            E,O,G,O,O,O,O,O,O,O,G,O,E,
            E,E,O,O,X,O,O,O,X,O,O,E,E,
            W,E,E,O,O,O,O,O,O,O,E,E,W,
            W,W,E,E,E,O,O,O,E,E,E,W,W,
            W,W,W,W,E,O,O,O,E,W,W,W,W,
            W,W,W,W,E,E,O,E,E,W,W,W,W,
            W,W,W,W,E,O,O,O,E,W,W,W,W,
            W,W,W,W,E,O,O,O,E,W,W,W,W,
            W,W,W,W,E,O,O,O,E,W,W,W,W,
            W,W,W,W,E,E,O,E,E,W,W,W,W,
            W,W,W,W,E,O,O,O,E,W,W,W,W,
            W,W,W,W,E,O,M,O,E,W,W,W,W,
            W,W,W,W,E,E,E,E,E,W,W,W,W,
    };

    @Override
    public void playLevelMusic(){
        Music.playModeBGM(Assets.Music.HOLLOW_CITY, true);
    }

    @Override
    public boolean activateTransition(Hero hero, LevelTransition transition) {
        //END(修复): 原只处理 BRANCH_EXIT，但地图上实际可走的楼梯是 REGULAR_ENTRANCE(格279)，
        //导致类型不匹配 → 走 super → 卡在 26F 无法下到 27F。
        //现改为：26F 上任何出口类过渡都直接送往 27F。
        if (depth == 26 && (transition.type == LevelTransition.Type.BRANCH_EXIT
                || transition.type == LevelTransition.Type.REGULAR_ENTRANCE
                || transition.type == LevelTransition.Type.REGULAR_EXIT)) {
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                    if (timeFreeze != null) timeFreeze.disarmPresses();
                    Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                    if (timeBubble != null) timeBubble.disarmPresses();
                    InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                    InterlevelScene.curTransition = new LevelTransition();
                    InterlevelScene.curTransition.destDepth = depth + 1;   // → 27F
                    InterlevelScene.curTransition.destType = LevelTransition.Type.REGULAR_ENTRANCE;
                    InterlevelScene.curTransition.destBranch = 0;
                    InterlevelScene.curTransition.type = LevelTransition.Type.REGULAR_ENTRANCE;
                    InterlevelScene.curTransition.centerCell  = -1;
                    Game.switchScene( InterlevelScene.class );
                }
            });
            return false;
        } else {
            return super.activateTransition(hero, transition);
        }
    }

    @Override
    protected boolean build() {
        feeling = Feeling.NONE;
        setSize(WIDTH, HEIGHT);
        map = code_map.clone();

        //END(修复): 从 25F(Boss层) 下来时可能残留 LockedFloor / locked 状态，
        //而 Hero.actTransition 要求 !level.locked 才能踩楼梯 → 必须清掉。
        unseal();
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(
                com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero,
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor.class);

        //END(修复): 地图里唯一的 ENTRANCE(地形M) 在【格 279】。
        //（原先手工数成 240，用 _tools/locate_stairs.py 算出真实值是 279）
        int enter = 279;
        LevelTransition entrance = new LevelTransition(this, enter, LevelTransition.Type.REGULAR_EXIT);
        transitions.add(entrance);

        LevelTransition exitCell = new LevelTransition(this, enter, LevelTransition.Type.BRANCH_EXIT);
        transitions.add(exitCell);

        CustomTilemap vis = new townBehind();
        vis.pos(0, 0);
        customTiles.add(vis);

        CustomTilemap via = new townAbove();
        via.pos(0, 0);
        customTiles.add(via);

        return true;
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        addVisuals(this, visuals);
        return visuals;
    }

    public static void addVisuals(Level level, Group group){
        for (int i=0; i < level.length(); i++) {
            if (level.map[i] == CUSTOM_DECO) {
                group.add( new TorchA( i ) );
                group.add( new TorchB( i ) );
                group.add( new TorchC( i ) );
            }
            if (level.map[i] == CUSTOM_DECO_EMPTY) {
                group.add( new TorchD( i ) );
                group.add( new TorchE( i ) );
                group.add( new TorchF( i ) );
                group.add( new TorchG( i ) );
                group.add( new TorchH( i ) );
                group.add( new TorchI( i ) );
            }
        }
    }

    @Override
    protected void createMobs() {

    }
    public static int AMULET_POS = 136;

    @Override
    protected void createItems() {
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_HOLLOW_CS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_HALLS;
    }

    public static class townBehind extends CustomTilemap {

        {
            texture = Assets.Environment.HALL_OPX;

            tileW = 13;
            tileH = 23;
        }

        final int TEX_WIDTH = 13*16;

        @Override
        public Tilemap create() {

            Tilemap v = super.create();

            int[] data = mapSimpleImage(0, 0, TEX_WIDTH);

            v.map(data, tileW);
            return v;
        }

    }

    public static class townAbove extends CustomTilemap {

        {
            texture = Assets.Environment.HALL_POX;

            tileW = 13;
            tileH = 23;
        }

        final int TEX_WIDTH = 13*16;

        @Override
        public Tilemap create() {

            Tilemap v = super.create();

            int[] data = mapSimpleImage(0, 0, TEX_WIDTH);

            v.map(data, tileW);
            return v;
        }

    }

    public static class TorchA extends Emitter {

        private int pos;

        public TorchA( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x - 1, p.y - 20, 1, 0 );

            pour(FrostFlameParticle.FACTORY, 0.08f );

            add( new Halo( 10, Window.GDX_COLOR, 0.2f ).point( p.x, p.y - 20 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchB extends Emitter {

        private int pos;

        public TorchB( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x - 8, p.y - 16, 1, 0 );

            pour(FrostFlameParticle.FACTORY, 0.08f );

            add( new Halo( 10, Window.GDX_COLOR, 0.2f ).point( p.x-8, p.y - 16 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchC extends Emitter {

        private int pos;

        public TorchC( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x + 6, p.y - 16, 1, 0 );

            pour(FrostFlameParticle.FACTORY, 0.08f );

            add( new Halo( 10, Window.GDX_COLOR, 0.2f ).point( p.x+6, p.y - 16 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchD extends Emitter {

        private int pos;

        public TorchD( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x + 5.2f, p.y - 10, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );

            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x + 5.2f, p.y - 10 ) );

        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchE extends Emitter {

        private int pos;

        public TorchE( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x - 6.8f, p.y - 10, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );
            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x - 6.8f, p.y - 10 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchF extends Emitter {

        private int pos;

        public TorchF( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x + 11.5f, p.y - 7, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );
            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x + 11.5f, p.y - 7 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchG extends Emitter {

        private int pos;

        public TorchG( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x - 12.8f, p.y - 7, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );
            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x - 12.8f, p.y - 7 ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchH extends Emitter {

        private int pos;

        public TorchH( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x + 6, p.y, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );
            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x + 6, p.y ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    public static class TorchI extends Emitter {

        private int pos;

        public TorchI( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );

            pos( p.x - 7.5f, p.y, 0.3f, 0 );

            pour(FlameParticle.FACTORY, 0.1f );
            add( new Halo( 3, 0xFFa500, 0.4f ).point( p.x - 7.5f, p.y ) );
        }

        @Override
        public void update() {
            if (visible == (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }

    @Override
    public String tileName( int tile ) {
        //注：本 fork 无魔绫的 SIGN/SIGN_SP 地形，墙上记号映射为 CUSTOM_DECO（故两个 case 合并）
        if (tile == CUSTOM_DECO) {
            return Messages.get(LastLevel.class, "candle_name");
        }
        return super.tileName( tile );
    }

    @Override
    public String tileDesc(int tile) {
        if (tile == CUSTOM_DECO) {
            return Messages.get(LastLevel.class, "candle_desc");
        }
        return super.tileDesc( tile );
    }

}
