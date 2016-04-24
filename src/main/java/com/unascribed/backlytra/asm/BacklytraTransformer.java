package com.unascribed.backlytra.asm;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.unascribed.backlytra.asm.repackage.net.malisis.core.asm.AsmHook;
import com.unascribed.backlytra.asm.repackage.net.malisis.core.asm.MalisisClassTransformer;
import com.unascribed.backlytra.asm.repackage.net.malisis.core.asm.mappings.McpFieldMapping;
import com.unascribed.backlytra.asm.repackage.net.malisis.core.asm.mappings.McpMethodMapping;

import static org.objectweb.asm.Opcodes.*;

public class BacklytraTransformer extends MalisisClassTransformer {

	/*
	 * I really do not want to have to do this, but it's quite honestly the
	 * cleanest way to do it. Using events puts it at the wrong place in the
	 * update method, which could do who knows what, and replacing every
	 * EntityPlayer is likely to cause ridiculous conflicts.
	 * 
	 * Such are backports of vanilla code, I suppose.
	 */
	
	@Override
	public void registerHooks() {
		register(moveEntityWithHeadingHook());
		register(getEyeHeightHook());
		register(rotateCorpseHook());
		register(movedTooQuicklyHook());
		register(setRotationAnglesHook());
	}
	
	private AsmHook setRotationAnglesHook() {
		AsmHook hook = new AsmHook(new McpMethodMapping("setRotationAngles", "func_78087_a",
				"net/minecraft/client/model/ModelBiped", "(FFFFFFLnet/minecraft/entity/Entity;)V"));
		
		McpFieldMapping rotateAngleY = new McpFieldMapping("rotateAngleY", "field_78796_g",
				"net/minecraft/client/model/ModelRenderer", "F");
		
		// Backlytra.setRotationAngles(this, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		InsnList inject = new InsnList();
		
		inject.add(new VarInsnNode(ALOAD, 0));
		inject.add(new VarInsnNode(FLOAD, 1));
		inject.add(new VarInsnNode(FLOAD, 2));
		inject.add(new VarInsnNode(FLOAD, 3));
		inject.add(new VarInsnNode(FLOAD, 4));
		inject.add(new VarInsnNode(FLOAD, 5));
		inject.add(new VarInsnNode(FLOAD, 6));
		inject.add(new VarInsnNode(ALOAD, 7));
		inject.add(new MethodInsnNode(INVOKESTATIC, "com/unascribed/backlytra/Backlytra", "setRotationAngles",
				"(Lnet/minecraft/client/model/ModelBiped;FFFFFFLnet/minecraft/entity/Entity;)V", false));
		
		// this.bipedLeftLeg.rotateAngleY = 0.0F;
		
		InsnList find = new InsnList();
		
		find.add(new InsnNode(FCONST_0));
		find.add(rotateAngleY.getInsnNode(PUTFIELD));
		
		hook.jumpAfter(find).insert(inject);
		
		return hook;
	}
	
	private AsmHook movedTooQuicklyHook() {
		AsmHook hook = new AsmHook(new McpMethodMapping("processPlayer", "func_147_347_a",
				"net/minecraft/network/NetHandlerPlayServer", "(Lnet/minecraft/network/play/client/C03PacketPlayer;)V"));
		
		McpFieldMapping logger = new McpFieldMapping("logger", "field_147370_c",
				"net/minecraft/network/NetHandlerPlayServer", "Lorg/apache/logging/log4j/Logger;");
		McpFieldMapping playerEntity = new McpFieldMapping("playerEntity", "field_147369_b",
				"net/minecraft/network/NetHandlerPlayServer", "Lnet/minecraft/entity/player/EntityPlayerMP;");
		
		// if (Backlytra.didPlayerReallyGoTooFast(playerEntity, d15-d14)) {
		
		InsnList inject = new InsnList();
		
		LabelNode l87 = new LabelNode();
		
		inject.add(new VarInsnNode(ALOAD, 0));
		inject.add(playerEntity.getInsnNode(GETFIELD));
		inject.add(new VarInsnNode(DLOAD, 35));
		inject.add(new VarInsnNode(DLOAD, 33));
		inject.add(new InsnNode(DSUB));
		inject.add(new MethodInsnNode(INVOKESTATIC, "com/unascribed/backlytra/Backlytra", "didPlayerReallyMoveTooQuickly",
				"(Lnet/minecraft/entity/player/EntityPlayerMP;D)Z", false));
		inject.add(new JumpInsnNode(IFEQ, l87));
		
		//     logger.warn(this.playerEntity.getName() + " moved too quickly! " + d11 + "," + d12 + "," + d13 + " (" + d11 + ", " + d12 + ", " + d13 + ")");
		
		InsnList find = new InsnList();
		
		find.add(logger.getInsnNode(GETSTATIC));
		find.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
		find.add(new InsnNode(DUP));
		
		//     return;
		
		InsnList find2 = new InsnList();
		
		find2.add(new InsnNode(RETURN));
		
		hook.jumpBefore(find).jump(-1).insert(inject)
			.jumpAfter(find2);
		
		// }
		
		hook.insert(l87);
		
		return hook;
	}
	
	private AsmHook rotateCorpseHook() {
		AsmHook hook = new AsmHook(new McpMethodMapping("rotateCorpse", "func_77043_a",
				"net/minecraft/client/renderer/entity/RenderPlayer", "(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V"));
		
		McpMethodMapping rotateCorpse = new McpMethodMapping("rotateCorpse", "func_77043_a",
				"net/minecraft/client/renderer/entity/RendererLivingEntity", "(Lnet/minecraft/entity/EntityLivingBase;FFF)V");
		
		// Backlytra.rotateCorpse(this, bat, p_77043_2_, p_77043_3_, partialTicks);
		
		InsnList inject = new InsnList();
		
		inject.add(new VarInsnNode(ALOAD, 1));
		inject.add(new VarInsnNode(FLOAD, 4));
		inject.add(new MethodInsnNode(INVOKESTATIC, "com/unascribed/backlytra/Backlytra",
				"rotateCorpse", "(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", false));
		
		// super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
		
		InsnList find = new InsnList();
		
		find.add(new VarInsnNode(ALOAD, 0));
		find.add(new VarInsnNode(ALOAD, 1));
		find.add(new VarInsnNode(FLOAD, 2));
		find.add(new VarInsnNode(FLOAD, 3));
		find.add(new VarInsnNode(FLOAD, 4));
		find.add(rotateCorpse.getInsnNode(INVOKESPECIAL));
		
		hook.jumpAfter(find).insert(inject);
		
		return hook;
	}
	
	private AsmHook getEyeHeightHook() {
		AsmHook hook = new AsmHook(new McpMethodMapping("getEyeHeight", "func_70047_e",
				"net/minecraft/entity/player/EntityPlayer", "()F"));

		// f = Backlytra.modifyEyeHeight(this, f);
		
		InsnList inject = new InsnList();
		
		inject.add(new VarInsnNode(ALOAD, 0));
		inject.add(new VarInsnNode(FLOAD, 1));
		inject.add(new MethodInsnNode(INVOKESTATIC, "com/unascribed/backlytra/Backlytra",
				"modifyEyeHeight", "(Lnet/minecraft/entity/player/EntityPlayer;F)F", false));
		inject.add(new VarInsnNode(FSTORE, 1));
		
		// return f;
		
		InsnList find = new InsnList();
		
		find.add(new VarInsnNode(FLOAD, 1));
		find.add(new InsnNode(FRETURN));
		
		
		hook.jumpBefore(find).insert(inject);
		
		return hook;
	}
	
	private AsmHook moveEntityWithHeadingHook() {
		AsmHook hook = new AsmHook(new McpMethodMapping("moveEntityWithHeading", "func_70612_e",
				"net/minecraft/entity/EntityLivingBase", "(FF)V"));
		
		// if (Backlytra.moveEntityWithHeading(this, strafe, forward)) return;
		
		InsnList inject = new InsnList();
		
		inject.add(new VarInsnNode(ALOAD, 0));
		inject.add(new VarInsnNode(FLOAD, 1));
		inject.add(new VarInsnNode(FLOAD, 2));
		inject.add(new MethodInsnNode(INVOKESTATIC, "com/unascribed/backlytra/Backlytra",
				"moveEntityWithHeading", "(Lnet/minecraft/entity/EntityLivingBase;FF)Z", false));
		LabelNode l7 = new LabelNode();
		inject.add(new JumpInsnNode(IFEQ, l7));
		inject.add(new InsnNode(RETURN));
		inject.add(l7);
		
		
		// float f4 = 0.91F;
		
		InsnList find = new InsnList();
		
		find.add(new LdcInsnNode(new Float("0.91")));
		
		
		hook.jumpBefore(find).insert(inject);
		
		return hook;
	}


}
