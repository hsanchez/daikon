package daikon.mints;

import com.google.common.collect.Lists;
import daikon.PptMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representation of a Mints Request object.
 *
 * @author Huascar Sanchez
 */
abstract class Request {

  /**
   * Logic that fulfills this request.
   */
  abstract void fulfill();

  /**
   * @return the log object
   */
  abstract Log getLog();


  /**
   * Persist Daikon invariants in a JSON file.
   *
   * @param input the dir where Daikon files are.
   * @param log log object
   * @return a new Request object.
   */
  static Request invariantsData(Path input, Log log){
    return new DataRequest(input, log);
  }

  /**
   * Find interesting sequence of likely invariants.
   *
   * @param jsonFile data.json file produced by {@link #invariantsData(Path, Log)}
   * @param log log object
   * @return a new Request object.
   */
  static Request patterns(Path jsonFile, Log log){
    return new PatternsRequest(jsonFile, log);
  }


  static class DataRequest extends Request {
    private final Path input;
    private final Log  log;

    DataRequest(Path input, Log log){

      this.input  = input;
      this.log    = log;
    }

    @Override void fulfill() {

      final List<SequenceSummary> summaries = generateSummaries(input);

      final int k = (int) Math.floor(Math.sqrt(summaries.size()));

      final List<List<SequenceSummary>> partitions = Lists.partition(summaries, k);

      final TaskQueue queue = new TaskQueue(log, 20);

      int count = 1;
      for(List<SequenceSummary> each : partitions){
        final Path file = Paths.get(Integer.toString(count) + ".json");
        final MapTask unit = new MapTask(file, each);

        queue.enqueue(unit);
        count++;
      }

      final List<Task> prerequisites = Immutable.listOf(queue.getTasks());

      final ReduceTask reduce = new ReduceTask();
      reduce.afterSuccess(prerequisites);

      queue.enqueue(reduce);

      queue.calibrateAndRunTask();

    }

    /**
     * Returns a list of non empty sequence summary objects.
     *
     * @param fromDirectory the source of all these summary objects.
     * @return an immutable list of sequence summaries.
     */
    static List<SequenceSummary> generateSummaries(Path fromDirectory){
      if(Objects.isNull(fromDirectory) || !Files.exists(fromDirectory)){
        return Collections.emptyList();
      }

      final List<PptMap>          containers  = Utils.mapList(fromDirectory);
      final Set<SequenceSummary> result      = new HashSet<>();

      containers.forEach(c -> result.addAll(Summaries.from(c)));

      return Immutable.listOf(result.stream()
        .filter(i -> !i.isEmpty()));
    }

    @Override Log getLog() {
      return log;
    }

    @Override public String toString() {
      return "Data request";
    }
  }

  static class PatternsRequest extends Request {
    private final Path jsonFile;
    private final Log  log;

    PatternsRequest(Path jsonFile, Log log){

      this.jsonFile = jsonFile;
      this.log      = log;
    }

    @Override void fulfill() {
      // TODO(Di or Huascar) implement this
    }

    @Override Log getLog() {
      return log;
    }

    @Override public String toString() {
      return "Patterns discovery request";
    }
  }
}
