package com.clubpetey.teleportals;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class PortalDef {
	public static final int R_CLICK = 0;
	public static final int BLOCK = 1;
	public static final int POS = 2;
	
	public int dim;
	public Vec3i pos = null;
	public int trigger = R_CLICK;
	public Vec3i triggerPos = null;
	public Item triggerItem;
	public Block triggerBlock;
	public Block base;
	public String[] itemMatch;
	public String[] blockMatch;
	public boolean relativePos = false;;
	public int moveFacing;
	public boolean currentDim = false;
	public int[] fromDim;
	public boolean itemMatchAny = false;
	public boolean blockMatchAny = false;
	
	
	public PortalDef() {
		this(0);
		currentDim = true;
	}
	
	public PortalDef(int dim) {
		this.dim = dim;
	}
	
	private String listMatches(String[] matches) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (String m : matches) 
			buf.append(m).append(", ");
		buf.delete(buf.length()-2, buf.length()).append("]");
		return buf.toString();
	}
	
	private String listDims(int[] dims) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int m : dims) 
			buf.append(m).append(", ");
		buf.delete(buf.length()-2, buf.length()).append("]");
		return buf.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (currentDim)
			buf.append("Portal in current dimension");
		else 
			buf.append("Portal to dim ").append(dim);
		if (fromDim != null) buf.append(" from dimensions ").append(listDims(fromDim)); 
		buf.append(", triggered by ");
		switch (trigger) {
		case R_CLICK: {
			buf.append("right click ");
			if (triggerItem != null) buf.append("with ").append(triggerItem.getRegistryName()); 
			if (triggerBlock != null) buf.append("on block ").append(triggerBlock.getRegistryName());
			break;
		}
		case BLOCK: buf.append("crossing block ").append(triggerBlock.getRegistryName()); break;
		case POS: buf.append("location ").append(triggerPos.toString()); break;
		}
		
		if (blockMatch != null ) buf.append(" block tag matches ").append(listMatches(blockMatch));
		if (itemMatch != null ) buf.append(", item tag matches ").append(listMatches(itemMatch));
		buf.append(".");
		
		if (relativePos) {
			if (moveFacing != 0) buf.append(" Move ").append(moveFacing).append(" blocks forward.");
			else buf.append(" Move ").append(pos.toString()).append(" blocks.");
		} else {
			if (base != null) buf.append(" Create base of ").append(base.toString());
			else buf.append(" Landing");
			if (pos != null) buf.append(" at ").append(pos.toString());
			else buf.append(" at spawn point.");			
		}
		
		return buf.toString();
	}
	
	public Vec3i parsePos(String sx, String sy, String sz) {
		int x = Utils.parseInt(sx, Integer.MAX_VALUE);
		int y = Utils.parseInt(sy, 0);
		int z = Utils.parseInt(sz, Integer.MAX_VALUE);
		if (x == Integer.MAX_VALUE || z == Integer.MAX_VALUE) return null;
		return new Vec3i(x, y, z);
	}
	
    public Vec3d getDestination(Entity entity, World world) {
        WorldBorder border = world.getWorldBorder();
    	BlockPos start = world.getSpawnPoint();
    	Vec3i newPos = null;
    	
    	if (relativePos) {
        	if (entity != null) start = entity.getPosition();
    		if (moveFacing > 0) {
    			Vec3d look = entity.getLookVec();
    			newPos = new Vec3i(start.getX() + look.x * moveFacing, start.getY() + look.y * moveFacing, start.getZ() + look.z * moveFacing);    	
    		} else {
    			newPos = new Vec3i(start.getX() + pos.getX(), start.getY() + pos.getY(), start.getZ() + pos.getZ());    			
    		}
        } else if (pos == null) {        	
        	newPos = new Vec3i(start.getX(), start.getY(), start.getZ());
        } else {
        	newPos = pos;
        }

        double x = MathHelper.clamp((double)newPos.getX(), border.minX() + 2, border.maxX() - 2);
        double z = MathHelper.clamp((double)newPos.getZ(), border.minZ() + 2, border.maxZ() - 2);
        double y = MathHelper.clamp((double)newPos.getY(), 1, world.getHeight() - 4);
        
        return new Vec3d(x+0.5, y+0.5, z+0.5);
    }	
    
    private void setBlocks(World world, double x, double y, double z, boolean fullBase) {
    	BlockPos pos = new BlockPos(x, y, z);
    	world.setBlockState(pos.add(-1, y, 1), base.getDefaultState());
    	world.setBlockState(pos.add(0, y, 1), fullBase ? base.getDefaultState() : Blocks.AIR.getDefaultState());
    	world.setBlockState(pos.add(1, y, 1), base.getDefaultState());

    	world.setBlockState(pos.add(-1, y, 0), fullBase ? base.getDefaultState() : Blocks.AIR.getDefaultState());
    	world.setBlockState(pos.add(0, y, 0), fullBase ? base.getDefaultState() : Blocks.AIR.getDefaultState());
    	world.setBlockState(pos.add(1, y, 0), fullBase ? base.getDefaultState() : Blocks.AIR.getDefaultState());

    	world.setBlockState(pos.add(-1, y, -1), base.getDefaultState());
    	world.setBlockState(pos.add(0, y, -1), fullBase ? base.getDefaultState() : Blocks.AIR.getDefaultState());
    	world.setBlockState(pos.add(1, y, -1), base.getDefaultState());
}
    
    public double fixYvalue(World world, Vec3d pos) {
    	//if y = 0, then start at sea level
    	// and go up, then down, to find 2 spaces to drop the player.
    	
    	double startY = pos.y;
    	if (startY == 0 || base == null) { 
    		startY = world.getSeaLevel();
			BlockPos blockpos;
	        for (blockpos = new BlockPos(pos.x, startY, pos.z); (!world.isAirBlock(blockpos) || !world.isAirBlock(blockpos.up())) && !world.isOutsideBuildHeight(blockpos); blockpos = blockpos.up()) {
	            ;
	        }
	        if (!world.isAirBlock(blockpos.down()) && !world.isOutsideBuildHeight(blockpos)) startY =  blockpos.getY();
	        else {
		        
		    	//try down
		        for (blockpos = new BlockPos(pos.x, startY, pos.z); (world.isAirBlock(blockpos.down()) || !world.isAirBlock(blockpos) || !world.isAirBlock(blockpos.up())) && !world.isOutsideBuildHeight(blockpos.down()); blockpos = blockpos.down()) {
		            ;
		        }            
		        if (!world.isOutsideBuildHeight(blockpos.down())) startY = blockpos.getY();
		        else startY = world.getSeaLevel();
	        }
    	}
    	
    	//if base specified, create 3x3 of base block
        if (base != null) {
	    	setBlocks(world, pos.x, startY-1, pos.z, true);
	    	setBlocks(world, pos.x, startY,   pos.z, false);
	    	setBlocks(world, pos.x, startY+1, pos.z, false);
	    	setBlocks(world, pos.x, startY+2, pos.z, false);
	    	setBlocks(world, pos.x, startY+3, pos.z, true);
        }
    	return startY;
    }
    
    public Entity telelportEntity(Entity entity, MinecraftServer server) {
        entity.dismountRidingEntity();
        entity.removePassengers();
        World world = entity.getEntityWorld();
        
        if (world.provider.getDimension() != dim && !currentDim) {
            return this.teleportEntityToDimension(entity, server);
        } else {
        	//check bounds
            Vec3d pos = getDestination(entity, entity.getEntityWorld());

            // Load the chunk first
            entity.getEntityWorld().getChunkFromBlockCoords(new BlockPos(pos));
            
            //ensure we can land somewhere
            if (!relativePos)
            	pos = new Vec3d(pos.x, fixYvalue(world, pos), pos.z);

            //boom!
            entity.setLocationAndAngles(pos.x, pos.y, pos.z, entity.rotationYaw, entity.rotationPitch);
            entity.setPositionAndUpdate(pos.x, pos.y, pos.z);
            return entity;        
        }

    }
    
    private Entity teleportEntityToDimension(Entity entity, MinecraftServer server) {
        WorldServer worldDst = server.getWorld(dim);

        if (worldDst == null) {
            Teleportals.logger.error("Cannot load world: {}", dim);
            return null;
        }

        Vec3d newPos = getDestination(null, worldDst);

        // Load the chunk first
        entity.getEntityWorld().getChunkFromBlockCoords(new BlockPos(newPos));
        
        //ensure we can land somewhere
        if (!relativePos)        
        	newPos = new Vec3d(newPos.x, fixYvalue(worldDst, newPos), newPos.z);

        double x = newPos.x;
        double y = newPos.y;
        double z = newPos.z;

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            World worldOld = player.getEntityWorld();
            TPTeleporter teleporter = new TPTeleporter(worldDst, this);

            player.setLocationAndAngles(x, y, z,player.rotationYaw, player.rotationPitch);
            server.getPlayerList().transferPlayerToDimension(player, dim, teleporter);

            // See PlayerList#transferEntityToWorld()
            if (worldOld.provider.getDimension() == 1)
                worldDst.spawnEntity(player);

            // Teleporting FROM The End
            if (worldOld.provider instanceof WorldProviderEnd)
                this.removeBossBar(player, (WorldProviderEnd) worldOld.provider);

            player.setPositionAndUpdate(x, y, z);
            worldDst.updateEntityWithOptionalForce(player, false);
            player.addExperience(0);
            player.setPlayerHealthUpdated();
        }
        else
        {
            /* Future -- teleport other entities
        	WorldServer worldSrc = (WorldServer) entity.getEntityWorld();

            worldSrc.removeEntity(entity);
            entity.isDead = false;
            worldSrc.updateEntityWithOptionalForce(entity, false);

            Entity entityNew = EntityList.newEntity(entity.getClass(), worldDst);

            if (entityNew != null)
            {
                this.copyDataFromOld(entityNew, entity);
                entityNew.setLocationAndAngles(x, y, z, data.getYaw(), data.getPitch());

                boolean flag = entityNew.forceSpawn;
                entityNew.forceSpawn = true;
                worldDst.spawnEntity(entityNew);
                entityNew.forceSpawn = flag;

                worldDst.updateEntityWithOptionalForce(entityNew, false);
                entity.isDead = true;

                worldSrc.resetUpdateEntityTick();
                worldDst.resetUpdateEntityTick();
            }

            entity = entityNew;
            */
        }

        return entity;
    }
    
    private void removeBossBar(EntityPlayerMP player, WorldProviderEnd provider) {
        DragonFightManager manager = provider.getDragonFightManager();
        if (manager != null)  {
            try {
                BossInfoServer bossInfo = ObfuscationReflectionHelper.getPrivateValue(DragonFightManager.class, manager, "field_186109_c"); // bossInfo
                if (bossInfo != null) {
                    bossInfo.removePlayer(player);
                }
            }
            catch (Exception e) { } //munch
        }
    }
    
	public boolean dimMatches(int dim) {
		if (fromDim == null) return true;
		for (int m : fromDim) 
			if (m == dim) return true;
		return false;
	}   
	
	public boolean itemMatches(ItemStack stack) {
		if (triggerItem == null && stack.isEmpty()) return true;
		if (triggerItem == null) return false;

		if (triggerItem != stack.getItem()) return false;

		if (itemMatch != null) {
			NBTTagCompound tag = stack.getTagCompound();
			String prefix = stack.getItem().getRegistryName()+ "@" + stack.getItemDamage();
			String s = (tag == null) ? prefix : prefix + tag.toString();
			for (String m : itemMatch) {
				boolean match = s.matches(m);
				if (!match && !itemMatchAny) return false;
				if (match && itemMatchAny) return true;
			}		
			return !itemMatchAny;
		}
		return true;		
	}	
   
	public boolean blockMatches(BlockPos bpos, World world) {
		if (triggerBlock == null || bpos == null) return true;
		IBlockState bs = world.getBlockState(bpos);

		if (triggerBlock != bs.getBlock()) return false;
		
		if (blockMatch != null) {
			TileEntity te = world.getTileEntity(bpos);
			NBTTagCompound tag = (te == null) ? null : te.serializeNBT();
			String s = (tag == null) ? bs.toString() : bs.toString() + tag.toString();
			for (String m : blockMatch) {
				boolean match = s.matches(m);
				if (!match && !blockMatchAny) return false;
				if (match && blockMatchAny) return true;
			}		
			return !blockMatchAny;
		}
		
		return true;		
	}	
   
    private String checkRegex(String r) {
    	if (r.charAt(0) == '/') return r.substring(1);
    	else return ".*" + r + ".*";
    }
    
	public boolean parse(String cmd, String[] args) {
		if ("match".equals(cmd))
			if (triggerItem != null) cmd = "itemmatch";
			else cmd = "blockmatch";
		
		switch (cmd) {
		case "trigger" :
			switch (args[0]) {
			case "click": 
				trigger = R_CLICK;
			break;
			case "block": 
				trigger = BLOCK;
			break;
			case "pos": 
				trigger = POS;
			break;
			default: 
				Teleportals.logger.error("Invalid trigger: {}", args[0]);
				return false;
			}
		break;
		case "item" :
			triggerItem = Item.REGISTRY.getObject(new ResourceLocation(args[0]));
			if (triggerItem == null) {
				Teleportals.logger.error("Could not find item: {}", args[0]);
				return false;
			}
		break;
		case "moveto" :
			pos = parsePos(args[0], args[1], args[2]);
			relativePos = false;
			if (pos == null) {
				Teleportals.logger.error("Bad position vector: {}", args[0]);
				return false;				
			}
		break;
		case "move" :
			relativePos = true;
			if (args.length == 1) {
				moveFacing = Utils.parseInt(args[0], 0);
				if (moveFacing == 0) {
					Teleportals.logger.error("Bad relative move: {}", args[0]);
					return false;									
				}
			} else {
				pos = parsePos(args[0], args[1], args[2]);
				if (pos == null) {
					Teleportals.logger.error("Bad relative vector: {}", args[0]);
					return false;				
				}
			}
		break;
		case "base" :
			base = Block.REGISTRY.getObject(new ResourceLocation(args[0]));
			if (base == null) {
				Teleportals.logger.error("Could not find block: {}", args[0]);
				return false;
			}			
		break;
		case "block" :
			triggerBlock = Block.REGISTRY.getObject(new ResourceLocation(args[0]));
			if (triggerBlock == null) {
				Teleportals.logger.error("Could not find block: {}", args[0]);
				return false;
			}
		break;
		case "pos" :
			pos = parsePos(args[0], args[1], args[2]);
			if (pos == null) {
				Teleportals.logger.error("Bad Trigger position vector: {}", args[0]);
				return false;				
			}			
		break;
		case "itemmatch" :
			itemMatchAny = "any".equalsIgnoreCase(args[0]);
			itemMatch = new String[args.length-1];
			for (int i=1;i<args.length;i++)
				itemMatch[i-1] = checkRegex(args[i]);
		break;
		case "blockmatch" :
			blockMatchAny = "any".equalsIgnoreCase(args[0]);
			blockMatch = new String[args.length-1];
			for (int i=1;i<args.length;i++)
				blockMatch[i-1] = checkRegex(args[i]);
		break;
		case "from" :
			fromDim = new int[args.length];
			for (int i=0;i<args.length;i++) {
				int v = Utils.parseInt(args[i], Integer.MAX_VALUE);
				if (v != Integer.MAX_VALUE) fromDim[i] = v;
				else {
					Teleportals.logger.error("Bad from dimension: {}", args[i]);
					return false;									
				}
			}			
		break;
		default:
			return false;
		}
		return true;
	}
	
}
