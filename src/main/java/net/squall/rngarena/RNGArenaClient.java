package net.squall.rngarena;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.squall.rngarena.client.model.HelmOfDarknessArmorModel;
import net.squall.rngarena.client.render.HelmOfDarknessArmorRenderer;

public class RNGArenaClient implements ClientModInitializer{
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(HelmOfDarknessArmorModel.LAYER,
                HelmOfDarknessArmorModel::createTexturedModelData);
        HelmOfDarknessArmorRenderer.register();
    }
}
