package com.clubpetey.teleportals;

import java.util.ArrayList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;

public class CommandTeleport extends CommandBase {
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "tpp";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/tpt <player>|@p|@a <dim>|* <x> <y> <z> <base?>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 5)
			throw new WrongUsageException(this.getUsage(sender));
		
		EntityPlayer entity = (EntityPlayer) sender.getCommandSenderEntity();
		ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>();

		if (args[0] == "@p") {
			players.add(entity);
		} else if (args[0] == "@a") {
			players.addAll(server.getPlayerList().getPlayers());
		} else {
			players.add(server.getPlayerList().getPlayerByUsername(args[0]));
		}
		
		int defaultDim = entity.dimension;
		
		PortalDef pd = new PortalDef(Utils.parseInt(args[1], defaultDim));
		pd.parsePos(args[2], args[3], args[4]);		

		if (args.length > 5) pd.base = Blocks.STONE;

		for (EntityPlayer p : players)
			pd.telelportEntity(p, server);
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
}
