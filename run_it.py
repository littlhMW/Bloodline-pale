import os

MOD_ID = "bloodline"
PACKAGE = "com.littlh.bloodline"
PACKAGE_PATH = PACKAGE.replace(".", "/")
SRC_JAVA = f"src/main/java/{PACKAGE_PATH}"

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ 写入: {path}")

# ========== 完整重写 BloodlineBlocks.java ==========
blocks_class = f'''package {PACKAGE};

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BloodlineBlocks {{
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks("{MOD_ID}");

    // 苍白草方块（继承 GrassBlock，拥有蔓延和枯萎能力）
    public static final DeferredBlock<PaleGrassBlock> PALE_GRASS_BLOCK = BLOCKS.register(
            "pale_grass_block",
            PaleGrassBlock::new
    );

    // 苍白小麦（两格高植物，仅下半选中，可被替换）
    public static final DeferredBlock<PaleWheatBlock> PALE_WHEAT = BLOCKS.register(
            "pale_wheat",
            PaleWheatBlock::new
    );

    // 苍白余烬（替代泥土）
    public static final DeferredBlock<Block> PALE_EMBER = BLOCKS.registerSimpleBlock(
            "pale_ember",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.GRAVEL)
    );

    // 纯白骨块（替代石头）
    public static final DeferredBlock<Block> PALE_BONE_BLOCK = BLOCKS.registerSimpleBlock(
            "pale_bone_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()
    );

    // 苍白原木
    public static final DeferredBlock<BoneLogBlock> BONE_LOG = BLOCKS.register(
            "bone_log",
            BoneLogBlock::new
    );

    // 苍白树叶
    public static final DeferredBlock<PaleLeavesBlock> PALE_LEAVES = BLOCKS.register(
            "pale_leaves",
            PaleLeavesBlock::new
    );
}}
'''
write_file(f"{SRC_JAVA}/BloodlineBlocks.java", blocks_class)

# ========== 确保 PaleGrassBlock.java 存在 ==========
pale_grass_class = f'''package {PACKAGE};

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class PaleGrassBlock extends GrassBlock {{

    public PaleGrassBlock() {{
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .randomTicks()
                .strength(0.6F)
                .sound(SoundType.GRASS)
        );
    }}

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {{
        if (!canBeGrass(state, level, pos)) {{
            level.setBlockAndUpdate(pos, BloodlineBlocks.PALE_EMBER.get().defaultBlockState());
        }} else {{
            for (int i = 0; i < 4; ++i) {{
                BlockPos targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(BloodlineBlocks.PALE_EMBER.get()) && canPropagate(state, level, targetPos)) {{
                    level.setBlockAndUpdate(targetPos, this.defaultBlockState());
                }}
            }}
        }}
    }}

    private static boolean canPropagate(BlockState state, ServerLevel level, BlockPos pos) {{
        BlockPos posAbove = pos.above();
        return level.getRawBrightness(posAbove, 0) >= 9;
    }}

    private static boolean canBeGrass(BlockState state, ServerLevel level, BlockPos pos) {{
        BlockPos posAbove = pos.above();
        BlockState blockAbove = level.getBlockState(posAbove);
        return !blockAbove.isSolidRender(level, posAbove) && !blockAbove.is(Blocks.WATER);
    }}
}}
'''
write_file(f"{SRC_JAVA}/PaleGrassBlock.java", pale_grass_class)

# ========== 确保 PaleWheatBlock.java 包含 replaceable ==========
pale_wheat_class = f'''package {PACKAGE};

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaleWheatBlock extends DoublePlantBlock {{

    private static final VoxelShape LOWER_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public PaleWheatBlock() {{
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .replaceable()
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
                .ignitedByLava()
                .noOcclusion()
        );
    }}

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {{
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }}

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {{
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }}

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {{
        return Shapes.empty();
    }}

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {{
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {{
            return true;
        }}
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }}

    @Override
    public boolean mayPlaceOn(BlockState groundState, BlockGetter level, BlockPos pos) {{
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }}
}}
'''
write_file(f"{SRC_JAVA}/PaleWheatBlock.java", pale_wheat_class)

print("\n🎉 BloodlineBlocks.java 已完全重写！")
print("📌 苍白草方块注册已修正为显式 register 方式")
print("📌 PaleGrassBlock 和 PaleWheatBlock 均已确保存在且正确")
print("📌 请运行 ./gradlew build 重新构建模组")