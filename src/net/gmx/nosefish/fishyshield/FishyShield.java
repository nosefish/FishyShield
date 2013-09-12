package net.gmx.nosefish.fishyshield;


import net.gmx.nosefish.fishylib.properties.Properties;

import net.canarymod.Canary;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.TaskOwner;
import net.gmx.nosefish.fishyshield.listeners.EntityListener;
import net.gmx.nosefish.fishyshield.listeners.ExplosionListener;
import net.gmx.nosefish.fishyshield.listeners.FireListener;
import net.gmx.nosefish.fishyshield.listeners.LiquidListener;
import net.gmx.nosefish.fishyshield.properties.Key;

/**
 * A CanaryMod plugin that protects the world from environmental damage.
 * <p>
 * FishyShield handles damage by fire, explosions, liquids, and mobs. Damage
 * amount dealt by mobs attacking players can be adjusted for certain mobs. It
 * can also prevent players from using, dropping, or picking up certain items,
 * for example to prevent players from obtaining bedrock dropped by admins by
 * accident or on death, or to restrict item use to certain player groups.
 * Protections can be configured globally and per world.
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class FishyShield extends Plugin implements TaskOwner {
	public static Logman logger;
	public static Properties properties;


	@Override
	public void disable() {
	}

	@Override
	public boolean enable() {
		logger = getLogman();
		properties = new Properties(this);
		properties.addMissingKeys(Key.getAllKeys());
		registerListeners();
		return true;
	}

	public Properties getProperties() {
		return properties;
	}

	// ----------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------
	/**
	 * Registers all PluginListeners with the CanaryMod hook system
	 */
	private void registerListeners() {
		// explosions
		Canary.hooks().registerListener(new ExplosionListener(this), this);
		// fire
		Canary.hooks().registerListener(new FireListener(this), this);
		// liquids
		Canary.hooks().registerListener(new LiquidListener(this), this);
		// entities
		Canary.hooks().registerListener(new EntityListener(this), this);
	}
}
