
public class StringPair {
	public String first;
	public String second;

	public StringPair(String f, String s) {
		first = f;
		second = s;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.hashCode() == this.hashCode();
	}

	public String toString() {
		return "class: " + first + ", method: " + second;
	}

	@Override
	public int hashCode() {
		return 3*first.hashCode() + second.hashCode();
	}
}