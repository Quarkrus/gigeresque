package mods.cybercat.gigeresque.client.entity.render;

import mods.cybercat.gigeresque.client.entity.model.HammerpedeEntityModel;
import mods.cybercat.gigeresque.common.entity.impl.mutant.HammerpedeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import mod.azure.azurelib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class HammerpedeEntityRenderer extends GeoEntityRenderer<HammerpedeEntity> {
	public HammerpedeEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new HammerpedeEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(HammerpedeEntity entityLivingBaseIn) {
		return 0.0F;
	}
}
