import syntaxtree.*;
import visitor.*;
import java.util.*;

public class Context {
	public ClassHierarchyAnalysis cha;
	public NullPtrAnalysis npa;

	public String cname;
	public String mname;

	public NullALatticeElement eval = null;
	public Set<String> expType = new HashSet<String>();

	public List<NullALatticeElement> argEvals = null;

	public boolean branch = false;
	public List<Record> branchRecords;

	public Context prevContext = null;

	public Record prevRecord;

	public boolean debug = true;

	// Need to have some notion of records being kept in the context...
	// Also need to have some sort of mapping from 
	public Context(ClassHierarchyAnalysis cha, NullPtrAnalysis npa) {
		this.cha = cha;
		this.npa = npa;
		branch = false;
		branchRecords = new ArrayList<Record>();
		prevContext = null;
		eval = null;
		expType = new HashSet<String>();
	}

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

	public String getVariableType(String iden) {
		if (cha.isVariable(getSP(), iden)) {
			return cha.getVariableType(getSP(), iden);
		}
		return "";
	}

	// There's gotta be a more efficient way of doing this.
	private List<Record> trueLeastUpperBound(List<Record> lr1, List<Record> lr2) {
		List<Record> lub = new ArrayList<Record>();
		List<Record> noMatch = new ArrayList<Record>();

		for (Record r : lr1) {
			Record isVisited = null;
			for (Record otherr : lr2) {
				if (r.equals(otherr)) {
					isVisited = otherr;
					NullALatticeElement newLE = NullALatticeElement.leastUpperBound(r.getLatticeElement(), otherr.getLatticeElement());
					lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), newLE, null));
				}
			}
			if (isVisited == null) {
				noMatch.add(r);
			}
			else {
				lr2.remove(isVisited);
			}
		}

		for (Record r : lr2) {
			Record isVisited = null;
			for (Record otherr : lr1) {
				if (r.equals(otherr)) {
					isVisited = otherr;
					NullALatticeElement newLE = NullALatticeElement.leastUpperBound(r.getLatticeElement(), otherr.getLatticeElement());
					lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), newLE, null));
				}
			}
			if (isVisited == null) {
				noMatch.add(r);
			}
		}

		for (Record r : noMatch) {
			StringPair cisp = r.getClassIdentifierSP();
			Record prevR = getRecord(cisp, r.isField());
			if (prevR == null) {
				lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), NullALatticeElement.getNotNull(), null));
			}
			else {
				NullALatticeElement newLE = NullALatticeElement.leastUpperBound(r.getLatticeElement(), prevR.getLatticeElement());
				lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), newLE, null));
			}
		}

		System.out.println("l1: " + lr1);
		System.out.println("l2: " + lr2);
		System.out.println("Least upper bound : " + lub);

		return lub;

	}

	public List<Record> leastUpperBound(List<Record> lr1, List<Record> lr2) {
		List<Record> uniquelr1 = npa.getUniqueRecords(lr1);
		List<Record> uniquelr2 = npa.getUniqueRecords(lr2);
		return trueLeastUpperBound(uniquelr1, uniquelr2);
	}

	public List<Record> leastUpperBound(List<Record> lr) {
		List<Record> uniqueRecords = npa.getUniqueRecords(lr);

		List<Record> priorRecords = new ArrayList<Record>();
		for (Record r : uniqueRecords) {
			StringPair cisp = r.getClassIdentifierSP();
			Record prevR = getRecord(cisp, r.isField());
			if (prevR != null){
				priorRecords.add(prevR);
			}
			else {
				priorRecords.add(Record.getNewRecord(cisp.first, cisp.second, r.isField(), NullALatticeElement.getDontKnow(), null));
			}
		}

		return trueLeastUpperBound(uniqueRecords, priorRecords);
	}

	public Record getRecord(StringPair cisp, boolean field) {
		String cOfIden = cha.getClassOfIdentifier(new StringPair(cisp.first, mname), cisp.second);

		ListIterator<Record> li = branchRecords.listIterator(branchRecords.size());
		while (li.hasPrevious()) {
			Record r = li.previous();
			StringPair id = r.getClassIdentifierSP();
			if (id.equals(cisp)) {
				if (field && r.isField()) {
					return r;
				}
				if (!field) {
					return r;
				}
			}
		}

		if (branch){
			return prevContext.getRecord(cisp, field);
		}
		else {
			return npa.getMostRecentRecord(cisp, field);
		}
	}

	public Record getRecord(String iden) {
		String cOfIden = cha.getClassOfIdentifier(getSP(), iden);
		if (cOfIden == null)
			return null;

		boolean isField = !(cha.isMethodVariable(new StringPair(cOfIden, mname), iden));
		return getRecord(new StringPair(cOfIden, iden), isField);
	}

	public void commitRecord(String iden, NullALatticeElement e, Node n) {
		String cOfIden = cha.getClassOfIdentifier(getSP(), iden);
		boolean isAField = !(cha.isMethodVariable(new StringPair(cOfIden, mname), iden));
		Record r = Record.getNewRecord(cname, iden, isAField, e, n);

		if (branch) {
			branchRecords.add(r);
		}
		else {
			npa.commitRecord(r);
		}
	}

	public void commitRecords(List<Record> rs) {
		if (branch) {
			branchRecords.addAll(rs);
		}
		else {
			npa.commitRecords(rs);
		}
	}

}

