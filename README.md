# BitSequence
Java bit sequence utility, toBinaryString

BitSequence can be created from number, byte array or BitCollector. 

Download jar and doc from subfolder jar.

You can use the following functions:

	* subSequence, split, concat, insert, extract of bit sequences
	* shift, rotate
	* bitwise and, or, xor, not
	* set, clear bits
	* iterate over bit sequence, boolean or int, reverse iterator
	* get binary string, continuous or grouped to byte or half-byte 


Examples for binary string:

    byte[] bArr = {10, 20, 30};
    BitSequence bs1 = new BitSequence(bArr);
    System.out.println(Arrays.toString(bArr)+"->"+bs1.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));

    int i = 10;
    BitSequence bs2 = new BitSequence(i, BitSequence.SOURCE_SIZE);
    System.out.println("i="+i+"->"+bs2.toBynaryString(ALIGN.RIGHT, GROUP.CONTINOUSLY));

    BigInteger bi = new BigInteger(""+10);
    BitSequence bs3 = new BitSequence(bi, BitSequence.SOURCE_SIZE);
    System.out.println("bi="+bi+"->"+bs3.toBynaryString(ALIGN.RIGHT, GROUP.BYTE));

    short sh = 10;
    BitSequence bs4 = new BitSequence(sh, 12);
    System.out.println("sh="+sh+"->"+bs4.toBynaryString(ALIGN.RIGHT, GROUP.HALF_BYTE));

    long l = 10;
    BitSequence bs5 = new BitSequence(l, 11);
    System.out.println("l="+l+"->"+bs5.toBynaryString(ALIGN.LEFT, GROUP.BYTE));


    prints:
    [10, 20, 30]->00001010 00010100 00011110
    i=10->00000000000000000000000000001010
    bi=10->1010
    sh=10->0000 0000 1010
    l=10->00000001 010



