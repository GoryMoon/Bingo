package se.gorymoon.bingo.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.OpenBingoGuiMessage;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class TrophyBlock extends Block {

    private static final VoxelShape AABB = Stream.of(
            Block.makeCuboidShape(4, 9, 5, 5, 16, 11),
            Block.makeCuboidShape(11, 9, 5, 12, 16, 11),
            Block.makeCuboidShape(4, 9, 4, 12, 16, 5),
            Block.makeCuboidShape(4, 9, 11, 12, 16, 12),
            Block.makeCuboidShape(5, 8, 5, 11, 9, 11),
            Block.makeCuboidShape(6, 7, 6, 10, 8, 10),
            Block.makeCuboidShape(6, 6, 6, 10, 7, 10),
            Block.makeCuboidShape(7, 4, 7, 9, 6, 9),
            Block.makeCuboidShape(6, 2, 6, 10, 4, 10),
            Block.makeCuboidShape(5, 1, 5, 11, 2, 11),
            Block.makeCuboidShape(4, 0, 4, 12, 1, 12),
            Block.makeCuboidShape(1, 9, 7, 4, 14, 9),
            Block.makeCuboidShape(12, 9, 7, 15, 14, 9)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();

    public TrophyBlock() {
        super(Properties.create(Material.IRON).hardnessAndResistance(-1.0F, 3600000.0F));
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote()) {
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) player), new OpenBingoGuiMessage());
        }
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new TrophyTileEntity();
    }
}
