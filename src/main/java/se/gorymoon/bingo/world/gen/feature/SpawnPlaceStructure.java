package se.gorymoon.bingo.world.gen.feature;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.IChunkGenSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureIO;
import net.minecraft.world.gen.feature.structure.StructureStart;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.world.gen.BingoChunkGenerator;

import java.util.Random;

public class SpawnPlaceStructure extends Structure<NoFeatureConfig> {

    //TODO 1.14 add registry
    public static void registerStructure() {
        StructureIO.registerStructure(SpawnPlaceStructure.Start.class, "Bingo_Spawn");
    }

    //TODO 1.14 add registry
    public static void registerStructurePieces() {
        StructureIO.registerStructureComponent(SpawnStructurePieces.Piece.class, "BiSp");
    }

    @Override
    public boolean place(IWorld worldIn, IChunkGenerator<? extends IChunkGenSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (!this.isEnabledIn(worldIn)) {
            return false;
        } else {
            int i = this.getSize();
            int j = pos.getX() >> 4;
            int k = pos.getZ() >> 4;
            int l = j << 4;
            int i1 = k << 4;
            long j1 = ChunkPos.asLong(j, k);
            boolean flag = false;

            for(int k1 = j - i; k1 <= j + i; ++k1) {
                for(int l1 = k - i; l1 <= k + i; ++l1) {
                    long i2 = ChunkPos.asLong(k1, l1);
                    StructureStart structurestart = this.getStructureStart(worldIn, generator, (SharedSeedRandom)rand, i2);
                    if (structurestart != NO_STRUCTURE && structurestart.getBoundingBox().intersectsWith(l, i1, l + 15, i1 + 15)) {
                        generator.getStructurePositionToReferenceMap(this).computeIfAbsent(j1, (p_208203_0_) -> new LongOpenHashSet()).add(i2);
                        worldIn.getChunkProvider().getChunkOrPrimer(j, k, true).addStructureReference(this.getStructureName(), i2);
                        structurestart.generateStructure(worldIn, rand, new MutableBoundingBox(l, i1, l + 15, i1 + 15), new ChunkPos(j, k));
                        structurestart.notifyPostProcessAt(new ChunkPos(j, k));
                        flag = true;
                    }
                }
            }

            return flag;
        }
    }

    private StructureStart getStructureStart(IWorld worldIn, IChunkGenerator<? extends IChunkGenSettings> generator, SharedSeedRandom rand, long packedChunkPos) {
        if (!generator.hasStructure(null,this)) {
            return NO_STRUCTURE;
        } else {
            Long2ObjectMap<StructureStart> long2objectmap = generator.getStructureReferenceToStartMap(this);
            StructureStart structurestart = long2objectmap.get(packedChunkPos);
            if (structurestart != null) {
                return structurestart;
            } else {
                ChunkPos chunkpos = new ChunkPos(packedChunkPos);
                IChunk ichunk = worldIn.getChunkProvider().getChunkOrPrimer(chunkpos.x, chunkpos.z, false);
                if (ichunk != null) {
                    structurestart = ichunk.getStructureStart(this.getStructureName());
                    if (structurestart != null) {
                        long2objectmap.put(packedChunkPos, structurestart);
                        return structurestart;
                    }
                }

                if (this.hasStartAt(generator, rand, chunkpos.x, chunkpos.z)) {
                    StructureStart structurestart1 = this.makeStart(worldIn, generator, rand, chunkpos.x, chunkpos.z);
                    structurestart = structurestart1.isValid() ? structurestart1 : NO_STRUCTURE;
                } else {
                    structurestart = NO_STRUCTURE;
                }

                if (structurestart.isValid()) {
                    worldIn.getChunkProvider().getChunkOrPrimer(chunkpos.x, chunkpos.z, true).putStructureStart(this.getStructureName(), structurestart);
                }

                long2objectmap.put(packedChunkPos, structurestart);
                return structurestart;
            }
        }
    }

    @Override
    protected boolean hasStartAt(IChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ) {
        return chunkGen instanceof BingoChunkGenerator && chunkPosX == 0 && chunkPosZ == 0;
    }

    @Override
    protected boolean isEnabledIn(IWorld worldIn) {
        return worldIn.getWorld().getWorldType() == Bingo.BINGO_WORLD;
    }

    @Override
    protected StructureStart makeStart(IWorld worldIn, IChunkGenerator<?> generator, SharedSeedRandom random, int x, int z) {
        Biome biome = generator.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9), null);
        return new Start(worldIn, generator, random, x, worldIn.getChunkProvider().getChunkGenerator().getGroundHeight(), z, biome);
    }

    @Override
    protected String getStructureName() {
        return "Bingo_Spawn";
    }

    @Override
    public int getSize() {
        return 2;
    }

    public static class Start extends StructureStart {
        public Start() {
        }

        public Start(IWorld world, IChunkGenerator<?> generator, SharedSeedRandom random, int x, int y, int z, Biome biome) {
            super(x, z, biome, random, world.getSeed());
            int i = x * 16;
            int j = z * 16;
            BlockPos blockpos = new BlockPos(i, y, j);
            this.components.add(new SpawnStructurePieces.Piece(blockpos));
            this.recalculateStructureSize(world);
        }

        public BlockPos getPos() {
            return new BlockPos((this.chunkPosX << 4) + 9, 0, (this.chunkPosZ << 4) + 9);
        }
    }
}
