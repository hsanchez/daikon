package daikon.mints;

import daikon.inv.Invariant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A sequence of invariants contained in some Java method.
 *
 * @author Huascar Sanchez
 */
class SequenceSummary implements Iterable<LikelyInvariant> {

  private final SummaryDescriptor seqEntry;
  private final List<LikelyInvariant> invariants;


  /**
   * Construct an Invariant Sequence located at some source method.
   * @param seqEntry the source of these invariants.
   */
  SequenceSummary(SummaryDescriptor seqEntry) {

    this.seqEntry = Objects.requireNonNull(seqEntry);
    this.invariants = new LinkedList<>();

  }

  /**
   * Adds a non empty list of invariants to this segment.
   *
   * @param x the list of invariants.
   */
  void add(List<Invariant> x) {
    if(x.isEmpty()) return;

    final List<LikelyInvariant> translated = translate(x);

    for(LikelyInvariant each : translated){
      if(Objects.isNull(each)) continue;

      invariants.add(each);
    }
  }

  private static List<LikelyInvariant> translate(List<Invariant> x){
    return x.stream()
      .map(LikelyInvariant::from)
      .collect(Collectors.toList());

  }

  /**
   * @return the content of this segment.
   */
  List<LikelyInvariant> content() {
    return invariants;
  }

  @Override public Iterator<LikelyInvariant> iterator() {
    return content().iterator();
  }

  /**
   * @return true if the segment is empty; false otherwise.
   */
  boolean isEmpty() {
    return content().isEmpty();
  }

  /**
   * @return a normalized view of this segment's invariants.
   */
  List<String> normalized() {
    final List<LikelyInvariant> content = content();
    final List<String> result = new LinkedList<>();

    for (LikelyInvariant each : content) {
      result.add(each.typeOfInvariant());
    }

    return result;
  }

  /**
   * @return the source or location from where these
   * invariants were extracted.
   */
  SummaryDescriptor source() {
    return seqEntry;
  }

  /**
   * @return the size of this segment.
   */
  int size() {
    return this.invariants.size();
  }

  @Override public String toString() {
    final SummaryDescriptor src = source();

    return src.className() + "#" + src.methodName()
      + "(" + size() + ((src.isEntry() ? " entry": " exit"))
      + " invariants)";
  }
}
