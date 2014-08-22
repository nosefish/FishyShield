package net.gmx.nosefish.fishyshield.listeners;

import net.gmx.nosefish.fishylib.properties.Properties;
import net.canarymod.api.DamageSource;
import net.canarymod.api.DamageType;
import net.canarymod.api.entity.Entity;
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
		//TODO: mostly broken in Canary recode; test again when this issue is fixed
		// https://github.com/FallenMoonNetwork/CanaryRecode/issues/71

		World world = hook.getPainting().getWorld();
		DamageSource damageSource = hook.getDamageSource();

		FishyShield.logger.warn("onHangingEntity called");
		if (damageSource == null) {
			// the block it's hanging on was destroyed
			FishyShield.logger.debug("HangingEntity destroyed: null source");
			// TODO: we're allowing this, but should we?
			return;
		}
		Entity damageSourceEntity = damageSource.getDamageDealer();

		 // only players may destroy paintings and item frames
		if (damageSourceEntity != null) {
			// damaged by an entity
			if (damageSourceEntity.isPlayer()) {
				// players may break it
				FishyShield.logger.debug("HangingEntity destroyed by player "
						+ ((Player)damageSourceEntity).getName());
				return; //allow
			} else if (!properties.getBoolean(world,
					Key.ENTITY_HANGING_MOBDAMAGE)) {
				// a mob damaged it, and it's protected
				FishyShield.logger.debug("Destruction of HangingEntity by "
						+ damageSource.getNativeName() + " blocked.");
				hook.setCanceled();
				return;
			}
		} else if (damageSource.getDamagetype().equals(DamageType.EXPLOSION)
				&& properties.getBoolean(world, Key.EXPLOSION_ENABLE)
				&& !properties.getBoolean(world, Key.EXPLOSION_DAMAGEBLOCKS)) {
			FishyShield.logger.debug("Destruction of HangingEntity by "
					+ damageSource.getNativeName() + " blocked.");
			hook.setCanceled(); // block
		}else if (damageSource.isFireDamage()
				&& properties.getBoolean(world, Key.IGNITE_ENABLE)
				&& !properties.getBoolean(world, Key.IGNITE_DESTROY)) {
			FishyShield.logger.debug("Destruction of HangingEntity by "
					+ damageSource.getNativeName() + " blocked.");
			hook.setCanceled(); // block
		}
	}

}
