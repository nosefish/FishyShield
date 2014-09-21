package net.gmx.nosefish.fishyshield.listeners;
import java.util.HashSet;
import java.util.Set;

import net.gmx.nosefish.fishylib.properties.Properties;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.chat.Colors;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.IgnitionHook;
import net.canarymod.hook.world.PortalCreateHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.ServerTaskManager;
import net.gmx.nosefish.fishyshield.FishyShield;
import net.gmx.nosefish.fishyshield.properties.Key;


/**
 * A <code>PluginListener</code> that protects blocks from fire damage.
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class FireListener implements PluginListener {

	private final Properties properties;

	private final Set<Location> portalAllowed;
	private final FishyShield plugin;

	/**
	 * Constructor
	 * 
	 * @param plugin
	 *            the plugin instantiating this <code>PluginListener</code>
	 */
	public FireListener(FishyShield plugin) {
		this.plugin = plugin;
		this.properties = plugin.getProperties();
		this.portalAllowed = new HashSet<>(16, 0.9F);
	}

	@HookHandler
	public void onPortalCreate(PortalCreateHook hook) {
		Block[][]blocks = hook.getBlockSet();
		// allow only players to create portals. Blocks that have been lit
		// by players with permission are in portalAllowed
		for (Block[] line : blocks) {
			for (Block block : line) {
				if (portalAllowed.contains(block.getLocation())) {
					FishyShield.logger.debug("Portal creation allowed");
					return;
				}
			}
		}
		FishyShield.logger.debug("Blocked portal creation");
		hook.setCanceled();
	}

	@HookHandler
	public void onIgnite(IgnitionHook hook) {
		Block block = hook.getBlock();
		if (!properties.getBoolean(block.getWorld(), Key.IGNITE_ENABLE)) {
			return;
		}
		boolean deny;
		switch (hook.getCause()) {
		case LAVA:
			deny = blockLavaIgnite(block);
			break;
		case FLINT_AND_STEEL:
			deny = blockFlintAndSteelIgnite(block, hook.getPlayer());
			break;
		case FIRE_SPREAD:
			deny = blockFireSpreadIgnite(block);
			break;
		case BURNT:
			deny = blockFireDestruction(block);
			break;
		case LIGHTNING_STRIKE:
			deny = blockLightningIgnite(block);
			break;
		case FIREBALL_CLICK:
			deny = blockFireChargeUseIgnite(block, hook.getPlayer());
			break;
		case FIREBALL_HIT:
			deny = blockFireballIgnite(block);
			break;
		default:
			FishyShield.logger.debug("Unknown fire source blocked: " + hook.getCause());
			deny = true; // whatever other fire source there may be in the future, block it
			break;
		}
		if (deny) {
			hook.setCanceled();
		}
	}

	/**
	 * Handles blocks being set on fire by lava.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockLavaIgnite(Block block) {
		return !properties.getBoolean(block.getWorld(), Key.IGNITE_LAVA)
				|| fireIsOnFireproofBlock(block);
	}

	/**
	 * Handles blocks being set on fire by a player using flint&steel.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockFlintAndSteelIgnite(Block block, Player player) {
		World world = block.getWorld();
		boolean denyPermission;
		boolean denyFireproof;
		FishyShield.logger.debug(player.getName() + " used flint&steel");
		if (properties.getBoolean(world, Key.IGNITE_FLINTANDSTEEL)) {
			// flint&steel is allowed for everyone
			if (fireIsOnFireproofBlock(block)) {
				if (properties.hasPermission(Key.IGNITE_FIREPROOF_PERM, player,
						Integer.valueOf(block.getRelative(0, -1, 0).getTypeId()))) {
					// player may even set fireproof blocks on fire
					denyPermission = false;
					denyFireproof = false;
				} else {
					// fireproof block must not be ignited by player
					denyPermission = false;
					denyFireproof = true;
				}
			} else {
				// flint&steel use is permitted, block is not fireproof
				denyPermission = false;
				denyFireproof = false;
			}
		} else if (properties.hasPermission(Key.IGNITE_FLINTANDSTEEL_PERM,
				player)) {
			// flint & steel needs permission
			if (fireIsOnFireproofBlock(block)) {
				if (properties.hasPermission(Key.IGNITE_FIREPROOF_PERM, player,
						Integer.valueOf(block.getRelative(0, -1, 0).getTypeId()))) {
					// player may even set fireproof blocks on fire
					denyPermission = false;
					denyFireproof = false;
				} else {
					// fireproof block must not be ignited by player
					denyPermission = false;
					denyFireproof = true;
				}
			} else {
				// player is allowed to use flint&steel, block may be set on
				// fire
				denyPermission = false;
				denyFireproof = false;
			}
		} else {
			// player may not ignite blocks with flint&steel
			denyPermission = true;
			denyFireproof = false;
		}
		if (denyPermission) {
			player.message(Colors.RED
					+ properties.getString(world,
							Key.IGNITE_FLINTANDSTEEL_MESSAGE));
		} else if (denyFireproof) {
			player.message(Colors.RED
					+ properties.getString(world, Key.IGNITE_FIREPROOF_MESSAGE));
		}
		boolean deny = denyPermission || denyFireproof;
		if (!deny) {
			allowLightPortal(block);
		}
		return deny;
	}

	/**
	 * Handles blocks being set on fire by fire spreading.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockFireSpreadIgnite(Block block) {
		return !properties.getBoolean(block.getWorld(), Key.IGNITE_FIRESPREAD)
				|| fireIsOnFireproofBlock(block);
	}

	/**
	 * Handles blocks being destroyed by fire.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * <p>
	 * If destruction is denied, the fire attached to this block is removed.
	 * 
	 * @param block
	 *            the block that will be destroyed unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockFireDestruction(Block block) {
		if (!properties.getBoolean(block.getWorld(), Key.IGNITE_DESTROY)
				|| fireIsOnFireproofBlock(block)) {
			extinguishBlock(block);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Handles blocks being set on fire by lightning.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockLightningIgnite(Block block) {
		return !properties.getBoolean(block.getWorld(), Key.IGNITE_LIGHTNING)
				|| fireIsOnFireproofBlock(block);
	}

	/**
	 * Handles blocks being set on fire by a fireball (Ghast, Blaze, firecharge
	 * fired by dispenser).
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockFireballIgnite(Block block) {
		return !properties.getBoolean(block.getWorld(), Key.IGNITE_FIREBALL)
				|| fireIsOnFireproofBlock(block);
	}

	/**
	 * Handles blocks being set on fire by a player right-clicking with a fire
	 * charge.
	 * <p>
	 * Called by <code>onIgnite.</code>
	 * 
	 * @param block
	 *            the block that will be made a fire block unless denied
	 * @return true to deny, false to allow
	 */
	private boolean blockFireChargeUseIgnite(Block block, Player player) {
		World world = block.getWorld();
		boolean denyPermission = false;
		boolean denyFireproof = false;
		if (properties.getBoolean(world, Key.IGNITE_FIRECHARGE)) {
			// firecharges is allowed for everyone
			if (fireIsOnFireproofBlock(block)) {
				if (properties.hasPermission(Key.IGNITE_FIREPROOF_PERM, player,
						Integer.valueOf(block.getRelative(0, -1, 0).getTypeId()))) {
					// player may even set fireproof blocks on fire
					denyPermission = false;
					denyFireproof = false;
				} else {
					// fireproof block must not be ignited by player
					denyPermission = false;
					denyFireproof = true;
				}
			} else {
				// firecharge use is permitted, block is not fireproof
				denyPermission = false;
				denyFireproof = false;
			}
		} else if (properties.hasPermission(Key.IGNITE_FIRECHARGE_PERM, player)) {
			// firecharges need permission
			if (fireIsOnFireproofBlock(block)) {
				if (properties.hasPermission(Key.IGNITE_FIREPROOF_PERM, player,
						Integer.valueOf(block.getRelative(0, -1, 0).getTypeId()))) {
					// player may even set fireproof blocks on fire
					denyPermission = false;
					denyFireproof = false;
				} else {
					// fireproof block must not be ignited by player
					denyPermission = false;
					denyFireproof = true;
				}
			} else {
				// player is allowed to use firecharges, block may be set on
				// fire
				denyPermission = false;
				denyFireproof = false;
			}
		} else {
			// player may not ignite blocks with firecharges
			denyPermission = true;
			denyFireproof = false;
		}
		if (denyPermission) {
			player.message(Colors.RED
					+ properties
					.getString(world, Key.IGNITE_FIRECHARGE_MESSAGE));
		} else if (denyFireproof) {
			player.message(Colors.RED
					+ properties.getString(world, Key.IGNITE_FIREPROOF_MESSAGE));
		}
		boolean deny = denyPermission || denyFireproof;
		if (!deny) {
			allowLightPortal(block);
		}
		return deny;
	}

	/**
	 * Extinguishes all fire attached to a block. Called by
	 * <code>blockFireDestruction</code>.
	 * 
	 * @param block
	 *            the block to extinguish
	 */
	private void extinguishBlock(Block block) {
		final short FIRE = BlockType.FireBlock.getId();
		final short AIR = BlockType.Air.getId();
		Block fireBlock;
		Block belowFireBlock;

		// fire on block
		fireBlock = block.getRelative(0, 1, 0);
		if (fireBlock != null && fireBlock.getTypeId() == FIRE) {
			fireBlock.setType(BlockType.Air);
			fireBlock.update();
		}
		// fire on sides, no other block below fire
		final int[][] offsets = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
		for (int[] offset : offsets) {
			fireBlock = block.getRelative(offset[0], offset[1], offset[2]);
            if (fireBlock != null) {
                belowFireBlock = fireBlock.getRelative(0, -1, 0);
                if (belowFireBlock != null) {
                    if (fireBlock.getTypeId() == FIRE && belowFireBlock.getTypeId() == AIR) {
                        // Yes, I know this won't extinguish blocks on level 0.
                        // There shouldn't be a flammable block at level 0 anyway,
                        // so I don't care.
                        fireBlock.setType(BlockType.Air);
                        fireBlock.update();
                    }
                }
            }
		}
	}
	

	/**
	 * Checks whether a block to be ignited is fireproof.
	 * <p>
	 * Only works for fire on top of the block
	 * 
	 * @param block
	 *            the potential fire block
	 * @return true if the block below is configured as fireproof, false
	 *         otherwise.
	 */
	private boolean fireIsOnFireproofBlock(Block block) {
		if (block == null) {
			return false;
		}
		return properties.containsInteger(block.getWorld(),
				Key.IGNITE_FIREPROOF, block.getType().getId());
	}

	/**
	 * Allows a portal to be lit around a block
	 * 
	 * @param block
	 *            the block that has been lit
	 */
	private void allowLightPortal(final Block block) {
		Block blockBelow = block.getRelative(0, -1, 0);
		if (!blockBelow.getType().equals(BlockType.Obsidian)) {
			// fire is not on obsidian, this can't be an attempt to light a
			// portal.
			FishyShield.logger.debug("allowLightPortal: not on obby");
			return;
		}
		FishyShield.logger.debug("Portal creation allowed.");
		final Location fireLocation = block.getLocation();
		this.portalAllowed.add(fireLocation);
		// remove from list after 500ms
		ServerTask expire = new ServerTask(plugin, 500) {
			@Override
			public void run() {
				portalAllowed.remove(fireLocation);
			}
		};
		ServerTaskManager.addTask(expire);
	}
}
