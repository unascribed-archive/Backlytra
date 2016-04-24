package com.unascribed.backlytra;

import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ClientProxy extends Proxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ModelLoader.setCustomModelResourceLocation(Backlytra.elytra, 0, new ModelResourceLocation(new ResourceLocation("backlytra", "elytra"), "inventory"));
	}
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		RenderManager manager = mc.getRenderManager();
		Map<String, RenderPlayer> renders = manager.getSkinMap();
		for(RenderPlayer render : renders.values()) {
			List<LayerRenderer<?>> list = ReflectionHelper.getPrivateValue(RendererLivingEntity.class, render, "i", "field_177097_h", "layerRenderers");
			list.add(new LayerBetterElytra(render));
		}
	}
	
	@Override
	public void update(EntityLivingBase elb) {
		if (elb instanceof EntityPlayerSP) {
			EntityPlayerSP e = (EntityPlayerSP) elb;
			boolean lastJumping = FieldImitations.get(e, "lastIsJumping", false);
			if (e.movementInput.jump && !lastJumping && !e.onGround && e.motionY < 0.0D && !MethodImitations.isElytraFlying(e) && !e.capabilities.isFlying) {
				ItemStack itemstack = e.getEquipmentInSlot(3);

				if (itemstack != null && itemstack.getItem() == Backlytra.elytra && ItemElytra.isBroken(itemstack)) {
					Backlytra.network.send()
						.packet("StartFallFlying")
						.toServer();
					Minecraft.getMinecraft().getSoundHandler().playSound(new ElytraSound(e));
				}
			}
			FieldImitations.set(e, "lastIsJumping", e.movementInput.jump);
		}
	}

}
