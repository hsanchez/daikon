package daikon.mints;

import daikon.inv.Invariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper methods for finding the Motif (see https://en.wikipedia.org/wiki/Sequence_motif)
 * of a sequence of invariants.
 *
 * @author Huascar Sanchez
 */
class Motif {
  /**
   * Gets a sequence motif, which recurring patterns (sub-strings) in
   * a sequence of invariants.
   *
   * @param longList a sequence of Daikon-produced invariants.
   * @return a new sublist.
   */
  static List<Invariant> sequence(List<Invariant> longList){
    if(longList.isEmpty()) return longList;

    final int n = longList.size();
    final SuffixArray sa = new SuffixArray(longList);

    List<Invariant> lrs = new ArrayList<>();

    for(int idx = 1; idx < n; idx++){
      int length = sa.lcp(idx);
      if(length > lrs.size()){
        lrs = longList.subList(sa.index(idx), sa.index(idx) + length);
      }
    }

    return lrs.isEmpty() ? longList : lrs;
  }

  /**
   * Suffix array data structure.
   */
  static class SuffixArray {
    final Suffix[] suffixes;

    SuffixArray(List<Invariant> invariants){
      int n = invariants.size();
      this.suffixes = new Suffix[n];
      for(int idx = 0; idx < n; idx++){
        suffixes[idx] = new Suffix(invariants, idx);
      }

      Arrays.sort(suffixes, Comparator.comparingInt(Suffix::length));
    }

    /**
     * @return the length of the input invariants.
     */
    int length(){
      return suffixes.length;
    }

    /**
     * Gets the index into the original list of invariants.
     * @param i an integer between 0 and <em>n</em>-1;
     * @return index into the original list of invariants.
     */
    int index(int i){
      if(i < 0 || i >= length()) throw new IllegalArgumentException();
      return suffixes[i].index;
    }

    /**
     * Gets the length of the longest common (and consecutive) list of invariants.
     * @param i an integer between 0 and <em>n</em>-1;
     * @return length of longest common list of invariants.
     */
    int lcp(int i){
      if (i < 1 || i >= length()) throw new IllegalArgumentException();
      return lcpSuffix(suffixes[i], suffixes[i-1]);
    }

    private int lcpSuffix(Suffix s, Suffix t){
      int n = Math.min(s.length(), t.length());
      for(int idx = 0; idx < n; idx++){
        if(!s.elementAt(idx).equals(t.elementAt(idx))){
          return idx;
        }
      }

      return n;
    }
  }

  static class Suffix implements Comparable<Suffix> {
    final List<Invariant> invariants;
    final int							index;

    Suffix(List<Invariant> invariants, int index){
      this.invariants = invariants;
      this.index			= index;
    }

    int length(){
      return invariants.size() - index;
    }

    String elementAt(int i){
      return LikelyInvariant.typeOf(invariants.get(index + i));
    }

    @Override public int compareTo(Suffix that){
      if(this == that) return 0;
      int n = Math.min(this.length(), that.length());
      for(int idx = 0; idx < n; idx++){
        final String thisElement = this.elementAt(idx);
        final String thatElement = that.elementAt(idx);

        if(thisElement.compareTo(thatElement) < 0){ return -1; }
        if(thisElement.compareTo(thatElement) > 0){ return +1; }
      }

      return this.length() - that.length();
    }

    @Override public String toString(){
      int limit = index - 1;
      limit = limit < 0 ? 0 : limit;
      return invariants
        .stream().map(Invariant::format)
        .skip(limit)
        .collect(Collectors.toList())
        .toString();
    }
  }
}