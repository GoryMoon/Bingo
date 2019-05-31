package se.gorymoon.bingo.world.gen.feature;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import se.gorymoon.bingo.blocks.ModBlocks;

import java.util.Random;

public class SpawnStructurePieces {


    public static class Piece extends StructurePiece {
        public Piece() {}

        public Piece(BlockPos pos) {
            this.setCoordBaseMode(EnumFacing.SOUTH);
            this.boundingBox = new MutableBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 14, pos.getY() + 2, pos.getZ() + 14);
        }

        @Override
        protected void writeAdditional(NBTTagCompound tagCompound) {

        }

        @Override
        protected void readAdditional(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {

        }

        @Override
        public boolean addComponentParts(IWorld worldIn, Random randomIn, MutableBoundingBox structureBoundingBoxIn, ChunkPos p_74875_4_) {
            final IBlockState woodState = Blocks.SPRUCE_LOG.getDefaultState();
            final IBlockState quartzState = Blocks.QUARTZ_BLOCK.getDefaultState();
            final IBlockState glowstoneState = Blocks.GLOWSTONE.getDefaultState();

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 14, 0, 14, woodState, woodState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 13, 0, 13, quartzState, quartzState,  false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 6, 8, 0, 8, glowstoneState, glowstoneState,  false);
            setBlockState(worldIn, Blocks.QUARTZ_PILLAR.getDefaultState(), 7, 1, 7, structureBoundingBoxIn);
            setBlockState(worldIn, ModBlocks.BINGO_TROPHY.orElse(null).getDefaultState(), 7, 2, 7, structureBoundingBoxIn);
            setBlockState(worldIn, Blocks.EMERALD_BLOCK.getDefaultState(), 7, 0, 3, structureBoundingBoxIn);
            setBlockState(worldIn, glowstoneState, 1, 0, 1, structureBoundingBoxIn);
            setBlockState(worldIn, glowstoneState, 1, 0, 13, structureBoundingBoxIn);
            setBlockState(worldIn, glowstoneState, 13, 0, 1, structureBoundingBoxIn);
            setBlockState(worldIn, glowstoneState, 13, 0, 13, structureBoundingBoxIn);
            return true;
        }
    }

}
