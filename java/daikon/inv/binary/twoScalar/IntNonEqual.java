package daikon.inv.binary.twoScalar;

import daikon.*;
import daikon.inv.*;
import daikon.inv.unary.sequence.*;
import daikon.inv.unary.scalar.*;
import daikon.inv.binary.sequenceScalar.*;
import daikon.inv.binary.twoSequence.*;
import daikon.derive.*;
import daikon.derive.unary.*;

import utilMDE.*;

import java.util.*;

// *****
// Do not edit this file directly:
// it is automatically generated from IntComparisons.java.jpp
// *****

// Also see NonEqual
public final class IntNonEqual 
  extends TwoScalar  
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff IntNonEqual  invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  final static boolean debugIntNonEqual  = false;

  private ValueTracker values_cache = new ValueTracker(8);

  protected Object clone() {
    IntNonEqual  result = (IntNonEqual ) super.clone();
    result.values_cache = (ValueTracker) values_cache.clone();
    return result;
  }

  protected IntNonEqual (PptSlice ppt) {
    super(ppt);
  }

  public static IntNonEqual  instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;

    VarInfo var1 = ppt.var_infos[0];
    VarInfo var2 = ppt.var_infos[1];
    VarInfo seqvar1 = var1.isDerivedSequenceMember();
    VarInfo seqvar2 = var2.isDerivedSequenceMember();

    if (debugIntNonEqual  || ppt.debugged) {
      System.out.println("IntNonEqual.instantiate(" + ppt.name + ")"
                         + ", seqvar1=" + seqvar1
                         + ", seqvar2=" + seqvar2);
    }

    { // Tests involving sequence lengths.

      SequenceLength sl1 = null;
      if (var1.isDerived() && (var1.derived instanceof SequenceLength))
        sl1 = (SequenceLength) var1.derived;
      SequenceLength sl2 = null;
      if (var2.isDerived() && (var2.derived instanceof SequenceLength))
        sl2 = (SequenceLength) var2.derived;

      // Avoid "size(a)-1 cmp size(b)-1"; use "size(a) cmp size(b)" instead.
      if ((sl1 != null) && (sl2 != null)
          && ((sl1.shift == sl2.shift) && (sl1.shift != 0) || (sl2.shift != 0))) {
        // "size(a)-1 cmp size(b)-1"; should just use "size(a) cmp size(b)"
        return null;
      }
    }

    boolean only_eq = false;
    boolean obvious_lt = false;
    boolean obvious_gt = false;
    boolean obvious_le = false;
    boolean obvious_ge = false;

    if (! (var1.type.isIntegral() && var2.type.isIntegral())) {
      return null;
    }

    // Commented out temporarily.
    if (false && (seqvar1 != null) && (seqvar2 != null)) {
      Derivation deriv1 = var1.derived;
      Derivation deriv2 = var2.derived;
      boolean min1 = (deriv1 instanceof SequenceMin);
      boolean max1 = (deriv1 instanceof SequenceMax);
      boolean min2 = (deriv2 instanceof SequenceMin);
      boolean max2 = (deriv2 instanceof SequenceMax);
      VarInfo super1 = seqvar1.isDerivedSubSequenceOf();
      VarInfo super2 = seqvar2.isDerivedSubSequenceOf();

      if (debugIntNonEqual  || ppt.debugged) {
        System.out.println("IntNonEqual.instantiate: "
                           + "min1=" + min1
                           + ", max1=" + max1
                           + ", min2=" + min2
                           + ", max2=" + max2
                           + ", super1=" + super1
                           + ", super2=" + super2
                           + ", iom(var2, seqvar1)=" + Member.isObviousMember(var2, seqvar1)
                           + ", iom(var1, seqvar2)=" + Member.isObviousMember(var1, seqvar2));
      }
      if (seqvar1 == seqvar2) {
        // Both variables are derived from the same sequence.  The
        // invariant is obvious as soon as it's nonequal, because "all
        // elements equal" will be reported elsewhere.
        if (min1 || max2)
          obvious_lt = true;
        else if (max1 || min2)
          obvious_gt = true;
      } else if ((min1 || max1) && Member.isObviousMember(var2, seqvar1)) {
        if (min1) {
          obvious_le = true;
        } else if (max1) {
          obvious_ge = true;
        }
      } else if ((min2 || max2) && Member.isObviousMember(var1, seqvar2)) {
        if (min2) {
          obvious_ge = true;
        } else if (max2) {
          obvious_le = true;
        }
      } else if (((min1 && max2) || (max1 && min2))
                 && (super1 != null) && (super2 != null) && (super1 == super2)
                 && VarInfo.seqs_overlap(seqvar1, seqvar2)) {
        // If the sequences overlap, then clearly the min of either is no
        // greater than the max of the other.
        if (min1 && max2) {
          obvious_le = true;
          // System.out.println("obvious_le: " + var1.name + " " + var2.name);
        } else if (max1 && min2) {
          obvious_ge = true;
          // System.out.println("obvious_ge: " + var1.name + " " + var2.name);
        }
      }
    }

    return new IntNonEqual (ppt);

  }

  protected Invariant resurrect_done_swapped() {

    // we don't care if things swap; we have symmetry
    return this;

  }

  // Look up a previously instantiated IntNonEqual  relationship.
  // Should this implementation be made more efficient?
  public static IntNonEqual  find(PptSlice ppt) {
    Assert.assert(ppt.arity == 2);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof IntNonEqual )
        return (IntNonEqual ) inv;
    }
    return null;
  }

  public String repr() {
    return "IntNonEqual"  + varNames();
  }

  public String format() {
    return var1().name.name() + " != " + var2().name.name();
  }

  public String format_esc() {
    return var1().name.esc_name() + " != " + var2().name.esc_name();
  }

  /* IOA */
  public String format_ioa() {

    String comparator = "~=";

    return var1().name.ioa_name()+" "+comparator+" "+var2().name.ioa_name();
  }

  public String format_simplify() {

    String comparator = "NEQ";

    return "(" + comparator + " " + var1().name.simplify_name() + " " + var2().name.simplify_name() + ")";
  }

  /* java output */
  public String format_java() {

    String comparator = "!=" ;

    return var1().name.java_name()+" "+comparator+" "+var2().name.java_name();
  }

  public void add_modified(long v1, long v2, int count) {
    // if (ppt.debugged) {
    //   System.out.println("IntNonEqual"  + ppt.varNames() + ".add_modified("
    //                      + v1 + "," + v2 + ", count=" + count + ")");
    // }
    if (!(v1 !=  v2)) {
      flowThis();
      destroy();
      return;
    }

    values_cache.add(v1, v2);

  }

  // This is very tricky, because whether two variables are equal should
  // presumably be transitive, but it's not guaranteed to be so when using
  // this method and not dropping out all variables whose values are ever
  // missing.
  public double computeProbability() {
    if (no_invariant) {
      return Invariant.PROBABILITY_NEVER;
    }
    // Should perhaps check number of samples and be unjustified if too few
    // samples.

    // The reason for this multiplication is that there might be only a
    // very few possible values.  Example:  towers of hanoi has only 6
    // possible (pegA, pegB) pairs.
    return (Math.pow(.5, values_cache.num_values())
	    * Math.pow(.99, ppt.num_mod_non_missing_samples()));

  }

  // For Comparison interface
  public double eq_probability() {
    if (isExact())
      return computeProbability();
    else
      return Invariant.PROBABILITY_NEVER;
  }

  public boolean isExact() {

    return false;

  }

  // // Temporary, for debugging
  // public void destroy() {
  //   if (debugIntNonEqual  || ppt.debugged) {
  //     System.out.println("IntNonEqual.destroy(" + ppt.name + ")");
  //     System.out.println(repr());
  //     (new Error()).printStackTrace();
  //   }
  //   super.destroy();
  // }

  public void add(long v1, long v2, int mod_index, int count) {
    if (ppt.debugged) {
      System.out.println("IntNonEqual"  + ppt.varNames() + ".add("
                         + v1 + "," + v2
                         + ", mod_index=" + mod_index + ")"
                         + ", count=" + count + ")");
    }
    super.add(v1, v2, mod_index, count);
  }

  public boolean isSameFormula(Invariant other)
  {
    return true;
  }

  public boolean isExclusiveFormula(Invariant other)
  {
    // Also ought to check against LinearBinary, etc.

    return false;
  }

  public boolean isObviousImplied() {
    VarInfo var1 = ppt.var_infos[0];
    VarInfo var2 = ppt.var_infos[1];

    { // If we know x<y or x>y, then x!=y is uninteresting
      IntLessThan ilt = IntLessThan.find(ppt);
      if ((ilt != null) /* && ilt.enoughSamples() */ ) {
        return true;
      }
      IntGreaterThan igt = IntGreaterThan.find(ppt);
      if ((igt != null) /* && igt.enoughSamples() */ ) {
        return true;
      }
    }

// #ifndef EQUAL
//     { // Check for comparisons against constants
//       if (var1.isConstant() || (var2.isConstant())) {
//         // One of the two variables is constant.  Figure out which one.
//         VarInfo varconst;
//         VarInfo varnonconst;
//         boolean var1const = var1.isConstant();
//         boolean can_be_lt;
//         boolean can_be_gt;
//         if (var1const) {
//           varconst = var1;
//           varnonconst = var2;
//           can_be_lt = core.can_be_gt;
//           can_be_gt = core.can_be_lt;
//         } else {
//           varconst = var2;
//           varnonconst = var1;
//           can_be_lt = core.can_be_lt;
//           can_be_gt = core.can_be_gt;
//         }
//         // Now "varconst" and "varnonconst" are set.
//         long valconst = ((Long) varconst.constantValue()).longValue();
//         PptSlice1 nonconstslice = ((PptTopLevel)ppt.parent).findSlice(varnonconst);
//         if (nonconstslice != null) {
//           if (can_be_lt) {
//             UpperBound ub = UpperBound.find(nonconstslice);
//             if ((ub != null) && ub.enoughSamples() && ub.core.max1 < valconst) {
//               return true;
//             }
//           } else {
//             LowerBound lb = LowerBound.find(nonconstslice);
//             if ((lb != null) && lb.enoughSamples() && lb.core.min1 > valconst) {
//               return true;
//             }
//           }
//         }
//       }
//     }
// #endif
    { // Sequence length tests

      SequenceLength sl1 = null;
      if (var1.isDerived() && (var1.derived instanceof SequenceLength))
        sl1 = (SequenceLength) var1.derived;
      SequenceLength sl2 = null;
      if (var2.isDerived() && (var2.derived instanceof SequenceLength))
        sl2 = (SequenceLength) var2.derived;

      // "size(a)-1 cmp size(b)-1" is never even instantiated;
      // use "size(a) cmp size(b)" instead.

      // This might never get invoked, as equality is printed out specially.
      VarInfo s1 = (sl1 == null) ? null : sl1.base;
      VarInfo s2 = (sl2 == null) ? null : sl2.base;
      /* [INCR]
      if ((s1 != null) && (s2 != null)
          && (s1.equal_to == s2.equal_to)) {
        // lengths of equal arrays being compared
        return true;
      }
      */

    }

//     { // Sequence sum tests
//       SequenceSum ss1 = null;
//       if (var1.isDerived() && (var1.derived instanceof SequenceSum))
//         ss1 = (SequenceSum) var1.derived;
//       SequenceSum ss2 = null;
//       if (var2.isDerived() && (var2.derived instanceof SequenceSum))
//         ss2 = (SequenceSum) var2.derived;
//       if ((ss1 != null) && (ss2 != null)) {
//         EltLowerBound lb = null;
//         EltUpperBound ub = null;
//         boolean shorter1 = false;
//         boolean shorter2 = false;
//         PptTopLevel parent = (PptTopLevel)ppt.parent;
//         if (SubSequence.isObviousDerived(ss1.base, ss2.base)) {
//           lb = EltLowerBound.find(parent.findSlice(ss2.base));
//           ub = EltUpperBound.find(parent.findSlice(ss2.base));
//           shorter1 = true;
//         } else if (SubSequence.isObviousDerived(ss2.base, ss1.base)) {
//           lb = EltLowerBound.find(parent.findSlice(ss1.base));
//           ub = EltUpperBound.find(parent.findSlice(ss1.base));
//           shorter2 = true;
//         }
//         if ((lb != null) && (!lb.enoughSamples()))
//           lb = null;
//         if ((ub != null) && (!ub.enoughSamples()))
//           ub = null;
//         // We are comparing sum(a) to sum(b).
//         boolean shorter_can_be_lt;
//         boolean shorter_can_be_gt;
//         if (shorter1) {
//           shorter_can_be_lt = core.can_be_lt;
//           shorter_can_be_gt = core.can_be_gt;
//         } else {
//           shorter_can_be_lt = core.can_be_gt;
//           shorter_can_be_gt = core.can_be_lt;
//         }
//         if (shorter_can_be_lt
//             && (lb != null) && ((lb.core.min1 > 0)
//                                 || (core.can_be_eq && lb.core.min1 == 0))) {
//           return true;
//         }
//         if (shorter_can_be_gt
//             && (ub != null) && ((ub.core.max1 < 0)
//                                 || (core.can_be_eq && ub.core.max1 == 0))) {
//           return true;
//         }
//       }
//     }

//     {
//       // (Is this test ever true?  Aren't SeqINTEQUAL and
//       // IntNonEqual  instantiated at the same time?  Apparently not:  see
//       // the printStackTrace below.
//
//       // For each sequence variable, if this is an obvious member, and
//       // it has the same invariant, then this one is obvious.
//       PptTopLevel pptt = (PptTopLevel) ppt.parent;
//       for (int i=0; i<pptt.var_infos.length; i++) {
//         VarInfo vi = pptt.var_infos[i];
//         if (Member.isObviousMember(var1, vi)) {
//           PptSlice2 other_slice = pptt.findSlice_unordered(vi, var2);
//           if (other_slice != null) {
//             SeqINTEQUAL sic = SeqINTEQUAL.find(other_slice);
//             if ((sic != null)
//                 && sic.enoughSamples()) {
//               // This DOES happen; verify by running on replace.c
//               // System.out.println("Surprise:  this can happen (var1 in INTEQUAL).");
//               // new Error().printStackTrace();
//               return true;
//             }
//           }
//         }
//         if (Member.isObviousMember(var2, vi)) {
//           PptSlice2 other_slice = pptt.findSlice_unordered(vi, var1);
//           if (other_slice != null) {
//             SeqINTEQUAL sic = SeqINTEQUAL.find(other_slice);
//             if ((sic != null)
//                 && sic.enoughSamples()) {
//               // This DOES happen
//               // System.out.println("Surprise:  this can happen (var2 in IntNonEqual).");
//               // new Error().printStackTrace();
//               return true;
//             }
//           }
//         }
//       }
//     }

    return false;

  } // isObviousImplied
}

