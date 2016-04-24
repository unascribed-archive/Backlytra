package com.unascribed.backlytra;

import net.minecraft.entity.EntityLivingBase;

public class MethodImitations {

	public static boolean isElytraFlying(EntityLivingBase e) {
		return (e.getDataWatcher().getWatchableObjectByte(0) & (1 << 7)) != 0;
	}
	
	public static void setElytraFlying(EntityLivingBase e, boolean state) {
		byte b0 = e.getDataWatcher().getWatchableObjectByte(0);

		if (state) {
			e.getDataWatcher().updateObject(0, Byte.valueOf((byte) (b0 | 1 << 7)));
		} else {
			e.getDataWatcher().updateObject(0, Byte.valueOf((byte) (b0 & ~(1 << 7))));
		}
	}

	public static int getTicksElytraFlying(EntityLivingBase e) {
		return FieldImitations.get(e, "ticksElytraFlying", 0);
	}

}
