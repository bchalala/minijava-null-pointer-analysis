
import java.util.*;

public class NullPtrAnalysis {

	ClassHierarchyAnalysis cha;

	public NullPtrAnalysis(ClassHierarchyAnalysis cha) {
		this.cha = cha;
	}
	
	// This is going to store the outputs of all the previous iteration's method outs. 
	private HashMap<StringPair, List<Record>> cmLastOutPrevious = new HashMap<StringPair, List<Record>>();

	// This is going to be the current iteration's last outs. 
	private HashMap<StringPair, List<Record>> cmLastOutCurrent = new HashMap<StringPair, List<Record>>();

	// Output of functions
	private HashMap<StringPair, NullALatticeElement> cmRet = new HashMap<StringPair, NullALatticeElement>();

	public void addRet(StringPair cmPair, NullALatticeElement e) {
		cmRet.put(cmPair, e);
	}

	public void getRet(StringPair cmPair) {
		cmRet.getOrDefault(cmPair, NullALatticeElement.getDontKnow());
	}

	// This is going to store the outputs of all the previous iteration's arg ins.
	// This is the LUB of cmArgsCurrent for a given StringPair.
	private HashMap<StringPair, List<Record>> cmArgsPrevious = new HashMap<StringPair, List<Record>>();

	// This is going to be the current iteration's arg ins. 
	// TODO This should probably be changed to a list of a list of lattice elements
	// Not going to care about the exact record. Everything when evaluated should return lattice element that 
	// it contains.
	private HashMap<StringPair, List<List<Record>>> cmArgsCurrent = new HashMap<StringPair, List<List<Record>>>();

	// This is going to be the map that is basically the kafka-esque record store. 
	// Updates are just thrown into a list. 
	private List<Record> methodRecordStore = new ArrayList<Record>();

	public void commitRecord(Record r) { methodRecordStore.add(r); }
	public void commitBranchRecords(List<Record> rs) { methodRecordStore.addAll(rs); }

	public Record getMostRecentRecord(StringPair cmPair, String iden) {
		boolean isAMethodVar = cha.isMethodVariable(cmPair, iden);
		StringPair thisCISP = new StringPair(cmPair.first, iden);

		ListIterator<Record> li = methodRecordStore.listIterator(methodRecordStore.size());
		while (li.hasPrevious()) {
			Record r = li.previous();
			StringPair id = r.getClassIdentifierSP();

			if (id.equals(thisCISP)) {
				if (isAMethodVar) {
					if (!(r.isField()))
						return r;
				}
				else {
					return r;
				}
			}
		}
		System.out.println("Tried to get the record for " + cIden + " but could not find it.");
		return null;
	}

	public void addMethodCall(StringPair currentCM, StringPair cmCall, List<Record> lr) {
		List<List<Record>> cmCallLR = cmArgsCurrent.getOrDefault(cmCall, new ArrayList<List<Record>>());
		cmCallLR.add(lr);
		cmArgsCurrent.put(cmCall, cmCallLR);

		refreshClassFields(currentCM);
		methodRecordStore.addAll(cmLastOutPrevious.getOrDefault(cmCall, new ArrayList<Record>()));
	}

	public NullALatticeElement getMethodReturn(StringPair cmCall) {
		return cmRet.getOrDefault(cmCall, NullALatticeElement.getDontKnow());
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

	public void refreshClassFields(StringPair classMethodPair) {
		String curClass = classMethodPair.first;
		while (curClass != null) {
			Set<String> fields = cha.getClassFields(curClass);
			List<Record> fieldRecords = getFreshFieldRecords(curClass, fields);
			methodRecordStore.addAll(fieldRecords);
			curClass = cha.getSuperClass(curClass);
		}
	}


	// Both of these have argument s which is
	public void flushMethodRecordStore(StringPair classMethodPair) { 
		cmLastOutCurrent.put(classMethodPair, methodRecordStore);
	}

	public void freshMethodRecordStore(StringPair classMethodPair) {
		methodRecordStore = new ArrayList<Record>();

		// 1. Get fresh fields usng the getFreshFieldRecords
		// These fields can come from both the current class as well as any superclass
		refreshClassFields(classMethodPair);


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
		}	
	}

	// Need to get a least upper bound of NullALatticeElement

	// This needs a bit more thought. When we pass an argument into a function
	// we would like to collect the 
	public void refreshCMArgs() {
		/*
		List<Record> rl;
		for (StringPair k : cmArgsCurrent.keySet()) {
			List<Record> newrl = new ArrayList<Record>();

			HashMap m = 

		}
		*/
	}

}