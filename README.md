# LZW Compression

LZW Compression algorithm using numerous modifications on the LZW algorithm. Uses some code from textbook, which is cited in the header of each (everything except MyLZW.java)

# Compression
java MyLZW - r < foo.txt > foo.lzw

The letter corresponds to the LZW algorithm's behavior once the codebook is full
n - Do Nothing mode
r - Reset the codebook
m - Monitor mode (monitor compression ratio and reset at a certain threshold)

# Expansion
(will overwrite if exists already)
java MyLZW + < foo.lzw > foo2.txt
letter is not required for expansion
