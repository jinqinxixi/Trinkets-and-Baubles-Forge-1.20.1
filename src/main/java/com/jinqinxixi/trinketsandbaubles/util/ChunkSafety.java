package com.jinqinxixi.trinketsandbaubles.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

/**
 * 扫描方块时的 chunk 安全读取。
 *
 * <p>直接用 {@code Level.getBlockState(pos)} 在坐标所在 chunk 未加载时会触发同步加载，扫描大片区域
 * 时可能造成卡顿。这里用 {@code getChunkNow}（chunk 未加载返回 null，不触发加载）判断，未加载的
 * chunk 跳过，避免探测让玩家周围一堆 chunk 被强制加载。
 */
public final class ChunkSafety
{
    @Nullable
    public static LevelChunk chunkAt(Level level, int chunkX, int chunkZ)
    {
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }

    public static boolean isLoaded(Level level, BlockPos pos)
    {
        return chunkAt(level, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())) != null;
    }

    private ChunkSafety()
    {
    }

    /**
     * 缓存当前 chunk 的方块读取器：同一 chunk 内的连续读不重复查 chunk，chunk 未加载时返回 null。
     */
    public static final class ChunkCache
    {
        private final Level level;
        private int cachedX = Integer.MIN_VALUE;
        private int cachedZ = Integer.MIN_VALUE;
        @Nullable
        private LevelChunk cached;

        public ChunkCache(Level level)
        {
            this.level = level;
        }

        @Nullable
        public BlockState getBlockState(BlockPos pos)
        {
            int cx = SectionPos.blockToSectionCoord(pos.getX());
            int cz = SectionPos.blockToSectionCoord(pos.getZ());
            if (cx != this.cachedX || cz != this.cachedZ)
            {
                this.cached = ChunkSafety.chunkAt(this.level, cx, cz);
                this.cachedX = cx;
                this.cachedZ = cz;
            }

            return this.cached == null ? null : this.cached.getBlockState(pos);
        }
    }
}