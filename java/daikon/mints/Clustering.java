package daikon.mints;

import daikon.mints.Numerics.Matrix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Huascar Sanchez
 */
abstract class Clustering {
  /**
   * Partitions a list of methods (with their respective
   * sequences of invariants) into a list of clusters.
   *
   * @param items items to be clustered
   * @return a list of clusters
   */
  abstract List<Cluster> cluster(Map<String, List<Record>> items);

  /**
   * Partitions a list of methods (with their respective
   * sequences of invariants) into a list of clusters, using
   * the KMeans algorithm.
   *
   * @param items items to be clustered.
   * @param log   log object
   * @return a list of clusters.
   */
  static List<Cluster> clusterWithKmeans(Map<String, List<Record>> items, Log log) {
    final Clustering kmeans = new KMeans(log);

    log.info(String.format("%d items to cluster.", items.size()));

    return kmeans.cluster(items);
  }

  /**
   * Partitions a list of methods (with their respective
   * sequences of invariants) into a list of clusters, using
   * the Simulated Annealing Clustering algorithm.
   *
   * @param items items to be clustered.
   * @param log log object
   * @return a list of clusters
   */
  static List<Cluster> clusterWithSimulatedAnnealing(Map<String, List<Record>> items, Log log){
    final Clustering sac = new SimulatedAnnealingClustering(true, log);

    log.info(String.format("%d items to cluster.", items.size()));

    return sac.cluster(items);
  }

  static class KMeans extends Clustering {
    private final Log log;

    KMeans(Log log) {
      this.log = log;
    }

    @Override List<Cluster> cluster(Map<String, List<Record>> items) {

      final Index index = Index.createIndex(items);

      final List<String> docList = index.docSet().stream()
        .collect(Collectors.toList());

      final Matrix docToMatrix = index.itemDocFrequency().transpose();

      final Map<String, Matrix> documents = Numerics.splitMatrix(docList, docToMatrix);

      int numSeqs = docList.size();

      int numClusters = (int) Math.floor(Math.sqrt(numSeqs));


      final List<String> initialClusters = new ArrayList<>(numClusters);
      initialClusters.addAll(
        docList.stream().limit(numClusters).collect(Collectors.toList())
      );

      // build initial clusters
      final List<Cluster> clusters = new ArrayList<>();
      for (int i = 0; i < numClusters; i++) {
        final Cluster cluster = new Cluster();
        cluster.add(initialClusters.get(i), documents.get(docList.get(i)));
        clusters.add(cluster);
      }

      final List<Cluster> prevClusters = new ArrayList<>();
      while (true) {
        int i;
        for (i = 0; i < numClusters; i++) {
          clusters.get(i).computeCentroid();
        }

        for (i = 0; i < numSeqs; i++) {
          int bestCluster = 0;
          double maxDistance = Double.MIN_VALUE;
          final String name = docList.get(i);
          final Matrix matrix = documents.get(name);

          for (int j = 0; j < numClusters; j++) {
            final double distance = clusters.get(j).getSimilarity(matrix);
            if (distance > maxDistance) {
              bestCluster = j;
              maxDistance = distance;
            }
          }


          clusters.stream()
            .filter(cluster -> !cluster.itemList().isEmpty())
            .forEach(cluster -> cluster.remove(name));

          clusters.get(bestCluster).add(name, matrix);
        }


        if (Objects.equals(clusters, prevClusters)) {
          break;
        }

        prevClusters.clear();
        prevClusters.addAll(clusters);

      }

      clusters.removeIf(cluster -> cluster.itemList().isEmpty());

      log.info(String.format("Formed %d clusters", clusters.size()));
      clusters.forEach(c -> log.info(c.toString()));

      return Immutable.listOf(clusters);
    }

  }

  static class SimulatedAnnealingClustering extends Clustering {

    static final double INITIAL_TEMP = 100.0D;
    static final double FINAL_TEMP   = 1.0D;

    private final boolean randomizeData;
    private final Log log;

    SimulatedAnnealingClustering(boolean randomizeData, Log log){
      this.randomizeData = randomizeData;
      this.log = log;
    }

    @Override List<Cluster> cluster(Map<String, List<Record>> items) {

      final List<Cluster> clusters = new ArrayList<>();
      final Index index = Index.createIndex(items);

      final List<String> docList = index.docSet().stream()
        .collect(Collectors.toList());

      final Matrix docToMatrix = index.itemDocFrequency().transpose();

      final Map<String, Matrix> documents = Numerics.splitMatrix(docList, docToMatrix);


      int numDocs = items.size();
      int numClusters = (int) Math.floor(Math.sqrt(numDocs));

      for (int i = 0; i < numClusters; i++) {
        clusters.add(new Cluster());
      }
      if(randomizeData){
        Collections.shuffle(docList);
      }

      // ...and set initial temperature parameter T.
      double hi = INITIAL_TEMP;
      double lo = FINAL_TEMP;

      for (int i = 0; i < numDocs; i++) {
        int targetCluster = i % numClusters;
        clusters.get(targetCluster).add(docList.get(i), documents.get(docList.get(i)));
      }

      log.info("Initial clusters: " + clusters.toString());


      while(hi > lo){
        double previousAverageRadius = 0.0D;

        List<Cluster> prevClusters = new ArrayList<>();

        for (int loop = 0; loop < 5/*recommended number of loops*/; loop++) {

//          // exchange the two random documents among the random clusters.
//          clusters.get(randomClusterIds.get(0)).removeDocument(
//            randomDocumentNames.get(0));
//          clusters.get(randomClusterIds.get(0)).addDocument(
//            randomDocumentNames.get(1),
//            collection.getDocument(randomDocumentNames.get(1)));
//          clusters.get(randomClusterIds.get(1)).removeDocument(
//            randomDocumentNames.get(1));
//          clusters.get(randomClusterIds.get(1)).addDocument(
//            randomDocumentNames.get(0),
//            collection.getDocument(randomDocumentNames.get(0)));z

          // Find a new set of clusters by altering the membership of some
          // documents. Start by picking two clusters at random
          List<Cluster> twoRandomClusters = randomSubList(clusters, 2);

          // pick two documents out of the randomly selected clusters
          // (also at random)
          List<String>  twoRandomDocuments = getRandomDocumentNames(twoRandomClusters);

          // exchange the two random documents among the random clusters.
          clusters.get(indexOf(clusters, twoRandomClusters.get(0)))
            .remove(twoRandomDocuments.get(0));

          clusters.get(indexOf(clusters, twoRandomClusters.get(0)))
            .add(twoRandomDocuments.get(1), documents.get(twoRandomDocuments.get(1)));

          clusters.get(indexOf(clusters, twoRandomClusters.get(1))).remove(
            twoRandomDocuments.get(1));

          clusters.get(indexOf(clusters, twoRandomClusters.get(1))).add(
            twoRandomDocuments.get(0),
            documents.get(twoRandomDocuments.get(0)));

          // Compare the difference between the values of the new and old
          // set of clusters. If there is an improvement, accept the new
          // set of clusters, otherwise accept the new set of clusters with
          // probability p.

          log.info("Intermediate clusters: " + clusters.toString());

          double averageRadius = clusters.stream()
            .mapToDouble(Cluster::getRadius)
            .average().orElse(0.0D);

          //  if (averageRadius > previousAverageRadius) {
          if (Double.compare(averageRadius, previousAverageRadius) > 0) {
            // possible downhill move, calculate the probability of it being
            // accepted

            final double probability = Math.exp((previousAverageRadius - averageRadius)/hi);
            if (Double.compare(probability, 0.7D/* downhill Probability Cutoff */) < 0) {
              // go back to the cluster before the changes
              clusters.clear();
              clusters.addAll(prevClusters);
              continue;
            }
          }

          prevClusters.clear();
          prevClusters.addAll(clusters);
          previousAverageRadius = averageRadius;
        }

        // Reduce the temperature based on the cooling schedule.
        hi = lo / 10;

      }

      log.info(String.format("Formed %d clusters", clusters.size()));

      return clusters;
    }

    private List<String> getRandomDocumentNames(List<Cluster> clusters) {

      List<String> randomDocumentNames = new ArrayList<>();

      for (Cluster randomCluster : clusters) {

        final List<String> itemList = new ArrayList<>(randomCluster.itemList());
        Collections.shuffle(itemList);

        randomDocumentNames.add(itemList.get(0));
      }

      return randomDocumentNames;
    }

    static <T> int indexOf(List<T> items, T item){
      for(int idx = 0; idx < items.size(); idx++){
        if(Objects.equals(item, items.get(idx))) return idx;
      }

      return -1;
    }

    static <T> List<T> randomSubList(List<T> list, int newSize) {
      list = new ArrayList<>(list);
      Collections.shuffle(list);
      return list.subList(0, newSize);
    }
  }

  /**
   * Cluster of items.
   */
  static class Cluster {
    private final Map<String, Matrix> itemToMatrixMap;
    private final List<String> items;

    Matrix centroid = null;

    Cluster() {
      this.itemToMatrixMap = new HashMap<>();
      this.items = new ArrayList<>();
    }

    void add(String name, Matrix matrix) {
      itemToMatrixMap().put(name, matrix);
      itemList().add(name);
    }

    boolean containsKey(String name) {
      return itemToMatrixMap().containsKey(name);
    }

    Matrix centroid() {
      return centroid;
    }

    Matrix computeCentroid() {
      if (itemToMatrixMap().size() == 0) {
        return null;
      }

      final Matrix matrixD = itemMatrix(itemList().get(0));
      this.centroid = Numerics.newMatrix(
        matrixD.getRowDimension(), matrixD.getColDimension()
      );

      for (String eachName : itemList()) {
        final Matrix methodMatrix = itemMatrix(eachName);
        centroid = centroid.plus(methodMatrix);
      }

      centroid = centroid.times(1.0D / itemToMatrixMap().size());

      return centroid;
    }

    @Override public boolean equals(Object obj) {
      if (!(obj instanceof Cluster)) {
        return false;
      }

      final Cluster that = (Cluster) obj;

      final String[] thisMethodNames = this.itemList().toArray(new String[0]);
      final String[] thatMethodNames = that.itemList().toArray(new String[0]);

      if (thisMethodNames.length != thatMethodNames.length) {
        return false;
      }

      Arrays.sort(thisMethodNames);
      Arrays.sort(thatMethodNames);

      return Arrays.equals(thisMethodNames, thatMethodNames);
    }

    @Override public int hashCode() {
      final String[] methodNames = itemToMatrixMap().keySet().toArray(new String[0]);

      Arrays.sort(methodNames);

      return String.join(",", methodNames).hashCode();
    }

    Map<String, Matrix> itemToMatrixMap() {
      return itemToMatrixMap;
    }

    List<String> itemList() {
      return items;
    }

    boolean isEmpty() {
      return itemList().isEmpty();
    }

    Matrix itemMatrix(String item) {
      if (containsKey(item)) {
        return itemToMatrixMap().get(item);
      }

      throw new NoSuchElementException(item + " item not found");
    }

    double getSimilarity(Matrix doc) {
      if (centroid() != null) {
        final double dotProduct = centroid().hadamardProduct(doc).oneNorm();
        final double normProduct = centroid().frobeniusNorm() * doc.frobeniusNorm();

        return dotProduct / normProduct;
      }

      return 0.0D;
    }

    /**
     * Returns the radius of the cluster. The radius is the average of the
     * square root of the sum of squares of its constituent document term
     * vector coordinates with that of the centroid.
     * @return the radius of the cluster.
     */
    public double getRadius() {
      double radius = 0.0D;
      if (centroid != null) {
        for (String itemName : itemList()) {
          Matrix doc = itemMatrix(itemName);
          radius += doc.minus(centroid).frobeniusNorm();
        }
      }

      return radius / itemList().size();
    }


    void remove(String methodName) {
      itemToMatrixMap().remove(methodName);
      itemList().remove(methodName);
    }

    @Override public String toString() {
      return itemList().toString();
    }
  }

  /**
   * Clustering Index
   */
  static class Index {
    private int docCount;
    private int itemCount;

    private Map<String, List<Item>> indexMap;
    private Map<String, String>     docMap;

    private Matrix      itemFrequencyMatrix;
    private List<Item>  itemList;
    private Set<String> docSet;

    Index() {
      this.itemCount  = 0;
      this.docCount   = 0;
      this.indexMap   = new ConcurrentHashMap<>();
      this.docMap     = new ConcurrentHashMap<>();
      this.itemList   = new CopyOnWriteArrayList<>();
      this.docSet     = new HashSet<>();

      this.itemFrequencyMatrix = null;
    }

    /**
     * It creates an index based on a map between method-to-records
     * data object.
     *
     * @param data data object.
     * @return a new Index object
     */
    static Index createIndex(Map<String, List<Record>> data) {

      final List<Item> wordSet = new ArrayList<>();
      for (String eachKey : data.keySet()) {
        final List<Record> records = data.get(eachKey);

        for (Record eachRecord : records) {
          final Item word = new Item(eachRecord.type());
          word.add(eachKey);

          wordSet.add(word);
        }
      }


      final ItemCounter counter = new ItemCounter(wordSet);

      return Index.createIndex(Immutable.listOf(counter.top(counter.totalCount())));
    }

    /**
     * It creates an index based on a flatten word list
     *
     * @param words flatten Word List.
     * @return a new Index object.
     */
    static Index createIndex(List<Item> words) {
      final Index index = new Index();
      index.index(words);
      return index;
    }

    Set<String> docSet() {
      return indexMap.keySet();
    }

    List<Item> itemList() {
      return itemList;
    }

    Matrix itemDocFrequency() {
      return itemFrequencyMatrix;
    }

    void index(List<Item> words/*unique*/) {

      final Map<String, List<Item>> map = new HashMap<>();
      final Set<Item> wordsSet = new LinkedHashSet<>();
      wordsSet.addAll(words);

      for (Item each : words) {
        final Set<String> docs = each.container();

        for (String doc : docs) {
          if (!map.containsKey(doc)) {
            map.put(doc, new ArrayList<>(Collections.singletonList(each)));
          } else {
            map.get(doc).add(each);
          }
        }
      }

      for (String each : map.keySet()) {
        indexMap.put(each, map.get(each));
        docMap.put(each, each);
        docSet.add(each);
      }

      docCount = indexMap.keySet().size();
      itemCount = wordsSet.size();
      itemList.addAll(wordsSet);

      createWordDocMatrix();
    }

    void createWordDocMatrix() {
      final double[][] data = new double[itemCount][docCount];

      final List<String> docList = docSet.stream()
        .collect(Collectors.toList());

      for (int i = 0; i < itemCount; i++) {
        for (int j = 0; j < docCount; j++) {

          final List<Item> ws = indexMap.get(docMap.get(docList.get(j)));
          if (ws == null) continue;

          final Item word = itemList().get(i);

          int count = 0;
          for (Item each : ws) {
            if (Objects.equals(each, word)) count++;
          }

          data[i][j] = count;
        }
      }

      itemFrequencyMatrix = Numerics.newMatrix(data);
    }
  }

  /**
   * Item representation
   */
  static class Item {
    final String element;
    final Set<String> container;

    int count;

    Item(String element) {
      this.element = element;
      this.container = new HashSet<>();
      this.count = 1;
    }

    void add(String container) {
      if (!this.container.contains(container)) {
        this.container.add(container);
      }
    }

    String element() {
      return element;
    }

    @Override public int hashCode() {
      return element().hashCode();
    }

    @Override public boolean equals(Object obj) {
      if (!(obj instanceof Item)) return false;

      final Item other = (Item) obj;

      return other.element().equalsIgnoreCase(element());
    }

    /**
     * Counts one step
     */
    int count() {
      return count(1);
    }

    int count(int step) {
      count = count + step;
      return count;
    }

    int value() {
      return count;
    }

    Set<String> container() {
      return container;
    }

    @Override public String toString() {
      return element();
    }
  }

  /**
   * Items counter
   */
  static class ItemCounter {
    private final Map<Item, Item> items;
    private final AtomicInteger   totalItemCount;

    /**
     * Counts words in some text.
     */
    ItemCounter(List<Item> items) {
      this.items = new HashMap<>();
      this.totalItemCount = new AtomicInteger(0);

      addAll(items);
    }

    /**
     * Adds all items in iterable to this counter.
     *
     * @param items iterable made of string items.
     */
    void addAll(final List<Item> items) {
      if (Objects.isNull(items)) return;
      if (items.contains(null)) return;

      for (Item each : items) {
        if (Objects.isNull(each)) continue;

        add(each);
      }
    }


    /**
     * Adds an item to this counter.
     *
     * @param item string item.
     */
    void add(Item item) {
      if (item == null) return;

      if (items.containsKey(item)) {
        addEntry(item);
      } else {
        if (items.containsKey(item)) {
          addEntry(item);
        } else {
          add(item, 1);
        }
      }
    }

    private void addEntry(Item item) {
      final Item entry = items.get(item);
      if (entry == null) {
        add(item, item.count());
      } else {
        entry.container().addAll(item.container());
        add(entry, entry.count());
      }
    }

    /**
     * Adds a fixed count of items to this counter.
     *
     * @param item  string item
     * @param count number of times this item will be added.
     */
    void add(Item item, int count) {
      items.put(item, item);
      totalItemCount.addAndGet(count);
    }

    /**
     * Returns the list of most frequent items.
     *
     * @param k number of results to collect.
     * @return A list of the min(k, size()) most frequent items
     */
    List<Item> top(int k) {
      final List<Item> all = entriesByFrequency();
      final int resultSize = Math.min(k, items.size());
      final List<Item> result = new ArrayList<>(resultSize);

      result.addAll(all.subList(0, resultSize).stream()
        .collect(Collectors.toList()));

      return Collections.unmodifiableList(result);
    }

    /**
     * Returns the list of items (ordered by their frequency)
     *
     * @return the list of ordered items.
     */
    private List<Item> entriesByFrequency() {
      return items.entrySet().stream()
        .map(Map.Entry::getValue)
        .sorted((a, b) -> Integer.compare(b.value(), a.value()))
        .collect(Collectors.toList());
    }

    int totalCount(){
      return totalItemCount.get();
    }

  }
}
