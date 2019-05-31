package se.gorymoon.bingo.world.gen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gorymoon.bingo.world.gen.feature.SpawnPlaceStructure;

public class BingoChunkGenerator extends ChunkGeneratorOverworld {

    private static final SpawnPlaceStructure SPAWN_STRUCTURE = new SpawnPlaceStructure();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int START_AREA_RADIUS = 7;
    private static final int START_AREA_BORDER = (START_AREA_RADIUS + 1) * 16;

    public BingoChunkGenerator(IWorld worldIn, BiomeProvider provider, OverworldGenSettings settingsIn) {
        super(worldIn, provider, settingsIn);
        LOGGER.info("Setting up chunk generator");
    }

    @Override
    public void makeBase(IChunk chunkIn) {
        ChunkPos chunkpos = chunkIn.getPos();
        int i = chunkpos.x;
        int j = chunkpos.z;

        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        sharedseedrandom.setBaseChunkSeed(i, j);
        Biome[] abiome = this.biomeProvider.getBiomeBlock(i * 16, j * 16, 16, 16);
        chunkIn.setBiomes(abiome);

        if (isOutside(i, j)) {
            //Normal
            this.setBlocksInChunk(i, j, chunkIn);
            chunkIn.createHeightMap(Heightmap.Type.WORLD_SURFACE_WG, Heightmap.Type.OCEAN_FLOOR_WG);
            this.buildSurface(chunkIn, abiome, sharedseedrandom, this.world.getSeaLevel());
            this.makeBedrock(chunkIn, sharedseedrandom);
            chunkIn.createHeightMap(Heightmap.Type.WORLD_SURFACE_WG, Heightmap.Type.OCEAN_FLOOR_WG);
            chunkIn.setStatus(ChunkStatus.BASE);
        } else {
            BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos();
            if (i == 0 && j == 0) {
                // Center
                int y = getGroundHeight();
                final IBlockState state = Blocks.BEDROCK.getDefaultState();
                for (int x = chunkpos.getXStart(); x < chunkpos.getXEnd(); x++) {
                    for (int z = chunkpos.getZStart(); z < chunkpos.getZEnd(); z++) {
                        mutableblockpos.setPos(x, y, z);
                        chunkIn.setBlockState(mutableblockpos, state, false);
                    }
                }
            } else {
                //Border
                final IBlockState state = Blocks.BARRIER.getDefaultState();
                for (int x = chunkpos.getXStart(); x <= chunkpos.getXEnd(); x++) {
                    for (int z = chunkpos.getZStart(); z <= chunkpos.getZEnd(); z++) {
                        if (x == START_AREA_BORDER -1 || x == -START_AREA_BORDER || z == START_AREA_BORDER -1 || z == -START_AREA_BORDER) {
                            for (int y = 0; y < 255; y++) {
                                mutableblockpos.setPos(x, y, z);
                                chunkIn.setBlockState(mutableblockpos, state, false);
                            }
                        }
                    }
                }

            }
            chunkIn.createHeightMap(Heightmap.Type.WORLD_SURFACE_WG, Heightmap.Type.OCEAN_FLOOR_WG);
            chunkIn.setStatus(ChunkStatus.BASE);
        }
    }

    @Override
    public void decorate(WorldGenRegion region) {
        int i = region.getMainChunkX();
        int j = region.getMainChunkZ();
        if (isOutside(i, j)) {
            super.decorate(region);
        } else if (Math.abs(i) <= START_AREA_RADIUS && Math.abs(j) <= START_AREA_RADIUS) {
            SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
            int k = i * 16;
            int l = j * 16;
            BlockPos blockpos = new BlockPos(k, getGroundHeight(), l);
            SPAWN_STRUCTURE.place(region, this, sharedseedrandom, blockpos, null);
        }
    }

    @Override
    public void carve(WorldGenRegion region, GenerationStage.Carving carvingStage) {
        int j = region.getMainChunkX();
        int k = region.getMainChunkZ();
        if (isOutside(j, k)) {
            super.carve(region, carvingStage);
        }
    }

    public boolean isOutside(int chunkX, int chunkZ) {
        return chunkX > START_AREA_RADIUS || chunkZ > START_AREA_RADIUS || chunkX < (-START_AREA_RADIUS) -1 || chunkZ < (-START_AREA_RADIUS) -1;
    }

    @Override
    public boolean hasStructure(Biome biomeIn, Structure<? extends IFeatureConfig> structureIn) {
        if (structureIn == SPAWN_STRUCTURE) return true;
        if (structureIn == Structure.MINESHAFT) return false;
        return super.hasStructure(biomeIn, structureIn);
    }

}
