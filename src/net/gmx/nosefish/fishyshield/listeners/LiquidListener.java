package net.gmx.nosefish.fishyshield.listeners;
import net.gmx.nosefish.fishylib.properties.Properties;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.LiquidDestroyHook;
import net.canarymod.plugin.PluginListener;
import net.gmx.nosefish.fishyshield.FishyShield;
import net.gmx.nosefish.fishyshield.properties.Key;

/**
 * A <code>PluginListener</code> that protects blocks from liquid damage.
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class LiquidListener implements PluginListener {
	private Properties properties;

	/**
	 * Constructor
	 * 
	 * @param fishyShield
	 *            the plugin instantiating this <code>PluginListener</code>
	 */
	public LiquidListener(FishyShield fishyShield) {
		this.properties = fishyShield.getProperties();
	}

	@HookHandler
	public void onLiquidDestroy(LiquidDestroyHook hook) {
		Block targetBlock = hook.getBlock();
		World world = targetBlock.getWorld();
		int id = targetBlock.getType().getId();
		boolean isProtected = properties.getBoolean(world, Key.LIQUID_ENABLE)
				&& properties.containsInteger(world, Key.LIQUID_BLOCKS, id);
		if (isProtected) {
			FishyShield.logger.logDebug("onLiquidDestroy protected a "
					+ id);
			hook.setCanceled();
		}
	}
}
