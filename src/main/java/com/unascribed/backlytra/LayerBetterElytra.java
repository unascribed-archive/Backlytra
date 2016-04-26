/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * [ADD-LICENSE-HERE]
 *
 * File Created @ [21/03/2016, 00:14:23 (GMT)]
 */
package com.unascribed.backlytra;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBetterElytra {

	private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
	private static final ModelElytra modelElytra = new ModelElytra();
	protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	public static void doRenderLayer(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack itemstack = entityIn.getEquipmentInSlot(3);
		if (itemstack != null && itemstack.getItem() == Backlytra.elytra) {
			GL11.glPushAttrib(-1);
			int colorIndex =
					itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("backlytra:elytraDye", NBT.TAG_ANY_NUMERIC)
					? itemstack.getTagCompound().getInteger("backlytra:elytraDye")
					: -1;

			if (colorIndex < 0 || colorIndex == 15)
				GL11.glColor3f(1, 1, 1);
			else {
				Color color = new Color(ItemDye.dyeColors[colorIndex]);
				float r = color.getRed() / 255F;
				float g = color.getGreen() / 255F;
				float b = color.getBlue() / 255F;
				GL11.glColor3f(r, g, b);
			}

			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_ELYTRA);

			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 0.0F, 0.125F);
			modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
			modelElytra.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

			if (itemstack.isItemEnchanted())
				renderGlint(entityIn, modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
	}

	private static void renderGlint(EntityLivingBase entitylivingbaseIn, ModelElytra modelbaseIn, float p_177183_3_, float p_177183_4_, float partialTicks, float p_177183_6_, float p_177183_7_, float p_177183_8_, float scale) {
		float f = entitylivingbaseIn.ticksExisted + partialTicks;
		Minecraft.getMinecraft().renderEngine.bindTexture(ENCHANTED_ITEM_GLINT_RES);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthFunc(514);
		GL11.glDepthMask(false);
		float f1 = 0.5F;
		GL11.glColor4f(f1, f1, f1, 1.0F);

		for (int i = 0; i < 2; ++i) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(768, 1);
			float f2 = 0.76F;
			GL11.glColor4f(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
			GL11.glMatrixMode(5890);
			GL11.glLoadIdentity();
			float f3 = 0.33333334F;
			GL11.glScalef(f3, f3, f3);
			GL11.glRotatef(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
			GL11.glTranslatef(0.0F, f * (0.001F + i * 0.003F) * 20.0F, 0.0F);
			GL11.glMatrixMode(5888);
			modelbaseIn.render(entitylivingbaseIn, p_177183_3_, p_177183_4_, p_177183_6_, p_177183_7_, p_177183_8_, scale);
		}

		GL11.glMatrixMode(5890);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(5888);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glDepthFunc(515);
		GL11.glDisable(GL11.GL_BLEND);
	}
}