package daikon.mints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Huascar Sanchez
 */
class Numerics {
  private Numerics(){}

  static Matrix newMatrix(int M, int N){
    return new Matrix(M, N);
  }

  static Matrix newMatrix(double[][] data){
    return new Matrix(data);
  }

  static Vector newVector(int dimension){
    return new Vector(dimension);
  }

  static Vector newVector(double... a){
    return new Vector(a);
  }


  static <T> Map<T, Matrix> splitMatrix(List<T> items, Matrix matrix){
    final Map<T, Matrix> map = new HashMap<>();

    int idx = 0; for (T each : items){
      map.put(each, Matrix.getRow(matrix, idx));
      idx++;
    }

    return map;
  }

  /**
   * Matrix of (real) numbers.
   */
  static class Matrix {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private final double[][] data;   // M-by-N array

    // create M-by-N matrix of 0's
    Matrix(int M, int N) {
      this.M = M;
      this.N = N;
      data = new double[M][N];
    }

    // create matrix based on 2d array
    Matrix(double[][] data) {
      this.M = data.length;
      this.N = data[0].length;

      this.data = new double[M][N];

      for (int i = 0; i < M; i++) {
        System.arraycopy(data[i], 0, this.data[i], 0, N);
      }
    }

    // copy constructor
    private Matrix(Matrix A) { this(A.data); }


    // swap rows i and j
    private void swap(int i, int j) {
      double[] temp = data[i];
      data[i] = data[j];
      data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
      Matrix A = new Matrix(N, M);

      for (int i = 0; i < M; i++){
        for (int j = 0; j < N; j++) {
          A.data[j][i] = this.data[i][j];
        }
      }

      return A;
    }

    // return C = A + B
    public Matrix plus(Matrix matrixB) {
      final Matrix matrixA = this;

      if (matrixB.M != matrixA.M || matrixB.N != matrixA.N) {
        throw new RuntimeException("Illegal matrix dimensions.");
      }

      final Matrix matrixC = new Matrix(M, N);

      for (int i = 0; i < M; i++) {
        for (int j = 0; j < N; j++) {
          matrixC.data[i][j] = matrixA.data[i][j] + matrixB.data[i][j];
        }
      }

      return matrixC;
    }


    // return C = A - B
    public Matrix minus(Matrix matrixB) {
      final Matrix matrixA = this;

      if (matrixB.M != matrixA.M || matrixB.N != matrixA.N) {
        throw new RuntimeException("Illegal matrix dimensions.");
      }

      final Matrix matrixC = new Matrix(M, N);

      for (int i = 0; i < M; i++){
        for (int j = 0; j < N; j++) {
          matrixC.data[i][j] = matrixA.data[i][j] - matrixB.data[i][j];
        }
      }

      return matrixC;
    }

    // does A = B exactly?
    public boolean eq(Matrix matrixB) {
      final Matrix matrixA = this;
      if (matrixB.M != matrixA.M || matrixB.N != matrixA.N) {
        throw new RuntimeException("Illegal matrix dimensions.");
      }

      for (int i = 0; i < M; i++){
        for (int j = 0; j < N; j++) {
          if (matrixA.data[i][j] != matrixB.data[i][j]) return false;
        }
      }

      return true;
    }

    int getRowDimension(){
      return M;
    }

    int getColDimension(){
      return N;
    }

    Matrix subMatrix(int a, int b, int c, int d){
      final Matrix matrixA    = this;
      final Matrix subMatrix  = new Matrix(b - a + 1, d - c + 1);

      try {
        for (int i = a; i <= b; i++){
          for(int j = c; j <= d; j++){
            subMatrix.data[i-a][j-c] = matrixA.data[i][j];
          }
        }
      } catch (Exception e){
        throw new RuntimeException("Illegal sub matrix indices");
      }



      return subMatrix;
    }

    // return C = A * B
    public Matrix times(Matrix matrixB) {
      final Matrix matrixA = this;

      if (matrixA.N != matrixB.M) {
        throw new RuntimeException("Illegal matrix dimensions.");
      }

      final Matrix matrixC = new Matrix(matrixA.M, matrixB.N);

      for (int i = 0; i < matrixC.M; i++){
        for (int j = 0; j < matrixC.N; j++){
          for (int k = 0; k < matrixA.N; k++) {
            matrixC.data[i][j] += (matrixA.data[i][k] * matrixB.data[k][j]);
          }
        }
      }

      return matrixC;
    }

    // return C = A * alpha
    public Matrix times(double alpha) {

      final Matrix matrixA = this;
      final Matrix matrixC = new Matrix(matrixA.M, matrixA.N);


      for (int i = 0; i < matrixC.M; i++) {
        for (int j = 0; j < matrixC.N; j++){
          matrixC.data[i][j] = alpha * matrixA.data[i][j];
        }
      }

      return matrixC;
    }

    /**
     * Computes the Frobenius norm
     * @return sqrt of sum of squares of all elements.
     */
    double frobeniusNorm() {
      double f = 0;
      for (int i = 0; i < M; i++) {
        for (int j = 0; j < N; j++) {
          f = hypotenuse(f, data[i][j]);
        }
      }

      return f;
    }

    /**
     * @return maximum column sum (One norm)
     */
    double oneNorm() {
      double f = 0;

      for (int j = 0; j < N; j++) {
        double s = 0;

        for (int i = 0; i < M; i++) {
          s += Math.abs(data[i][j]);
        }

        f = Math.max(f,s);
      }

      return f;
    }

    /**
     * Gets the specified row of a matrix.
     *
     * @param m the matrix.
     * @param row the row to get.
     * @return the specified row of m.
     */
    public static Matrix getRow(Matrix m, int row) {
      return m.subMatrix(row, row, 0, m.getColDimension() - 1);
    }

    /**
     * @return {@code sqrt(a^2 + b^2)} without under/overflow.
     */
    static double hypotenuse(double a, double b) {
      double r;
      if (Math.abs(a) > Math.abs(b)) {
        r = b/a;
        r = Math.abs(a) * Math.sqrt(1 + (r * r));
      } else if (b != 0) {
        r = a/b;
        r = Math.abs(b) * Math.sqrt(1 + (r * r));
      } else {
        r = 0.0;
      }

      return r;
    }

    /**
     * @return element-by-element multiplication
     */
    Matrix hadamardProduct(Matrix matrixB){

      final Matrix matrixA = this;

      if (matrixA.N != matrixB.N || matrixA.M != matrixB.M) {
        throw new RuntimeException("Illegal matrix dimensions.");
      }

      final Matrix matrixC = new Matrix(matrixA.M, matrixB.N);

      for (int i = 0; i < matrixC.M; i++){
        for (int j = 0; j < matrixC.N; j++){
          matrixC.data[i][j] = (matrixA.data[i][j] * matrixB.data[i][j]);
        }
      }

      return matrixC;
    }

    // print matrix to standard output
    void printMatrix() {
      for (int i = 0; i < M; i++) {
        for (int j = 0; j < N; j++) {
          System.out.printf("%9.4f ", data[i][j]);
        }

        System.out.println();
      }
    }

  }

  /**
   * Vector of (real) numbers.
   */
  static class Vector {
    private int dimension;
    private double[] data;

    /**
     * Constructs a new d-dimensional zero vector.
     *
     * @param dimension the dimension of this vector.
     */
    Vector(int dimension){
      this.dimension  = dimension;
      this.data       = new double[this.dimension];
    }


    /**
     * Construct a vector from either an array or a vararg list.
     * The vararg syntax supports a constructor that takes a variable number of
     * arguments, such as Vector x = new Vector(1.0, 2.0, 3.0, 4.0).
     *
     * @param a  the array or vararg list
     */
    public Vector(double... a) {
      this.dimension = a.length;

      // defensive copy so that client can't alter our copy of data[]
      data = new double[this.dimension];

      System.arraycopy(a, 0, data, 0, this.dimension);
    }

    /**
     * @return the dimension of this vector
     */
    int dimension() {
      return dimension;
    }

    /**
     * Computes the dot product between this vector and another vector.
     *
     * @param  that the other vector
     * @return the dot product of this vector and that vector
     * @throws IllegalArgumentException if the dimensions of the two vectors are not equal
     */
    double dot(Vector that) {
      if (dimension() != that.dimension) {
        throw new IllegalArgumentException("Dimensions don't agree");
      }

      int    n   = dimension();
      double sum = 0.0;

      for (int i = 0; i < n; i++) {
        sum = sum + (this.data[i] * that.data[i]);
      }

      return sum;
    }


    /**
     * @return the magnitude (a.k.a., L2 norm or the Euclidean norm) of
     * this vector
     */
    double magnitude() {
      return Math.sqrt(this.dot(this));
    }

    /**
     * Computes the Euclidean distance between this vector and another
     * vector.
     *
     * @param  that the other vector
     * @return the Euclidean distance between two vectors.
     * @throws IllegalArgumentException if the dimensions of the two vectors are not equal
     */
    double distanceTo(Vector that) {
      if (dimension() != that.dimension()) {
        throw new IllegalArgumentException("Dimensions don't agree");
      }

      return this.minus(that).magnitude();
    }

    /**
     * Difference between this vector and another vector.
     *
     * @param  that the vector to subtract from this vector
     * @return the vector which value is {@code (this - that)}
     * @throws IllegalArgumentException if the dimensions of the two vectors are not equal
     */
    Vector minus(Vector that) {
      if (dimension() != that.dimension()) {
        throw new IllegalArgumentException("Dimensions don't agree");
      }

      final int    n = dimension();
      final Vector c = new Vector(n);

      for (int i = 0; i < n; i++) {
        c.data[i] = this.data[i] - that.data[i];
      }

      return c;
    }

    /**
     * Sum of this vector and the specified vector.
     *
     * @param  that the vector to add to this vector
     * @return the vector which value is {@code (this + that)}
     * @throws IllegalArgumentException if the dimensions of the two vectors are not equal
     */
    Vector plus(Vector that) {
      if (dimension() != that.dimension()) {
        throw new IllegalArgumentException("Dimensions don't agree");
      }

      final int    n = dimension();
      final Vector c = new Vector(n);

      for (int i = 0; i < n; i++) {
        c.data[i] = this.data[i] + that.data[i];
      }

      return c;
    }

    /**
     * Gets the i-th cartesian coordinate.
     *
     * @param  idx the coordinate index
     * @return the ith cartesian coordinate
     */
    double cartesian(int idx) {
      return data[idx];
    }

    /**
     * Computes the scalar-vector product of this vector and the given scalar.
     *
     * @param  alpha the scalar
     * @return the vector whose value is {@code (alpha * this)}
     */
    Vector scale(double alpha) {
      final int    n = dimension();
      final Vector c = new Vector(n);

      for (int i = 0; i < n; i++) {
        c.data[i] = alpha * data[i];
      }

      return c;
    }

    /**
     * Creates a unit vector in the direction of this vector.
     *
     * @return a unit vector in the direction of this vector
     * @throws ArithmeticException if this vector is the zero vector
     */
    Vector direction() {
      if (this.magnitude() == 0.0) {
        throw new ArithmeticException("Zero-vector has no direction");
      }

      return this.scale(1.0 / this.magnitude());
    }

    @Override public String toString() {
      return Arrays.toString(data);
    }
  }
}
