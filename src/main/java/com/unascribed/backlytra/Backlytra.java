package com.unascribed.backlytra;

import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Enums;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = "backlytra", name = "Backlytra", version = "@VERSION@")
public class Backlytra {
	public enum ElytraRecipe {
		NONE, DAY_ONE, SIMPLE, RIDICULOUS;
	}

	public static boolean isObfEnv = false;
	
	public static ItemElytra elytra;
	public static int durability;

	public static SimpleNetworkWrapper network;

	@SidedProxy(clientSide = "com.unascribed.backlytra.ClientProxy", serverSide = "com.unascribed.backlytra.Proxy")
	public static Proxy proxy;

	public Backlytra() {
		isObfEnv = !(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		durability = config.getInt("durability", "etc", 432, 1, 32767, "Max amount of hits the Elytra can take before breaking.");
		String recipeStr = config.getString("recipe", "etc", "simple", "The recipe to use for the Elytra. Can be none, day_one, simple, or ridiculous.");
		config.save();
		
		elytra = new ItemElytra();
		elytra.setUnlocalizedName("elytra")
			.setTextureName("minecraft:elytra");
		GameRegistry.registerItem(elytra, "elytra");
		
		network = NetworkRegistry.INSTANCE.newSimpleChannel("Backlytra");
		network.registerMessage(StartFallFlying.class, StartFallFlying.class, 0, Side.SERVER);
		
		switch (Enums.getIfPresent(ElytraRecipe.class, recipeStr.toUpperCase()).or(ElytraRecipe.NONE)) {
			case NONE:
				break;
			case DAY_ONE:
				GameRegistry.addRecipe(new ShapedOreRecipe(elytra,
						"lLl",
						"lLl",
						"fLf",
						
						'l', Items.leather,
						'L', "logWood",
						'f', Items.feather
						));
				break;
			case SIMPLE:
				GameRegistry.addRecipe(new ShapedOreRecipe(elytra,
						"ses",
						"plp",
						"f f",
						
						'l', Items.leather,
						'p', Items.paper,
						's', Items.string,
						'f', Items.feather,
						'e', "gemEmerald"
						));
				break;
			case RIDICULOUS:
				GameRegistry.addRecipe(new ShapedOreRecipe(elytra,
						"ses",
						"plp",
						"f f",
						
						'l', Items.leather,
						'p', Items.paper,
						's', Items.string,
						'f', Items.feather,
						'e', Blocks.dragon_egg
						));
				break;
		}
		GameRegistry.addRecipe(new ElytraDyingRecipe());
		RecipeSorter.register("backlytra:elytraDying", ElytraDyingRecipe.class, RecipeSorter.Category.SHAPELESS, "");
		proxy.preInit(e);
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingTick(LivingUpdateEvent e) {
		if (e.isCanceled()) return;
		proxy.update(e.entityLiving);
		updateElytra(e.entityLiving);
	}
	
	@SubscribeEvent
	public void onPostPlayerTick(PlayerTickEvent e) {
		if (e.phase == Phase.END) {
			boolean isElytraFlying = MethodImitations.isElytraFlying(e.player);
			if (e.player instanceof EntityPlayerMP && isElytraFlying) {
				((EntityPlayerMP)e.player).playerNetServerHandler.floatingTickCount = 0;
			}
			if (isElytraFlying != FieldImitations.get(e.player, "lastIsElytraFlying", false)) {
				float f = 0.6f;
				float f1 = isElytraFlying ? 0.6f : 1.8f;
				
				if (f != e.player.width || f1 != e.player.height) {
					AxisAlignedBB axisalignedbb = e.player.boundingBox;
					axisalignedbb = AxisAlignedBB.getBoundingBox(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + f, axisalignedbb.minY + f1, axisalignedbb.minZ + f);
	
					if (e.player.worldObj.func_147461_a(axisalignedbb).isEmpty()) {
						float f2 = e.player.width;
						e.player.width = f;
						e.player.height = f1;
						e.player.boundingBox.setBounds(e.player.boundingBox.minX, e.player.boundingBox.minY, e.player.boundingBox.minZ, e.player.boundingBox.minX + e.player.width, e.player.boundingBox.minY + e.player.height, e.player.boundingBox.minZ + e.player.width);
	
						if (e.player.width > f2 && !e.player.worldObj.isRemote) {
							e.player.moveEntity(f2 - e.player.width, 0.0D, f2 - e.player.width);
						}
					}
				}
				FieldImitations.set(e.player, "lastIsElytraFlying", isElytraFlying);
			}
		}
	}
	
	@SubscribeEvent
	public void onPostWorldTick(WorldTickEvent e) {
		if (e.phase == Phase.END && e.world instanceof WorldServer) {
			WorldServer ws = (WorldServer)e.world;
			for (EntityTrackerEntry ete : (Set<EntityTrackerEntry>)ws.getEntityTracker().trackedEntities) {
				if (ete.trackedEntity instanceof EntityLivingBase) {
					EntityLivingBase elb = (EntityLivingBase) ete.trackedEntity;
					boolean flying = MethodImitations.isElytraFlying(elb);
					if (!flying && FieldImitations.get(ete, "wasForcingVelocityUpdates", false)) {
						ete.sendVelocityUpdates = false;
					} else if (flying) {
						if (!ete.sendVelocityUpdates) {
							FieldImitations.get(ete, "wasForcingVelocityUpdates", true);
						}
						ete.sendVelocityUpdates = true;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		e.world.getGameRules().addGameRule("disableElytraMovementCheck", "false");
	}
	
	private void updateElytra(EntityLivingBase e) {
		boolean flag = MethodImitations.isElytraFlying(e);

		if (flag && !e.onGround && !e.isRiding() && !e.isInWater()) {
			ItemStack itemstack = e.getEquipmentInSlot(3);

			if (itemstack != null && itemstack.getItem() == elytra && ItemElytra.isBroken(itemstack)) {
				flag = true;

				if (!e.worldObj.isRemote && (MethodImitations.getTicksElytraFlying(e) + 1) % 20 == 0) {
					itemstack.damageItem(1, e);
				}
			} else {
				flag = false;
			}
		} else {
			flag = false;
		}

		if (!e.worldObj.isRemote) {
			MethodImitations.setElytraFlying(e, flag);
		}
		
		if (MethodImitations.isElytraFlying(e)) {
			FieldImitations.set(e, "ticksElytraFlying", FieldImitations.get(e, "ticksElytraFlying", 0)+1);
		} else {
			FieldImitations.set(e, "ticksElytraFlying", 0);
		}
	}
	
	public static DamageSource flyIntoWall = (new DamageSource("flyIntoWall")).setDamageBypassesArmor();
	
	public static boolean moveEntityWithHeading(EntityLivingBase e, float strafe, float forward) {
		if (e.isServerWorld() && !e.isInWater() && !e.handleLavaMovement()) {
			if (MethodImitations.isElytraFlying(e)) {
				if (e.motionY > -0.5D) {
					e.fallDistance = 1.0F;
				}

				Vec3 vec3d = e.getLookVec();
				float f = e.rotationPitch * 0.017453292F;
				double d6 = Math.sqrt(vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord);
				double d8 = Math.sqrt(e.motionX * e.motionX + e.motionZ * e.motionZ);
				double d1 = vec3d.lengthVector();
				float f4 = MathHelper.cos(f);
				f4 = (float) ((double) f4 * (double) f4 * Math.min(1.0D, d1 / 0.4D));
				e.motionY += -0.08D + f4 * 0.06D;

				if (e.motionY < 0.0D && d6 > 0.0D) {
					double d2 = e.motionY * -0.1D * f4;
					e.motionY += d2;
					e.motionX += vec3d.xCoord * d2 / d6;
					e.motionZ += vec3d.zCoord * d2 / d6;
				}

				if (f < 0.0F) {
					double d9 = d8 * (-MathHelper.sin(f)) * 0.04D;
					e.motionY += d9 * 3.2D;
					e.motionX -= vec3d.xCoord * d9 / d6;
					e.motionZ -= vec3d.zCoord * d9 / d6;
				}

				if (d6 > 0.0D) {
					e.motionX += (vec3d.xCoord / d6 * d8 - e.motionX) * 0.1D;
					e.motionZ += (vec3d.zCoord / d6 * d8 - e.motionZ) * 0.1D;
				}

				e.motionX *= 0.9900000095367432D;
				e.motionY *= 0.9800000190734863D;
				e.motionZ *= 0.9900000095367432D;
				e.moveEntity(e.motionX, e.motionY, e.motionZ);

				if (e.isCollidedHorizontally && !e.worldObj.isRemote) {
					double d10 = Math.sqrt(e.motionX * e.motionX + e.motionZ * e.motionZ);
					double d3 = d8 - d10;
					float f5 = (float) (d3 * 10.0D - 3.0D);

					if (f5 > 0.0F) {
						e.playSound((int) f5 > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small", 1.0F, 1.0F);
						e.attackEntityFrom(flyIntoWall, f5);
					}
				}

				if (e.onGround && !e.worldObj.isRemote) {
					MethodImitations.setElytraFlying(e, false);
				}
				return true;
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static void rotateCorpse(AbstractClientPlayer entityLiving, float partialTicks) {
		if (MethodImitations.isElytraFlying(entityLiving)) {
			float f = MethodImitations.getTicksElytraFlying(entityLiving) + partialTicks;
			float f1 = MathHelper.clamp_float(f * f / 100.0F, 0.0F, 1.0F);
			GL11.glRotatef(f1 * (-90.0F - entityLiving.rotationPitch), 1.0F, 0.0F, 0.0F);
			Vec3 vec3d = entityLiving.getLook(partialTicks);
			double d0 = entityLiving.motionX * entityLiving.motionX + entityLiving.motionZ * entityLiving.motionZ;
			double d1 = vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord;
	
			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (entityLiving.motionX * vec3d.xCoord + entityLiving.motionZ * vec3d.zCoord) / (Math.sqrt(d0) * Math.sqrt(d1));
				double d3 = entityLiving.motionX * vec3d.zCoord - entityLiving.motionZ * vec3d.xCoord;
				GL11.glRotatef((float) (Math.signum(d3) * Math.acos(d2)) * 180.0F / (float) Math.PI, 0.0F, 1.0F, 0.0F);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void postSetRotationAngles(ModelBiped modelBiped, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (entityIn instanceof EntityLivingBase && (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 ? entityIn != Minecraft.getMinecraft().renderViewEntity : true)) {
			LayerBetterElytra.doRenderLayer((EntityLivingBase)entityIn, limbSwing, limbSwingAmount, Minecraft.getMinecraft().timer.renderPartialTicks, ageInTicks, netHeadYaw, headPitch, 0.0625F);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void setRotationAngles(ModelBiped modelBiped, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		boolean flag = entityIn instanceof EntityLivingBase && MethodImitations.getTicksElytraFlying((EntityLivingBase) entityIn) > 4;
		
		if (flag) {
			limbSwing = ageInTicks;
			
			modelBiped.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
	
			modelBiped.bipedHead.rotateAngleX = -((float) Math.PI / 4F);
	
			modelBiped.bipedBody.rotateAngleY = 0.0F;
			modelBiped.bipedRightArm.rotationPointZ = 0.0F;
			modelBiped.bipedRightArm.rotationPointX = -5.0F;
			modelBiped.bipedLeftArm.rotationPointZ = 0.0F;
			modelBiped.bipedLeftArm.rotationPointX = 5.0F;
			float f = 1.0F;
	
			f = (float) (entityIn.motionX * entityIn.motionX + entityIn.motionY * entityIn.motionY + entityIn.motionZ * entityIn.motionZ);
			f = f / 0.2F;
			f = f * f * f;
	
			if (f < 1.0F) {
				f = 1.0F;
			}
	
			modelBiped.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
			modelBiped.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
			modelBiped.bipedRightArm.rotateAngleZ = 0.0F;
			modelBiped.bipedLeftArm.rotateAngleZ = 0.0F;
			modelBiped.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
			modelBiped.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
			modelBiped.bipedRightLeg.rotateAngleY = 0.0F;
			modelBiped.bipedLeftLeg.rotateAngleY = 0.0F;
			modelBiped.bipedRightLeg.rotateAngleZ = 0.0F;
			modelBiped.bipedLeftLeg.rotateAngleZ = 0.0F;
		}
	}

	public static boolean shouldOverrideEyeHeight(EntityPlayer entityPlayer) {
		return MethodImitations.isElytraFlying(entityPlayer) && !entityPlayer.isPlayerSleeping();
	}
	
	public static float getEyeHeight(EntityPlayer player) {
		return 0.4f;
	}

	public static double modifyMovementDelta(EntityPlayerMP playerEntity, double d10) {
		if (MethodImitations.isElytraFlying(playerEntity)) {
			if (playerEntity.worldObj.getGameRules().getGameRuleBooleanValue("disableElytraMovementCheck")) {
				return 0;
			}
			return d10/3;
		}
		return d10;
	}

}
