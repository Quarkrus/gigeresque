package com.bvanseg.gigeresque.common.entity.ai.brain.memory;

import com.bvanseg.gigeresque.common.entity.impl.AlienEggEntity;
import com.bvanseg.gigeresque.common.entity.impl.FacehuggerEntity;
import com.bvanseg.gigeresque.mixins.common.entity.ai.MemoryModuleTypeInvoker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MemoryModuleTypes {
    private MemoryModuleTypes() {}

    public static final MemoryModuleType<LivingEntity> EGGMORPH_TARGET = MemoryModuleTypeInvoker.register("eggmorph_target");
    public static final MemoryModuleType<BlockPos> NEAREST_ALIEN_WEBBING = MemoryModuleTypeInvoker.register("nearest_alien_webbing");
    public static final MemoryModuleType<List<AlienEggEntity>> NEAREST_EGGS = MemoryModuleTypeInvoker.register("nearest_eggs");
    public static final MemoryModuleType<List<FacehuggerEntity>> NEAREST_FACEHUGGERS = MemoryModuleTypeInvoker.register("nearest_facehuggers");
    public static final MemoryModuleType<List<LivingEntity>> NEAREST_HOSTS = MemoryModuleTypeInvoker.register("nearest_hosts");
    public static final MemoryModuleType<BlockPos> NEAREST_LIGHT_SOURCE = MemoryModuleTypeInvoker.register("nearest_light_source");
}