

public class NullALatticeElement {

	private static int DONTKNOW = 1;
	private static int NOTNULL = 0;

	private int val;

	private NullALatticeElement(int val) {
		this.val = val;
	}

	public static NullALatticeElement getDontKnow() { return new NullALatticeElement(DONTKNOW); }
	public static NullALatticeElement getNotNull() { return new NullALatticeElement(NOTNULL); }

	public static NullALatticeElement leastUpperBound(NullALatticeElement a, NullALatticeElement b) {
		if (a.isNotNull() && b.isNotNull()) {
			return getNotNull();
		}
		else return getDontKnow();
	}

	public boolean equals(NullALatticeElement a) {
		return this.isNotNull() == a.isNotNull();
	}

	public String toString() {
		if (isNotNull()) 
			return "NOT NULL";
		return "DON'T KNOW";
	}

	public boolean isNotNull() { return val == NOTNULL; }
}