package mods.cybercat.gigeresque.common.item;

import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.Gigeresque;
import mods.cybercat.gigeresque.common.entity.Entities;
import mods.cybercat.gigeresque.common.entity.impl.classic.ChestbursterEntity;
import mods.cybercat.gigeresque.common.entity.impl.classic.FacehuggerEntity;
import mods.cybercat.gigeresque.common.sound.GigSounds;
import mods.cybercat.gigeresque.common.status.effect.GigStatusEffects;
import mods.cybercat.gigeresque.common.tags.GigTags;
import mods.cybercat.gigeresque.interfacing.Host;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SurgeryKitItem extends Item {

    public SurgeryKitItem() {
        super(new Item.Properties().durability(Gigeresque.config.maxSurgeryKitUses));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, @NotNull Player player, LivingEntity livingEntity, @NotNull InteractionHand interactionHand) {
        if (livingEntity.getPassengers().stream().noneMatch(FacehuggerEntity.class::isInstance)) {
            tryRemoveParasite(itemStack, livingEntity);
            player.getCooldowns().addCooldown(this, Gigeresque.config.surgeryKitCooldownTicks);
            itemStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(interactionHand));
        }
        return super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        if (user.getPassengers().stream().noneMatch(FacehuggerEntity.class::isInstance))
            tryRemoveParasite(user.getItemInHand(hand), user);
        return super.use(world, user, hand);
    }

    private void tryRemoveParasite(ItemStack stack, LivingEntity entity) {
        var host = (Host) entity;
        if (host.hasParasite() || entity.hasEffect(GigStatusEffects.SPORE) && !entity.level().isClientSide) {
            entity.removeEffect(MobEffects.HUNGER);
            entity.removeEffect(MobEffects.WEAKNESS);
            entity.removeEffect(MobEffects.DIG_SLOWDOWN);
            host.setBleeding(false);
            spawnParasite(entity);
            host.setTicksUntilImpregnation(-1);

            host.removeParasite();
            if (entity instanceof Player playerentity) {
                playerentity.getCooldowns().addCooldown(this, Gigeresque.config.surgeryKitCooldownTicks);
                stack.hurtAndBreak(1, playerentity, p -> p.broadcastBreakEvent(playerentity.getUsedItemHand()));
            }
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), GigSounds.CHESTBURSTING,
                    SoundSource.NEUTRAL, 2.0f, 1.0f);
            entity.addEffect(new MobEffectInstance(GigStatusEffects.TRAUMA, Constants.TPD));
        }
    }

    private void spawnParasite(LivingEntity entity) {
        LivingEntity burster = Entities.CHESTBURSTER.create(entity.level());

        if (!entity.hasEffect(GigStatusEffects.SPORE) && !entity.hasEffect(GigStatusEffects.DNA)) {
            if (entity.getType().is(GigTags.RUNNER_HOSTS)) {
                burster = Entities.RUNNERBURSTER.create(entity.level());
                assert burster != null;
                ((ChestbursterEntity) burster).setHostId("runner");
            } else if (entity.getType().is(GigTags.AQUATIC_HOSTS))
                burster = Entities.AQUATIC_CHESTBURSTER.create(entity.level());
            else burster = Entities.CHESTBURSTER.create(entity.level());
        } else if (entity.getType().is(GigTags.NEOHOST) && entity.hasEffect(GigStatusEffects.SPORE))
            burster = Entities.NEOBURSTER.create(entity.level());
        else if (entity.getType().is(GigTags.CLASSIC_HOSTS) && entity.hasEffect(GigStatusEffects.DNA))
            burster = Entities.SPITTER.create(entity.level());

        if (entity.hasCustomName()) {
            assert burster != null;
            burster.setCustomName(entity.getCustomName());
        }
        if (burster instanceof ChestbursterEntity chest)
            chest.setHostId(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        assert burster != null;
        burster.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
        entity.level().addFreshEntity(burster);
    }
}
