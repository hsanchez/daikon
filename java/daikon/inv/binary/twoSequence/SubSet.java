// ***** This file is automatically generated from SubSet.java.jpp

package daikon.inv.binary.twoSequence;

import daikon.*;
import daikon.inv.*;
import daikon.derive.*;
import daikon.derive.binary.*;
import daikon.suppress.*;
import daikon.inv.unary.sequence.EltOneOf;
import daikon.VarInfoName.QuantHelper;
import daikon.VarInfoName.QuantHelper.QuantifyReturn;

import java.util.*;
import utilMDE.*;
import org.apache.log4j.Logger;

public class SubSet
  extends TwoSequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  private static final Logger debug =
    Logger.getLogger("daikon.inv.binary.twoSequence.SubSet");

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff SubSet invariants should be considered.
   **/
  public static boolean dkconfig_enabled = false;

  public boolean var1_in_var2 = true;
  public boolean var2_in_var1 = true;

  protected SubSet(PptSlice ppt) {
    super(ppt);
  }

  public static SubSet instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;

    VarInfo var1 = ppt.var_infos[0];
    VarInfo var2 = ppt.var_infos[1];
    if ((SubSet.isObviousSubSet(var1, var2))
        || (SubSet.isObviousSubSet(var2, var1))) {
      Global.implied_noninstantiated_invariants++;
      if (debug.isDebugEnabled()) {
        debug.debug (var1 + ", " + var2);
        debug.debug ("Obvious derived, returning null");
      }
      return null;
    }

    if (debug.isDebugEnabled()) {
      debug.debug ("Instantiating " + var1.name + " and " + var2.name);
    }

    return new SubSet(ppt);
  }

  protected Invariant resurrect_done_swapped() {
    // was a swap
    boolean tmp = var1_in_var2;
    var1_in_var2 = var2_in_var1;
    var2_in_var1 = tmp;
    return this;
  }

  public String repr() {
    return "SubSet" + varNames() + ": "
      + "1in2=" + var1_in_var2
      + ",2in1=" + var2_in_var1
      + ",falsified=" + falsified;
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.DAIKON) return format();
    if (format == OutputFormat.IOA) return format_ioa();
    if (format == OutputFormat.JAVA) return format();
    if (format == OutputFormat.ESCJAVA) return format_esc();
    if (format == OutputFormat.JML) return format_jml();

    return format_unimplemented(format);
  }

  public String format() {
    String v1 = var1().name.name();
    String v2 = var2().name.name();
    if (var1_in_var2 && var2_in_var1) {
      return v1 + " is a {sub,super}set of " + v2;
    } else {
      String subvar = (var1_in_var2 ? v1 : v2);
      String supervar = (var1_in_var2 ? v2 : v1);
      return subvar + " is a subset of " + supervar;
    }
  }

  /* IOA */
  public String format_ioa() {
    String result;
    String v1 = var1().name.ioa_name();
    String v2 = var2().name.ioa_name();
    if (var1_in_var2 && var2_in_var1) {
      result = "(" + v1 + " \\subseteq " + v2 + ") /\\ (" + v2 + " \\subseteq " + v1 + ")";
    } else {
      String subvar = (var1_in_var2 ? v1 : v2);
      String supervar = (var1_in_var2 ? v2 : v1);
      result = subvar + " \\subseteq " + supervar;
    }

    if (var1().isIOAArray() || var2().isIOAArray()) {
      // Temporarily disabled because IOA frontend outputs sets as
      // arrays for comparability.

      // result += " *** (Invalid syntax for arrays)";
    }

    return result;
  }

  public String format_esc() {
    String classname = this.getClass().toString().substring(6); // remove leading "class"
    return "warning: method " + classname + ".format_esc() needs to be implemented: " + format();
  }

  public String format_simplify() {
    String classname = this.getClass().toString().substring(6); // remove leading "class"
    return "warning: method " + classname + ".format_simplify() needs to be implemented: " + format();
  }

  public String format_jml() {
    String classname = this.getClass().toString().substring(6); // remove leading "class"
    return "warning: method " + classname + ".format_jml() needs to be implemented: " + format();
  }

  public void add_modified(long[] a1, long[] a2, int count) {
    boolean new_var1_in_var2 = var1_in_var2;
    boolean new_var2_in_var1 = var2_in_var1;
    boolean changed = false;
    if (debug.isDebugEnabled()) {
      debug.debug (a1);
      debug.debug (a2);
    }

    if (new_var1_in_var2 && (!ArraysMDE.isSubset(a1, a2))) {
      new_var1_in_var2 = false;
      debug.debug ("Falsified 1");
      if (!new_var2_in_var1) {
        destroyAndFlow();
        return;
      } else {
        // We are weakening me
        changed = true;
      }
    }
    if (new_var2_in_var1 && (!ArraysMDE.isSubset(a2, a1))) {
      new_var2_in_var1 = false;
      debug.debug ("Falsified 2");
      if (!new_var1_in_var2) {
        destroyAndFlow();
        return;
      } else {
        // We are weakening me
        changed = true;
      }
    }
    if (changed) {
      cloneAndFlow();
    }
    var1_in_var2 = new_var1_in_var2;
    var2_in_var1 = new_var2_in_var1;
    Assert.assertTrue(var1_in_var2 || var2_in_var1);
  }

  protected double computeProbability() {
    if (falsified)
      return Invariant.PROBABILITY_NEVER;
    else if (var1_in_var2 && var2_in_var1)
      return Invariant.PROBABILITY_UNJUSTIFIED;
    else
      return Invariant.PROBABILITY_JUSTIFIED;
  }

  // Convenience name to make this easier to find.
  public static boolean isObviousSubSet(VarInfo subvar, VarInfo supervar) {
    return SubSequence.isObviousSubSequence(subvar, supervar);
  }

  public boolean isObviousStatically(VarInfo[] vis) {
    VarInfo var1 = vis[0];
    VarInfo var2 = vis[1];
    if ((SubSet.isObviousSubSet(var1, var2))
        || (SubSet.isObviousSubSet(var2, var1))) {
      return true;
    }
    return super.isObviousStatically(vis);
  }

  // Look up a previously instantiated SubSet relationship.
  public static SubSet find(PptSlice ppt) {
    Assert.assertTrue(ppt.arity == 2);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof SubSet)
        return (SubSet) inv;
    }
    return null;
  }

  // Two ways to go about this:
  //   * look at all subseq relationships, see if one is over a variable of
  //     interest
  //   * look at all variables derived from the

  // (Seems overkill to check for other transitive relationships.
  // Eventually that is probably the right thing, however.)
  public boolean isObviousDynamically(VarInfo[] vis) {

    // System.out.println("checking isObviousImplied for: " + format());

    if (var1_in_var2 && var2_in_var1) {
      // Suppress this invariant; we should get an equality invariant from
      // elsewhere.
      return true;
    }
    return super.isObviousDynamically(vis);
  }

  public boolean isSameFormula(Invariant other)
  {
    Assert.assertTrue(other instanceof SubSet);
    return true;
  }

  private static SuppressionFactory[] suppressionFactories = null;

  public SuppressionFactory[] getSuppressionFactories() {
    if (suppressionFactories == null) {
      SuppressionFactory[] supers = super.getSuppressionFactories();
      suppressionFactories = new SuppressionFactory[supers.length + 1];
      System.arraycopy (supers, 0, suppressionFactories, 0, supers.length);
      suppressionFactories[supers.length] = SubSetSuppressionFactory.getInstance();
    }
    return suppressionFactories;
  }

  /**
   * Suppression generator for SubSet type invariants.  When A and B
   * are subsets of each other, they're just equal, so no need to check
   * them.  Replaces isObviousImplied.
   **/

  static class SubSetSuppressionFactory extends SuppressionFactory {

    public static final Logger debug = Logger.getLogger ("daikon.suppress.factories.SubSetSuppressionFactory");

    private static final SubSetSuppressionFactory theInstance =
      new SubSetSuppressionFactory();

    public static SuppressionFactory getInstance() {
      return theInstance;
    }

    private Object readResolve() {
      return theInstance;
    }

    public SuppressionLink generateSuppressionLink (Invariant arg) {
      Assert.assertTrue (arg instanceof SubSet);
      SubSet inv = (SubSet) arg;

      SuppressionTemplate template = new SuppressionTemplate();
      template.invTypes = new Class[] {PairwiseIntComparison.class};
      template.varInfos = new VarInfo[][] {new VarInfo[] {inv.var1(), inv.var2()}};

      SuppressionLink result = byTemplate (template, inv);
      if (result != null) {
        String comparator = ((PairwiseIntComparison) template.results[0]).getComparator();
        if (comparator.indexOf("=") > -1 ||
            comparator.indexOf("?") > -1) {
          return result;
        }
      }
      return null;
    }

  }

}
