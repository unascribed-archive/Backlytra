package com.unascribed.backlytra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends Proxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}
	@Override
	public void postInit(FMLPostInitializationEvent event) {
	}
	
	@Override
	public void update(EntityLivingBase elb) {
		if (elb instanceof EntityPlayerSP) {
			EntityPlayerSP e = (EntityPlayerSP) elb;
			boolean lastJumping = FieldImitations.get(e, "lastIsJumping", false);
			if (e.movementInput.jump && !lastJumping && !e.onGround && e.motionY < 0.0D && !MethodImitations.isElytraFlying(e) && !e.capabilities.isFlying) {
				ItemStack itemstack = e.getEquipmentInSlot(3);

				if (itemstack != null && itemstack.getItem() == Backlytra.elytra && ItemElytra.isBroken(itemstack)) {
					Backlytra.network.sendToServer(new StartFallFlying());
					Minecraft.getMinecraft().getSoundHandler().playSound(new ElytraSound(e));
				}
			}
			FieldImitations.set(e, "lastIsJumping", e.movementInput.jump);
		}
	}

}
