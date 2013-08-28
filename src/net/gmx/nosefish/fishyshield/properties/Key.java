package net.gmx.nosefish.fishyshield.properties;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.gmx.nosefish.fishylib.properties.PropertyKey;
import net.gmx.nosefish.fishylib.properties.ValueType;

/**
 * Enum of known property keys, along with the string used in the properties
 * file, the type of their value, and the default value.
 * 
 * @author Stefan Steinheimer
 * 
 */
public enum Key implements PropertyKey{
	DEBUG("debug",
			ValueType.BOOLEAN, false),
	IGNITE_ENABLE("ignite.protection-enable",
			ValueType.BOOLEAN, false),
	IGNITE_DESTROY("ignite.fire-destroys-blocks",
			ValueType.BOOLEAN, true),
	IGNITE_LAVA("ignite.lava",
			ValueType.BOOLEAN, true),
	IGNITE_FLINTANDSTEEL("ignite.flint-and-steel",
			ValueType.BOOLEAN, true),
	IGNITE_FLINTANDSTEEL_PERM("ignite.flint-and-steel-override-permission",
			ValueType.CSV_PERMISSIONS, new String[]{"NOBODY"}),
	IGNITE_FLINTANDSTEEL_MESSAGE("ignite.flint-and-steel-message",
			ValueType.STRING, "You do not have permission to use flint&steel."),
	IGNITE_FIRECHARGE("ignite.firecharge-rightclick",
			ValueType.BOOLEAN, true),
	IGNITE_FIRECHARGE_PERM("ignite.firecharge-override-permission",
			ValueType.CSV_PERMISSIONS,new String[]{"NOBODY"}),
	IGNITE_FIRECHARGE_MESSAGE("ignite.firecharge-message",
			ValueType.STRING, "You do not have permission to use firecharges."),
	IGNITE_FIRESPREAD("ignite.fire-spread",
			ValueType.BOOLEAN, true),
	IGNITE_LIGHTNING("ignite.lightning",
			ValueType.BOOLEAN, true),
	IGNITE_FIREBALL("ignite.fireball",
			ValueType.BOOLEAN, true),
	IGNITE_FIREPROOF("ignite.fireproof-blocks",
			ValueType.CSV_INT, new int[] {-1}),
	IGNITE_FIREPROOF_PERM("ignite.fireproof-override-permissions",
			ValueType.CSV_PERMISSIONS, new String[]{"NOBODY"}),
	IGNITE_FIREPROOF_MESSAGE("ignite.fireproof-message",
			ValueType.STRING,"You do not have permission to set this block on fire."),
	LIQUID_ENABLE("liquid.protection-enable",
			ValueType.BOOLEAN, false),
	LIQUID_BLOCKS("liquid.protected-blocks",
			ValueType.CSV_INT, new int[] {-1}),
	EXPLOSION_ENABLE("explosion.protection-enable",
			ValueType.BOOLEAN, false),
	EXPLOSION_DAMAGEBLOCKS("explosion.damage-blocks",
			ValueType.BOOLEAN, true),
	EXPLOSION_CASCADETNT("explosion.cascade-tnt",
			ValueType.BOOLEAN, true),
	ENTITY_HANGING_MOBDAMAGE("entity.mobs-break-hanging-entities",
			ValueType.BOOLEAN, true);
	
	private static Map<String, PropertyKey> map;
	private String propertyName;
	private Object defaultValue;
	private ValueType propertyType;

	private Key(String propertyName, ValueType type, Object defaultValue) {
		this.propertyName = propertyName;
		this.propertyType = type;
		this.defaultValue = defaultValue;
		addToMap(propertyName, this);
	}

	/**
	 * Gets the name of the property key that is used in the properties file
	 * 
	 * @return property key as it appears in the file
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Gets the <code>ValueType</code> of this property. Used to determine how
	 * to load this property
	 * 
	 * @return the <code>ValueType</code> associated with this property
	 */
	public ValueType getType() {
		return propertyType;
	}

	/**
	 * Gets the default value of this property. This value will be applied if
	 * the property is missing from the properties file and will be written back
	 * to the file.
	 * 
	 * @return the default value
	 */
	public Object getDefault() {
		return defaultValue;
	}

	/**
	 * Gets all known keys. Used to find keys that are missing from the
	 * propertied file
	 * 
	 * @return the set of known keys
	 */
	public static Collection<PropertyKey> getAllKeys() {
		return map.values();
	}

	/**
	 * Gets the <code>Key</code> associated with a property string.
	 * 
	 * @param propertyName
	 *            the key string of the property as it appears in the properties
	 *            file
	 * @return the <code>Key</code> for the given property name
	 */
	public static PropertyKey getKey(String propertyName) {
		return map.get(propertyName);
	}

	/**
	 * Adds a property name/<code>Key</code> pair to the internal Map. Used by
	 * the constructor to make <code>getKey</code> possible.
	 * 
	 * @param propertyName
	 * @param k
	 */
	private static void addToMap(String propertyName, PropertyKey k) {
		if (map == null) {
			map = new TreeMap<String, PropertyKey>();
		}
		map.put(propertyName, k);
	}
}