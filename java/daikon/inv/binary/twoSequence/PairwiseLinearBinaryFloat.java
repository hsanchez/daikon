// ***** This file is automatically generated from PairwiseLinearBinary.java.jpp

package daikon.inv.binary.twoSequence;

import daikon.*;
import daikon.inv.Invariant;
import daikon.inv.binary.twoScalar.*;

public class PairwiseLinearBinaryFloat
  extends TwoSequenceFloat
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff PairwiseLinearBinary invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  public LinearBinaryCoreFloat  core;

  protected PairwiseLinearBinaryFloat (PptSlice ppt) {
    super(ppt);
    core = new LinearBinaryCoreFloat(this);
  }

  public static PairwiseLinearBinaryFloat  instantiate(PptSlice ppt) {
    if (!dkconfig_enabled) return null;
    return new PairwiseLinearBinaryFloat(ppt);
  }

  protected Object clone() {
    PairwiseLinearBinaryFloat  result = (PairwiseLinearBinaryFloat) super.clone();
    result.core = (LinearBinaryCoreFloat) core.clone();
    result.core.wrapper = result;
    return result;
  }

  protected Invariant resurrect_done_swapped() {
    core.swap();
    return this;
  }

  public String repr() {
    return "PairwiseLinearBinaryFloat"  + varNames() + ": "
      + "falsified=" + falsified
      + "; " + core.repr();
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.DAIKON) return format_daikon();
    if (format == OutputFormat.IOA) return format_ioa();
    if (format == OutputFormat.JML) return format_jml();

    return format_unimplemented(format);
  }

  public String format_daikon() {
    return core.format_using(OutputFormat.DAIKON, var1().name, var2().name);
  }

  /* IOA */
  public String format_ioa() {
    if (var1().isIOASet() || var2().isIOASet())
      return "Not valid for sets: " + format();
    VarInfoName.QuantHelper.IOAQuantification quant1 = new VarInfoName.QuantHelper.IOAQuantification(var1());
    VarInfoName.QuantHelper.IOAQuantification quant2 = new VarInfoName.QuantHelper.IOAQuantification(var2());

    return quant1.getQuantifierExp()
      + core.format_using(OutputFormat.IOA,
                          quant1.getVarName(0),
                          quant2.getVarName(0))
      + quant1.getClosingExp();
  }

  public String format_jml() {
    VarInfoName.QuantHelper.QuantifyReturn qret = VarInfoName.QuantHelper.quantify(new VarInfoName[] {var1().name,var2().name});
    VarInfoName var1index = ((VarInfoName[])(qret.bound_vars.get(0)))[0];
    VarInfoName var2index = ((VarInfoName[])(qret.bound_vars.get(1)))[0];

    String quantResult[] = VarInfoName.QuantHelper.format_jml(qret,true);

    VarInfoName seq1 = var1().name.JMLElementCorrector();
    VarInfoName seq2 = var2().name.JMLElementCorrector();

    //      int seq1state = 0, seq2state = 0;

    //      if (seq1 instanceof VarInfoName.Prestate) {
    //        seq1 = ((VarInfoName.Prestate)seq1).term;
    //        seq1state = 1;
    //      } else if (seq1 instanceof VarInfoName.Poststate) {
    //        seq1 = ((VarInfoName.Poststate)seq1).term;
    //        seq1state = 2;
    //      }
    //      if (seq2 instanceof VarInfoName.Prestate) {
    //        seq2 = ((VarInfoName.Prestate)seq2).term;
    //        seq2state = 1;
    //      } else if (seq2 instanceof VarInfoName.Poststate) {
    //        seq2 = ((VarInfoName.Poststate)seq2).term;
    //        seq2state = 2;
    //      }

    //      if (seq1 instanceof VarInfoName.Slice) {
    //        seq1 = ((VarInfoName.Slice)seq1).sequence;
    //      }
    //      if (seq2 instanceof VarInfoName.Slice) {
    //        seq2 = ((VarInfoName.Slice)seq2).sequence;
    //      }

    //      seq1 = seq1.applySubscript(var1index);
    //      seq2 = seq2.applySubscript(var2index);

    //      seq1 = (seq1state == 1 ? seq1.applyPrestate() : seq1);
    //      seq1 = (seq1state == 2 ? seq1.applyPoststate() : seq1);
    //      seq2 = (seq2state == 1 ? seq2.applyPrestate() : seq2);
    //      seq2 = (seq2state == 2 ? seq2.applyPoststate() : seq2);

    return quantResult[0] + core.format_using(OutputFormat.JML,
                                              seq1,
                                              seq2) + quantResult[3];
  }

  public void add_modified(double [] x_arr, double [] y_arr, int count) {
    if (x_arr.length != y_arr.length) {
      destroyAndFlow();
      return;
    }
    int len = x_arr.length;
    // int len = Math.min(x_arr.length, y_arr.length);

    for (int i=0; i<len; i++) {
      double  x = x_arr[i];
      double  y = y_arr[i];

      core.add_modified(x, y, count);
      if (falsified) {
        // destroy() must have already been called
        return;
      }
    }
  }

  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((PairwiseLinearBinaryFloat) other).core);
  }

  public boolean isExclusiveFormula(Invariant other)
  {
    if (other instanceof PairwiseLinearBinaryFloat) {
      return core.isExclusiveFormula(((PairwiseLinearBinaryFloat) other).core);
    }
    return false;
  }

  public boolean isObviousImplied() {
    if (core.a == 0) return true;                // Constant
    if (core.a == 1 && core.b == 0) return true; // Equality
    return false;
  }
}
