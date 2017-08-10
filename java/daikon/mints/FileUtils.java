package daikon.mints;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import daikon.FileIO;
import daikon.PptMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Huascar Sanchez
 */
class FileUtils {

  private static final PathMatcher MATCHER = FileSystems.getDefault()
    .getPathMatcher("glob:*.json");

  private FileUtils(){}


  /**
   * Crawls a directory structure in search of files matching an file extension. At
   * the same time it will skip those files that contains certain keywords.
   *
   * @param location the directory location
   * @param extension the file extension
   * @param files the file storage
   * @param keywords skip hints
   * @throws IOException unexpected error has occurred.
   */
  private static void walkDirectory(final File location, final String extension,
                 final Collection<File> files, final String... keywords) throws IOException {

    final Path start   = Paths.get(location.toURI());
    final PathMatcher matcher = FileSystems.getDefault()
      .getPathMatcher("glob:*." + extension);

    final Set<String> blackSet = new HashSet<>(Arrays.asList(keywords));

    try {
      Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
        @Override public FileVisitResult visitFile(Path file,
                                                   BasicFileAttributes attrs) throws IOException {

          final Path fileName = file.getFileName();
          if(matcher.matches(fileName)){
            final File visitedFile = file.toFile();
            final String name = visitedFile.getName().replace(("." + extension), "");
            if(inTheClub(name, blackSet)){
              files.add(visitedFile);
            }
          }

          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ignored) {

    }

  }

  /**
   * List all files matching a file extension under a given location.
   * Skip those ones matching the provide skip hints.
   *
   * @param directory directory to access
   * @param skipHints keywords used to avoid certain files collection.
   * @return the list of files matching a given extension.
   */
  static List<File> findFiles(File directory, Log log, String extension, String... skipHints){

    final List<File> data = new ArrayList<>();

    try {
      walkDirectory(directory, extension, data, skipHints);
    } catch (IOException e) {
      log.error(
        String.format("Error: unable to crawl %s.", directory.getName()), e
      );
    }

    return data;
  }

  private static boolean inTheClub(String name, Set<String> blackSet){
    for(String each : blackSet){
      if(name.contains(each)) return false;
    }

    return true;
  }

  /**
   * Lists all Daikon-generated .gz files found under some directory.
   *
   * @param directory location containing .gz files.
   * @return a new list of Daikon-generated .gz files.
   **/
  private static List<File> fileList(Path directory){
    if(!Files.exists(directory)) return Collections.emptyList();
    final File location = directory.toFile();

    return findFiles(location, Log.verbose(), "gz", /*skip dtrace.gz files*/"dtrace");
  }

  static boolean isJsonfile(Path file){
    return MATCHER.matches(file);
  }

  /**
   * Checks if two paths are the same.
   *
   * @param a first path
   * @param b second path
   * @return true if a and b are the same; false otherwise.
   */
  static boolean isSamePath(Path a, Path b){
    try {

      final String pathA = a.toFile().getCanonicalPath();
      final String pathB = b.toFile().getCanonicalPath();

      return pathA.equalsIgnoreCase(pathB) || Files.isSameFile(a, b);
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Translates a .gz file into a PptMap object.
   *
   * @param file serialized .gz file.
   * @return a new PptMap from a file
   **/
  private static PptMap toPptMap(File file){
    if(Objects.isNull(file)) return new PptMap();

    try {
      return FileIO.read_serialized_pptmap(file, true);
    } catch (IOException ignored) {
      System.out.println("Error: unable to read serialized file.");
      return new PptMap();
    }
  }

  /**
   * Lists all PptMap objects found in Daikon-generated .gz files.
   *
   * @param directory location containing .gz files.
   * @return a new list of PptMap objects.
   **/
  static List<PptMap> mapList(Path directory){
    final List<File> invFiles = fileList(directory);
    final List<PptMap> invContainers = invFiles.stream()
      .map(FileUtils::toPptMap)
      .collect(Collectors.toList());

    if(invContainers.isEmpty()){
      System.out.println("INFO: no PptMap objects found.");
    }

    return invContainers;
  }

  /**
   * Loads the content of a JSON file.
   *
   * @param filepath the path to the JSON file.
   * @return a new Json value.
   */
  static JsonValue toJson(Path filepath){
    JsonValue result = Json.object();

    if(!Files.exists(filepath)){
      return result;
    }

    try {
      final String content = new String(Files.readAllBytes(filepath));
      result = Json.parse(content);
    } catch (IOException ignored){
      Log.verbose().error("Unable to read JSON file", ignored);
    }

    return result;
  }

  /**
   * Deletes file in path.
   *
   * @param path file path
   */
  static void deleteFile(Path path){
    try {
      Files.delete(path);
    } catch (IOException ignored){}
  }
}
