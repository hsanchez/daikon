// ***** This file is automatically generated from SequenceSubsequence.java.jpp

package daikon.derive.binary;

import daikon.*;
import daikon.derive.*;

import utilMDE.*;

public final class SequenceScalarSubsequence
  extends SequenceSubsequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20020801L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff SequenceScalarSubsequence  invariants should be considered.
   **/
  public static boolean dkconfig_enabled = true;

  /**
   * @param from_start true means the range goes 0..n; false means the
   * range goes n..end.  (n might be fudged through off_by_one)
   * @param off_by_one true means we should exclude the scalar from
   * the range; false means we should include it
   **/
  public SequenceScalarSubsequence(VarInfo vi1, VarInfo vi2, boolean from_start, boolean off_by_one) {
    super (vi1, vi2, from_start, off_by_one);
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
    long[] val1_array = (long[]) val1;
    int val2 = base2.getIndexValue(full_vt);

    // One could argue that if the range exceeds the array bounds, one
    // should just return the whole array; but we don't do that.  We
    // say MISSING instead.

    int begin_inclusive, end_exclusive;
    if (from_start) {
      begin_inclusive = 0;
      end_exclusive = val2+index_shift+1; // +1: endpoint is exclusive
      // end_exclusive = 0 is acceptable; that means the empty array (given
      // that begin_inclusive is 0)
      if ((end_exclusive < 0) || (end_exclusive > val1_array.length))
        return ValueAndModified.MISSING_NONSENSICAL;
    } else {
      begin_inclusive = val2+index_shift;
      end_exclusive = val1_array.length;
      // begin_inclusive = val1_array.length is acceptable; that means the
      // empty array (given that end_exclusive is val1_arrayl.length)
      if ((begin_inclusive < 0) || (begin_inclusive > val1_array.length))
        return ValueAndModified.MISSING_NONSENSICAL;
    }

    int mod = (((mod1 == ValueTuple.UNMODIFIED)
                && (mod2 == ValueTuple.UNMODIFIED))
               ? ValueTuple.UNMODIFIED
               : ValueTuple.MODIFIED);

    if ((begin_inclusive == 0) && (end_exclusive == val1_array.length))
      return new ValueAndModified(val1, mod);

    long[] subarr = ArraysMDE.subarray(val1_array, begin_inclusive, end_exclusive - begin_inclusive);
    subarr = (long[]) Intern.intern(subarr);
    return new ValueAndModified(subarr, mod);
  }

  public  boolean isSameFormula(Derivation other) {
    return (other instanceof SequenceScalarSubsequence)
      && (((SequenceScalarSubsequence) other).index_shift == this.index_shift)
      && (((SequenceScalarSubsequence) other).from_start == this.from_start);
  }

}
