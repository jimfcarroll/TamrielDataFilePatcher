
public class Translation {
	private String previousId, newId;

	public Translation(String previousId, String newId) {
		super();
		this.previousId = previousId;
		this.newId = newId;
	}

	public String getPreviousId() {
		return previousId;
	}

	public String getNewId() {
		return newId;
	}
}
