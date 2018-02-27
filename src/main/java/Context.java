

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

	public String getVariableType(String iden) {
		if (cha.isVariable(getSP(), iden)) {
			return cha.getVariableType(getSP(), iden);
		}
		return "";
	}

	public void evalFun(StringPair cmPair) {
		if (npa.isFunTyped(cmPair)) {
			eval =  npa.getRet(cmPair);
		}
		eval = null;
	}

	// There's gotta be a more efficient way of doing this.
	private List<Record> trueLeastUpperBound(List<Record> lr1, List<Record> lr2) {
		List<Record> lub = new ArrayList<Record>();
		List<Record> noMatch = new ArrayList<Record>();

		for (Record r : lr1) {
			Record isVisited;
			for (Record otherr : lr2) {
				if (r.equals(otherr)) {
					isVisited = otherr;
					NullALatticeElement newLE = NullALatticeElement.lub(r.getLatticeElement(), otherr.getLatticeElement());
					lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), newLE, null));
				}
			}
			if (isVisited == null) {
				noMatch.add(r);
			}
			else {
				lr2.remove(isVisited)
			}
		}

		for (Record r : lr2) {
			Record isVisited;
			for (Record otherr : lr1) {
				if (r.equals(otherr)) {
					isVisited = otherr;
					NullALatticeElement newLE = NullALatticeElement.lub(r.getLatticeElement(), otherr.getLatticeElement());
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
				lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), NullALatticeElement.getDontKnow(), null));
			}
			else {
				NullALatticeElement newLE = NullALatticeElement.lub(r.getLatticeElement(), prevR.getLatticeElement());
				lub.add(Record.getNewRecord(r.getCName(), r.getIden(), r.isField(), newLE, null));
			}
		}

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
				priorRecords.add(Record.getNewRecord(cisp.first, cisp.second, r.isField, NullALatticeElement.getDontKnow(), null));
			}
		}

		return trueLeastUpperBound(uniqueRecords, priorRecords);
	}

	public Record getRecord(StringPair cisp, boolean field) {
		StringPair cisp = new StringPair(c, iden);

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
			return c.getMostRecentRecord(getSP(), iden, field);
		}
		else {
			return npa.getMostRecentRecord(getSP(), iden, field);
		}
	}

	public Record getRecord(String iden) {
		boolean isField = !(cha.isMethodVariable(getSP(), iden));
		return getRecord(new StringPair(cname, iden), isField);
	}

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

	public void commitRecords(List<Record> rs) {
		if (branch) {
			branch.addAll(rs);
		}
		else {
			npa.commitRecords(rs);
		}
	}

}

