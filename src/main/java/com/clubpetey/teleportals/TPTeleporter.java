package com.clubpetey.teleportals;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TPTeleporter extends Teleporter {
    private final WorldServer world;
    private final PortalDef data;

    public TPTeleporter(WorldServer worldIn, PortalDef data)
    {
        super(worldIn);

        this.world = worldIn;
        this.data = data;
    }

    @Override
    public boolean makePortal(Entity entityIn)
    {
        return true;
    }

    @Override
    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw)
    {
        Vec3d pos = this.data.getDestination(entityIn, entityIn.getEntityWorld());
        entityIn.setLocationAndAngles(pos.x, pos.y, pos.z, entityIn.rotationYaw, entityIn.rotationPitch);
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime)
    {
        // NO-OP
    }

    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw) {

    }
}
