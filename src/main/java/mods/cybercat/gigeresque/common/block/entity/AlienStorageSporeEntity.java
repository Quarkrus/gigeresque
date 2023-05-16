package mods.cybercat.gigeresque.common.block.entity;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import mods.cybercat.gigeresque.common.Gigeresque;
import mods.cybercat.gigeresque.common.block.GIgBlocks;
import mods.cybercat.gigeresque.common.block.storage.StorageProperties;
import mods.cybercat.gigeresque.common.block.storage.StorageStates;
import mods.cybercat.gigeresque.common.entity.Entities;
import mods.cybercat.gigeresque.common.status.effect.GigStatusEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class AlienStorageSporeEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity {

	private NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	public static final EnumProperty<StorageStates> CHEST_STATE = StorageProperties.STORAGE_STATE;
	private boolean check = true;
	protected final ContainerOpenersCounter stateManager = new ContainerOpenersCounter() {

		@Override
		protected void onOpen(Level world, BlockPos pos, BlockState state) {
			AlienStorageSporeEntity.this.level.playSound(null, pos, SoundEvents.ITEM_FRAME_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		@Override
		protected void onClose(Level world, BlockPos pos, BlockState state) {
			AlienStorageSporeEntity.this.level.playSound(null, pos, SoundEvents.ITEM_FRAME_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		@Override
		protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
			AlienStorageSporeEntity.this.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu menu)
				return menu.getContainer() == AlienStorageSporeEntity.this;
			return false;
		}
	};

	public AlienStorageSporeEntity(BlockPos pos, BlockState state) {
		super(Entities.ALIEN_STORAGE_BLOCK_ENTITY_1_SPORE, pos, state);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("spawnspore", checkSporestatus());
		return nbt;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		hasSpawnSpore(nbt.getBoolean("spawnspore"));
		if (!this.tryLoadLootTable(nbt))
			ContainerHelper.loadAllItems(nbt, this.items);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putBoolean("spawnspore", check);
		if (!this.trySaveLootTable(nbt))
			ContainerHelper.saveAllItems(nbt, this.items);
	}

	@Override
	public int getContainerSize() {
		return 36;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> list) {
		this.items = list;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.gigeresque.alien_storage_block1");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x4, syncId, inventory, this, 4);
	}

	@Override
	public void startOpen(Player player) {
		if (!this.isRemoved() && !player.isSpectator())
			this.stateManager.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	@Override
	public void stopOpen(Player player) {
		if (!this.isRemoved() && !player.isSpectator())
			this.stateManager.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	public void tick() {
		if (!this.isRemoved())
			this.stateManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	protected void onInvOpenOrClose(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
		world.blockEvent(pos, state.getBlock(), 1, newViewerCount);
		if (oldViewerCount != newViewerCount)
			if (newViewerCount > 0)
				world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.OPENED));
			else
				world.setBlockAndUpdate(pos, state.setValue(CHEST_STATE, StorageStates.CLOSING));
	}

	public StorageStates getChestState() {
		return this.getBlockState().getValue(AlienStorageSporeEntity.CHEST_STATE);
	}

	public void setChestState(StorageStates state) {
		this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(CHEST_STATE, state));
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, event -> {
			if (getChestState().equals(StorageStates.CLOSING) && !event.isCurrentAnimation(RawAnimation.begin().thenPlay("opening").thenPlayAndHold("opened")))
				return event.setAndContinue(RawAnimation.begin().thenPlay("closing").thenPlayAndHold("closed"));
			else if (getChestState().equals(StorageStates.OPENED) && !event.isCurrentAnimation(RawAnimation.begin().thenPlay("closing").thenPlayAndHold("closed")))
				return event.setAndContinue(RawAnimation.begin().thenPlay("opening").thenPlayAndHold("opened"));
			return event.setAndContinue(RawAnimation.begin().thenLoop("closed"));
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	public void hasSpawnSpore(boolean spawn) {
		check = spawn;
	}

	public boolean checkSporestatus() {
		return check;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AlienStorageSporeEntity blockEntity) {
		if (level != null) {
			if (!blockEntity.getLevel().isClientSide()) {
				BlockPos.betweenClosed(pos, pos.above(2)).forEach(testPos -> {
					if (!testPos.equals(pos) && !level.getBlockState(testPos).is(GIgBlocks.ALIEN_STORAGE_BLOCK_INVIS))
						level.setBlock(testPos, GIgBlocks.ALIEN_STORAGE_BLOCK_INVIS.defaultBlockState(), Block.UPDATE_ALL);
				});
				if (blockEntity.getChestState().equals(StorageStates.OPENED) && blockEntity.checkSporestatus() == true) {
					blockEntity.particleCloud();
					blockEntity.hasSpawnSpore(false);
					blockEntity.getLevel().playSound(null, pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
				}
			}
			if (!blockEntity.isRemoved())
				blockEntity.stateManager.recheckOpeners(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
		}
	}

	public void particleCloud() {
		var areaEffectCloudEntity = new AreaEffectCloud(this.level, worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ());
		areaEffectCloudEntity.setRadius(2.0F);
		areaEffectCloudEntity.setDuration(3);
		areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		areaEffectCloudEntity.addEffect(new MobEffectInstance(GigStatusEffects.SPORE, Gigeresque.config.sporeTickTimer, 0));
		this.level.addFreshEntity(areaEffectCloudEntity);
	}
}
