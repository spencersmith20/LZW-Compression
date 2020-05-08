# LZW Compression algorithm using numerous modifications on the LZW algorithm. Uses some code from textbook, which is cited in # the header of each (everything except MyLZW.java)

# Compression of foo.txt to foo.lzw (arbitrary extension)
java MyLZW - r < foo.txt > foo.lzw

# Expansion of foo.lzw to foo2.txt (will overwrite if exists already). n, r, m not required for expansion
java MyLZW + < foo.lzw > foo2.txt

# The letter corresponds to the LZW algorithm's behavior once the codebook is full
n - Do Nothing mode
r - Reset the codebook
m - Monitor mode (monitor compression ratio and reset at a certain threshold)
