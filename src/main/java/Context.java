

public class Context {
	public ClassHierarchyAnalysis cha;
	public NullPtrAnalysis npa;

	public String cname;
	public String mname;

	public NullALatticeElement eval = null;

	public boolean branch = false;
	public List<Record> branchRecords;

	public Context prevContext = null;

	// Need to have some notion of records being kept in the context...
	// Also need to have some sort of mapping from 
	public Context() {}

	public Context(Context c) {
		cha = c.cha;
		npa = c.npa;
		cname = c.cname;
		mname = c.mname;
		branch = true;
		branchRecords = new ArrayList<Record>();
		prevContext = c;
	}

	public StringPair getSP() { return new StringPair(cname, mname); }

	public void evalFun(StringPair cmPair) {
		if (npa.isFunTyped(cmPair)) {
			eval =  npa.getRet(cmPair);
		}
		eval = null;
	}

	public 

	public void commitRecord(String iden, NullALatticeElement e, Node n) {
		boolean isAField = !(cha.isMethodVariable(new StringPair(cname, mname), iden));
		Record r = Record.getNewRecord(cname, iden, isAField, e, n);

		if (branch) {
			branchRecords.add(r);
		}
		else {
			npa.commitRecord(r);
		}
	}

	public void commitRecords(List<Record> rs, Node n) {
		if (branch) {
			branch.addAll(rs);
		}
		else {
			npa.commitRecords(rs);
		}
	}

}

