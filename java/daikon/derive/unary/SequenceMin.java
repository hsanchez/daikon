package daikon.derive.unary;
import daikon.*;
import daikon.derive.*;
import utilMDE.*;

// like SequenceMax; if one changes, change the other, too
public final class SequenceMin extends UnaryDerivation {

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  public static boolean dkconfig_enabled = true;

  public SequenceMin(VarInfo vi) {
    super(vi);
  }

  public ValueAndModified computeValueAndModified(ValueTuple vt) {
    int source_mod = base.getModified(vt);
    if (source_mod == ValueTuple.MISSING)
      return ValueAndModified.MISSING;
    Object val = base.getValue(vt);
    if (val == null)
      return ValueAndModified.MISSING;
    long[] val_array = (long[])val;
    if (val_array.length == 0)
      return ValueAndModified.MISSING;
    return new ValueAndModified(Intern.internedLong(ArraysMDE.min(val_array)),
                                source_mod);
  }

  protected VarInfo makeVarInfo() {
    VarInfoName name = base.name.applyFunction("min");
    ProglangType ptype = base.type.elementType();
    ProglangType frtype = base.file_rep_type.elementType();
    VarComparability comp = base.comparability.elementType();
    return new VarInfo(name, ptype, frtype, comp);
  }

}
