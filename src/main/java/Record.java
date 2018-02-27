
import syntaxtree.*;
import java.util.*;


public class Record {

	private String identifier;
	private String cname;

	private boolean isField;
	private boolean first;

	private Node n;

	private NullALatticeElement e;

	private Record(String identifier, String cname, boolean isField, NullALatticeElement e, Node n, boolean first) {
		this.identifier = identifier;
		this.cname = cname;
		this.isField = isField;
		this.e = e;
		this.n = n;
		this.first = first;
	}

	public NullALatticeElement getLatticeElement() { return e; }

	public boolean isThisRecord(String cname, String identifier) {
		return cname.equals(this.cname) && identifier.equals(this.identifier);
	}

	public Node getNode() { return n; }

	public boolean isField() { return isField; }

	public StringPair getClassIdentifierSP() { return new StringPair(cname, identifier); }


	public static Record getFreshFieldRecord(String classname, String fieldname) {
		return new Record(fieldname, classname, true, NullALatticeElement.getDontKnow(), null, true);
	}

	public static Record getFreshVariableRecord(String classname, String varname) {
		return new Record(varname, classname, false, NullALatticeElement.getNotNull(), null, true);
	}

	public static Record getNewRecord(String c, String v, boolean f, NullALatticeElement e, Node n) {
		return new Record(v, c, f, e, n, false);
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