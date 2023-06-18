package mods.cybercat.gigeresque.common.fluid;

import mods.cybercat.gigeresque.common.block.GIgBlocks;
import mods.cybercat.gigeresque.common.item.GigItems;
import mods.cybercat.gigeresque.common.tags.GigTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class BlackFluid extends FlowingFluid {
	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == GigFluids.BLACK_FLUID_STILL || fluid == GigFluids.BLACK_FLUID_FLOWING;
	}

	@Override
	public Item getBucket() {
		return GigItems.BLACK_FLUID_BUCKET;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	public int getTickDelay(LevelReader world) {
		return 20;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0f;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return GIgBlocks.BLACK_FLUID.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(state));
	}

	@Override
	public Fluid getFlowing() {
		return GigFluids.BLACK_FLUID_FLOWING;
	}

	@Override
	public Fluid getSource() {
		return GigFluids.BLACK_FLUID_STILL;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropResources(state, world, pos, blockEntity);
	}

	@Override
	protected int getSlopeFindDistance(LevelReader world) {
		return 2;
	}

	@Override
	protected int getDropOff(LevelReader world) {
		return 2;
	}

	@Override
	protected void randomTick(Level level, BlockPos blockPos, FluidState fluidState, RandomSource randomSource) {
		int i = randomSource.nextInt(50);
		if (i > 40) 
			for (var j = 0; j < 10; ++j) {
				if (!level.isLoaded(blockPos = blockPos.offset(randomSource.nextInt(10) - 1, 1, randomSource.nextInt(10) - 1)))
					return;
				if (this.isSporeReplaceable(level, blockPos)) {
					if (!this.hasSporeReplacements(level, blockPos))
						continue;
					if (!level.getBlockState(blockPos).is(GIgBlocks.BLACK_FLUID))
						level.setBlockAndUpdate(blockPos, GIgBlocks.SPORE_BLOCK.defaultBlockState());
					return;
				}
				if (!level.getBlockState(blockPos).getMaterial().blocksMotion())
					continue;
				return;
			}
	}

	private boolean hasSporeReplacements(LevelReader levelReader, BlockPos blockPos) {
		for (var direction : Direction.values()) {
			if (!this.isSporeReplaceable(levelReader, blockPos.relative(direction)))
				continue;
			return true;
		}
		return false;
	}

	private boolean isSporeReplaceable(LevelReader levelReader, BlockPos blockPos) {
		if (blockPos.getY() >= levelReader.getMinBuildHeight() && blockPos.getY() < levelReader.getMaxBuildHeight() && !levelReader.hasChunkAt(blockPos))
			return false;
		return levelReader.getBlockState(blockPos).is(GigTags.SPORE_REPLACE);
	}

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

	static class Flowing extends BlackFluid {
		@Override
		public void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState fluidState) {
			return fluidState.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return false;
		}

		@Override
		protected boolean canConvertToSource(Level var1) {
			return false;
		}
	}

	static class Still extends BlackFluid {

		@Override
		public int getAmount(FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState fluidState) {
			return true;
		}

		@Override
		protected boolean canConvertToSource(Level var1) {
			return false;
		}
	}
}
