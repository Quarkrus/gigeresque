package mods.cybercat.gigeresque.common.entity.ai.tasks;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mods.cybercat.gigeresque.common.block.GIgBlocks;
import mods.cybercat.gigeresque.common.entity.AlienEntity;
import mods.cybercat.gigeresque.common.entity.impl.neo.NeomorphEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.util.BrainUtils;

public class AlienMeleeAttack<E extends AlienEntity> extends CustomDelayedMeleeBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));

	protected Function<E, Integer> attackIntervalSupplier = entity -> 20;

	@Nullable
	protected LivingEntity target = null;

	public AlienMeleeAttack(int delayTicks) {
		super(delayTicks);
	}

	/**
	 * Set the time between attacks.
	 * 
	 * @param supplier The tick value provider
	 * @return this
	 */
	public AlienMeleeAttack<E> attackInterval(Function<E, Integer> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.target = BrainUtils.getTargetOfEntity(entity);

		return entity.getSensing().hasLineOfSight(this.target) && entity.isWithinMeleeAttackRange(this.target) && !this.target.getLevel().getBlockStates(this.target.getBoundingBox().inflate(1)).anyMatch(state -> state.is(GIgBlocks.NEST_RESIN_WEB_CROSS));
	}

	@Override
	protected void start(E entity) {
		entity.swing(InteractionHand.MAIN_HAND);
		BehaviorUtils.lookAtEntity(entity, this.target);
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}

	@Override
	protected void doDelayedAction(E entity) {
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));

		if (this.target == null)
			return;

		if (!entity.getSensing().hasLineOfSight(this.target) || !entity.isWithinMeleeAttackRange(this.target))
			return;

		if (this.target.getLevel().getBlockStates(this.target.getBoundingBox().inflate(1)).anyMatch(state -> state.is(GIgBlocks.NEST_RESIN_WEB_CROSS)))
			return;
		
		if (entity instanceof NeomorphEntity neo) {
			entity.getNavigation().stop();
			neo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 100, false, false));
		}

		entity.doHurtTarget(this.target);
	}
}