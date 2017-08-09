package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.google.common.collect.Lists;
import daikon.PptMap;
import daikon.mints.Clustering.Cluster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static daikon.mints.CommandLineConst.DBC;
import static daikon.mints.CommandLineConst.KM;
import static daikon.mints.CommandLineConst.MLCS;
import static daikon.mints.CommandLineConst.PIM;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

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
   * @param output the output.json file.
   * @param pruning true if pruning is enabled; false otherwise.
   * @param log log object
   * @return a new Data Request object.
   */
  static Request invariantsData(Path input, Path output, boolean pruning, Log log){
    return new DataRequest(input, output, pruning, log);
  }

  /**
   * Find interesting sequence of likely invariants.
   *
   * @param invokeWith mining strategy (mlcs | pim)
   * @param jsonFile data.json file produced by {@link #invariantsData(Path, Path, boolean, Log)}
   * @param outFile file where output will be written.
   * @param log log object
   * @return a new Patterns Request object.
   */
  static Request interestingPatterns(String invokeWith, Path jsonFile, Path outFile, Log log){
    return new PatternsRequest(invokeWith, jsonFile, outFile, log);
  }

  /**
   * Finds similar methods based on shared likely invariants.
   *
   * @param invokeWith similarity strategy (km|dbc).
   * @param paths the files to compare
   * @param log log object
   * @return a new Similarity Request object.
   */
  static Request similarMethods(String invokeWith, List<Path> paths, Log log){
    return new SimRequest(invokeWith, paths, log);
  }

  static class DataRequest extends Request {
    private final Path    input;
    private final Path    output;
    private final boolean pruning;
    private final Log     log;

    DataRequest(Path input, Path output, boolean pruning, Log log){

      this.input    = input;
      this.output   = output;
      this.pruning  = pruning;
      this.log      = log;
    }

    @Override void fulfill() {

      final List<SequenceSummary> summaries = generateSummaries(input, pruning);

      final int k = (int) Math.floor(Math.sqrt(summaries.size()));

      final List<List<SequenceSummary>> partitions = Lists.partition(summaries, k);

      final TaskQueue queue = new TaskQueue(log, 20);

      int count = 1;
      for(List<SequenceSummary> each : partitions){
        final Path file = Paths.get(Integer.toString(count) + ".json");
        final MapTask unit = new MapTask(file, each, log);

        queue.enqueue(unit);
        count++;
      }

      final List<Task> prerequisites = Immutable.listOf(queue.getTasks());

      final ReduceTask reduce = new ReduceTask(output, log);
      reduce.afterSuccess(prerequisites);

      queue.enqueue(reduce);

      queue.calibrateAndRunTask();

    }

    /**
     * Returns a list of non empty sequence summary objects.
     *
     * @param fromDirectory the source of all these summary objects.
     * @param pruning true if pruning is enabled; false otherwise.
     * @return an immutable list of sequence summaries.
     */
    static List<SequenceSummary> generateSummaries(Path fromDirectory, final boolean pruning){
      if(Objects.isNull(fromDirectory) || !Files.exists(fromDirectory)){
        return Collections.emptyList();
      }

      final List<PptMap>         containers  = FileUtils.mapList(fromDirectory);
      final Set<SequenceSummary> result      = new HashSet<>();

      containers.forEach(c -> result.addAll(Summaries.from(c, pruning)));

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

    private final String  command;
    private final Path    jsonFile;
    private final Path    outFile;
    private final Log     log;

    PatternsRequest(String invokeWith, Path jsonFile, Path outFile, Log log){
      this.command    = invokeWith;
      this.jsonFile   = jsonFile;
      this.outFile    = outFile;
      this.log        = log;
    }

    @Override void fulfill() {

      final Map<String, List<Record>> records = Jsons.readJson(this.jsonFile);
      final List<List<Record>> allRecords = new ArrayList<>();
      records.entrySet().forEach(e -> allRecords.add(e.getValue()));

      final List<Record> interestingRecords = invokeWith(command, allRecords, log);
      log.info(String.format("Pattern size: %d", interestingRecords.size()));
      log.info(String.format("Updated %s", outFile.toString()));

      if(interestingRecords.isEmpty()) return;

      final JsonObject object = Json.object();

      final JsonArray  array  = Json.array().asArray();

      for(Record each : interestingRecords){
        final JsonObject jo = Json.object();

        jo.add("i", each.format());
        jo.add("t", each.type());

        array.add(jo);
      }

      object.add("pattern", array);

      final byte[] dataBytes = object.toString().getBytes();

      try {
        if(log.isVerbose()){
          log.info("Pattern: " + recordsAsString(interestingRecords));
        }

        Files.write(outFile, dataBytes, CREATE, WRITE);
      } catch (IOException e) {
        log.error(String.format("Unable to write %s.", outFile.toString()), e);
      }
    }

    private static List<Record> invokeWith(String command, List<List<Record>> allRecords, Log log){
      switch (command){
        case MLCS:
          return Inference.commonSubsequence(allRecords, log);
        case PIM:
          return Inference.pim(allRecords);
        default:
          return Immutable.emptyList();
      }
    }

    private static String recordsAsString(List<Record> interestingSequence){
      final StringBuilder builder = new StringBuilder(200 * interestingSequence.size());
      builder.append("[\n");
      interestingSequence.forEach(r -> builder.append("\t").append(r.type()).append("\n"));
      builder.append("]");
      return builder.toString();
    }

    @Override Log getLog() {
      return log;
    }

    @Override public String toString() {
      return "Patterns discovery request";
    }
  }

  static class SimRequest extends Request {

    private final String      invokeWith;
    private final List<Path>  paths;
    private final Log         log;

    SimRequest(String invokeWith, List<Path> paths, Log log){
      this.invokeWith = invokeWith;
      this.paths      = paths;
      this.log        = log;
    }

    @Override void fulfill() {

      // the output file is always last
      final Path output = paths.get(paths.size() - 1);

      final Map<String, List<Record>> data = new HashMap<>();

      for(Path each: paths){
        if(FileUtils.isSamePath(each, output)) continue;

        final Map<String, List<Record>> invariantSeq = Jsons.readJson(each);

        data.putAll(invariantSeq);
      }

      final List<Cluster> clusters = invokeWith(invokeWith, data, log);

      log.info(String.format("Formed %d clusters", clusters.size()));
    }

    private static List<Cluster> invokeWith(String command, Map<String, List<Record>> data, Log log){

      switch (command){
        case KM:
          return Clustering.clusterWithKmeans(data, log);
        case DBC:
          return Immutable.emptyList();
        default:
          return Immutable.emptyList();
      }
    }

    @Override Log getLog() {
      return log;
    }
  }
}
