package net.gmx.nosefish.fishyshield.listeners;
import java.util.LinkedList;
import java.util.List;

import net.gmx.nosefish.fishylib.properties.Properties;

import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.TNTPrimed;
import net.canarymod.api.entity.WitherSkull;
import net.canarymod.api.entity.living.monster.Creeper;
import net.canarymod.api.entity.vehicle.TNTMinecart;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.ExplosionHook;
import net.canarymod.plugin.PluginListener;
import net.gmx.nosefish.fishyshield.FishyShield;
import net.gmx.nosefish.fishyshield.properties.Key;


/**
 * A <code>PluginListener</code> that protects blocks from explosion damage.
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class ExplosionListener implements PluginListener {
	private Properties properties;

	/**
	 * Constructor
	 * 
	 * @param fishyShield
	 *            the plugin instantiating this <code>PluginListener</code>
	 */
	public ExplosionListener(FishyShield fishyShield) {
		properties = fishyShield.getProperties();
	}

	
	@HookHandler
	public void onExplosion(ExplosionHook hook) {
		Block block = hook.getBlock();
		World world = block.getWorld();
		if (!properties.getBoolean(world, Key.EXPLOSION_ENABLE)) {
			return;
		}
		Entity entity = hook.getEntity();
		List<Block> blocksaffected = hook.getAffectedBlocks();
		ExplosionType explosionType = ExplosionType.getExplosionType(entity);
		FishyShield.logger.debug("Explosion: " + explosionType);
		switch (explosionType) {
		case TNT:
			onTNTExplosion(world, entity, blocksaffected);
			break;
		case CREEPER:
			onCreeperExplosion(world, blocksaffected);
			break;
		case GHAST:
			onGhastFireballExplosion(world, blocksaffected);
			break;
		case WITHER:
			onWitherSkullExplosion(world, blocksaffected);
			break;
		default:
			hook.setCanceled(); // block unknown explosions just to be safe.
		}
		// always allow the explosion after we've handled it, to keep the other
		// effects,
		// like fire, or damage to entities.
	}
	


	/**
	 * Handles TNT damage.
	 * <p>
	 * Depending on configuration, even if block damage is disabled, it may
	 * still set off nearby TNT blocks.
	 * 
	 * @param world
	 *            the world in which the TNT is exploding
	 * @param blocksaffected
	 *            the blocks that will be affected by the explosion. Can be
	 *            modified by this method.
	 */
	private void onTNTExplosion(World world, Entity entity,
			List<Block> blocksaffected) {
		if (!properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
			if (properties.getBoolean(world, Key.EXPLOSION_CASCADETNT)) {
				// allow cascading TNT explosions
				List<Block> tntBlocks = new LinkedList<Block>();
				for (Block affectedBlock : blocksaffected) {
					if (affectedBlock.getType().equals(BlockType.Tnt)) {
						tntBlocks.add(affectedBlock);
					}
				}
				blocksaffected.clear();
				blocksaffected.addAll(tntBlocks);
			} else {
				// prevent all block damage
				blocksaffected.clear();
			}
		}
	}

	/**
	 * Handles creeper damage.
	 * 
	 * @param world
	 *            the world in which the creeper is exploding
	 * @param blocksaffected
	 *            the blocks that will be affected by the explosion. Can be
	 *            modified by this method.
	 */
	@SuppressWarnings({ "rawtypes" })
	private void onCreeperExplosion(World world, List blocksaffected) {
		if (!properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
			blocksaffected.clear();
		}
	}

	/**
	 * Handles ghast fireball damage.
	 * <p>
	 * Even if block damage is disabled, the fireball may still start fires.
	 * 
	 * @param world
	 *            the world in which the ghast fireball is exploding
	 * @param blocksaffected
	 *            the blocks that will be affected by the explosion. Can be
	 *            modified by this method.
	 */
	private void onGhastFireballExplosion(World world, List<Block> blocksaffected) {
		if (!properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
			// Air blocks stay affected to allow fireballs to start fires and
			// let FS_IgniteListener handle that.
			List<Block> airBlocks = new LinkedList<Block>();
			for (Block affectedBlock : blocksaffected) {
					if (affectedBlock.getType().equals(BlockType.Air)) {
						airBlocks.add(affectedBlock);
					}
			}
			blocksaffected.clear();
			blocksaffected.addAll(airBlocks);
		}
	}

	/**
	 * Handles wither skull damage.
	 * 
	 * @param world
	 *            the world in which the wither skull is exploding
	 * @param blocksaffected
	 *            the blocks that will be affected by the explosion. Can be
	 *            modified by this method.
	 */
	@SuppressWarnings({ "rawtypes" })
	private void onWitherSkullExplosion(World world, List blocksaffected) {
		if (!properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
			blocksaffected.clear();
		}
	}

	private static enum ExplosionType {
		UNKNOWN, TNT, CREEPER, GHAST, WITHER;
		
		public static ExplosionType getExplosionType(Entity entity) {
			if (entity == null) return GHAST;
			if (entity instanceof Creeper) return CREEPER;
			if (entity instanceof TNTPrimed) return TNT;
			if (entity instanceof TNTMinecart) return TNT;
			if (entity instanceof WitherSkull) return WITHER;
			return UNKNOWN;
		}
	}

}
