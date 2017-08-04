package daikon.mints;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Huascar Sanchez
 */
public class Immutable {
  private Immutable(){}

  public static <T> List<T> emptyList(){
    return Immutable.listOf(Collections.emptyList());
  }

  /**
   * Converts stream of objects into an immutable list.
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listOf(Stream<? extends T> stream){
    return stream.collect(toImmutableList());
  }

  /**
   * Converts a mutable list into an immutable one.
   * @param list mutable list
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listOf(Collection<? extends T> list){
    return list.stream().collect(toImmutableList());
  }


  /**
   * Converts a mutable set into an immutable one.
   * @param list mutable set
   * @param <T> type parameter
   * @return an immutable set.
   */
  public static <T> Set<T> setOf(Collection<? extends T> list){
    return list.stream().collect(toImmutableSet());
  }

  /**
   * Converts stream of object into an immutable set.
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> Set<T> setOf(Stream<? extends T> stream){
    return stream.collect(toImmutableSet());
  }

  /**
   * Creates a collector that transforms a mutable list into an immutable one.
   *
   * @param <T> the type parameter.
   * @return a new collector object.
   */
  private static <T> Collector<T, ?, List<T>> toImmutableList() {
    return Collectors.collectingAndThen(
      Collectors.toList(),
      Collections::unmodifiableList
    );
  }

  /**
   * Creates a collector that transforms a mutable set into an immutable one.
   *
   * @param <T> the type parameter.
   * @return a new collector object.
   */
  private static <T> Collector<T, ?, Set<T>> toImmutableSet() {
    return Collectors.collectingAndThen(
      Collectors.toSet(),
      Collections::unmodifiableSet
    );
  }
}