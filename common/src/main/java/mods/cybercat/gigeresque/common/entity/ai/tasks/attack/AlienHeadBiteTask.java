package mods.cybercat.gigeresque.common.entity.ai.tasks.attack;

import com.mojang.datafixers.util.Pair;
import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.sblforked.api.core.behaviour.DelayedBehaviour;
import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.client.particle.GigParticles;
import mods.cybercat.gigeresque.common.source.GigDamageSources;
import mods.cybercat.gigeresque.interfacing.AbstractAlien;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;

public class AlienHeadBiteTask<E extends PathfinderMob & AbstractAlien & GeoEntity> extends DelayedBehaviour<E> {

    private long lastUpdateTime = 0L;

    public AlienHeadBiteTask(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return entity.isVehicle();
    }

    @Override
    protected void doDelayedAction(E entity) {
        entity.setDeltaMovement(0, 0, 0);
        if (entity.getFirstPassenger() != null) {
            var yOffset = entity.getEyeY() - ((entity.getFirstPassenger().getEyeY() - entity.getFirstPassenger().blockPosition().getY()) / 2.0);
            var e = entity.getFirstPassenger().getX() + ((entity.getRandom().nextDouble() / 2.0) - 0.5) * (entity.getRandom().nextBoolean() ? -1 : 1);
            var f = entity.getFirstPassenger().getZ() + ((entity.getRandom().nextDouble() / 2.0) - 0.5) * (entity.getRandom().nextBoolean() ? -1 : 1);
            if (!entity.isExecuting()) entity.triggerAnim(Constants.ATTACK_CONTROLLER, "kidnap");
            if (entity.getFirstPassenger() instanceof Mob mob && !mob.isPersistenceRequired())
                mob.setPersistenceRequired();
            // Get the current time in milliseconds
            var currentTime = System.currentTimeMillis();
            if (entity.isBiting() && entity.getFirstPassenger() != null) {
                // Check if enough time has elapsed since the last update
                if (currentTime - lastUpdateTime >= 4400L) {
                    lastUpdateTime = currentTime;
                    entity.getFirstPassenger().hurt(GigDamageSources.of(entity.level(), GigDamageSources.EXECUTION),
                            Integer.MAX_VALUE);
                    entity.heal(50);
                    if (entity.level().isClientSide)
                        entity.getFirstPassenger().level().addAlwaysVisibleParticle(GigParticles.BLOOD.get(), e, yOffset, f, 0.0,
                                -0.15, 0.0);
                    entity.setIsBiting(false);
                    entity.setIsExecuting(false);
                    entity.triggerAnim(Constants.ATTACK_CONTROLLER, "execution");
                    lastUpdateTime = currentTime;
                }
            } else if (entity.getFirstPassenger() != null) {
                if (currentTime - lastUpdateTime == 38000L) {
                    entity.setDeltaMovement(0, 0, 0);
                    entity.setIsExecuting(true);
                    entity.setAggressive(false);
                    entity.triggerAnim(Constants.ATTACK_CONTROLLER, "execution");
                }
                if (currentTime - lastUpdateTime >= 42150) {
                    entity.getFirstPassenger().hurt(GigDamageSources.of(entity.level(), GigDamageSources.EXECUTION),
                            Integer.MAX_VALUE);
                    entity.heal(50);
                    if (entity.level().isClientSide)
                        entity.getFirstPassenger().level().addAlwaysVisibleParticle(GigParticles.BLOOD.get(), e, yOffset, f, 0.0,
                                -0.15, 0.0);
                    entity.triggerAnim(Constants.ATTACK_CONTROLLER, "reset");
                    lastUpdateTime = currentTime;
                }
            }
        }
    }
}