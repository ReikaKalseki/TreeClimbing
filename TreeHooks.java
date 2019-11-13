package Reika.TreeClimbing;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.BlockStruct.TreeReader;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockBox;
import Reika.DragonAPI.Interfaces.Registry.TreeType;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaTreeHelper;
import Reika.DragonAPI.ModRegistry.ModWoodList;


public class TreeHooks {

	public static void getLeafAABB(World world, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> li, Entity e) {
		//ModularLogger.instance.log("leafbox", "Checking Leaf AABB @ "+new Coordinate(x, y, z)+" for "+e);
		if (world.getBlock(x, y, z).isLeaves(world, x, y, z) && e instanceof EntityPlayer) {
			//ModularLogger.instance.log("leafboxcheck", "Check 1");
			if (e.posY < y+0.99 || e.isSneaking()) {
				//ModularLogger.instance.log("leafboxcheck", "Check 2");
				if (ReikaTreeHelper.isNaturalLeaf(world, x, y, z)) {
					//ModularLogger.instance.log("leafret", "return empty");
					return;
				}
			}
		}

		AxisAlignedBB bb = world.getBlock(x, y, z).getCollisionBoundingBoxFromPool(world, x, y, z);
		if (bb != null && mask.intersectsWith(bb)) {
			li.add(bb);
		}
	}

	public static AxisAlignedBB getLogAABB(World world, int x, int y, int z) {
		double s = 0.03125;
		return ReikaAABBHelper.getBlockAABB(x, y, z).contract(s, 0, s);
	}

	public static void getLogAABB2(World world, int x, int y, int z, AxisAlignedBB mask, List li, Entity e) {
		AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(x, y, z);
		if (e instanceof EntityPlayer) {
			double s = 0.03125;
			box = box.contract(s, 0, s);
		}
		if (mask.intersectsWith(box))
			li.add(box);
	}

	public static boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		if (entity.isSneaking())
			return false;
		if (!(entity instanceof EntityPlayer))
			return false;
		Block b = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		TreeType tree = ReikaTreeHelper.getTree(b, meta);
		if (tree == null)
			tree = ModWoodList.getModWood(b, meta);
		if (tree != null) {
			TreeReader reader = new TreeReader();
			reader.setTree(tree);
			reader.bounds = BlockBox.block(x, y, z).expand(30, 255, 30); //this should roughly fit any tree
			reader.bounds.clampTo(tree.getTypicalMaximumSize().offset(x, y, z));
			reader.setStopIfValid();
			reader.addTree(world, x, y, z);
			return reader.isValidTree();
		}
		return false;
	}
}
