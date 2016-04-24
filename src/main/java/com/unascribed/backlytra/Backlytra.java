package com.unascribed.backlytra;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "backlytra", name = "Backlytra", version = "@VERSION@")
public class Backlytra {
	
	public static SoundEvent elytraFlying;

	@SidedProxy(clientSide = "com.unascribed.backlytra.ClientProxy", serverSide = "com.unascribed.backlytra.Proxy")
	public static Proxy proxy;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		elytraFlying = new SoundEvent(new ResourceLocation("backlytra", "item.elytra.flying"));
		proxy.preInit(e);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingTick(LivingUpdateEvent e) {
		if (e.isCanceled()) return;
		proxy.update(e.getEntityLiving());
	}
	

}
