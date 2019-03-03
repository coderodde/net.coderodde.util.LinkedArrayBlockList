package net.coderodde.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 *
 * @author Rodion "rodde" Efremov
 */
public final class LinkedArrayBlockList<E>
        implements List<E>, Deque<E>, Queue<E>, Cloneable, Serializable {

    private static final long serialVersionUID = 6372710952572697241L;
    
    /**
     * The format for the exception message whenever the constructor receives a
     * negative value.
     */
    private static final String NEGATIVE_BLOCK_CAPACITY_EXCEPTION_FORMAT = 
            "Block capacity is negative (%d).";
    
    /**
     * The format for the exception message whenever the constructor receives a
     * value between zero and <tt>capacity - 1</tt>.
     */
    private static final String BLOCK_CAPACITY_TOO_SMALL_EXCEPTION_FORMAT = 
            "Block capacity is too small (%d). Must be at least %d.";
    
    /**
     * The default initial capacity.
     */
    private static final int DEFAULT_BLOCK_CAPACITY = 100;
    
    /**
     * The minimum allowed capacity.
     */
    private static final int MINIMUM_BLOCK_CAPACITY = 4;
    
    /**
     * This static inner class defines the data type for representing the
     * blocks.
     * 
     * @param <E> the array component type.
     */
    static final class Block<E> {
        
        /**
         * The capacity of {@code array}.
         */
        final int capacity;
        
        /**
         * The number of elements stored in this block.
         */
        int size;
        
        /**
         * The index to the first logical array component.
         */
        int headIndex;
        
        /**
         * The actual storage array.
         */
        E[] array;
        
        /**
         * The predecessor block or is set to {@code null} if this block is at
         * the head of the list.
         */
        Block<E> previousBlock;
        
        /**
         * The successor block or is set to {@code null} if this block is at the
         * tail of the list.
         */
        Block<E> nextBlock;
        
        Block(int capacity) {
            this.capacity = capacity;
            this.array = (E[]) new Object[capacity];
        }
        
        boolean isFull() {
            return size == capacity;
        }
        
        boolean isEmpty() {
            return size == 0;
        }
        
        E get(int logicalIndex) {
            return array[logicalIndexToPhysical(logicalIndex)];
        }
        
        void set(int logicalIndex, E element) {
            array[logicalIndexToPhysical(logicalIndex)] = element;
        }
        
        /**
         * Shifts array components <tt>array[startIndex], array[startIndex + 1],
         * ..., array[startIndex + shiftLength - 1]</tt> {@code shiftLeft}
         * array components to the left. If necessary, this method will wrap 
         * over the beginning of the array and shift from the right end of the
         * array.
         * 
         * @param startIndex    the physical index of the leftmost element of
         *                      the portion to shift.
         * @param portionLength the length of the portion to shift.
         * @param shiftLength   the number of elements to shift.
         */
        void shiftLeft(int startIndex, int portionLength, int shiftLength) {
            int sourceIndex = startIndex;
            int targetIndex = mod(startIndex - portionLength, capacity);
            
            for (int i = 0; i < portionLength; i++) {
                array[targetIndex] = array[sourceIndex];
                targetIndex = mod(targetIndex - 1, capacity);
                sourceIndex = mod(sourceIndex - 1, capacity);
            }
        }
        
        /**
         * Shifts array components <tt>array[startIndex], array[startIndex + 1],
         * ..., array[startIndex + portionLength - 1]</tt> {@code shiftLeft}
         * array components to the <b>right</b>. If necessary, this method will 
         * wrap over the end of the array and shift from the left end.
         * 
         * @param startIndex    the physical index of the leftmost element of 
         *                      the portion to shift.
         * @param portionLength the length of the portion to shift.
         * @param shiftLength   the number of elements to shift.
         */
        void shiftRight(int startIndex, int portionLength, int shiftLength) {
            int sourceIndex = mod(startIndex + portionLength - 1, capacity);
            int targetIndex = mod(sourceIndex + shiftLength, capacity);
            
            for (int i = 0; i < portionLength; i++) {
                array[targetIndex] = array[sourceIndex];
                targetIndex = mod(targetIndex - 1, capacity);
                sourceIndex = mod(sourceIndex - 1, capacity);
            }
        }
        
        void shiftLeftSingleElement(int startIndex) {
            shiftLeft(startIndex, 1, 1);
        }
        
        void shiftRightSingleElement(int startIndex) {
            shiftRight(startIndex, 1, 1);
        }
        
        private int logicalIndexToPhysical(int logicalIndex) {
            return (headIndex + logicalIndex) % capacity;
        }
        
        private void remove(int logicalIndex) {
            int elementsOnLeft = logicalIndex;
            int elementsOnRight = size - logicalIndex - 1;
            
            if (elementsOnLeft < elementsOnRight) {
                shiftRight(headIndex, elementsOnLeft, 1);
                set(0, null); // Let the GC do its job.
                incrementHeadIndex();
                size--;
            } else {
                shiftLeft(headIndex, elementsOnRight, 1);
                set(--size, null); // Let the GC do its job.
            }
        }
        
        private void incrementHeadIndex() {
            headIndex = (headIndex + 1) % capacity;
        }
    }
    
    /**
     * The number or elements in this list.
     * 
     * @serial 
     */
    private int size;
    
    /**
     * The number of blocks in this list.
     */
    private int blocks;
    
    /**
     * The capacity of each block.
     */
    private final int blockCapacity;
    
    /**
     * Used to count the number of modifications of this list. A modification is
     * any of the following operations:
     * <ul>
     * <li>{@code }</li>
     * <li>{@code }</li>
     * <li>{@code }</li>
     * </ul>
     */
    private transient int modificationCount = 0;
    
    /**
     * The first block of the chain.
     */
    private Block<E> headBlock;
    
    /**
     * The last block of the chain.
     */
    private Block<E> tailBlock;
    
    public LinkedArrayBlockList(int blockCapacity) {
        this.blockCapacity = checkBlockCapacity(blockCapacity);
        tailBlock = headBlock = new Block<>(blockCapacity);
    }
    
    public LinkedArrayBlockList() {
        this(DEFAULT_BLOCK_CAPACITY);
    }
    
    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list. 
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     * 
     * @return <tt>true</tt> if this list contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        if (headBlock == null) {
            return false; // Empty list.
        }
        
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int index = 0;
        
        for (Block<E> block = headBlock; 
                block != null; 
                block = block.nextBlock) {
            for (int i = 0; i < block.size; i++, index++) {
                array[index] = block.get(i);
            }
        }
        
        return array;
    }

    @Override
    public <E> E[] toArray(E[] a) {
        E[] targetArray = 
                a.length < size ? 
                (E[]) Array.newInstance(a.getClass()
                                         .getComponentType(), size) :
                a;
        
        int index = 0;

        for (Block<E> block = (Block<E>) headBlock; 
                block != null;
                block = block.nextBlock) {
            for (int i = 0; i < block.size; i++, index++) {
                targetArray[index] = block.get(i);
            }
        }
        
        if (targetArray.length > size) {
            targetArray[size] = null;
        }
        
        return targetArray;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(E e) {
        if (headBlock == null) {
            headBlock = new Block<>(blockCapacity);
            tailBlock = headBlock;
            blocks = 1;
        }
        
        if (tailBlock.isFull()) {
            Block<E> newBlock = new Block<>(blockCapacity);
            newBlock.array[0] = e;
            newBlock.size = 1;
            tailBlock.nextBlock = newBlock;
            newBlock.previousBlock = tailBlock;
            tailBlock = newBlock;
            blocks++;
        } else {
            tailBlock.set(tailBlock.size, e);
            tailBlock.size++;
        }
        
        modificationCount++;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (Block<E> block = headBlock;
                block != null;
                block = block.nextBlock) {
            for (int i = 0; i < block.size; i++) {
                if (Objects.equals(block.get(i), o)) {
                    block.remove(i);

                    if (block.isEmpty()) {
                        blocks--;
                        unlinkBlock(block);
                    }

                    modificationCount++;
                    size--;
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (indexOf(o) < 0) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        
        if (headBlock == null) {
            headBlock = new Block<>(blockCapacity);
            tailBlock = headBlock;
            blocks = 1;
        }
        
        Block<E> lastBlock = tailBlock;
        
        for (E element : c) {
            if (lastBlock.isFull()) {
                Block<E> newTailBlock = new Block<>(blockCapacity);
                lastBlock.nextBlock = newTailBlock;
                newTailBlock.previousBlock = lastBlock;
                blocks++;
                lastBlock = newTailBlock;
            }
            
            lastBlock.set(lastBlock.size++, element);
        }
        
        tailBlock = lastBlock;
        modificationCount++;
        size += c.size();
        return true;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        List.super.replaceAll(operator); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sort(Comparator<? super E> c) {
        List.super.sort(c); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        blocks = 0;
        size = 0;
        modificationCount++;
        headBlock = null;
        tailBlock = null;
    }

    @Override
    public E get(int index) {
        checkAccessIndex(index);
        int elementsOnLeft = index;
        int elementsOnRight = size - elementsOnLeft;
        
        if (elementsOnLeft < elementsOnRight) {
            return getFromBeginning(index);
        } else {
            return getFromEnding(index);
        }
    }
    
    private E getFromBeginning(int index) {
        Block<E> block = headBlock;
        
        while (true) {
            if (index >= block.size) {
                index -= block.size;
                block = block.nextBlock;
            } else {
                return block.get(index);
            }
        }
    }
    
    private E getFromEnding(int index) {
        Block<E> block = tailBlock;
        
        while (true) {
            if (index < block.size) {
                return block.get(index);
            } else {
                index -= block.size;
                block = block.previousBlock;
            }
        }
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        
        if (o == null) {
            for (Block<E> block = headBlock; 
                    block != null; 
                    block = block.nextBlock) {
                for (int i = 0; i < block.size; i++, index++) {
                    if (block.get(i) == null) {
                        return index;
                    }
                }
            }
        } else {
            for (Block<E> block = headBlock;
                    block != null;
                    block = block.nextBlock) {
                for (int i = 0; i < block.size; i++, index++) {
                    if (o.equals(block.get(i))) {
                        return index;
                    }
                }
            }
        }
        
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = size - 1;
        
        if (o == null) {
            for (Block<E> block = tailBlock; 
                    block != block; 
                    block = block.previousBlock) {
                for (int i = block.size - 1; i >= 0; i--, index--) {
                    if (block.get(i) == null) {
                        return index;
                    }
                }
            }
        } else {
            for (Block<E> block = tailBlock; 
                    block != null; 
                    block = block.previousBlock) {
                for (int i = block.size - 1; i >= 0; i--, index--) {
                    if (o.equals(block.get(i))) {
                        return index;
                    }
                }
            }
        }
        
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Spliterator<E> spliterator() {
        return List.super.spliterator(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return List.super.removeIf(filter); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<E> stream() {
        return List.super.stream(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Stream<E> parallelStream() {
        return List.super.parallelStream(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        List.super.forEach(action); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offerLast(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E getFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E getLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekFirst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peekLast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void push(E e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E pop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private int checkBlockCapacity(int blockCapacity) {
        if (blockCapacity < 0) {
            String exceptionMessage =
                    String.format(
                            NEGATIVE_BLOCK_CAPACITY_EXCEPTION_FORMAT, 
                            blockCapacity);
            
            throw new IllegalArgumentException(exceptionMessage);
        } else if (blockCapacity < MINIMUM_BLOCK_CAPACITY) {
            String exceptionMessage = 
                    String.format(
                            BLOCK_CAPACITY_TOO_SMALL_EXCEPTION_FORMAT,
                            blockCapacity,
                            DEFAULT_BLOCK_CAPACITY);
            throw new IllegalArgumentException(exceptionMessage);
        }
        
        return blockCapacity;
    }
    
    /**
     * Computes the modulus of any integer. For example, <tt>0 mod 3 = 0,
     * -1 mod 3 = 2, -2 mod 3 = 1, -3 mod 3 = 0,</tt> and so on.
     *
     * @param x the target integer.
     * @param n the remainder integer.
     * @return the modulus of {@code x} with remainder {@code n}.
     * @see https://dev.to/maurobringolf/a-neat-trick-to-compute-modulo-of-negative-numbers-111e
     */
    private static int mod(int x, int n) {
        return (n + x % n) % n;
    }
    
    public static void main(String[] args) {
        for (int i = 5; i >= -5; i--) {
            System.out.println("i = " + i + ": " + mod(i, 3));
        }
        
        System.out.println(new Random().nextLong());
    }
    
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "The access index (" + index + ") must not be negative.");
        }
        
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "The access index (" + index + ") is too large. Must be " +
                    " at most " + size + ".");
        }
    }
    
    /**
     * Unlinks the input block from the linked list of blocks.
     * 
     * @param block the block to unlink.
     */
    private void unlinkBlock(Block<E> block) {
        if (block.previousBlock == null) {
            if (block.nextBlock == null) {
                headBlock = null;
                tailBlock = null;
            } else {
                // Here, previousBlock == null && nextBlock != null.
                headBlock = block.nextBlock;
                headBlock.previousBlock = null;
            }
        } else if (block.nextBlock == null) {
            // Here, previousBlock != null && nextBlock == null.
            tailBlock = block.previousBlock;
            tailBlock.nextBlock = null;
        } else {
            // Here, prevoiusBlock != null && nextBlock != null.
            block.previousBlock.nextBlock = block.nextBlock;
            block.nextBlock.previousBlock = block.previousBlock;
        }
    }
}
