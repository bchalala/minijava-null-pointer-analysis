
import syntaxtree.*;
import java.util.*;

class NullCheck {
    public static void main(String[] args){
        try {
            Goal goal = new MiniJavaParser(System.in).Goal();
            
            // visit the AST and gather classes for each method
            ClassMethodVisitor cmv = new ClassMethodVisitor();
            goal.accept(cmv, null);
            cmv.cha.setCFandMVandMA(cmv.classFields, cmv.methodVariables, cmv.methodArguments);

            // Testing output
            /*
            System.out.println("Class fields\n" + cmv.classFields);
            System.out.println("Method variables\n" + cmv.methodVariables);
            System.out.println("Method arguments\n" + cmv.methodArguments);

            System.out.println("\nCHA Analysis:");
            cmv.cha.print();
            System.out.println("\n");
            */

            NullPtrAnalysis npa = new NullPtrAnalysis(cmv.cha);
            NullPtrAnalysisVisitor npaVisitor = new NullPtrAnalysisVisitor(cmv.cha, npa);
            goal.accept(npaVisitor, new Context(cmv.cha, npa));
            do {
                npa.newIteration();
                goal.accept(npaVisitor, new Context(cmv.cha, npa));
            } while (npa.shouldIterate());

            npa.newIteration();
            goal.accept(npaVisitor, new Context(cmv.cha, npa));

            Record.resetCounter();
            NullPtrCheckVisitor npaCheck = new NullPtrCheckVisitor(cmv.cha, npa);
            goal.accept(npaCheck, new Context(cmv.cha, npa));
        } 
        catch (ParseException e) {
            System.out.println("PARSE EXCEPTION: ");
            System.out.println(e.toString());
        }
    }
}
