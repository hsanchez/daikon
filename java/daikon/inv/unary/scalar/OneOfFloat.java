// ***** This file is automatically generated from OneOf.java.jpp

package daikon.inv.unary.scalar;

import daikon.*;
import daikon.inv.*;
import daikon.derive.unary.*;
import daikon.inv.unary.scalar.*;
import daikon.inv.unary.sequence.*;
import daikon.inv.binary.sequenceScalar.*;
import daikon.inv.binary.twoSequence.SubSequence;

import utilMDE.*;

import org.apache.log4j.Logger;

import java.util.*;
import java.io.*;

// States that the value is one of the specified values.

// This subsumes an "exact" invariant that says the value is always exactly
// a specific value.  Do I want to make that a separate invariant
// nonetheless?  Probably not, as this will simplify implication and such.

public final class OneOfFloat
  extends SingleFloat
  implements OneOf
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  /**
   * Debugging logger.
   **/
  public static final Logger debug = Logger.getLogger (OneOfFloat.class.getName());

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff OneOf invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  /**
   * Positive integer.  Specifies the maximum set size for this type
   * of invariant (x is one of 'n' items).
   **/

  public static int dkconfig_size = 3;

  // Probably needs to keep its own list of the values, and number of each seen.
  // (That depends on the slice; maybe not until the slice is cleared out.
  // But so few values is cheap, so this is quite fine for now and long-term.)

  private double[] elts;
  private int num_elts;

  public OneOfFloat(PptSlice ppt) {
    super(ppt);

    elts = new double[dkconfig_size];

    num_elts = 0;

  }

  public static OneOfFloat instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;
    return new OneOfFloat(ppt);
  }

  protected Object clone() {
    OneOfFloat result = (OneOfFloat) super.clone();
    result.elts = (double[]) elts.clone();

    result.num_elts = this.num_elts;

    return result;
  }

  public int num_elts() {
    return num_elts;
  }

  public Object elt() {
    if (num_elts != 1)
      throw new Error("Represents " + num_elts + " elements");

    return Intern.internedDouble(elts[0]);
  }

  private void sort_rep() {
    Arrays.sort(elts, 0, num_elts );
  }

  public double min_elt() {
    if (num_elts == 0)
      throw new Error("Represents no elements");
    sort_rep();
    return elts[0];
  }

  public double max_elt() {
    if (num_elts == 0)
      throw new Error("Represents no elements");
    sort_rep();
    return elts[num_elts-1];
  }

  // Assumes the other array is already sorted
  public boolean compare_rep(int num_other_elts, double[] other_elts) {
    if (num_elts != num_other_elts)
      return false;
    sort_rep();
    for (int i=0; i < num_elts; i++)
      if (! ((elts[i] == other_elts[i]) || (Double.isNaN(elts[i]) && Double.isNaN(other_elts[i])))) // elements are interned
        return false;
    return true;
  }

  private String subarray_rep() {
    // Not so efficient an implementation, but simple;
    // and how often will we need to print this anyway?
    sort_rep();
    StringBuffer sb = new StringBuffer();
    sb.append("{ ");
    for (int i=0; i<num_elts; i++) {
      if (i != 0)
        sb.append(", ");
      sb.append(String.valueOf(elts[i]));
    }
    sb.append(" }");
    return sb.toString();
  }

  public String repr() {
    return "OneOfFloat" + varNames() + ": "
      + "falsified=" + falsified
      + ", num_elts=" + num_elts
      + ", elts=" + subarray_rep();
  }

  public String format_using(OutputFormat format) {
    sort_rep();
    if (format == OutputFormat.DAIKON) {
      return format_daikon();
    } else if (format == OutputFormat.JAVA) {
      return format_java();
    } else if (format == OutputFormat.IOA) {
      return format_ioa();
    } else if (format == OutputFormat.SIMPLIFY) {
      return format_simplify();
    } else if (format == OutputFormat.ESCJAVA) {
      return format_esc();
    } else if (format == OutputFormat.JML) {
      return format_jml();
    } else {
      return format_unimplemented(format);
    }
  }

  public String format_daikon() {
    String varname = var().name.name();
    if (num_elts == 1) {

      return varname + " == " + String.valueOf(elts[0]);
    } else {
      return varname + " one of " + subarray_rep();
    }
  }

  /*
    public String format_java() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < num_elts; i++) {
    sb.append (" || (" + var().name.java_name() + ".equals(" + String.valueOf(elts[i]) + ")");
    sb.append (")");
    }
    // trim off the && at the beginning for the first case
    return sb.toString().substring (4);
    }
  */

  public String format_java() {
    // have to take a closer look at this!
    sort_rep();

    String varname = var().name.java_name();

    String result;

    {
      result = "";
      for (int i=0; i<num_elts; i++) {
        if (i != 0) { result += " || "; }
        // Not quite right for the case of NaN, I think.
        result += varname + " == " + String.valueOf(elts[i]);
      }
    }

    return result;
  }

  /* IOA */
  public String format_ioa() {
    sort_rep();

    String varname = var().name.ioa_name();

    String result;

    {
      result = "";
      for (int i=0; i<num_elts; i++) {
        if (i != 0) { result += " \\/ "; }
        result += "(" + varname + " = " + String.valueOf(elts[i]) + ")";
      }
    }

    return result;
  }

  public String format_esc() {
    sort_rep();

    String varname = var().name.esc_name();

    String result;

    {
      result = "";
      for (int i=0; i<num_elts; i++) {
        if (i != 0) { result += " || "; }
        result += varname + " == " + String.valueOf(elts[i]);
      }
    }

    return result;
  }

  public String format_jml() {

    String varname = var().name.JMLElementCorrector().jml_name();

    String result;

    {
      result = "";
      for (int i=0; i<num_elts; i++) {
        if (i != 0) { result += " || "; }
        result += varname + " == " + String.valueOf(elts[i]);
      }
    }

    return result;
  }

  public String format_simplify() {

    return "format_simplify failed: " +
      "Simplify can't express floating point constants";

  }

  public void add_modified(double v, int count) {

    for (int i=0; i<num_elts; i++)
      if (elts[i] == v) {

        return;

      }
    if (num_elts == dkconfig_size) {
      destroyAndFlow();
      return;
    }

    // We are significantly changing our state (not just zeroing in on
    // a constant), so we have to flow a copy before we do so.  We even
    // need to clone if this has 0 elements becuase otherwise, lower
    // ppts will get versions of this with multiple elements once this is
    // expanded.
    cloneAndFlow();

    elts[num_elts] = v;
    num_elts++;

  }

  protected double computeProbability() {
    // This is not ideal.
    if (num_elts == 0) {
      return Invariant.PROBABILITY_UNJUSTIFIED;
    } else {
      return Invariant.PROBABILITY_JUSTIFIED;
    }
  }

  public boolean isObviousStatically(VarInfo[] vis) {
    // Static constants are necessarily OneOf precisely one value.
    // This removes static constants from the output, which might not be
    // desirable if the user doesn't know their actual value.
    if (vis[0].isStaticConstant()) {
      Assert.assertTrue(num_elts <= 1);
      return true;
    }
    return super.isObviousStatically(vis);
  }

  public boolean isObviousDynamically(VarInfo[] vis) {
    VarInfo v = vis[0];
    if (v.isDerived() && (v.derived instanceof SequenceLength)) {
      SequenceLength sl = (SequenceLength) v.derived;
      if (sl.shift != 0) {
        return true;
      }
    }

    // For every EltOneOfFloat at this program point, see if this variable is
    // an obvious member of that sequence.
    PptTopLevel parent = ppt.parent;
    for (Iterator itor = parent.invariants_iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if ((inv instanceof EltOneOfFloat) && inv.enoughSamples()) {
        VarInfo v1 = var();
        VarInfo v2 = inv.ppt.var_infos[0];
        // System.out.println("isObviousImplied: calling  Member.isObviousMember(" + v1.name + ", " + v2.name + ")");
        // Don't use isEqualToObviousMember:  that is too subtle
        // and eliminates desirable invariants such as "return == null".
        if (Member.isObviousMember(v1, v2)) {
          EltOneOfFloat other = (EltOneOfFloat) inv;
          if (num_elts == other.num_elts()) {
            sort_rep();
            if (other.compare_rep(num_elts, elts)) {
              // System.out.println("isObviousImplied true");
              return true;
            }
          }
        }
      }
    }

    return super.isObviousDynamically(vis);
  }

  public boolean isSameFormula(Invariant o)
  {
    OneOfFloat other = (OneOfFloat) o;
    if (num_elts != other.num_elts)
      return false;
    if (num_elts == 0 && other.num_elts == 0)
      return true;

    sort_rep();
    other.sort_rep();

    for (int i=0; i < num_elts; i++) {
      if (! ((elts[i] == other.elts[i]) || (Double.isNaN(elts[i]) && Double.isNaN(other.elts[i]))))
        return false;
    }

    return true;
  }

  public boolean isExclusiveFormula(Invariant o)
  {
    if (o instanceof OneOfFloat) {
      OneOfFloat other = (OneOfFloat) o;

      for (int i=0; i < num_elts; i++) {
        for (int j=0; j < other.num_elts; j++) {
          if (((elts[i] == other.elts[j]) || (Double.isNaN(elts[i]) && Double.isNaN(other.elts[j])))) // elements are interned
            return false;
        }
      }

      return true;
    }

    // Many more checks can be added here:  against nonzero, modulus, etc.
    if ((o instanceof NonZeroFloat) && (num_elts == 1) && (elts[0] == 0)) {
      return true;
    }
    double elts_min = Double.MAX_VALUE;
    double elts_max = Double.MIN_VALUE;
    for (int i=0; i < num_elts; i++) {
      elts_min = Math.min(elts_min, elts[i]);
      elts_max = Math.max(elts_max, elts[i]);
    }
    if ((o instanceof LowerBoundFloat) && (elts_max < ((LowerBoundFloat)o).core.min1))
      return true;
    if ((o instanceof UpperBoundFloat) && (elts_min > ((UpperBoundFloat)o).core.max1))
      return true;

    return false;
  }

  // OneOf invariants that indicate a small set of possible values are
  // uninteresting.  OneOf invariants that indicate exactly one value
  // are interesting.
  public boolean isInteresting() {
    if (num_elts() > 1) {
      return false;
    } else {
      return true;
    }
  }

  // Look up a previously instantiated invariant.
  public static OneOfFloat find(PptSlice ppt) {
    Assert.assertTrue(ppt.arity == 1);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof OneOfFloat)
        return (OneOfFloat) inv;
    }
    return null;
  }

  // Interning is lost when an object is serialized and deserialized.
  // Manually re-intern any interned fields upon deserialization.
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    for (int i=0; i < num_elts; i++)
      elts[i] = Intern.intern(elts[i]);
  }

}
