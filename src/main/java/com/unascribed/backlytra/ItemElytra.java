package com.unascribed.backlytra;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemElytra extends Item {
	public ItemElytra() {
		this.maxStackSize = 1;
		this.setMaxDamage(Backlytra.durability);
		this.setCreativeTab(CreativeTabs.tabTransport);
		BlockDispenser.dispenseBehaviorRegistry.putObject(this, BlockDispenser.dispenseBehaviorRegistry.getObject(Items.iron_chestplate));
	}

	public static boolean isBroken(ItemStack stack) {
		return stack.getItemDamage() < stack.getMaxDamage() - 1;
	}

	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == Items.leather;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		int entityequipmentslot = 3;
		ItemStack itemstack = playerIn.getEquipmentInSlot(entityequipmentslot);

		if (itemstack == null) {
			playerIn.setCurrentItemOrArmor(entityequipmentslot, itemStackIn.copy());
			itemStackIn.stackSize = 0;
			return itemStackIn;
		} else {
			return itemStackIn;
		}
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		int color =
				stack.hasTagCompound() && stack.getTagCompound().hasKey("backlytra:elytraDye", NBT.TAG_ANY_NUMERIC)
				? stack.getTagCompound().getInteger("backlytra:elytraDye")
				: -1;
		if(color == -1 || color == 15)
			return -1;

		return ItemDye.dyeColors[color];
	}
}