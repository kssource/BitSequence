package de.jk.bit.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;


/**
 * A sequence of bits.
 * </br>
 * </br>
 * Align LEFT or RIGHT.
 * <p>
 * When converting the bit sequence from/to byte [], it is often the case that the number of bits is not a multiple of 8.
 * For example, 12-bit sequence 101000111001 to be converted to byte [].</br> 
 * It can be 16 bit with trailing zeros fill [10100011 10010000] - left aligned</br> 
 * or with leading zeros [00001010 00111001] - right aligned.
 * </p>  
 * <p>
 * Data, written with bit streams to a file is often left aligned.</br>
 * Numbers, such as int or byte[] of BigInteger - right aligned (filled with leading zeros).</br>
 * Default (and internally) this class used right aligned data, like number.
 * </p>  
 * <p>
 * For strings align means start of grouping.
 * </p>  
 * <p>
 * In bitwise operations alignment right means, that smaller sequence extended with leading zeros to equals size.</br>
 * On alignment left - with trailing zeros.
 * </p>  
 * <p>
 * Indexing.</br>
 * Left-most bit has index 0.
 * </p>  
 */

public class BitSequence implements Iterable<Boolean>, Comparable<BitSequence>{

	
	public enum ALIGN{
		LEFT, RIGHT
	}

	public enum GROUP{
		CONTINOUSLY(0), BYTE(8), HALF_BYTE(4);
		
		private int size;
		private GROUP(int size){
			this.size = size;
		}
		
		public int getSize() {
			return size;
		}
	}

	public enum KEEP_SIZE{
		KEEP, NOT_KEEP
	}

	public enum DIRECTION{
		LEFT_TO_RIGHT, RIGHT_TO_LEFT
	}

	/**
	 * Constant, specifies that number of bits is determined by the source size.
	 */
	public static final int SOURCE_SIZE = -1;
	
	private BigInteger bInt;
	private int targetBitsCount = -1;// bits, coded in this inst  


	/**
	 * Translates a byte array into BitSequence.
	 * @param sourceArr it is assumed that the array is right-aligned.
	 * Bit count is (length of sourceArr) * 8 .
	 */
	public BitSequence(byte[] sourceArr) {
		this(sourceArr, SOURCE_SIZE, ALIGN.RIGHT);
	}

	

	
	// if bitCount == SOURCE_SIZE, calculated bitCount = bytesCount*8
	// if sourceAlign == RIGHT, right-most bitCount bits used, 
	// if bitCount>sourceBitCount, sequence filled with leading zeros
	// if sourceAlign == LEFT, left-most bitCount bits used, 
	// if bitCount>sourceBitCount, sequence filled with trailing zeros

	/**
	 * Translates a byte array into BitSequence.
	 * @param bitCount if bitCount == SOURCE_SIZE, calculated bitCount = bytesCount*8
	 * @param sourceAlign see above
	 */
	public BitSequence(byte[] sourceArr, int bitCount, ALIGN sourceAlign) {
		if(bitCount <= SOURCE_SIZE){
			targetBitsCount = sourceArr.length*8;
			bInt = new BigInteger(1, sourceArr);
		}else{
			targetBitsCount = bitCount;
			createBitSequence(sourceArr, bitCount, sourceAlign);
		}
	}

	
	/**
	 * Constructs a BitSequence from binary representation of number.
	 * @param number accept BigInteger, Byte, Integer, Long, Short.
	 * Sign ignored
	 */
	public BitSequence(Number number, int bitCount) {
		initFromNumber(number, bitCount);	
	}


	private void initFromNumber(Number number, int bitCount) {
		if(number instanceof BigInteger){
			BigInteger bi = (BigInteger) number;
			int len = bitCount;
			if(bitCount<=SOURCE_SIZE){
				len = bi.bitLength();
			}
			createBsFromBigInt(bi, len);
		}else if(number instanceof Byte){
			int len = bitCount;
			if(bitCount<=SOURCE_SIZE){
				len = Byte.SIZE;
			}
			BigInteger bi = new BigInteger(number.toString());
			createBsFromBigInt(bi, len);
		}else if(number instanceof Integer){
			int len = bitCount;
			if(bitCount<=SOURCE_SIZE){
				len = Integer.SIZE;
			}
			BigInteger bi = new BigInteger(number.toString());
			createBsFromBigInt(bi, len);
		}else if(number instanceof Long){
			int len = bitCount;
			if(bitCount<=SOURCE_SIZE){
				len = Long.SIZE;
			}
			BigInteger bi = new BigInteger(number.toString());
			createBsFromBigInt(bi, len);
		}else if(number instanceof Short){
			int len = bitCount;
			if(bitCount<=SOURCE_SIZE){
				len = Short.SIZE;
			}
			BigInteger bi = new BigInteger(number.toString());
			createBsFromBigInt(bi, len);
		}else{
			throw new IllegalArgumentException("Only BigInteger, Byte, Integer, Long or Short types accepted");
		}
		
	}


	private void createBsFromBigInt(BigInteger bi, int bitCount) {
		targetBitsCount = bitCount;
		//convert to positive
		bInt = bi.abs();
	}


	private void createBitSequence(byte[] sourceArr, int bitCount, ALIGN align) {
		int sourceBytesCount = sourceArr.length;
		int sourceBitCount = sourceBytesCount*8;
		if(sourceBitCount == bitCount){
			bInt = new BigInteger(1, sourceArr);
		}else{// different bit count
			int targetByteCount = getTargetByteArrLength();
			byte[] targetArr;
			if(targetByteCount == sourceBytesCount){
				targetArr = sourceArr;
			}else{// different bytes count 
				targetArr = new byte[targetByteCount];
				if(targetByteCount > sourceBytesCount){
					Arrays.fill(targetArr, (byte)0);
				}
				
				int fromIndex = 0;
				if(align == ALIGN.RIGHT && sourceBytesCount>targetByteCount){
					fromIndex = sourceBytesCount - targetByteCount;
				}

				int length = Math.min(sourceBytesCount, targetByteCount); 
				int targetArrPos = 0;
				if(align == ALIGN.RIGHT){
					targetArrPos = targetByteCount - length;
				}
				
				
				System.arraycopy(sourceArr, fromIndex, targetArr, targetArrPos, length);
				bInt = new BigInteger(1, targetArr);
			}
			
			// set fill-bits to zero
			int relevantBitsCountOfUnfullByte = targetBitsCount % 8;
			int countOfAdditionalZeroBits = 8-relevantBitsCountOfUnfullByte;
			if(relevantBitsCountOfUnfullByte != 0){
				if(align == ALIGN.RIGHT){
					int mask = 0xFF;//0b11111111
					mask = mask>>countOfAdditionalZeroBits;

					byte leadingByte = targetArr[0];
					int leadingInt = leadingByte & mask ;
					leadingByte = (byte) (leadingInt & 0xff);
					targetArr[0] = leadingByte;
					bInt = new BigInteger(1, targetArr);
				}else{//sourceLeftAligned
					bInt = new BigInteger(1, targetArr);
					bInt = bInt.shiftRight(countOfAdditionalZeroBits);
				}
			}
		}
		
	}



	public int getBitCount() {
		return targetBitsCount;
	}

	
	// !!! bits added or removed at front of the sequence
	private void setBitCount(int count) {

		if(count != targetBitsCount){
			this.targetBitsCount = count;
			// reinit bInt value
			byte[] arr = this.toByteArray();
			bInt = new BigInteger(1, arr);
		}
	}

	private int getTargetByteArrLength() {
		int bytesCount = targetBitsCount / 8;
		int mod = targetBitsCount % 8;
		if(mod > 0){
			++bytesCount;
		}

		return bytesCount;
	}

	/**
	 * 
	 * @return positive BitInteger with this bit sequence as magnitude
	 */
	public BigInteger getAsBigInteger() {
		return bInt;
	}

	/**
	 * Return right aligned byte[]
	 */
	public byte[] toByteArray() {
		byte[] arr = bInt.toByteArray();
		byte[] fullArr = toTargetArray(arr);
		
		return fullArr;// extendet or cutted to needed length
	}

	public byte[] toByteArray(ALIGN align) {
		byte[] arr = bInt.toByteArray();
		byte[] outArr = toTargetArray(arr);
		
		if(align == ALIGN.LEFT){
			BitSequence bitSequence = new BitSequence(outArr, targetBitsCount, ALIGN.RIGHT);
			int relevantBitsCountOfLeadingByte = targetBitsCount % 8;
			
			if(relevantBitsCountOfLeadingByte>0){
				int countOfZeroBits = 8-relevantBitsCountOfLeadingByte;
				BigInteger bi = bitSequence.getAsBigInteger();
				bi = bi.shiftLeft(countOfZeroBits);
				
				outArr = bi.toByteArray();
			}
		
			int targetByteArrLength = getTargetByteArrLength();
			if(outArr.length > targetByteArrLength){
				byte[] destination = new byte[targetByteArrLength];
				int startIndex = outArr.length - targetByteArrLength;
				System.arraycopy(outArr, startIndex, destination, 0, targetByteArrLength);
				outArr = destination;
			}
			
		}
		
		return outArr;// extended or cutted to needed length
	}

	// extend or cut sourceArr to needed length, set leading bits to 0 if need
	private byte[] toTargetArray(byte[] sourceArr) {
		int targetByteArrLength = getTargetByteArrLength();
		
		int arrLen = sourceArr.length;
		if(targetByteArrLength != arrLen){
			
			byte[] destination = new byte[targetByteArrLength];
			if(arrLen > targetByteArrLength){//cut first bytes
				int startIndex = arrLen - targetByteArrLength;
				System.arraycopy(sourceArr, startIndex, destination, 0, targetByteArrLength);
			}else{
				// add additional bytes to start
				int additonalBytesCount = targetByteArrLength - arrLen;
				Arrays.fill(destination, 0, additonalBytesCount, (byte)0);
				System.arraycopy(sourceArr, 0, destination, additonalBytesCount, arrLen);
			}

			sourceArr = destination;
		}

		// set leading (irrelevant, cutted) bits to 0
		int relevantBitsCountOfLeadingByte = targetBitsCount % 8;
		
		if(relevantBitsCountOfLeadingByte != 0){
			int countOfZeroBits = 8-relevantBitsCountOfLeadingByte;
			int mask = 0xFF;//0b11111111
			mask = mask>>countOfZeroBits;

			byte leadingByte = sourceArr[0];
			int leadingInt = leadingByte & mask ;
			leadingByte = (byte) (leadingInt & 0xff);
			sourceArr[0] = leadingByte;
		}
		
		return sourceArr;
	}

    /**
	 * return deep copy of this
     */
    @Override
    public BitSequence clone() {
		byte[] magnitude = bInt.toByteArray();
		BitSequence out = new BitSequence(magnitude, targetBitsCount, ALIGN.RIGHT);
		return out;
	}


	/**
	 * removed n right-most bits, equivalent to getSubsequence(0, bitCount-n)
	 * @param n distance
	 */
    public void shiftRight(int n) {
		if(n < 0 || n > targetBitsCount){
            throw new RuntimeException("Argument must be in range 0-"+targetBitsCount+", " + n);
		}
		bInt = bInt.shiftRight(n);
		setBitCount(this.targetBitsCount-n);
	}

	/**
	 * added n zero-bits at right, equivalent to this.concat(n * zeroBit)
	 * @param n distance
	 */
	public void shiftLeft(int n) {
		if(n < 0){
            throw new RuntimeException("Argument must be >= 0, " + n);
		}
		bInt = bInt.shiftLeft(n);
		setBitCount(this.targetBitsCount+n);
	}

	/**
	 * 
	 * @param n distance, n>0 - shift left, else right
	 * @param keep keep old sequence size.
	 * </br>When keep == KEEP 
	 * <ul>
	 * 		<li>left shift - n left bits removed, n zero-bits added at right </li>	
	 * 		<li>right shift - n right bits removed, n zero-bits added at left </li>	
	 * </ul>
	 */
	public void shift(int n, KEEP_SIZE keep) {
		int oldSize = this.targetBitsCount;
		if(n > 0){// shift left
			bInt = bInt.shiftLeft(n);
			if(keep == KEEP_SIZE.KEEP){
				BitSequence subSeq = this.getSubSequence(0, oldSize);
				bInt = subSeq.bInt;
			}else{
				setBitCount(this.targetBitsCount+n);
			}
		}else if(n < 0){// shift right
			n = -n;
			
			this.shiftRight(n);
			if(keep == KEEP_SIZE.KEEP){
				// add n zero-bits at front of this seq
				BitSequence zeroBits = new BitSequence(0, n); 
				BitSequence seq = zeroBits.concat(this);
				bInt = seq.bInt;
				setBitCount(oldSize);
			}
		}
	}

	/**
	 * 
	 * @param n distance, n>0 - rotate left, else right
	 */
	public void rotate(int n) {
		int distance = n % this.targetBitsCount;
		int splitIndex = 0;
		if(distance > 0){// rotate left
			splitIndex = distance;
			
		}else if(distance < 0){// rotate right
			splitIndex = this.targetBitsCount + distance;//distance negative
		}
		
		if(splitIndex>0 && splitIndex<this.targetBitsCount){
			BitSequence[] splitArr = this.split(splitIndex);
			BitSequence concatBs = splitArr[1].concat(splitArr[0]);
			
			this.bInt = concatBs.bInt;
			this.targetBitsCount = concatBs.targetBitsCount;
		}
	}

	
	/**
	 * 
	 * @param index startBit of second bit sequence 
	 * @return [first part as sequence, second part as sequence]
	 */
	public BitSequence[] split(int index){
		BitSequence firstBs = this.clone();// make copy, for changing reason
		BitSequence secondBs = this.clone();

		if(index<0){
			firstBs = new BitSequence(0, 0);
		}else if(index>targetBitsCount){
			secondBs = new BitSequence(0, 0);
		}else{//split
			int inBitLen = firstBs.getBitCount();
			firstBs.shiftRight(inBitLen - index);

			secondBs.setBitCount(inBitLen-index);
		}

		BitSequence[] resultArr = {firstBs, secondBs};
		return resultArr;
	}
	
	
	public BitSequence getSubSequence(int start, int length){
		BitSequence outBs = this.clone();

		if(start<0 || start>targetBitsCount-1){
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if(length < 0){
            throw new RuntimeException("length must be >= 0, " + length);
		}
		
		int endIndex = start+length-1;
		if(endIndex > targetBitsCount-1){
            throw new RuntimeException("endIndex out of range " + endIndex);
		}else if(endIndex == targetBitsCount-1){
			//pass
		}else{
			int distance = targetBitsCount - endIndex - 1;
			outBs.shiftRight(distance);
		}

		outBs.setBitCount(length);// cut
		
		return outBs;
	}

	/**
	 * Insert bit sequence bs at index in this sequence
	 */
	public void insert(int index, BitSequence bs){
		if(index<0 || index>targetBitsCount){
			throw new ArrayIndexOutOfBoundsException(index);
		}

		BitSequence[] spliArr = this.split(index);
		BitSequence outBs = spliArr[0].concat(bs).concat(spliArr[1]);
		this.setBitCount(outBs.getBitCount());
		this.bInt = outBs.bInt;
	}

	/**
	 * Opposite to insert.
	 * @return extracted sequence. 
	 * </br>This sequence changed to concat of remainders
	 */
	public BitSequence extractSequence(int fromIndex, int length){
		if(fromIndex<0 || fromIndex>targetBitsCount){
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if(length < 0){
            throw new RuntimeException("length must be >= 0, " + length);
		}
		
		int endIndex = fromIndex+length-1;
		if(endIndex > targetBitsCount-1){
            throw new RuntimeException("endIndex out of range " + endIndex);
		}

		BitSequence[] spliArr = this.split(fromIndex);
		BitSequence remainder1 = spliArr[0];
		BitSequence[] spliArr2 = spliArr[1].split(length);
		
		BitSequence extractedBs = spliArr2[0];

		BitSequence concatedRemainders = remainder1.concat(spliArr2[1]);
		this.setBitCount(concatedRemainders.getBitCount());
		this.bInt = concatedRemainders.bInt;
		
		return extractedBs;
	}

	public BitSequence concat(BitSequence secondBs){
		BitSequence leadingBs = this.clone(); 
		secondBs = secondBs.clone(); 
		
		if(leadingBs.targetBitsCount==0)	return secondBs;
		if(secondBs.targetBitsCount==0)		return leadingBs;

		byte[] startArr = leadingBs.toByteArray();
		byte[] endArr = secondBs.toByteArray();
		byte[] middleArr = new byte[1];

		int secondBitCount = secondBs.targetBitsCount;
		int bitCountOfLeadingByteSecondBs = secondBitCount % 8;
		
		if(bitCountOfLeadingByteSecondBs != 0){
			int countOfZeroBitsSecondBs = 8-bitCountOfLeadingByteSecondBs;
			BitSequence[] leadingBsSplit = leadingBs.split(leadingBs.targetBitsCount - countOfZeroBitsSecondBs);
			BitSequence[] secondBsSplit = secondBs.split(bitCountOfLeadingByteSecondBs);
		
			startArr = leadingBsSplit[0].toByteArray();
			endArr = secondBsSplit[1].toByteArray();
			
			// middle byte
			BitSequence startOfMiddleBs = leadingBsSplit[1];
			startOfMiddleBs.shiftLeft(bitCountOfLeadingByteSecondBs);
			byte[] bar1 = startOfMiddleBs.toByteArray();
			byte[] bar2 = secondBsSplit[0].toByteArray();
			byte middleByte = (byte) (bar1[0] | bar2[0]);
			middleArr[0] = middleByte;
		}
		
		int outBitsCount = leadingBs.getBitCount() + secondBs.getBitCount();
		int outBytesCount = outBitsCount / 8;
		int mod = outBitsCount % 8;
		if(mod > 0){
			++outBytesCount;
		}
		byte[] outArr = new byte[outBytesCount];
		
		int outPos = 0;
		// firstBs
		System.arraycopy(startArr, 0, outArr, outPos, startArr.length);
		outPos += startArr.length;
		// middle byte
		if(bitCountOfLeadingByteSecondBs != 0){
			System.arraycopy(middleArr, 0, outArr, outPos, 1);
			++outPos;
		}
		// secondBs
		System.arraycopy(endArr, 0, outArr, outPos, endArr.length);
		
		return new BitSequence(outArr, outBitsCount, ALIGN.RIGHT);
	}

	// left-most bit has index 0  
	public boolean getBitValue(int index){
		int biIndex = toBiIndex(index);
		boolean b = bInt.testBit(biIndex);
		return b;
	}

	private int toBiIndex(int indexFromLeft){
		int biIndex = this.targetBitsCount -1  - indexFromLeft;
		if(biIndex<0 || biIndex>targetBitsCount-1){
			throw new ArrayIndexOutOfBoundsException(indexFromLeft);
		}

		return biIndex;
	}
	
	
	//index of bit to set (left-most bit has index 0) 
	// value 0 or 1, (accept 0 or !0 for 1)
	public void setBitValue(int index, boolean value){
		int biIndex = toBiIndex(index);
		BigInteger bi = bInt.setBit(biIndex);
		if(value){
			bi = bInt.setBit(biIndex);
		}else{
			bi = bInt.clearBit(biIndex);
		}
		
		this.bInt = bi;
	}

	
	/**
	 * Bitwise and.
	 * Source sequences aligned right before and (smallest sequence extended with leading zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence and(BitSequence secondBs){
		BigInteger bi = this.bInt.and(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	/**
	 * Bitwise and.
	 * Source sequences aligned before and (smallest sequence extended with 
	 * leading - RIGHT-align or trailing - LEFT-align zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence and(BitSequence secondBs, ALIGN align){
		if(align == ALIGN.RIGHT){
			return this.and(secondBs);
		}
		//else left aligned
		BitSequence firstBs = this;
		int countDif = this.targetBitsCount - secondBs.targetBitsCount;
		if(countDif>0){
			secondBs = secondBs.concat(new BitSequence(0, countDif));
		}else if(countDif<0){
			firstBs = this.concat(new BitSequence(0, -countDif));
		}
		
		BigInteger bi = firstBs.bInt.and(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	/**
	 * Bitwise or.
	 * Source sequences aligned right before or (smallest sequence extended with leading zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence or(BitSequence secondBs){
		BigInteger bi = this.bInt.or(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	/**
	 * Bitwise or.
	 * Source sequences aligned before or (smallest sequence extended with 
	 * leading - RIGHT-align or trailing - LEFT-align zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence or(BitSequence secondBs, ALIGN align){
		if(align == ALIGN.RIGHT){
			return this.or(secondBs);
		}
		//else left aligned
		BitSequence firstBs = this;
		int countDif = this.targetBitsCount - secondBs.targetBitsCount;
		if(countDif>0){
			secondBs = secondBs.concat(new BitSequence(0, countDif));
		}else if(countDif<0){
			firstBs = this.concat(new BitSequence(0, -countDif));
		}
		
		BigInteger bi = firstBs.bInt.or(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	
	/**
	 * Bitwise xor.
	 * Source sequences aligned right before xor (smallest sequence extended with leading zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence xor(BitSequence secondBs){
		BigInteger bi = this.bInt.xor(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	/**
	 * Bitwise xor.
	 * Source sequences aligned before xor (smallest sequence extended with 
	 * leading - RIGHT-align or trailing - LEFT-align zeros) .
	 * Result bitSequence has bitCount = max(this.bitCount, secondBs.bitcount)
	 */
	public BitSequence xor(BitSequence secondBs, ALIGN align){
		if(align == ALIGN.RIGHT){
			return this.xor(secondBs);
		}
		//else left aligned
		BitSequence firstBs = this;
		int countDif = this.targetBitsCount - secondBs.targetBitsCount;
		if(countDif>0){
			secondBs = secondBs.concat(new BitSequence(0, countDif));
		}else if(countDif<0){
			firstBs = this.concat(new BitSequence(0, -countDif));
		}
		
		BigInteger bi = firstBs.bInt.xor(secondBs.bInt);
		int bitCount = Math.max(this.targetBitsCount, secondBs.targetBitsCount);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	/**
	 * Bitwise not, flip all bits.
	 */
	public BitSequence not(){
		int bytesCount = this.getTargetByteArrLength();
		byte[] onesArr = new byte[bytesCount];
		Arrays.fill(onesArr, (byte)0xff);
		int bitCount = this.targetBitsCount;
		
		BitSequence onesBs = new BitSequence(onesArr, bitCount, ALIGN.RIGHT);
		
		BigInteger bi = this.bInt.xor(onesBs.bInt);
		
		BitSequence resultBs = new BitSequence(bi.toByteArray(), bitCount, ALIGN.RIGHT);
		return resultBs;
	}

	
	
	@Override
	public String toString() {
		String valStr = bInt.toString();
		if(valStr.length() > 50){
			valStr = valStr.substring(0, 50-3)+"...";
		}
		return "BitSequence ["
				+"val="+valStr
				+", targetBitsCount=" + targetBitsCount
				+"]";
	}
	
	/**
	 * Return continuously binary string  
	 */
	public String toBynaryString() {
		StringBuilder builder = new StringBuilder();
		String suffix = bInt.toString(2);
		int leadingZerros = this.targetBitsCount - suffix.length();
		while (leadingZerros-- >0) {
			builder.append('0');
		}
		builder.append(suffix);
		return builder.toString();
	}
	
	/**
	 * 
	 * @param align start grouping on left or right
	 * @param groupType byte, half-byte or continuous
	 */
	public String toBynaryString(ALIGN align, GROUP groupType){
		if(groupType == GROUP.CONTINOUSLY){
    		return toBynaryString();
    	}else{
    		if(align == ALIGN.RIGHT){
        		return toGroupedRightAlignedString(groupType);
    		}else{
        		return toGroupedLeftAlignedString(groupType);
    		}
    	}
    }

	
	// with spaces between groups
	private String toGroupedRightAlignedString(GROUP groupType) {
		int groupSize = groupType.getSize();
		
		StringBuilder builder = new StringBuilder();
		
		String continuousStr = this.toBynaryString();
		int startIndex = 0;
		
		int leftGroupBitCount = this.targetBitsCount % groupSize;
		if(leftGroupBitCount>0){
			String firstSection = continuousStr.substring(0, leftGroupBitCount);
			builder.append(firstSection);
			startIndex = leftGroupBitCount;
		}
		
		int fullGroupsCount = this.targetBitsCount / groupSize;
		for(int i=0; i<fullGroupsCount; ++i){
			if(builder.length()>0){
				builder.append(' ');
			}
			
			int index = startIndex + i*groupSize;
			String byteStr = continuousStr.substring(index, index+groupSize);
			builder.append(byteStr);
		}
		
		return builder.toString();
	}
	
	// with spaces between groups
	private String toGroupedLeftAlignedString(GROUP groupType) {
		int groupSize = groupType.getSize();

		StringBuilder builder = new StringBuilder();
		
		String continuousStr = this.toBynaryString();
		int nextIndex = 0;
		
		int fullGroupsCount = this.targetBitsCount / groupSize;
		for(int i=0; i<fullGroupsCount; ++i){
			if(builder.length()>0){
				builder.append(' ');
			}
			
			int index = i*groupSize;
			nextIndex = index + groupSize ;
			String byteStr = continuousStr.substring(index, index+groupSize);
			builder.append(byteStr);
		}

		int rightGroupBitCount = this.targetBitsCount % groupSize;
		if(rightGroupBitCount>0){
			if(builder.length()>0){
				builder.append(' ');
			}
			String lastSection = continuousStr.substring(nextIndex, nextIndex+rightGroupBitCount);
			builder.append(lastSection);
		}

		return builder.toString();
	}
	
	/**
	 * Default iterator, boolean iterator.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Boolean> iterator() {
		BitIterator bitIterator = new BitIterator(this.toByteArray(), this.getBitCount());
		return bitIterator;
	}

	/**
	 * Integer iterator (0 or 1).
	 * @param direction left to right or right to left
	 */
	public Iterator<Integer> iterarorInt(DIRECTION direction){
		BitIteratorInt bitIterator = new BitIteratorInt(this.toByteArray(), this.getBitCount(), direction);
		return bitIterator;
		
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bInt == null) ? 0 : bInt.hashCode());
		result = prime * result + targetBitsCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitSequence other = (BitSequence) obj;
		if (bInt == null) {
			if (other.bInt != null)
				return false;
		} else if (!bInt.equals(other.bInt))
			return false;
		if (targetBitsCount != other.targetBitsCount)
			return false;
		return true;
	}




	@Override
	public int compareTo(BitSequence o) {
		int result = Integer.compare(this.targetBitsCount, o.getBitCount());
		if(result == 0){
			result = this.getAsBigInteger().compareTo(o.getAsBigInteger());
		}
		return result;
	}

	
}
