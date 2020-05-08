/*************************************************************************
 *  Compilation:  javac MyLZW.java
 *  Execution:    java MyLZW - (MODE) < input.txt > output.lzw  (compress)
 *  Execution:    java MyLZW + < input.lzw > output.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
*************************************************************************/
import java.util.Arrays;
import java.lang.Math;

public class MyLZW {

    private static final int R = 256;               // number of input chars, extended ASCII

    private static int L = 512;                     // number of codewords for this value of W
    private static int W = 9;                       // codeword width -> varies from 9 to 16

    private static final int MAX_W = 16;
    private static final int MAX_L = 65536;

    public static void compress(char mode) {

      double oldRatio = 0; double newRatio = 0; double ratioOfRatios;

      // read input, create trie
      String input = BinaryStdIn.readString();

      // ternary search trie stores dictionary
      TST<Integer> st = new TST<Integer>();

      // OUTPUT MODE TO FILE
      BinaryStdOut.write(mode, W);

      // add each character encoding 0 to 255 to dictionary
      for (int i = 0; i < R; i++)
          st.put("" + (char) i, i);

      int code = R+1;  // R is codeword for EOF, so increment by one

      // set bit counts for compression ratio monitoring
      long uncompressedBits = 0; long compressedBits = 0; int charSize = 16;

      while (input.length() > 0) {

        // WRITE TO FILE ... first, find max prefix match s
        String s = st.longestPrefixOf(input);

        // check if we hit the top limit for current codeword width, if so increment L & W
        if ((code == L) && (W <= MAX_W) && (L != MAX_L)){
          W++; L *= 2;
          System.err.println("Move W to " + W + ", L to " + L);
        }

        // print s encoding, add to running total of bits
        BinaryStdOut.write(st.get(s), W);

        // EXPAND DICTIONARY
        int t = s.length();

        // increase bitCounts, if using monitor mode
        if (mode == 'm'){
          compressedBits += W; uncompressedBits += (t * charSize);
        }

        // add s to symbol table
        if (t < input.length() && code < L)
            st.put(input.substring(0, t + 1), code++);

        // Scan past s in input
        input = input.substring(t);

        //should new ratio be calculated
        if (oldRatio > 0 ){

          //calculate new ratio and ratio of ratios
          newRatio = uncompressedBits / compressedBits;
          ratioOfRatios = oldRatio/newRatio;

          // if ratio of ratios is above threshold
          if (ratioOfRatios > 1.1){
            System.err.println("Resetting...");

            // reset
            oldRatio = 0; newRatio = 0; ratioOfRatios = 0;
            uncompressedBits = 0; compressedBits = 0;

            // ternary search trie stores dictionary
            st = new TST<Integer>();

            // add each character encoding 0 to 255 to dictionary
            for (int i = 0; i < R; i++)
                st.put("" + (char) i, i);

            L = 512; W = 9; code = R+1;  // R is codeword for EOF, increment by one
          }
        }

        // check for full code book | reset mode
        if ((code == MAX_L) && (mode == 'r')){

          System.err.println("Resetting...");

          // ternary search trie stores dictionary
          st = new TST<Integer>();

          // add each character encoding 0 to 255 to dictionary
          for (int i = 0; i < R; i++)
              st.put("" + (char) i, i);

          L = 512; W = 9; code = R+1;  // R is codeword for EOF, increment by one
        }

        // check for full codebook | monitor mode, set value for oldRatio
        else if ((code == MAX_L) && (mode == 'm') && (oldRatio == 0)){
          oldRatio = uncompressedBits / compressedBits;
          System.err.println("Monitoring compression ratio...");
        }
      }

      // WRITE FILE ENDING CODEWORD
      BinaryStdOut.write(R, W); BinaryStdOut.close();
      System.err.println("Compression complete!");
    }

    public static void expand() {

      long oldRatio = 0; long newRatio = 0;

      String[] st = new String[MAX_L];
      int i; // next available codeword value

      char mode = (char) BinaryStdIn.readInt(W);
      System.err.println(mode);

      // initialize symbol table with all 1-character strings
      for (i = 0; i < R; i++)
          st[i] = "" + (char) i;
      st[i++] = "";                        // (unused) lookahead for EOF

      // read in first codeword of length W
      int codeword = BinaryStdIn.readInt(W);

      // initialize val
      String val = st[codeword];

      // to get size of uncompressed
      long uncompressedBits = 0; long compressedBits = 0; int charSize = 16;
      long ratioOfRatios;

      while (true) {

        // if monitor mode, increment numBits for both compressed & uncompressed
        if (mode == 'm'){
          compressedBits += W; uncompressedBits += (val.length() * charSize);
        }

        // OUTPUT PATTERN TO FILE
        BinaryStdOut.write(val);

        // if EOF, break
        if (codeword == R)
          break;

        //should newRatio be calculated
        if (oldRatio > 0 ){

         //calculate new ratio and ratio of ratios
          newRatio = uncompressedBits / compressedBits;
          ratioOfRatios = oldRatio / newRatio;

          // if ratio of ratios is above threshold
          if (ratioOfRatios > 1.1){
            System.err.println("Resetting...");

            // reset
            oldRatio = 0; newRatio = 0; ratioOfRatios = 0;
            uncompressedBits = 0; compressedBits = 0;

            // ternary search trie stores dictionary
            Arrays.fill(st, null);

            // add each character encoding 0 to 255 to dictionary
            for (int j = 0; i < R; i++)
                st[i] = "" + (char) i;

            // lookahead for EOF & reset
            st[i] = "";
            W = 9; L = 512;

          }
        }

        // check for full codebook | reset mode
        if ((i == MAX_L-1) && (mode == 'r')){

          System.err.println("Resetting...");

          // clear array
          Arrays.fill(st, null);

          // fill with initial ASCII again
          for (i = 0; i < R; i++)
              st[i] = "" + (char) i;

          // lookahead for EOF & reset
          st[i] = "";
          W = 9; L = 512;
        }

        // check for full codebook | monitor mode, set value for oldRatio
        else if ((i == MAX_L - 1) && (mode == 'm') && (oldRatio == 0)){
          oldRatio = uncompressedBits / compressedBits;
          System.err.println("Monitoring compression ratio...");
        }


        // CHECK FOR CODEBOOK SIZE INCREASE
        if ((i == L-1) && (L != MAX_L)){
          W++; L*=2;
          System.err.println("Moving W to " + W + ", L to " + L);
        }

        // READ NEXT CODEWORD & LOOK UP IN CODEBOOK
        codeword = BinaryStdIn.readInt(W);
        String s = st[codeword];

        // manage weird case
        if (i == codeword)
          s = val + val.charAt(0);   // special case hack

        // ADD PREVIOUS PATTERN + FIRST CHAR OF CURRENT PATTERN TO CODEBOOK
        if (i < L)
          st[i++] = val + s.charAt(0);

        // set val to current pattern
        val = s;

      }

    BinaryStdOut.close();
  }

    public static void main(String[] args) {
      if      (args[0].equals("-")) compress(args[1].charAt(0));
      else if (args[0].equals("+")) expand();
      else throw new IllegalArgumentException("Illegal command line argument");
    }
}
