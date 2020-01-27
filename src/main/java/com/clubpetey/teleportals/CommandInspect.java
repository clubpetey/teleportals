package com.clubpetey.teleportals;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

public class CommandInspect extends CommandBase {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "tpi";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/tpi hand|look";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
		NBTTagCompound tag = null;
		if (args.length > 0 && "hand".equals(args[0])) {
			ItemStack item = player.getHeldItemMainhand();
			sender.sendMessage(new TextComponentString("Item: " + item.getItem().getRegistryName()+ "@" + item.getItemDamage()));
			if (item.hasTagCompound()) tag = item.getTagCompound();
		} else {
			/* this is client-only for some dumb reason, so we need to re-write it...
			RayTraceResult result = player.rayTrace(10, 1.0f);
			*/
			
			float f = 10.0f; //distance
			Vec3d look = player.getLookVec();
			Vec3d start = player.getPositionEyes(1.0f);
			Vec3d end = start.addVector(look.x * f, look.y * f, look.z * f);
			RayTraceResult result = player.getEntityWorld().rayTraceBlocks(start, end, false, false, true);

			if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
				sender.sendMessage(new TextComponentString("Block: " + player.getEntityWorld().getBlockState(result.getBlockPos()).toString()));
				TileEntity te = player.getEntityWorld().getTileEntity(result.getBlockPos());
				if (te != null) tag = te.serializeNBT();
			}			
		}
		
		if (tag == null) sender.sendMessage(new TextComponentString("No NBTTag data found"));
		else sender.sendMessage(new TextComponentString(tag.toString()));
	}


}
