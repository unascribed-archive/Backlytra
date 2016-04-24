package com.unascribed.backlytra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends Proxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}
	@Override
	public void postInit(FMLPostInitializationEvent event) {}
	
	@Override
	public void update(EntityLivingBase elb) {
		if (elb instanceof EntityPlayerSP) {
			EntityPlayerSP e = (EntityPlayerSP) elb;
			boolean lastFlying = FieldImitations.get(e, "lastElytraFlying", false);
			if (e.isElytraFlying() && !lastFlying) {
				Minecraft.getMinecraft().getSoundHandler().playSound(new ElytraSound(e));
			}
			FieldImitations.set(e, "lastElytraFlying", e.isElytraFlying());
		}
	}

}
