// ***** This file is automatically generated from NoDuplicates.java.jpp

package daikon.inv.unary.sequence;

import daikon.*;
import daikon.inv.*;
import daikon.inv.binary.twoSequence.*;
import daikon.suppress.SuppressionFactory;

import daikon.derive.binary.SequencesPredicate;
import daikon.derive.binary.SequencesConcat;
import daikon.derive.binary.SequencesJoin;

import utilMDE.*;

import org.apache.log4j.Logger;

import java.util.*;

public class NoDuplicates
  extends SingleScalarSequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff NoDuplicates invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  /** Debug tracer **/
  public static final Logger debug = Logger.getLogger("daikon.inv.unary.sequence.NoDuplicates");
  int elts = 0;

  protected NoDuplicates(PptSlice ppt) {
    super(ppt);
  }

  public static NoDuplicates instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;
    NoDuplicates result = new NoDuplicates(ppt);
    // Don't instantiate if the variable can't have dupliates
    return result;
  }

  private static SuppressionFactory[] suppressionFactories = null;

  public SuppressionFactory[] getSuppressionFactories() {
    if (suppressionFactories == null) {
      suppressionFactories = getSubsetImpliedSuppressionFactories
        (super.getSuppressionFactories());
    }
    return suppressionFactories;
  }

  public String repr() {
    return "NoDuplicates" + varNames() + ": "
      + "elts=\"" + elts;
  }

  public String format_using(OutputFormat format) {
    if (debug.isDebugEnabled()) {
      debug.debug(repr());
    }

    if (format == OutputFormat.DAIKON) {
      return var().name.name() + " contains no duplicates";
    }

    if (format == OutputFormat.IOA) {
      return format_ioa();
    }

    if (format == OutputFormat.JML) {
      VarInfoName.QuantHelper.QuantifyReturn qret = VarInfoName.QuantHelper.quantify(new VarInfoName[] {var().name,var().name});
      String quantResult[] = VarInfoName.QuantHelper.format_jml(qret,false);

      VarInfoName index1 = ((VarInfoName [])qret.bound_vars.get(0))[0];
      VarInfoName index2 = ((VarInfoName [])qret.bound_vars.get(1))[0];

      return quantResult[0] + "(" + index1.jml_name() + " != " + index2.jml_name() + ") ==> (" + quantResult[1] + " != " +
        quantResult[2] + ")" + quantResult[3];
    }

    return format_unimplemented(format);
  }

  /* IOA */
  public String format_ioa() {
    if (debugPrint.isDebugEnabled()) {
      debugPrint.debug ("Format_ioa: " + this.toString());
    }

    // We first see if we can special case for certain types of variables
    if (var().isDerived() && var().derived instanceof SequencesPredicate) {
      VarInfoName.FunctionOfN myName =
        (VarInfoName.FunctionOfN) ((VarInfoName.Elements) var().name).term;
      String predicateValue = myName.getArg(2).ioa_name();

      SequencesPredicate derivation = (SequencesPredicate) var().derived;
      VarInfo varField = derivation.var1();
      VarInfoName.Field varFieldName = (VarInfoName.Field) varField.name;
      String fieldName = varFieldName.field;

      VarInfo varPredicateField = derivation.var2();
      VarInfoName.Field varPredicateFieldName = (VarInfoName.Field) varPredicateField.name;
      String predicateName = varPredicateFieldName.field;

      VarInfoName varOrigName = varFieldName.term;
      VarInfo fakeVarOrig = new VarInfo (varOrigName, varField.type,
                                         varField.file_rep_type,
                                         varField.comparability,
                                         VarInfoAux.getDefault());

      VarInfoName.QuantHelper.IOAQuantification quant
        = new VarInfoName.QuantHelper.IOAQuantification (fakeVarOrig, fakeVarOrig);

      //     \A i : type, j : type(   i \in X
      return quant.getQuantifierExp() + "(" + quant.getMembershipRestriction(0) +
        //         /\ j \ in X
        " /\\ " + quant.getMembershipRestriction(1) +
        //           i.field = j.field
        " /\\ " + quant.getVarName(0).ioa_name() + "." + fieldName + " = " + quant.getVarName(1).ioa_name() + "." + fieldName +
        //           i.pred = value
        " /\\ " + quant.getVarName(0).ioa_name() + "." + predicateName + " = " + predicateValue +
        //           j.pred = value
        " /\\ " + quant.getVarName(1).ioa_name() + "." + predicateName + " = " + predicateValue +
        //  =>      i           =       j           )
        ") => " + quant.getVarName(0).ioa_name() + " = " + quant.getVarName(1).ioa_name() + quant.getClosingExp();

    } else if (var().isDerived() && var().derived instanceof SequencesJoin) {
      SequencesJoin derivation = (SequencesJoin) var().derived;
      VarInfo varField1 = derivation.var1();
      VarInfoName.Field varFieldName1 = (VarInfoName.Field) varField1.name;
      String fieldName1 = varFieldName1.field;
      VarInfo varField2 = derivation.var2();
      VarInfoName.Field varFieldName2 = (VarInfoName.Field) varField2.name;
      String fieldName2 = varFieldName2.field;

      VarInfoName varOrigName = varFieldName1.term;
      VarInfo fakeVarOrig = new VarInfo (varOrigName, varField1.type,
                                         varField1.file_rep_type,
                                         varField1.comparability,
                                         VarInfoAux.getDefault());

      VarInfoName.QuantHelper.IOAQuantification quant
        = new VarInfoName.QuantHelper.IOAQuantification (fakeVarOrig, fakeVarOrig);

      //     \A i : type, j : type(   i \in X
      return quant.getQuantifierExp() + "(" + quant.getMembershipRestriction(0) +
        //         /\ j \ in X
        " /\\ " + quant.getMembershipRestriction(1) +
        //           i.field = j.field
        " /\\ " + quant.getVarName(0).ioa_name() + "." + fieldName1 + " = " + quant.getVarName(1).ioa_name() + "." + fieldName1 +
        //           i.field = j.field
        " /\\ " + quant.getVarName(0).ioa_name() + "." + fieldName2 + " = " + quant.getVarName(1).ioa_name() + "." + fieldName2 +
        //  =>      i           =       j           )
        ") => " + quant.getVarName(0).ioa_name() + " = " + quant.getVarName(1).ioa_name() + quant.getClosingExp();

    } else {
      VarInfoName.QuantHelper.IOAQuantification quant
        = new VarInfoName.QuantHelper.IOAQuantification (var(), var());

      //     \A i, j(                 i \in X /\ j \ in X
      return quant.getQuantifierExp() + "(" + quant.getMembershipRestriction(0) +
        " /\\ " + quant.getMembershipRestriction(1) +
        //           X[i] = X[j]
        " /\\ " + quant.getVarIndexed(0) + " = " + quant.getVarIndexed(1) +
        //  =>      i           =       j           )
        ") => " + quant.getVarName(0).ioa_name() + " = " + quant.getVarName(1).ioa_name() + quant.getClosingExp();

    }

  }

  public void add_modified(long[] a, int count) {
    for (int i=1; i<a.length; i++) {
      if (ArraysMDE.indexOf(a, a[i]) < i) {
        if (debug.isDebugEnabled()) {
          debug.debug ("Flowing myself with: " + var().name.repr());
          debug.debug (ArraysMDE.toString(a));
        }
        destroyAndFlow();
        return;
      }
    }
    if (a.length > 1)
      elts += 1;
  }

  protected double computeProbability() {
    if (falsified) {
      return Invariant.PROBABILITY_NEVER;
    } else {
      return Math.pow(.9, elts);
    }
  }

  public boolean isObviousStatically (VarInfo[] vis) {
    if (!vis[0].aux.getFlag(VarInfoAux.HAS_DUPLICATES)) {
      return true;
    }
    return super.isObviousStatically (vis);
  }

  // Lifted from EltNonZero; should abstract some of this out.
  public boolean isObviousDynamically(VarInfo[] vis) {
    // For every other NoDuplicates at this program point, see if there is a
    // subsequence relationship between that array and this one.
    VarInfo v1 = vis[0];

    PptTopLevel parent = ppt.parent;

    for (Iterator itor = parent.invariants_iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if ((inv instanceof NoDuplicates) && (inv != this) && inv.enoughSamples()) {
        VarInfo v2 = inv.ppt.var_infos[0];
        if (SubSequence.isObviousSubSequenceDynamically(v1, v2)) {
          // System.out.println("obvious: " + format() + "   because of " + inv.format());
          return true;
        }

        boolean this_var_first = (v1.varinfo_index < v2.varinfo_index);
        PptSlice2 slice_2seq = parent.findSlice_unordered(v1, v2);
        if (slice_2seq == null) {
          // System.out.println("NoDuplicates.isObviousImplied: no slice for " + v1.name + ", " + v2.name);
        } else  {
          // slice_2seq != null
          SubSequence ss = SubSequence.find(slice_2seq);
          if (ss == null) {
            // System.out.println("NoDuplicates.isObviousImplied: no SubSequence for " + v1.name + ", " + v2.name);
          } else {
            // System.out.println("NoDuplicates.isObviousImplied: found SubSequence: " + ss.repr());
            if (this_var_first
                ? ss.var1_in_var2
                : ss.var2_in_var1) {
              return true;
            }
          }
        }
      }
    }

    // If the sequence is sorted by < or >, then there are obviously no duplicates

    Iterator invs = ppt.invs.iterator();
    while (invs.hasNext()) {
      Invariant inv = (Invariant)invs.next();
      if ((inv instanceof EltwiseIntLessThan) || (inv instanceof EltwiseFloatLessThan) ||
          (inv instanceof EltwiseIntGreaterThan) || (inv instanceof EltwiseFloatGreaterThan))
        return true;
    }

    return super.isObviousDynamically(vis);
  }

  public boolean isSameFormula(Invariant other)
  {
    Assert.assertTrue(other instanceof NoDuplicates);
    return true;
  }
}
