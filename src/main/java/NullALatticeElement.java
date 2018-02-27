

public class NullALatticeElement {

	private static int DONTKNOW = 1;
	private static int NOTNULL = 0;

	private int val;

	private NullALatticeElement(int val) {
		this.val = val;
	}

	public static NullALatticeElement getDontKnow() { return new NullALatticeElement(DONTKNOW); }
	public static NullALatticeElement getNotNull() { return new NullALatticeElement(NOTNULL); }

	public boolean isNotNull() { return val == NOTNULL; }
}