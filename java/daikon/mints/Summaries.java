package daikon.mints;

import daikon.PptMap;
import daikon.PptName;
import daikon.PptTopLevel;
import daikon.inv.Invariant;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Huascar Sanchez
 */
class Summaries {
  private Summaries() {}

  /**
   * Returns a list of summary objects. Each summary object contain a sequence of invariants;
   * warnings will be filter out.
   *
   * @param pptMap program point map
   * @return a new list of summary objects.
   */
  static List<SequenceSummary> from(PptMap pptMap) {
    return from(pptMap, true);
  }

  @SuppressWarnings("SameParameterValue")
  private static List<SequenceSummary> from(PptMap pptMap, boolean skipWarning) {
    if (Objects.isNull(pptMap)) return Collections.emptyList();

    final List<SequenceSummary> result = new LinkedList<>();

    final Map<SequenceEntry, SequenceSummary> segmentMap = new HashMap<>();

    for (String eachKey : pptMap.nameStringSet()) {
      // ignore Randoop & JUnit related artifacts
      if (eachKey.contains("BuildersRegression")) continue;
      if (eachKey.contains("RegressionTestDriver")) continue;
      if (eachKey.contains("org.junit.")) continue;
      if (eachKey.contains("RegressionTest")) continue;

      final boolean isEnter = eachKey.contains("ENTER");
      final boolean isExit = eachKey.contains("EXIT") && !isEnter;

      //if(isExit) continue;
      System.out.println("INFO: Processing " + ((isExit ? "input " : "output ")) + "invariants.");

      final PptTopLevel eachValue = pptMap.get(eachKey);
      final PptName pptName = eachValue.ppt_name;

      final Optional<SequenceEntry> candidateSource = Optional
        .ofNullable(SequenceEntry.from(pptName, !isExit));

      if (!candidateSource.isPresent()) continue;

      final SequenceEntry seqEntry = candidateSource.get();

      // skip constructors
      if (seqEntry.isConstructor()) continue;

      final List<Invariant> validOnes = filterWarnings(
        eachValue.getInvariants(),
        skipWarning
      );

      if (!segmentMap.containsKey(seqEntry)) {
        final SequenceSummary sequence = new SequenceSummary(seqEntry);
        sequence.add(validOnes);
        segmentMap.put(seqEntry, sequence);
      } else {
        segmentMap.get(seqEntry).add(validOnes);
      }

      result.addAll(segmentMap.values());
    }

    return result;
  }

  private static List<Invariant> filterWarnings(List<Invariant> invariants, boolean skipWarnings) {
    List<Invariant> validOnes = invariants;

    if (skipWarnings) {
      validOnes = validOnes
        .stream()
        .filter(i -> !i.format().contains("warning:"))
        .collect(Collectors.toList());
    }

    return validOnes;
  }
}
