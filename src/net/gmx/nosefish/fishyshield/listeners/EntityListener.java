package net.gmx.nosefish.fishyshield.listeners;

import net.gmx.nosefish.fishylib.properties.Properties;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.entity.HangingEntityDestroyHook;
import net.canarymod.plugin.PluginListener;
import net.gmx.nosefish.fishyshield.FishyShield;
import net.gmx.nosefish.fishyshield.properties.Key;


/**
 * A <code>PluginListener</code> that handles protections related to Entities.
 * 
 * Protects <code>HangingEntities</code> like paintings and item frames from
 * destruction by fire, explosions, and mobs. Players are always allowed to
 * destroy them. Uses the same properties as the protection for blocks, so if
 * block damage is allowed for the damage type, <code>HangingEntities</code>
 * will also be destroyed, and the other way round.
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class EntityListener implements PluginListener {
	private Properties properties;

	/**
	 * Constructor
	 * 
	 * @param fishyShield
	 *            the plugin instantiating this <code>PluginListener</code>
	 */
	public EntityListener(FishyShield fishyShield) {
		this.properties = fishyShield.getProperties();
	}

	@HookHandler
	public void onHangingEntityDestroyed(HangingEntityDestroyHook hook) {
		Player player = hook.getPlayer();
		World world = hook.getPainting().getWorld();
		if (player == null) {
			if (! properties.getBoolean(world, Key.ENTITY_HANGING_MOBDAMAGE)) {
				hook.setCanceled();
			}
		}
		//Entity baseEntity = hook.
		//World world = baseEntity.getWorld();
		// only players may destroy paintings and item frames
//		if (damageSource == null) {
//			// the block it's hanging on was destroyed
//			FishyShield.logDebug("HangingEntity destroyed: null source");
//			return false;
//		}
//		if (damageSource.getSourceEntity() != null) {
//			// damaged by an entity
//			if (damageSource.getSourceEntity().isPlayer()) {
//				// players may break it
//				FishyShield.logDebug("HangingEntity destroyed by player "
//						+ damageSource.getSourceEntity().getPlayer().getName());
//				return false;
//			} else if (!properties.getBoolean(world,
//					Key.ENTITY_HANGING_MOBDAMAGE)) {
//				// a mob damaged it, and it's protected
//				FishyShield.logDebug("Destruction of HangingEntity by "
//						+ damageSource.getName() + " blocked.");
//				return true;
//			}
//		}
//		if (damageSource.isExplosionDamage()
//				&& properties.getBoolean(world, Key.EXPLOSION_ENABLE)
//				&& !properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
//			FishyShield.logDebug("Destruction of HangingEntity by "
//					+ damageSource.getName() + " blocked.");
//			return true;
//		}
//		if (damageSource.isFireDamage()
//				&& properties.getBoolean(world, Key.IGNITE_ENABLE)
//				&& !properties.getBoolean(world, Key.IGNITE_DESTROY)) {
//			FishyShield.logDebug("Destruction of HangingEntity by "
//					+ damageSource.getName() + " blocked.");
//			return true;
//		}
//		return false;
	}

}
