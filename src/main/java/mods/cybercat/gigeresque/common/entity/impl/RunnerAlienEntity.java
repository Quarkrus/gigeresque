package mods.cybercat.gigeresque.common.entity.impl;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import mods.cybercat.gigeresque.common.Gigeresque;
import mods.cybercat.gigeresque.common.entity.AlienEntity;
import mods.cybercat.gigeresque.common.entity.ai.enums.AlienAttackType;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyLightsBlocksSensor;
import mods.cybercat.gigeresque.common.entity.ai.sensors.NearbyRepellentsSensor;
import mods.cybercat.gigeresque.common.entity.ai.tasks.AlienMeleeAttack;
import mods.cybercat.gigeresque.common.entity.ai.tasks.BuildNestTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.FleeFireTask;
import mods.cybercat.gigeresque.common.entity.ai.tasks.KillLightsTask;
import mods.cybercat.gigeresque.common.entity.attribute.AlienEntityAttributes;
import mods.cybercat.gigeresque.common.entity.helper.GigAnimationsDefault;
import mods.cybercat.gigeresque.common.sound.GigSounds;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.common.util.GigEntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

public class RunnerAlienEntity extends AdultAlienEntity implements SmartBrainOwner<RunnerAlienEntity> {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

	public RunnerAlienEntity(EntityType<? extends AlienEntity> type, Level world) {
		super(type, world);
	}

	@Override
	public void travel(Vec3 movementInput) {
		this.navigation = (this.isUnderWater() || (this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8)) ? swimNavigation : landNavigation;
		this.moveControl = (this.wasEyeInWater || (this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8)) ? swimMoveControl : landMoveControl;
		this.lookControl = (this.wasEyeInWater || (this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8)) ? swimLookControl : landLookControl;

		if (isEffectiveAi() && (this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8)) {
			moveRelative(getSpeed(), movementInput);
			move(MoverType.SELF, getDeltaMovement());
			setDeltaMovement(getDeltaMovement().scale(0.5));
			if (getTarget() == null)
				setDeltaMovement(getDeltaMovement().add(0.0, -0.005, 0.0));
		} else
			super.travel(movementInput);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, Gigeresque.config.runnerXenoHealth).add(Attributes.ARMOR, Gigeresque.config.runnerXenoArmor).add(Attributes.ARMOR_TOUGHNESS, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 7.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.13000000417232513).add(Attributes.ATTACK_DAMAGE, Gigeresque.config.runnerXenoAttackDamage).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(AlienEntityAttributes.INTELLIGENCE_ATTRIBUTE,
				0.5);
	}

	@Override
	public void tick() {
		super.tick();

		// Attack logic

		if (attackProgress > 0) {
			attackProgress--;

			if (!getLevel().isClientSide && attackProgress <= 0)
				setCurrentAttackType(AlienAttackType.NONE);
		}

		if (attackProgress == 0 && swinging)
			attackProgress = 10;

		if (!getLevel().isClientSide && getCurrentAttackType() == AlienAttackType.NONE)
			setCurrentAttackType(switch (random.nextInt(5)) {
			case 0 -> AlienAttackType.CLAW_LEFT_MOVING;
			case 1 -> AlienAttackType.CLAW_RIGHT_MOVING;
			case 2 -> AlienAttackType.TAIL_LEFT;
			case 3 -> AlienAttackType.TAIL_RIGHT;
			default -> AlienAttackType.CLAW_LEFT_MOVING;
			});
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		var additionalDamage = switch (getCurrentAttackType().genericAttackType) {
		case TAIL -> Gigeresque.config.runnerXenoTailAttackDamage;
		case EXECUTION -> Float.MAX_VALUE;
		default -> 0.0f;
		};

		if (target instanceof LivingEntity && !getLevel().isClientSide)
			switch (getAttckingState()) {
			case 1 -> {
				if (target instanceof Player playerEntity && this.random.nextInt(7) == 0) {
					playerEntity.drop(playerEntity.getInventory().getSelected(), true, false);
					playerEntity.getInventory().removeItem(playerEntity.getInventory().getSelected());
				}
				target.hurt(damageSources().mobAttack(this), additionalDamage);
				return super.doHurtTarget(target);
			}
			case 2 -> {
				if (target instanceof Player playerEntity && this.random.nextInt(7) == 0) {
					playerEntity.drop(playerEntity.getInventory().getSelected(), true, false);
					playerEntity.getInventory().removeItem(playerEntity.getInventory().getSelected());
				}
				target.hurt(damageSources().mobAttack(this), additionalDamage);
				return super.doHurtTarget(target);
			}
			case 3 -> {
				var armorItems = StreamSupport.stream(target.getArmorSlots().spliterator(), false).collect(Collectors.toList());
				if (!armorItems.isEmpty())
					armorItems.get(new Random().nextInt(armorItems.size())).hurtAndBreak(10, this, it -> {
					});
				target.hurt(damageSources().mobAttack(this), additionalDamage);
				return super.doHurtTarget(target);
			}
			case 4 -> {
				var armorItems = StreamSupport.stream(target.getArmorSlots().spliterator(), false).collect(Collectors.toList());
				if (!armorItems.isEmpty())
					armorItems.get(new Random().nextInt(armorItems.size())).hurtAndBreak(10, this, it -> {
					});
				target.hurt(damageSources().mobAttack(this), additionalDamage);
				return super.doHurtTarget(target);
			}
//			case 5 -> {
//				var health = ((LivingEntity) target).getHealth();
//				var maxhealth = ((LivingEntity) target).getMaxHealth();
//				if (health >= (maxhealth * 0.10)) {
//					target.hurt(DamageSource.mobAttack(this), Float.MAX_VALUE);
//					this.grabTarget(target);
//				}
//				return super.doHurtTarget(target);
//			}
			}
		this.heal(1.0833f);
		return super.doHurtTarget(target);
	}

	@Override
	public float getGrowthMultiplier() {
		return Gigeresque.config.runnerAlienGrowthMultiplier;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData entityData, CompoundTag entityNbt) {
		if (spawnReason != MobSpawnType.NATURAL)
			setGrowth(getMaxGrowth());
		return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
	}

	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	protected void customServerAiStep() {
		tickBrain(this);
		super.customServerAiStep();
	}

	@Override
	public List<ExtendedSensor<RunnerAlienEntity>> getSensors() {
		return ObjectArrayList.of(new NearbyPlayersSensor<>(),
				new NearbyLivingEntitySensor<RunnerAlienEntity>().setPredicate((target, self) -> GigEntityUtils.entityTest(target, self)),
				new NearbyBlocksSensor<RunnerAlienEntity>().setRadius(7), new NearbyRepellentsSensor<RunnerAlienEntity>().setRadius(15).setPredicate((block, entity) -> block.is(GigTags.ALIEN_REPELLENTS) || block.is(Blocks.LAVA)), new NearbyLightsBlocksSensor<RunnerAlienEntity>().setRadius(7).setPredicate((block, entity) -> block.is(GigTags.DESTRUCTIBLE_LIGHT)), new HurtBySensor<>(), new UnreachableTargetSensor<>(), new HurtBySensor<>());
	}

	@Override
	public BrainActivityGroup<RunnerAlienEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				// Looks at target
				new LookAtTarget<>().stopIf(entity -> this.isPassedOut()).startCondition(entity -> !this.isPassedOut() || !this.isSearching),
				// Flee Fire
				new FleeFireTask<>(3.5F),
				// Move to target
				new MoveToWalkTarget<>().startCondition(entity -> !this.isPassedOut()).stopIf(entity -> this.isPassedOut()));
	}

	@Override
	public BrainActivityGroup<RunnerAlienEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				// Build Nest
				new BuildNestTask(90).stopIf(target -> (this.isAggressive() || this.isVehicle() || this.isPassedOut() || this.entityData.get(FLEEING_FIRE).booleanValue() == true)), 
				// Kill Lights
				new KillLightsTask<>().stopIf(target -> (this.isAggressive() || this.isVehicle())),
				// Do first
				new FirstApplicableBehaviour<RunnerAlienEntity>(
						// Targeting
						new TargetOrRetaliate<>(), 
						// Look at players
						new SetPlayerLookTarget<>().predicate(target -> target.isAlive() && (!target.isCreative() || !target.isSpectator())), 
						// Look around randomly
						new SetRandomLookTarget<>()), 
				// Random
				new OneRandomBehaviour<>(
						// Randomly walk around
						new SetRandomWalkTarget<>().speedModifier(1.05f).startCondition(entity -> !this.isPassedOut()).stopIf(entity -> this.isPassedOut()),
						// Idle
						new Idle<>().startCondition(entity -> !this.isAggressive()).runFor(entity -> entity.getRandom().nextInt(30, 60))));
	}

	@Override
	public BrainActivityGroup<RunnerAlienEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				new InvalidateAttackTarget<>().invalidateIf((entity, target) -> GigEntityUtils.removeTarget(target, this)), 
				new SetWalkTargetToAttackTarget<>().speedMod(Gigeresque.config.runnerXenoAttackSpeed).stopIf(entity -> this.isPassedOut()), 
				new AlienMeleeAttack(10));
	}
	@Override
	public void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, Entity var5, Entity var6, float var7) {
		if (this.isDeadOrDying())
			return;
		if (this.isVehicle())
			return;
		if (this.isAggressive())
			return;
		if (!(var6 instanceof IronGolem))
			BrainUtils.setMemory(this, MemoryModuleType.WALK_TARGET, new WalkTarget(var3, 1.2F, 0));
	}

	/*
	 * ANIMATIONS
	 */
	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 5, event -> {
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
			if (isDead)
				return event.setAndContinue(GigAnimationsDefault.DEATH);
			if (event.isMoving() && !this.isCrawling() && this.isExecuting() == false && !isDead && this.isPassedOut() == false && !this.swinging)
				if (!(this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8) && this.isExecuting() == false)
					if (walkAnimation.speedOld > 0.35F && this.getFirstPassenger() == null)
						return event.setAndContinue(GigAnimationsDefault.RUN);
					else if (this.isExecuting() == false && walkAnimation.speedOld < 0.35F || (!this.isCrawling() && !this.isOnGround()))
						return event.setAndContinue(GigAnimationsDefault.WALK);
					else if ((this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8) && this.isExecuting() == false && !this.isVehicle())
						if (this.isAggressive() && !this.isVehicle())
							return event.setAndContinue(GigAnimationsDefault.RUSH_SWIM);
						else
							return event.setAndContinue(GigAnimationsDefault.SWIM);
			return event.setAndContinue(this.isNoAi() ? GigAnimationsDefault.STATIS_ENTER : (this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8) ? GigAnimationsDefault.IDLE_WATER : GigAnimationsDefault.IDLE_LAND);
		}).triggerableAnim("death", GigAnimationsDefault.DEATH) // death
				.triggerableAnim("idle", GigAnimationsDefault.IDLE_LAND) // idle
				.setSoundKeyframeHandler(event -> {
					if (event.getKeyframeData().getSound().matches("footstepSoundkey"))
						if (this.getLevel().isClientSide)
							this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_FOOTSTEP, SoundSource.HOSTILE, 0.5F, 1.0F, true);
					if (event.getKeyframeData().getSound().matches("idleSoundkey"))
						if (this.getLevel().isClientSide)
							this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.0F, true);
				})).add(new AnimationController<>(this, "attackController", 1, event -> {
					if (event.getAnimatable().isPassedOut())
						return event.setAndContinue(RawAnimation.begin().thenLoop("stasis_loop"));
					return PlayState.STOP;
				}).triggerableAnim("alert", GigAnimationsDefault.AMBIENT) // reset hands
						.triggerableAnim("death", GigAnimationsDefault.DEATH) // death
						.triggerableAnim("alert", GigAnimationsDefault.HISS) // reset hands
						.triggerableAnim("passout", GigAnimationsDefault.STATIS_ENTER) // pass out
						.triggerableAnim("passoutloop", GigAnimationsDefault.STATIS_LOOP) // pass out
						.triggerableAnim("wakeup", GigAnimationsDefault.STATIS_LEAVE.then("idle_land", LoopType.PLAY_ONCE)) // wake up
						.triggerableAnim("swipe", GigAnimationsDefault.LEFT_CLAW) // swipe
						.triggerableAnim("left_claw", GigAnimationsDefault.LEFT_CLAW) // attack
						.triggerableAnim("right_claw", GigAnimationsDefault.RIGHT_CLAW) // attack
						.triggerableAnim("left_tail_basic", GigAnimationsDefault.LEFT_TAIL_BASIC) // attack
						.triggerableAnim("right_tail_basic", GigAnimationsDefault.RIGHT_TAIL_BASIC) // attack
						.setSoundKeyframeHandler(event -> {
							if (event.getKeyframeData().getSound().matches("clawSoundkey"))
								if (this.getLevel().isClientSide)
									this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_CLAW, SoundSource.HOSTILE, 0.25F, 1.0F, true);
							if (event.getKeyframeData().getSound().matches("tailSoundkey"))
								if (this.getLevel().isClientSide)
									this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_TAIL, SoundSource.HOSTILE, 0.25F, 1.0F, true);
						}))
				.add(new AnimationController<>(this, "hissController", 0, event -> {
					var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
					if (this.entityData.get(IS_HISSING) == true && !this.isVehicle() && this.isExecuting() == false && !isDead && !(this.getLevel().getFluidState(this.blockPosition()).is(Fluids.WATER) && this.getLevel().getFluidState(this.blockPosition()).getAmount() >= 8))
						return event.setAndContinue(GigAnimationsDefault.HISS);
					return PlayState.STOP;
				}).setSoundKeyframeHandler(event -> {
					if (event.getKeyframeData().getSound().matches("hissSoundkey"))
						if (this.getLevel().isClientSide)
							this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), GigSounds.ALIEN_HISS, SoundSource.HOSTILE, 1.0F, 1.0F, true);
				}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}
}
