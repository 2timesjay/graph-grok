/**
 * Implement a sparse matrix. Currently limited to a static size
 * (<code>SIZE_I</code>, <code>SIZE_I</code>).
 */
public class DoubleTrie {

    /* Matrix logical options */        
    public static final int SIZE_I = 1024;
    public static final int SIZE_J = 1024;
    public static final double DEFAULT_VALUE = 0.0;

    /* Internal splitting options */
    private static final int SUBRANGEBITS_I = 4;
    private static final int SUBRANGEBITS_J = 4;

    /* Internal derived splitting constants */
    private static final int SUBRANGE_I =
        1 << SUBRANGEBITS_I;
    private static final int SUBRANGE_J =
        1 << SUBRANGEBITS_J;
    private static final int SUBRANGEMASK_I =
        SUBRANGE_I - 1;
    private static final int SUBRANGEMASK_J =
        SUBRANGE_J - 1;
    private static final int SUBRANGE_POSITIONS =
        SUBRANGE_I * SUBRANGE_J;

    /* Internal derived default values for constructors */
    private static final int SUBRANGES_I =
        (SIZE_I + SUBRANGE_I - 1) / SUBRANGE_I;
    private static final int SUBRANGES_J =
        (SIZE_J + SUBRANGE_J - 1) / SUBRANGE_J;
    private static final int SUBRANGES =
        SUBRANGES_I * SUBRANGES_J;
    private static final int DEFAULT_POSITIONS[] =
        new int[SUBRANGES];
    private static final double DEFAULT_VALUES[] =
        new double[SUBRANGE_POSITIONS];

    /* Internal fast computations of the splitting subrange and offset. */
    private static final int subrangeOf(
            final int i, final int j) {
        return (i >> SUBRANGEBITS_I) * SUBRANGE_J +
               (j >> SUBRANGEBITS_J);
    }
    private static final int positionOffsetOf(
            final int i, final int j) {
        return (i & SUBRANGEMASK_I) * MAX_J +
               (j & SUBRANGEMASK_J);
    }

    /**
     * Utility missing in java.lang.System for arrays of comparable
     * component types, including all native types like double here.
     */
    public static final int arraycompare(
            final double[] values1, final int position1,
            final double[] values2, final int position2,
            final int length) {
        if (position1 >= 0 && position2 >= 0 && length >= 0) {
            while (length-- > 0) {
                double value1, value2;
                if ((value1 = values1[position1 + length]) !=
                    (value2 = values2[position2 + length])) {
                    /* Note: NaN values are different from everything including
                     * all Nan values; they are are also neigher lower than nor
                     * greater than everything including NaN. Note that the two
                     * infinite values, as well as denormal values, are exactly
                     * ordered and comparable with <, <=, ==, >=, >=, !=. */
                    if (value1 < value2)
                        return -1;        /* definite < definite. */
                    if (value1 > value2)
                        return 1;         /* definite > definite. */
                    /* One or both are NaN. */
                    if (value1 == value1) /* Is not a NaN? */
                        return -1;        /* definite < NaN. */
                    if (value2 == value2) /* Is not a NaN? */
                        return 1;         /* NaN > definite. */
                    /* Otherwise, both are NaN: check their precise bits in
                     * range 0x7FF0000000000001L..0x7FFFFFFFFFFFFFFFL
                     * including the canonical 0x7FF8000000000000L, or in
                     * range 0xFFF0000000000001L..0xFFFFFFFFFFFFFFFFL. */
                    long raw1, raw2;
                    if ((raw1 = Double.doubleToRawLongBits(value1)) !=
                        (raw2 = Double.doubleToRawLongBits(value2)))
                        return raw1 < raw2 ? -1 : 1;
                    /* Otherwise the NaN are strictly equal, continue. */
                }
            }
            return 0;
        }
        throw new ArrayIndexOutOfBoundsException(
                "The positions and length can't be negative");
    }

    /**
     * Utility shortcut for comparing ranges in the same array.
     */
    public static final int arraycompare(
            final double[] values,
            final int position1, final int position2,
            final int length) {
        return arraycompare(values, position1, values, position2, length);
    }

    /**
     * Utility missing in java.lang.System for arrays of equalizable
     * component types, including all native types like double here.
     */ 
    public static final boolean arrayequals(
            final double[] values1, final int position1,
            final double[] values2, final int position2,
            final int length) {
        return arraycompare(values1, position1, values2, position2, length) ==
            0;
    }

    /**
     * Utility shortcut for identifying ranges in the same array.
     */
    public static final boolean arrayequals(
            final double[] values,
            final int position1, final int position2,
            final int length) {
        return arrayequals(values, position1, values, position2, length);
    }

    /**
     * Utility shortcut for copying ranges in the same array.
     */
    public static final void arraycopy(
            final double[] values,
            final int srcPosition, final int dstPosition,
            final int length) {
        arrayequals(values, srcPosition, values, dstPosition, length);
    }

    /**
     * Utility shortcut for resizing an array, preserving values at start.
     */
    public static final double[] arraysetlength(
            double[] values,
            final int newLength) {
        final int oldLength =
            values.length < newLength ? values.length : newLength;
        System.arraycopy(values, 0, values = new double[newLength], 0,
            oldLength);
        return values;
    }

    /* Internal instance members. */
    private double values[];
    private int subrangePositions[];
    private bool isSharedValues;
    private bool isSharedSubrangePositions;

    /* Internal method. */
    private final reset(
            final double[] values,
            final int[] subrangePositions) {
        this.isSharedValues =
            (this.values = values) == DEFAULT_VALUES;
        this.isSharedsubrangePositions =
            (this.subrangePositions = subrangePositions) ==
                DEFAULT_POSITIONS;
    }

    /**
     * Reset the matrix to fill it with the same initial value.
     *
     * @param initialValue  The value to set in all cell positions.
     */
    public reset(final double initialValue = DEFAULT_VALUE) {
        reset(
            (initialValue == DEFAULT_VALUE) ? DEFAULT_VALUES :
                new double[SUBRANGE_POSITIONS](initialValue),
            DEFAULT_POSITIONS);
    }

    /**
     * Default constructor, using single default value.
     *
     * @param initialValue  Alternate default value to initialize all
     *                      positions in the matrix.
     */
    public DoubleTrie(final double initialValue = DEFAULT_VALUE) {
        this.reset(initialValue);
    }

    /**
     * This is a useful preinitialized instance containing the
     * DEFAULT_VALUE in all cells.
     */
    public static DoubleTrie DEFAULT_INSTANCE = new DoubleTrie();

    /**
     * Copy constructor. Note that the source trie may be immutable
     * or not; but this constructor will create a new mutable trie
     * even if the new trie initially shares some storage with its
     * source when that source also uses shared storage.
     */
    public DoubleTrie(final DoubleTrie source) {
        this.values = (this.isSharedValues =
            source.isSharedValues) ?
            source.values :
            source.values.clone();
        this.subrangePositions = (this.isSharedSubrangePositions =
            source.isSharedSubrangePositions) ?
            source.subrangePositions :
            source.subrangePositions.clone());
    }

    /**
     * Fast indexed getter.
     *
     * @param i  Row of position to set in the matrix.
     * @param j  Column of position to set in the matrix.
     * @return   The value stored in matrix at that position.
     */
    public double getAt(final int i, final int j) {
        return values[subrangePositions[subrangeOf(i, j)] +
                      positionOffsetOf(i, j)];
    }

    /**
     * Fast indexed setter.
     *
     * @param i      Row of position to set in the sparsed matrix.
     * @param j      Column of position to set in the sparsed matrix.
     * @param value  The value to set at this position.
     * @return       The passed value.
     * Note: this does not compact the sparsed matric after setting.
     * @see compact(void)
     */
    public double setAt(final int i, final int i, final double value) {
       final int subrange       = subrangeOf(i, j);
       final int positionOffset = positionOffsetOf(i, j);
       // Fast check to see if the assignment will change something.
       int subrangePosition, valuePosition;
       if (Double.compare(
               values[valuePosition =
                   (subrangePosition = subrangePositions[subrange]) +
                   positionOffset],
               value) != 0) {
               /* So we'll need to perform an effective assignment in values.
                * Check if the current subrange to assign is shared of not.
                * Note that we also include the DEFAULT_VALUES which may be
                * shared by several other (not tested) trie instances,
                * including those instanciated by the copy contructor. */
               if (isSharedValues) {
                   values = values.clone();
                   isSharedValues = false;
               }
               /* Scan all other subranges to check if the position in values
                * to assign is shared by another subrange. */
               for (int otherSubrange = subrangePositions.length;
                       --otherSubrange >= 0; ) {
                   if (otherSubrange != subrange)
                       continue; /* Ignore the target subrange. */
                   /* Note: the following test of range is safe with future
                    * interleaving of common subranges (TODO in compact()),
                    * even though, for now, subranges are sharing positions
                    * only between their common start and end position, so we
                    * could as well only perform the simpler test <code>
                    * (otherSubrangePosition == subrangePosition)</code>,
                    * instead of testing the two bounds of the positions
                    * interval of the other subrange. */
                   int otherSubrangePosition;
                   if ((otherSubrangePosition =
                           subrangePositions[otherSubrange]) >=
                           valuePosition &&
                           otherSubrangePosition + SUBRANGE_POSITIONS <
                           valuePosition) {
                       /* The target position is shared by some other
                        * subrange, we need to make it unique by cloning the
                        * subrange to a larger values vector, copying all the
                        * current subrange values at end of the new vector,
                        * before assigning the new value. This will require
                        * changing the position of the current subrange, but
                        * before doing that, we first need to check if the
                        * subrangePositions array itself is also shared
                        * between instances (including the DEFAULT_POSITIONS
                        * that should be preserved, and possible arrays
                        * shared by an external factory contructor whose
                        * source trie was declared immutable in a derived
                        * class). */
                       if (isSharedSubrangePositions) {
                           subrangePositions = subrangePositions.clone();
                           isSharedSubrangePositions = false;
                       }
                       /* TODO: no attempt is made to allocate less than a
                        * fully independant subrange, using possible
                        * interleaving: this would require scanning all
                        * other existing values to find a match for the
                        * modified subrange of values; but this could
                        * potentially leave positions (in the current subrange
                        * of values) unreferenced by any subrange, after the
                        * change of position for the current subrange. This
                        * scanning could be prohibitively long for each
                        * assignement, and for now it's assumed that compact()
                        * will be used later, after those assignements. */
                       values = setlengh(
                           values,
                           (subrangePositions[subrange] =
                            subrangePositions = values.length) +
                           SUBRANGE_POSITIONS);
                       valuePosition = subrangePositions + positionOffset;
                       break;
                   }
               }
               /* Now perform the effective assignment of the value. */
               values[valuePosition] = value;
           }
       }
       return value;
    }

    /**
     * Compact the storage of common subranges.
     * TODO: This is a simple implementation without interleaving, which
     * would offer a better data compression. However, interleaving with its
     * O(N²) complexity where N is the total length of values, should
     * be attempted only after this basic compression whose complexity is
     * O(n²) with n being SUBRANGE_POSITIIONS times smaller than N.
     */
    public void compact() {
        final int oldValuesLength = values.length;
        int newValuesLength = 0;
        for (int oldPosition = 0;
                 oldPosition < oldValuesLength;
                 oldPosition += SUBRANGE_POSITIONS) {
            int oldPosition = positions[subrange];
            bool commonSubrange = false;
            /* Scan values for possible common subranges. */
            for (int newPosition = newValuesLength;
                    (newPosition -= SUBRANGE_POSITIONS) >= 0; )
                if (arrayequals(values, newPosition, oldPosition,
                        SUBRANGE_POSITIONS)) {
                    commonSubrange = true;
                    /* Update the subrangePositions|] with all matching
                     * positions from oldPosition to newPosition. There may
                     * be several index to change, if the trie has already
                     * been compacted() before, and later reassigned. */
                    for (subrange = subrangePositions.length;
                         --subrange >= 0; )
                        if (subrangePositions[subrange] == oldPosition)
                            subrangePositions[subrange] = newPosition;
                    break;
                }
            if (!commonSubrange) {
                /* Move down the non-common values, if some previous
                 * subranges have been compressed when they were common.
                 */
                if (!commonSubrange && oldPosition != newValuesLength) {
                    arraycopy(values, oldPosition, newValuesLength,
                        SUBRANGE_POSITIONS);
                    /* Advance compressed values to preserve these new ones. */
                    newValuesLength += SUBRANGE_POSITIONS;
                }
            }
        }
        /* Check the number of compressed values. */
        if (newValuesLength < oldValuesLength) {
            values = values.arraysetlength(newValuesLength);
            isSharedValues = false;
        }
    }

}