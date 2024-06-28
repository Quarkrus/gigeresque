package mods.cybercat.gigeresque.client.entity.render;

import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import mods.cybercat.gigeresque.client.entity.model.PopperEntityModel;
import mods.cybercat.gigeresque.common.entity.impl.mutant.PopperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PopperEntityRenderer extends GeoEntityRenderer<PopperEntity> {
    public PopperEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PopperEntityModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    protected float getDeathMaxRotation(PopperEntity entityLivingBaseIn) {
        return 0.0F;
    }
}
