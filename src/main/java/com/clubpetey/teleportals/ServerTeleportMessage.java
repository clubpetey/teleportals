package com.clubpetey.teleportals;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerTeleportMessage implements IMessage {
	BlockPos pos;
	
	public ServerTeleportMessage() {
	}
	
	public ServerTeleportMessage(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		if (buf.readBoolean()) {
			int x = buf.readInt();
			int y = buf.readInt();
			int z = buf.readInt();
			pos = new BlockPos(x, y, z);
		} else pos = null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		buf.writeBoolean(pos != null);
		if (pos != null) {
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
		}
	}
	
	public static class ServerTeleportHandler implements IMessageHandler<ServerTeleportMessage, IMessage> {

		@Override
		public IMessage onMessage(final ServerTeleportMessage message, final MessageContext ctx) {
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.getEntityWorld();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
					ItemStack item = serverPlayer.getHeldItem(EnumHand.MAIN_HAND);
					PortalDef portal = Teleportals.checkRightClick(item, message.pos, serverPlayer.getEntityWorld(), serverPlayer.dimension);
					if (portal != null) portal.telelportEntity(serverPlayer, serverPlayer.getServer());
				}
			});
			return null;
		}
	}	

}
