// ***** This file is automatically generated from SequenceSubscript.java.jpp

package daikon.derive.binary;

import daikon.*;
import daikon.derive.*;

import utilMDE.*;

public final class SequenceFloatSubscript
  extends BinaryDerivation
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020122L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff SequenceFloatSubscript  invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  // base1 is the sequence
  // base2 is the scalar
  public VarInfo seqvar() { return base1; }
  public VarInfo sclvar() { return base2; }

  // Indicates whether the subscript is an index of valid data or a limit
  // (one element beyond the data of interest).
  // Value is -1 or 0.
  public final int index_shift;

  public SequenceFloatSubscript(VarInfo vi1, VarInfo vi2, boolean less1) {
    super(vi1, vi2);
    if (less1)
      index_shift = -1;
    else
      index_shift = 0;
  }

  public ValueAndModified computeValueAndModifiedImpl(ValueTuple full_vt) {
    int mod1 = base1.getModified(full_vt);
    if (mod1 == ValueTuple.MISSING_NONSENSICAL)
      return ValueAndModified.MISSING_NONSENSICAL;
    int mod2 = base2.getModified(full_vt);
    if (mod2 == ValueTuple.MISSING_NONSENSICAL)
      return ValueAndModified.MISSING_NONSENSICAL;
    Object val1 = base1.getValue(full_vt);
    if (val1 == null)
      return ValueAndModified.MISSING_NONSENSICAL;
    double[] val1_array = (double[]) val1;
    int val2 = base2.getIndexValue(full_vt) + index_shift;
    if ((val2 < 0) || (val2 >= val1_array.length))
      return ValueAndModified.MISSING_NONSENSICAL;
    double  val = val1_array[val2];
    int mod = (((mod1 == ValueTuple.UNMODIFIED)
                && (mod2 == ValueTuple.UNMODIFIED))
               ? ValueTuple.UNMODIFIED
               : ValueTuple.MODIFIED);
    return new ValueAndModified(Intern.internedDouble( val ) , mod);
  }

  protected VarInfo makeVarInfo() {
    VarInfo seqvar = seqvar();

    VarInfoName index = sclvar().name;
    switch (index_shift) {
    case 0:
      break;
    case -1:
      index = index.applyDecrement();
      break;
    default:
      throw new UnsupportedOperationException("Unsupported shift: " + index_shift);
    }

    VarInfoName name = seqvar.name.applySubscript(index);
    ProglangType type = seqvar.type.elementType();
    ProglangType file_rep_type = seqvar.file_rep_type.elementType();
    VarComparability compar = base1.comparability.elementType();
    return new VarInfo(name, type, file_rep_type, compar, VarInfoAux.getDefault());
  }

  public  boolean isSameFormula(Derivation other) {
    return (other instanceof SequenceFloatSubscript)
      && (((SequenceFloatSubscript) other).index_shift == this.index_shift);
  }

}
