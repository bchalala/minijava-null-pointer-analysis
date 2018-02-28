
import java.util.*;

public class NullPtrAnalysis {

	ClassHierarchyAnalysis cha;

	public NullPtrAnalysis(ClassHierarchyAnalysis cha) {
		this.cha = cha;
	}

	public void newIteration() {
		/*
		System.out.println("\nLearned in this iteration:");
		System.out.println("cmRet: " + cmRet);
		System.out.println("cmLastOutCurrent: " + cmLastOutCurrent);
		System.out.println("cmArgsCurrent:" + cmArgsCurrent + "\n\n");
	    */
		
			
		Record.resetCounter();

		refreshCMArgs();
		refreshCMOut();	
	}

	public boolean shouldIterate() {
		/*
		System.out.println(cmLastOutCurrentPRIORRUN);
		System.out.println(cmLastOutCurrent);
		*/

		if (cmLastOutCurrentPRIORRUN == null) {
			cmLastOutCurrentPRIORRUN = cmLastOutCurrent;
			return true;
		}

		for (StringPair k : cmArgsCurrent.keySet()) {
			List<Record> cur = cmLastOutCurrent.get(k);
			List<Record> prior = cmLastOutCurrentPRIORRUN.get(k);

			if (cur.size() != prior.size()) {
				cmLastOutCurrentPRIORRUN = cmLastOutCurrent;
				return true;
			}

			for (int i = 0; i < cur.size(); i++) {
				if (cur.get(i).equals(prior.get(i)) && (cur.get(i).getLatticeElement().equals(prior.get(i).getLatticeElement())))
					continue;
				else {
					cmLastOutCurrentPRIORRUN = cmLastOutCurrent;
					return true;
				}
			}
		}

		return false;
		
	}
	
	// This is going to store the outputs of all the previous iteration's method outs. 
	private HashMap<StringPair, List<Record>> cmLastOutPrevious = new HashMap<StringPair, List<Record>>();

	// This is going to be the current iteration's last outs. 
	private HashMap<StringPair, List<Record>> cmLastOutCurrent = new HashMap<StringPair, List<Record>>();
	private HashMap<StringPair, List<Record>> cmLastOutCurrentPRIORRUN = null;

	// Output of functions
	private HashMap<StringPair, NullALatticeElement> cmRet = new HashMap<StringPair, NullALatticeElement>();

	// This is going to store the outputs of all the previous iteration's arg ins.
	// This is the LUB of cmArgsCurrent for a given StringPair.
	private HashMap<StringPair, List<Record>> cmArgsPrevious = new HashMap<StringPair, List<Record>>();

	// This is going to be the current iteration's arg ins. 

	private HashMap<StringPair, List<List<NullALatticeElement>>> cmArgsCurrent = 
		new HashMap<StringPair, List<List<NullALatticeElement>>>();

	// This is going to be the map that is basically the kafka-esque record store. 
	// Updates are just thrown into a list. 
	private List<Record> methodRecordStore = new ArrayList<Record>();

	public void commitRecord(Record r) { methodRecordStore.add(r); }
	public void commitRecords(List<Record> rs) { methodRecordStore.addAll(rs); }

	public void addRet(StringPair cmPair, NullALatticeElement e) {
		cmRet.put(cmPair, e);
	}

	public NullALatticeElement getRet(StringPair cmPair) {
		if (cha.isFunTyped(cmPair))
			return cmRet.getOrDefault(cmPair, NullALatticeElement.getDontKnow());
		else 
			return null;
	}

	public Record getMostRecentRecord(StringPair cisp, boolean field) {
		ListIterator<Record> li = methodRecordStore.listIterator(methodRecordStore.size());
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
		return null;
	}

	public List<Record> getMethodCallRecords(StringPair cmCall) {
		return cmLastOutPrevious.getOrDefault(cmCall, new ArrayList<Record>());
	}

	public NullALatticeElement getMethodReturn(StringPair cmCall) {
		return cmRet.getOrDefault(cmCall, NullALatticeElement.getDontKnow());
	}

	public void addMethodCallArgs(Set<String> cnames, String mname, List<NullALatticeElement> argLEs) { 
		StringPair cmPair = new StringPair("", mname);
		for (String c : cnames) {
			cmPair.first = c;
			List<List<NullALatticeElement>> argList = cmArgsCurrent.getOrDefault(cmPair, new ArrayList<List<NullALatticeElement>>());
			argList.add(argLEs);
			cmArgsCurrent.put(cmPair, argList);
		}
	}

	// Input is a list of StringPairs that are Class, Field.
	public List<Record> getFreshFieldRecords(String c, Set<String> s) {
		List<Record> rl = new ArrayList<Record>();

		for (String f : s) {
			rl.add(Record.getFreshFieldRecord(c, f));
		}
		return rl;
	}

	public List<Record> getFreshVariableRecords(String c, Set<String> s) {
		List<Record> rl = new ArrayList<Record>();
		for (String f : s) {
			rl.add(Record.getFreshVariableRecord(c, f));
		}
		return rl;
	}

	public List<Record> getFreshArgumentRecords(String c, List<String> s) {
		List<Record> rl = new ArrayList<Record>();
		for (String f : s) {
			rl.add(Record.getFreshVariableRecord(c, f));
		}
		return rl;
	}

	public List<Record> refreshClassFields(StringPair classMethodPair) {
		String curClass = classMethodPair.first;
		List<Record> fieldRecords = new ArrayList<Record>();
		while (curClass != null) {
			Set<String> fields = cha.getClassFields(curClass);
			fieldRecords.addAll(getFreshFieldRecords(curClass, fields));

			curClass = cha.getSuperClass(curClass);
		}
		return fieldRecords;
	}


	// Both of these have argument s which is
	public void flushMethodRecordStore(StringPair classMethodPair) { 
		cmLastOutCurrent.put(classMethodPair, methodRecordStore);
	}

	public void freshMethodRecordStore(StringPair classMethodPair) {
		methodRecordStore = new ArrayList<Record>();

		// 1. Get fresh fields usng the getFreshFieldRecords
		// These fields can come from both the current class as well as any superclass
		methodRecordStore.addAll(refreshClassFields(classMethodPair));

		// 2a. Get fresh variables using the getFreshVariableRecords
		Set<String> variables = cha.getMethodVariables(classMethodPair);
		List<Record> variableRecords = getFreshVariableRecords(classMethodPair.first, variables);
		methodRecordStore.addAll(variableRecords);

		// 2b. Get whatever is the newest cmArgsPrevious
		List<Record> inArgRecords = cmArgsPrevious.getOrDefault(classMethodPair, new ArrayList<Record>());
		if (inArgRecords.isEmpty()) {
			List<String> args = cha.getMethodArguments(classMethodPair);
			inArgRecords = getFreshArgumentRecords(classMethodPair.first, args);
		}

		methodRecordStore.addAll(inArgRecords);
	}

	public List<Record> getUniqueRecords(List<Record> lr) {
		Set<StringPair> uniqueSPs = new HashSet<StringPair>();
		List<Record> uniqueRecords = new ArrayList<Record>();

		ListIterator<Record> li = methodRecordStore.listIterator(methodRecordStore.size());
		while (li.hasPrevious()) {
			Record r = li.previous();
			StringPair cIden = r.getClassIdentifierSP();
			boolean field = r.isField();
			if (field) {
				cIden.second = cIden.second + "~";
			}
			if (!uniqueSPs.contains(cIden)) {
				uniqueSPs.add(cIden);
				uniqueRecords.add(r);
			}
		}

		return uniqueRecords;
	}


	// Called at the end of an iteration
	public void refreshCMOut() { 
		List<Record> rl;
		for (StringPair k : cmLastOutCurrent.keySet()) {
			List<Record> newrl = new ArrayList<Record>();

			// outset contains all of the keys that we currently care about.
			Set<StringPair> outSet = new HashSet<StringPair>();
			rl = cmLastOutCurrent.get(k);

			ListIterator<Record> li = rl.listIterator(rl.size());
			while (li.hasPrevious()) {
				Record r = li.previous();
				StringPair id = r.getClassIdentifierSP();
				if (r.isField() && !(outSet.contains(id))) {
					outSet.add(id);
					newrl.add(r);
				}
			}

			cmLastOutPrevious.put(k, newrl);
			cmLastOutCurrent.put(k, new ArrayList<Record>());
		}	
	}

	// Need to get a least upper bound of NullALatticeElement
	public void refreshCMArgs() {
		for (StringPair k : cmArgsCurrent.keySet()) {
			List<Record> newCMArgRecs = new ArrayList<Record>();
			List<String> methodArgs = cha.getMethodArguments(k);
			List<List<NullALatticeElement>> methodCalls = cmArgsCurrent.get(k);
			NullALatticeElement e = null;
			for (int i = 0; i < methodArgs.size(); i++) {
				for (List<NullALatticeElement> lle : methodCalls){
					if (e == null)
						e = lle.get(i);
					else 
						e = NullALatticeElement.leastUpperBound(e, lle.get(i));
				}
				newCMArgRecs.add(Record.getNewRecord(k.first, methodArgs.get(i), false, e, null));
			}
			cmArgsPrevious.put(k, newCMArgRecs);
			cmArgsCurrent.put(k, new ArrayList<List<NullALatticeElement>>());
		}
	}

}