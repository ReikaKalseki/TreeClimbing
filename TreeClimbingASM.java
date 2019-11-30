/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.TreeClimbing;

import java.lang.reflect.Modifier;
import java.util.HashSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;

import Reika.DragonAPI.Exception.ASMException;
import Reika.DragonAPI.Exception.ASMException.NoSuchASMMethodException;
import Reika.DragonAPI.Libraries.Java.ReikaASMHelper;

public class TreeClimbingASM implements IClassTransformer {

	private final HashSet<String> logClasses = new HashSet();
	private final HashSet<String> leafClasses = new HashSet();

	public TreeClimbingASM() {
		logClasses.add("net/minecraft/block/BlockLog");
		logClasses.add("alx");

		logClasses.add("biomesoplenty/common/blocks/BlockBOPLog");
		logClasses.add("thaumcraft/common/blocks/BlockMagicalLog");
		logClasses.add("mods/natura/blocks/trees/Planks");
		logClasses.add("ic2/core/block/BlockRubWood");


		leafClasses.add("net/minecraft/block/BlockLeavesBase");
		leafClasses.add("aod");

		leafClasses.add("thaumcraft/common/blocks/BlockMagicalLeaves");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		ClassNode cn = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(cn, 0);

		if (logClasses.contains(cn.name) || leafClasses.contains(cn.name)) {
			ReikaASMHelper.activeMod = "TreeClimbing";
			//if ((classNode.access & Modifier.ABSTRACT) == 0) {
			try {
				ReikaASMHelper.activeMod = "TreeClimbing";
				ReikaASMHelper.log("Patching "+cn.name);
				if (logClasses.contains(cn.name))
					this.patchLogClass(cn);
				else
					this.patchLeafClass(cn);
				ReikaASMHelper.log("Patched "+cn.name);
			}
			catch (ASMException e) {
				ReikaASMHelper.log("Could not patch "+cn.name+":");
				e.printStackTrace();
			}
			ReikaASMHelper.activeMod = null;

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cn.accept(writer);
			cn.check(cn.version);
			return writer.toByteArray();
		}
		else if (cn.name.equals("net/minecraft/block/BlockLeaves")) {
			ReikaASMHelper.activeMod = "TreeClimbing";
			try {
				MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_150122_b", "setGraphicsLevel", "(Z)V");
				AbstractInsnNode loc1 = ReikaASMHelper.getFirstOpcode(m.instructions, Opcodes.ALOAD);
				AbstractInsnNode loc2 = ReikaASMHelper.getFirstOpcode(m.instructions, Opcodes.PUTFIELD);
				ReikaASMHelper.deleteFrom(cn, m.instructions, loc1, loc2);
				ReikaASMHelper.log("Successfully applied leaf graphics patch!");
				ReikaASMHelper.activeMod = null;
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				cn.accept(writer);
				cn.check(cn.version);
				return writer.toByteArray();
			}
			catch (NoSuchASMMethodException e) { //serverside
				//e.printStackTrace();
			}
		}
		return bytes;
	}

	private void patchLeafClass(ClassNode cn) {
		InsnList li = new InsnList();
		String sig = "(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V";

		li.add(new VarInsnNode(Opcodes.ALOAD, 1));
		li.add(new VarInsnNode(Opcodes.ILOAD, 2));
		li.add(new VarInsnNode(Opcodes.ILOAD, 3));
		li.add(new VarInsnNode(Opcodes.ILOAD, 4));
		li.add(new VarInsnNode(Opcodes.ALOAD, 5));
		li.add(new VarInsnNode(Opcodes.ALOAD, 6));
		li.add(new VarInsnNode(Opcodes.ALOAD, 7));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/TreeClimbing/TreeHooks", "getLeafAABB", sig, false));
		li.add(new InsnNode(Opcodes.RETURN));

		ReikaASMHelper.addMethod(cn, li, FMLForgePlugin.RUNTIME_DEOBF ? "func_149743_a" : "addCollisionBoxesToList", sig, Modifier.PUBLIC);
		ReikaASMHelper.log("Successfully applied leaf AABB patch!");
	}

	private void patchLogClass(ClassNode cn) {
		InsnList li = new InsnList();
		String sig = "(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/entity/EntityLivingBase;)Z";

		li.add(new VarInsnNode(Opcodes.ALOAD, 1));
		li.add(new VarInsnNode(Opcodes.ILOAD, 2));
		li.add(new VarInsnNode(Opcodes.ILOAD, 3));
		li.add(new VarInsnNode(Opcodes.ILOAD, 4));
		li.add(new VarInsnNode(Opcodes.ALOAD, 5));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/TreeClimbing/TreeHooks", "isLadder", sig, false));
		li.add(new InsnNode(Opcodes.IRETURN));

		ReikaASMHelper.addMethod(cn, li, "isLadder", sig, Modifier.PUBLIC);
		ReikaASMHelper.log("Successfully applied part 1!");

		/*
		li = new InsnList();
		sig = "(Lnet/minecraft/world/World;III)Lnet/minecraft/util/AxisAlignedBB;";

		li.add(new VarInsnNode(Opcodes.ALOAD, 1));
		li.add(new VarInsnNode(Opcodes.ILOAD, 2));
		li.add(new VarInsnNode(Opcodes.ILOAD, 3));
		li.add(new VarInsnNode(Opcodes.ILOAD, 4));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/TreeClimbing/TreeHooks", "getLogAABB", sig, false));
		li.add(new InsnNode(Opcodes.ARETURN));

		ReikaASMHelper.addMethod(cn, li, FMLForgePlugin.RUNTIME_DEOBF ? "func_149668_a" : "getCollisionBoundingBoxFromPool", sig, Modifier.PUBLIC);
		ReikaASMHelper.log("Successfully applied part 2!");
		 */

		li = new InsnList();
		sig = "(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V";

		li.add(new VarInsnNode(Opcodes.ALOAD, 1));
		li.add(new VarInsnNode(Opcodes.ILOAD, 2));
		li.add(new VarInsnNode(Opcodes.ILOAD, 3));
		li.add(new VarInsnNode(Opcodes.ILOAD, 4));
		li.add(new VarInsnNode(Opcodes.ALOAD, 5));
		li.add(new VarInsnNode(Opcodes.ALOAD, 6));
		li.add(new VarInsnNode(Opcodes.ALOAD, 7));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/TreeClimbing/TreeHooks", "getLogAABB2", sig, false));
		li.add(new InsnNode(Opcodes.RETURN));

		ReikaASMHelper.addMethod(cn, li, FMLForgePlugin.RUNTIME_DEOBF ? "func_149743_a" : "addCollisionBoxesToList", sig, Modifier.PUBLIC);
		ReikaASMHelper.log("Successfully applied part 2!");
	}
}
