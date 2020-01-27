package com.clubpetey.teleportals;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = Teleportals.MODID, name = Teleportals.NAME, version = Teleportals.VERSION)
public class Teleportals
{
    public static final String MODID = "teleportals";
    public static final String NAME = "Teleportals";
    public static final String VERSION = "1.0";
    
    public static Logger logger;
    
    public static final List<PortalDef> BLOCK_LIST = new ArrayList<PortalDef>(); 
    public static final Map<String, PortalDef> POS_MAP = new HashMap<String, PortalDef>(); 
    public static final List<PortalDef> CLICK_LIST = new ArrayList<PortalDef>(); 

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        new ConfigManager(event.getModConfigurationDirectory().getAbsolutePath(), event.getSuggestedConfigurationFile());
        PacketHandler.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigManager.reload();
    }

    @EventHandler
    public void start(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTeleport());
        event.registerServerCommand(new CommandInspect());
    }
    
	@SubscribeEvent(priority = HIGH)
    public void onLeftClickBlock (PlayerInteractEvent.LeftClickBlock event) {
		
    }  

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    	if(event.phase == TickEvent.Phase.START && event.side == Side.SERVER) {
    		if (event.player.ticksExisted % 5 != 1) return; //every 0.25 seconds...
    		
    		//check the position list
    		Vec3i pos = new Vec3i(event.player.posX, event.player.posY, event.player.posZ);
    		PortalDef portal = POS_MAP.get(pos.toString());
    		if (portal == null) {	    		
	    		//check the block list (below feet, feet, head)
	    		World world = event.player.world;
	    		BlockPos feetPos = new BlockPos(event.player.posX, event.player.posY, event.player.posZ);
	    		
	    		for (PortalDef pd : BLOCK_LIST) {
	    			if (!pd.dimMatches(event.player.dimension)) continue;

	    			if (pd.blockMatches(feetPos.down(), world)) portal = pd;
	    			if (pd.blockMatches(feetPos, world)) portal = pd;
	    			if (pd.blockMatches(feetPos.up(), world)) portal = pd;
		    		if (portal != null) break;
	    		}
    		}
    		
    		if (portal != null) portal.telelportEntity(event.player, event.player.getServer());
    	}
    }
    
    public static PortalDef checkRightClick(ItemStack item, BlockPos bpos, World world, int currentDim) {
    	PortalDef portal = null;
		for (PortalDef pd : CLICK_LIST) {
			boolean dimMatch = pd.dimMatches(currentDim);
			boolean blockMatch = pd.blockMatches(bpos, world);
			boolean itemMatch = pd.itemMatches(item);
			if (dimMatch && blockMatch && itemMatch) portal = pd;
    		if (portal != null) break;
		}
    	return portal;
    }
    
	@SubscribeEvent
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getSide() == Side.SERVER) return;
		PacketHandler.INSTANCE.sendToServer(new ServerTeleportMessage(event.getPos()));
	}
	
	@SubscribeEvent
	public void onRightClickItem(RightClickItem event) {
		if (event.getSide() == Side.SERVER) return;
		PacketHandler.INSTANCE.sendToServer(new ServerTeleportMessage());
	}
}
