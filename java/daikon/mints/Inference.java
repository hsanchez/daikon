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
   *
   * @param from
   * @return
   */
  abstract List<Record> examine(List<List<Record>> from);

  /**
   *
   * @param from
   * @return
   */
  static List<Record> commonSubList(List<List<Record>> from){
    return new CommonSublistInference().examine(from);
  }


  /**
   * Inspired by the solution to the problem of the longest common subsequence
   * for N strings, this class implements the longest common subsequence for
   * N lists of records.
   */
  static class CommonSublistInference extends Inference {
    @Override List<Record> examine(List<List<Record>> from) {
      if(from.isEmpty()) return Immutable.emptyList();
      if(from.size() == 1) return from.get(0);

      final OptionalDouble avg = from.stream().mapToInt(List::size).average();
      final double avgAsDouble = avg.isPresent() ? avg.getAsDouble() : 20.0;

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

//      from = from.stream().filter(s -> s.size() > 10).collect(Collectors.toList());
//
//      List<Record> common = new ArrayList<>();
//      List<Record> small  = from.get(0);
//
//      // identify smallest sequence of records
//      for(List<Record> each : from){
//        if(each.size() < small.size()){
//          small = each;
//        }
//      }
//
//      List<Record> commonTemp = new ArrayList<>();
//      for(Record eachX : small){
//
//        commonTemp.add(eachX);
//
//        for(List<Record> eachY : from){
//          if(!containsAll(eachY, commonTemp)){
//            commonTemp = new ArrayList<>();
//            break;
//          }
//        }
//
//        if(!commonTemp.isEmpty() && commonTemp.size() > common.size()){
//          common = new ArrayList<>(commonTemp);
//        }
//
//      }
//
//      return common;
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
