package daikon.mints;

import daikon.PptMap;
import daikon.PptName;
import daikon.PptTopLevel;
import daikon.inv.Invariant;

import java.util.*;
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

    final Map<SummaryDescriptor, SequenceSummary> segmentMap = new HashMap<>();

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

      final Optional<SummaryDescriptor> candidateSource = Optional
        .ofNullable(SummaryDescriptor.from(pptName, !isExit));

      if (!candidateSource.isPresent()) continue;

      final SummaryDescriptor descriptor = candidateSource.get();

      // skip constructors
      if (descriptor.isConstructor()) continue;

      final List<Invariant> filtered = filterWarnings(
        eachValue.getInvariants(),
        skipWarning
      );

      final List<Invariant> validOnes = LongestRepeated.sublist(filtered);

      if (!segmentMap.containsKey(descriptor)) {
        final SequenceSummary sequence = new SequenceSummary(descriptor);
        sequence.add(validOnes);
        segmentMap.put(descriptor, sequence);
      } else {
        segmentMap.get(descriptor).add(validOnes);
      }

      result.addAll(segmentMap.values());
    }

    return result;
  }

  // Inspired by the algorithm that solves the Longest Repeated Substring using Suffix Arrays
  private static List<Invariant> commonInvariantSublistOfNInvariants(List<Invariant> validOnes){

    int max = 0;
    List<Invariant> result = new ArrayList<>();

    final int N = validOnes.size();

    for(int start = 0; start + max < N - 1; start++){
      for(int shift = 1; start + shift + max < N; shift++){
        int length = 0;

        // While types of invariants match, count the length
        while(
          /* matching invariant types */
          validOnes.get(start + length).format()
            .equals(validOnes.get(start + shift + length).format())
          /* hasn't reached the end yet*/
          && start + shift + length < N - 1) {

          length++;

        }

        // If the length is larger - update the new max size
        if (length > max ) {
          max     = length;
          result  = validOnes.subList(start, start + length + 1);
        }
      }
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
