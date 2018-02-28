
import syntaxtree.*;
import java.util.*;


public class Record {

	private static int num = 0;

	private int thisnum;

	private String identifier;
	private String cname;

	private boolean isField;
	private boolean first;

	private Node n;

	private NullALatticeElement e;

	private Record(String identifier, String cname, boolean isField, NullALatticeElement e, Node n) {
		this.identifier = identifier;
		this.cname = cname;
		this.isField = isField;
		this.e = e;
		this.n = n;
		this.first = first;
		thisnum = num;
		num++;
	}

	public NullALatticeElement getLatticeElement() { return e; }

	public Node getNode() { return n; }

	public boolean isField() { return isField; }

	public StringPair getClassIdentifierSP() { return new StringPair(cname, identifier); }

	public String getIden() { return identifier; }
	public String getCName() { return cname; }

	public boolean equals(Record r) {
		return (getClassIdentifierSP().equals(r.getClassIdentifierSP()) && (isField == r.isField()));
	}

	public static void resetCounter() { num = 0; }

	public static Record getFreshFieldRecord(String classname, String fieldname) {
		return new Record(fieldname, classname, true, NullALatticeElement.getDontKnow(), null);
	}

	public static Record getFreshVariableRecord(String classname, String varname) {
		return new Record(varname, classname, false, NullALatticeElement.getNotNull(), null);
	}

	public static Record getNewRecord(String c, String v, boolean f, NullALatticeElement e, Node n) {
		return new Record(v, c, f, e, n);
	}

	public String toString() {
		String f = "variable: ";
		if (isField) 
			f = "field: ";

		return "\nclass: " + cname + " | " + f + identifier + " " + "Lattice: " + e.toString() + " num = " + thisnum;
	}

	/*
	public static Record LUBRecords(List<Record> sameRecords) {
		boolean isnotnull = true;
		for (Record r : sameRecords) {
			isnotnull = r.getLatticeElement.isNotNull() && isnotnull;
		}

		return new Record()
	}
	*/
}