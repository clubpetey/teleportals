package com.clubpetey.teleportals;

import com.clubpetey.teleportals.ServerTeleportMessage.ServerTeleportHandler;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("Teleportals");

	public static void init() {
		INSTANCE.registerMessage(ServerTeleportHandler.class, ServerTeleportMessage.class, 1, Side.SERVER);
	}
}
