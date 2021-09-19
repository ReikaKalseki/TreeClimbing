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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraftforge.common.ForgeModContainer;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.IO.SimpleConfig;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod( modid = "TreeClimbing", name="TreeClimbing", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class TreeClimbing extends DragonAPIMod {

	@Instance("TreeClimbing")
	public static TreeClimbing instance = new TreeClimbing();

	public static ModLogger logger;

	public static final SimpleConfig config = new SimpleConfig(instance);

	public static boolean leafSneak;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyInstallation();

		logger = new ModLogger(instance, false);
		if (DragonOptions.FILELOG.getState())
			logger.setOutput("**_Loading_Log.log");

		config.loadSubfolderedConfigFile(evt);
		config.loadDataFromFile(evt);
		config.finishReading();

		leafSneak = config.getBoolean("Options", "Sneaking falls through leaves", true);

		this.basicSetup(evt);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);

		this.finishTiming();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);
		ForgeModContainer.fullBoundingBoxLadders = true;
		try {
			Field f = BlockLeavesBase.class.getDeclaredField("field_150121_P");
			f.setAccessible(true);
			for (Object o : Block.blockRegistry.getKeys()) {
				Block b = (Block)Block.blockRegistry.getObject(o);
				if (b instanceof BlockLeavesBase) {
					f.set(b, true);
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.finishTiming();
	}

	@Override
	public String getDisplayName() {
		return "TreeClimbing";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public URL getBugSite() {
		return DragonAPICore.getReikaGithubPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

	@Override
	public File getConfigFolder() {
		return config.getConfigFolder();
	}

}
