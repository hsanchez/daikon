package daikon.mints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * @author Huascar Sanchez
 */
abstract class Inference {

  /**
   * Examines a sequence of invariants sequences in order
   * to discover patterns of likely invariants.
   *
   * @param from data
   * @return the pattern
   */
  abstract List<Record> examine(List<List<Record>> from);

  /**
   * This strategy finds the longest subsequence of invariants common to all sequences
   * of invariants in a list of sequences (known as Multiple Longest Common
   * Subsequence--MLCS--problem). MLCS is an NP-hard problem. Consequently, we use a
   * dominant-subsequence algorithm for getting an approximate solution to MLCS.
   *
   * @param inAllSequences the list of invariant lists.
   * @return a pattern of likely invariants.
   */
  static List<Record> commonSubsequence(List<List<Record>> inAllSequences){
    return new CommonSublistInference().examine(inAllSequences);
  }

  /**
   *
   * TODO(Huascar): describe model
   *
   * @param inAllSequences the list of invariant lists.
   * @return a pattern of interesting likely invariants.
   */
  static List<Record> pim(List<List<Record>> inAllSequences){
    return Immutable.emptyList();
  }


  /**
   * Inspired by the solution to the problem of the longest common subsequence
   * for N strings, this class implements the longest common subsequence for
   * N lists of records.
   */
  static class CommonSublistInference extends Inference {

    static final int DEFAULT_VALUE = 20;

    @Override List<Record> examine(List<List<Record>> from) {
      if(from.isEmpty()) return Immutable.emptyList();
      if(from.size() == 1) return from.get(0);

      final OptionalDouble avg = from.stream().mapToInt(List::size).average();
      final double avgAsDouble = avg.isPresent() ? avg.getAsDouble() : DEFAULT_VALUE;

      int pivot = (int) avgAsDouble;

      from = from.stream().filter(s -> s.size() > pivot).collect(Collectors.toList());

      final List<List<Record>> sorted = from.stream().collect(Collectors.toSet())
        .stream().sorted(Comparator.comparingInt(List::size))
        .collect(Collectors.toList());

      final List<Record> smallest = sorted.get(0);
      final List<List<Record>> trimmed = sorted.stream()
        .skip(1)
        .collect(Collectors.toList());

      int j = 0;
      List<Record> LCS = new ArrayList<>();
      for(int i = smallest.size(); i > 2; i--){
        j = j + 1;

        List<Record> subSeq; for(int s = 0; s < j; s++){
          subSeq = smallest.subList(s, j);
          if(foundLcs(subSeq, trimmed) && subSeq.size() > LCS.size() ){
            LCS = subSeq;
          }
        }
      }

      return LCS;
    }

    private static boolean foundLcs(List<Record> subSeq, List<List<Record>> trimmed){
      for (List<Record> eachSequence : trimmed) {
        if (!containsAll(eachSequence, subSeq)) return false;
      }

      return true;
    }


    private static boolean containsAll(List<Record> eachY, List<Record> common){
      return eachY.containsAll(common);
    }
  }

}
