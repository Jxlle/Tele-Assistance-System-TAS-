package tas.mape.planner;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Enum that represents the type of the rating for a service combination chosen by the analyzer component
 */
public enum RatingType {
	
	// Available ratings
	NUMBER(Double.class),
	CLASS(Integer.class);
	
	// Fields
	private final Class<?> typeClass;
	
	/**
	 * Create a new rating type with a given class representing the type of the rating
	 * @param typeClass class representing the given type of the rating
	 */
	private RatingType(Class<?> typeClass) {
		this.typeClass = typeClass;
	}
	
	/**
	 * Return the type class of this rating type
	 * @return the type class
	 */
	public Class<?> getTypeClass() {
		return typeClass;
	}
	
}
