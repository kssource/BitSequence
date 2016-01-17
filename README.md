# BitSequence
Java bit sequence utility, toBinaryString

BitSequence can be created from number, byte array or BitCollector. 

Download jar and doc [zip](https://github.com/kssource/BitSequence/raw/master/jar/bitSeq.zip)

You can use the following functions:

	* create byte sequence required length from byte array or number
	* convert back to byte array or number
	* subSequence, split, concat, insert, extract of bit sequences
	* shift, rotate
	* bitwise and, or, xor, not
	* set, clear bits
	* iterate over bit sequence, boolean or int, reverse iterator
	* get binary string, continuous or grouped to byte, half-byte or any group size 


Examples for binary string:

    byte[] bArr = {10, -20, 30};
    BitSequence bs1 = new BitSequence(bArr);
    System.out.println(Arrays.toString(bArr)+"->"+bs1.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));

    int i = -10;
    BitSequence bs2 = new BitSequence(i, BitSequence.SOURCE_TYPE_SIZE);
    System.out.println("i="+i+"->"+bs2.toBynaryString(ALIGN.RIGHT, GROUP.CONTINOUSLY));

    BigInteger bi = new BigInteger(""+10);
    BitSequence bs3 = new BitSequence(bi, BitSequence.SOURCE_TYPE_SIZE);
    System.out.println("bi="+bi+"->"+bs3.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));

    short sh = 10;
    BitSequence bs4 = new BitSequence(sh, 14);
    System.out.println("sh="+sh+"->"+bs4.toBynaryString(ALIGN.RIGHT, GROUP.HALF_BYTE));

    long l = 10;
    BitSequence bs5 = new BitSequence(l, 11);
    System.out.println("l="+l+"->"+bs5.toBynaryString(ALIGN.LEFT, GROUP.BYTE));

    byte by = -10;
    BitSequence bs6 = new BitSequence(by, BitSequence.MIN_SIZE);
    System.out.println("by="+by+"->"+bs6.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));

    long l2 = 10;
    BitSequence bs7 = new BitSequence(l2, 11);
    System.out.println("l2="+l2+"->"+bs7.toBynaryString(ALIGN.RIGHT, 3));

    BitCollector bc = new BitCollector();
    bc.append(32, i);
    System.out.println("i="+i+"->bc->"+bc.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));



    prints:
    [10, -20, 30]->00001010 11101100 00011110
    i=-10->00000000000000000000000000001010
    bi=10->1010
    sh=10->00 0000 0000 1010
    l=10->00000001 010
    by=-10->1010
    l2=10->00 000 001 010
    i=-10->bc->11111111 11111111 11111111 11110110




