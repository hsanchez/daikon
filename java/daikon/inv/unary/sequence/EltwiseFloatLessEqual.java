package daikon.inv.unary.sequence;

// New jpp logic (one class for each type of comparison, eg. <, >, =, etc.)

  // These definitions are included such that the class names can be changed
  // easily and furthermore such that segments of this code can be easily
  // modified to work elsewhere.

// Old jpp logic (one comparison type)

// #if !(defined(TYPELONG) || defined())
// #error "ONE MUST BE DEFINED"
// #endif

// #if defined()
// #define EltwiseFloatLessEqual EltwiseFloatComparison
// #define EltwiseFloatComparison SingleFloatSequence
// #define double double
// #define CORECLASS FloatComparisonCore
// #define "EltwiseFloatLessEqual" "EltwiseFloatComparison"
// #define EltOneOfFloat EltOneOfFloat
// #elif defined(TYPELONG)
// #define EltwiseFloatLessEqual EltwiseIntComparison
// #define EltwiseFloatComparison SingleScalarSequence
// #define double long
// #define CORECLASS IntComparisonCore
// #define "EltwiseFloatLessEqual" "EltwiseIntComparison"
// #define EltOneOfFloat EltOneOf
// #endif

// ***** This file is automatically generated from EltwiseIntComparison.java.jpp

import daikon.*;
import daikon.derive.*;
import daikon.derive.binary.*;
import daikon.inv.*;
// import daikon.inv.binary.twoScalar.CORECLASS;
import utilMDE.*;
import java.util.*;

/**
 * This compares adjacent elements in the sequence.
 **/
public class EltwiseFloatLessEqual
  extends EltwiseFloatComparison
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20030112L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff EltwiseIntComparison invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  final static boolean debugEltwiseIntComparison = false;

  // If no sample passes by that has 2 or more elements in an array then the
  // invariant is vacuously true; no comparison was ever made and thus the
  // invariant remains true by default. However, it is certainly not worth
  // printing in this case.
  private boolean seenNonTrivialSample = false;

  // Not using core after splitting invariant into different comparison types
  // public CORECLASS core;

  protected EltwiseFloatLessEqual(PptSlice ppt) {
    super(ppt);
  }

  public static EltwiseFloatLessEqual instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;
    // Don't compute ordering relationships over object addresses for
    // elements of a Vector.  (But do compute equality/constant!)

    // This check was before contained in the core

    if (!ppt.var_infos[0].type.baseIsIntegral())
      return null;

    return new EltwiseFloatLessEqual(ppt);
  }

  protected Object clone() {
    EltwiseFloatLessEqual result = (EltwiseFloatLessEqual) super.clone();
    return result;
  }

  public String repr() {
    return "EltwiseFloatLessEqual" + varNames() + ": "
      + "falsified=" + falsified;
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.DAIKON) return format_daikon();
    if (format == OutputFormat.IOA) return format_ioa();
    if (format == OutputFormat.ESCJAVA) return format_esc();
    if (format == OutputFormat.SIMPLIFY) return format_simplify();
    if (format == OutputFormat.JML) return format_jml();

    return format_unimplemented(format);
  }

  public String format_daikon() {
    if (debugEltwiseIntComparison) {
      System.out.println(repr());
    }

    return (var().name.name() + " sorted by <=");
  }

  /* IOA */
  public String format_ioa() {
    VarInfoName.QuantHelper.IOAQuantification quant = new VarInfoName.QuantHelper.IOAQuantification(var(), var());

    String comparator = "<=";
    String result = quant.getQuantifierExp() + "(" + quant.getMembershipRestriction(0) + " /\\ " + quant.getMembershipRestriction(1);
    if ("==".equals(comparator)) {
      // i \in X /\ j \in X => X[i] = X[j]
      result = result + ") => " + quant.getVarIndexed(0) + " = " + quant.getVarIndexed(1);
    } else {
      // i \in X /\ j \in X /\ i+1 = j => X[i] = X[j]
      result = result +
        " /\\ " + quant.getVarName(0) + "+1 = " + quant.getVarName(1) +
        ") => " +
        quant.getVarIndexed(0) + " = " + quant.getVarIndexed(1);
    }
    return result + quant.getClosingExp();
  }

  public String format_esc() {
    String[] form =
      VarInfoName.QuantHelper.format_esc(new VarInfoName[]
        { var().name, var().name });
    String comparator = "<=";
    if ("==".equals(comparator)) {
      return form[0] + "(" + form[1] + " == " + form[2] + ")" + form[3];
    } else {
      return form[0] + "((i+1 == j) ==> (" + form[1] + " " + comparator + " " + form[2] + "))" + form[3];
    }
  }

  public String format_jml() {
    String[] form =
      VarInfoName.QuantHelper.format_jml(new VarInfoName[]
        { var().name, var().name });
    String comparator = "<=";
    if ("==".equals(comparator)) {
      return form[0] + "(" + form[1] + " == " + form[2] + ")" + form[3];
    } else {
      return form[0] + "((i+1 == j) ==> (" + form[1] + " " + comparator + " " + form[2] + ")" + form[3];
    }
  }

  public String format_simplify() {
    String[] form =
      VarInfoName.QuantHelper.format_simplify(new VarInfoName[]
        { var().name, var().name });
    String comparator = "<=";
    if ("==".equals(comparator)) {
      return form[0] + "(EQ " + form[1] + " " + form[2] + ")" + form[3];
    } else {
      return form[0] + "(IMPLIES (EQ (+ i 1) j) (" + comparator + " " + form[1] + " " + form[2] + "))" + form[3];
    }
  }

  public void add_modified(double[] a, int count) {
    if (a.length >= 2)
      seenNonTrivialSample = true;
    for (int i=1; i<a.length; i++) {
      if (!(a[i-1] <= a[i])) {
        destroyAndFlow();
        return;
      }
    }
  }

  // Perhaps check whether all the arrays of interest have length 0 or 1.

  protected double computeProbability() {
    if (falsified) {
      return Invariant.PROBABILITY_NEVER;
    }

      return Math.pow(.5, ppt.num_samples());
  }

  public boolean isExact() {

    return false;
  }

  public boolean isSameFormula(Invariant other)
  {
    return (other instanceof EltwiseFloatLessEqual);
  }

  // Not pretty... is there another way?
  // Also, reasonably complicated, need to ensure exact correctness, not sure if the
  // regression tests test this functionality

  public boolean isExclusiveFormula(Invariant other)
  {
    if (other instanceof EltwiseFloatComparison) {

      return (other instanceof EltwiseIntGreaterThan) || (other instanceof EltwiseFloatGreaterThan);
    }
    return false;
  }

  // Look up a previously instantiated invariant.
  public static EltwiseFloatLessEqual find(PptSlice ppt) {
    Assert.assertTrue(ppt.arity == 1);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof EltwiseFloatLessEqual)
        return (EltwiseFloatLessEqual) inv;
    }
    return null;
  }

  // Copied from IntComparison.
  // public boolean isExclusiveFormula(Invariant other)
  // {
  //   if (other instanceof IntComparison) {
  //     return core.isExclusiveFormula(((IntComparison) other).core);
  //   }
  //   if (other instanceof IntNonEqual) {
  //     return isExact();
  //   }
  //   return false;
  // }

  /**
   * This function returns whether a sample has been seen by this Invariant
   * that includes two or more entries in an array. For a 0 or 1 element array
   * a, a[] sorted by any binary operation is "vacuously true" because no check
   * is ever made since the binary operation requires two operands.  Thus
   * although invariants of this type are true regarding 0 or 1 length arrays,
   * they are meaningless.  This function is meant to be used in isObviousImplied()
   * to prevent such meaningless invariants from being printed.
   */
  public boolean hasSeenNonTrivialSample() {
    return seenNonTrivialSample;
  }

  // Note to self: Be sure to port this back to version 2l

  public boolean isObviousDynamically() {
    if (!hasSeenNonTrivialSample())
      return true;

    EltOneOfFloat eoo = EltOneOfFloat.find(ppt);
    if ((eoo != null) && eoo.enoughSamples() && (eoo.num_elts() == 1)) {
      return true;
    }

    {
    // some relations imply others as follows: < -> <=, > -> >=, > or < -> !=,
    // == -> <=, >=
    // also, a contains no duplicates -> !=

    // This code lets through some implied invariants, here is how:
    // In the ESC, JML, Java modes of output, the invariants are guarded before
    // printing.  This guarding makes some of the invariants that are searched for
    // (example, NoDuplicates) unable to be found since it won't find something of
    // the form (a -> NoDuplicates).  This can cause cases to exist, for example,
    // the invariants (a -> b[] sorted by !=), (a -> b[] has no duplicates) existing
    // in the same ppt where one is obviously implied by the other. I am not sure
    // currently of the best way with dealing with this, but I am going to allow
    // these other invariants to exist for now because they are not wrong, just
    // obvious.

    Iterator invs = ppt.invs.iterator();
    while (invs.hasNext()) {
      Invariant inv = (Invariant)invs.next();
      if ((inv instanceof EltwiseIntLessThan) || (inv instanceof EltwiseFloatLessThan) ||
          (inv instanceof EltwiseIntEqual) || (inv instanceof EltwiseFloatEqual))
        return true;
    }

    }

    // sorted by (any operation) for an entire sequence -> sorted by that same
    // operation for a subsequence

    // Is there a better way to check if the VarInfo in this invariant is part
    // of a larger sequence?

    Derivation deriv = ppt.var_infos[0].derived;

    if (deriv != null && deriv instanceof SequenceSubsequence) {
      // Find the slice with the full sequence, check for an invariant of this type
      PptSlice sliceToCheck = ppt.parent.findSlice(((SequenceSubsequence)deriv).seqvar());
      if (sliceToCheck != null) {
        Iterator invs = sliceToCheck.invs.iterator();

        while (invs.hasNext()) {
          Invariant inv = (Invariant)invs.next();
          if (inv.getClass().equals(getClass()))
            return true;
        }
      }
    }

    return false;
  }
}
