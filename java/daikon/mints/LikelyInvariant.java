package daikon.mints;

import daikon.inv.Invariant;

import java.util.Objects;

/**
 * @author Huascar Sanchez
 */
class LikelyInvariant {
  private final String    typeOfInvariant;
  private final Invariant invariantLiteral;


  /**
   * A Likely Invariant.
   */
  private LikelyInvariant(String typeOfInvariant, Invariant invariantLiteral){

    this.typeOfInvariant  = Objects.requireNonNull(typeOfInvariant);
    this.invariantLiteral = Objects.requireNonNull(invariantLiteral);

  }

  /**
   * Creates a new known invariant given a Daikon invariant.
   *
   * @param inv Daikon invariant
   * @return a new LikelyInvariant object.
   */
  static LikelyInvariant from(Invariant inv){

    final Class<? extends Invariant> daikonClass =
      Objects.requireNonNull(inv.getClass());

    return new LikelyInvariant(daikonClass.getSimpleName(), inv);
  }

  /**
   * @return Daikon's type of invariant.
   */
  String typeOfInvariant(){
    return typeOfInvariant;
  }

  /**
   * @return Daikon's invariant object.
   */
  Invariant invariantObject(){
    return invariantLiteral;
  }


  @Override public String toString() {
    return typeOfInvariant() + "=>" + invariantObject().format();
  }
}
