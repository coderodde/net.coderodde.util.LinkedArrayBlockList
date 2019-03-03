package net.coderodde.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the correctness of the 
 * {@link net.coderodde.util.LinkedArrayBlockList}.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 2, 2019)
 */
public class LinkedArrayBlockListTest {
    
    private LinkedArrayBlockList<Integer> targetList;
    
    public LinkedArrayBlockListTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        targetList = new LinkedArrayBlockList<>(5);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of size method, of class LinkedArrayBlockList.
     */
    @Test
    public void testSize() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i, targetList.size());
            targetList.add(i);
            assertEquals(i + 1, targetList.size());
        }
        
        // TODO: test with remove()!
    }

    /**
     * Test of isEmpty method, of class LinkedArrayBlockList.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(targetList.isEmpty());
        targetList.add(1);
        assertFalse(targetList.isEmpty());
        targetList.add(2);
        assertFalse(targetList.isEmpty());
        // TODO: Test with remove()!
    }

    /**
     * Test of contains method, of class LinkedArrayBlockList.
     */
    @Test
    public void testContains() {
        assertFalse(targetList.contains(""));
        assertFalse(targetList.contains(1));
        
        targetList.add(10);
        
        assertFalse(targetList.contains(9));
        assertTrue(targetList.contains(10));
        assertFalse(targetList.contains(11));
        
        targetList.add(11);
        
        assertTrue(targetList.contains(11));
    }

    /**
     * Test of iterator method, of class LinkedArrayBlockList.
     */
    @Test
    public void testIterator() {
        
    }

    /**
     * Test of toArray method, of class LinkedArrayBlockList.
     */
    @Test
    public void testToArray_0args() {
        targetList.add(1);
        targetList.add(2);
        targetList.add(3);
        
        Object[] returnedArray = targetList.toArray();
        Object[] expectedArray = new Object[] { 1, 2, 3 };
        
        assertTrue(Arrays.equals(returnedArray, 
                                 expectedArray));
    }

    /**
     * Test of toArray method, of class LinkedArrayBlockList.
     */
    @Test
    public void testToArray_GenericType() {
        targetList.add(1);
        targetList.add(2);
        targetList.add(3);
     
        Integer[] array = new Integer[2];
        Integer[] returnedArray = targetList.toArray(array);
        assertNotEquals(array, returnedArray);
        assertEquals(3, returnedArray.length);
        assertEquals((Integer) 1, returnedArray[0]);
        assertEquals((Integer) 2, returnedArray[1]);
        assertEquals((Integer) 3, returnedArray[2]);
        
        array = new Integer[3];
        returnedArray = targetList.toArray(array);
        assertTrue(Arrays.equals(array, returnedArray));
        
        array = new Integer[] { 1, 2, 3, 4, 5 };
        returnedArray = targetList.toArray(array);
        assertEquals(array, returnedArray);
        assertEquals((Integer) 1, returnedArray[0]);
        assertEquals((Integer) 2, returnedArray[1]);
        assertEquals((Integer) 3, returnedArray[2]);
        assertNull(returnedArray[3]);
        assertEquals((Integer) 5, returnedArray[4]);
    }

    /**
     * Test of add method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAdd_GenericType() {
        
    }

    /**
     * Test of remove method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemove_Object() {
        targetList.add(10);
        targetList.add(20);
        targetList.add(30);
        targetList.add(40);
        targetList.add(50);
        
        assertEquals(5, targetList.size());
        
        targetList.remove((Integer) 10);
        
        assertEquals(4, targetList.size());
        
        targetList.remove((Integer) 30);
        
        assertEquals(3, targetList.size());
        
        targetList.remove((Integer) 50);
        
        assertEquals(2, targetList.size());
        assertEquals((Integer) 20, targetList.get(0));
        assertEquals((Integer) 40, targetList.get(1));
    }

    /**
     * Test of containsAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testContainsAll() {
        
    }

    /**
     * Test of addAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAddAll_Collection() {
        for (int i = 0; i < 13; i++) {
            targetList.add(i);
        }
        
        Collection<Integer> collection = new ArrayList<>();
        
        assertFalse(targetList.addAll(collection));
        
        for (int i = 13; i < 21; i++) {
            collection.add(i);
        }
        
        boolean modified = targetList.addAll(collection);
        
        assertTrue(modified);
        
        for (int i = 0; i < 21; i++) {
            assertEquals((Integer) i, targetList.get(i));
        }
    }

    /**
     * Test of addAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAddAll_int_Collection() {
        
    }

    /**
     * Test of removeAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveAll() {
        
    }

    /**
     * Test of retainAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRetainAll() {
        
    }

    /**
     * Test of replaceAll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testReplaceAll() {
        
    }

    /**
     * Test of sort method, of class LinkedArrayBlockList.
     */
    @Test
    public void testSort() {
        
    }

    /**
     * Test of clear method, of class LinkedArrayBlockList.
     */
    @Test
    public void testClear() {
        
    }

    /**
     * Test of get method, of class LinkedArrayBlockList.
     */
    @Test
    public void testGet() {
        
    }

    /**
     * Test of set method, of class LinkedArrayBlockList.
     */
    @Test
    public void testSet() {
        
    }

    /**
     * Test of add method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAdd_int_GenericType() {
        
    }

    /**
     * Test of remove method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemove_int() {
        
    }

    /**
     * Test of indexOf method, of class LinkedArrayBlockList.
     */
    @Test
    public void testIndexOf() {
        for (int i = 0; i < 10; i++) {
            targetList.add(i);
        }
        
        for (int i = 9; i >= 0; i--) {
            assertEquals(i, targetList.indexOf(i));
        }
        
        targetList.add(0);
        assertEquals(0, targetList.indexOf(0));
    }

    /**
     * Test of lastIndexOf method, of class LinkedArrayBlockList.
     */
    @Test
    public void testLastIndexOf() {
        targetList.add(1);
        
        assertEquals(0, targetList.lastIndexOf(1));
        
        targetList.add(2);
        targetList.add(1);
        
        assertEquals(2, targetList.lastIndexOf(1));
    }

    /**
     * Test of listIterator method, of class LinkedArrayBlockList.
     */
    @Test
    public void testListIterator_0args() {
        
    }

    /**
     * Test of listIterator method, of class LinkedArrayBlockList.
     */
    @Test
    public void testListIterator_int() {
        
    }

    /**
     * Test of subList method, of class LinkedArrayBlockList.
     */
    @Test
    public void testSubList() {
        
    }

    /**
     * Test of spliterator method, of class LinkedArrayBlockList.
     */
    @Test
    public void testSpliterator() {
        
    }

    /**
     * Test of removeIf method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveIf() {
    }

    /**
     * Test of stream method, of class LinkedArrayBlockList.
     */
    @Test
    public void testStream() {

    }

    /**
     * Test of parallelStream method, of class LinkedArrayBlockList.
     */
    @Test
    public void testParallelStream() {
        
    }

    /**
     * Test of forEach method, of class LinkedArrayBlockList.
     */
    @Test
    public void testForEach() {
        
    }

    /**
     * Test of addFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAddFirst() {
        
    }

    /**
     * Test of addLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testAddLast() {
        
    }

    /**
     * Test of offerFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testOfferFirst() {
        
    }

    /**
     * Test of offerLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testOfferLast() {
        
    }

    /**
     * Test of removeFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveFirst() {
        
    }

    /**
     * Test of removeLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveLast() {
        
    }

    /**
     * Test of pollFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPollFirst() {
        
    }

    /**
     * Test of pollLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPollLast() {
        
    }

    /**
     * Test of getFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testGetFirst() {
        
    }

    /**
     * Test of getLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testGetLast() {
        
    }

    /**
     * Test of peekFirst method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPeekFirst() {
        
    }

    /**
     * Test of peekLast method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPeekLast() {
        
    }

    /**
     * Test of removeFirstOccurrence method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveFirstOccurrence() {
        
    }

    /**
     * Test of removeLastOccurrence method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemoveLastOccurrence() {
        
    }

    /**
     * Test of offer method, of class LinkedArrayBlockList.
     */
    @Test
    public void testOffer() {
        
    }

    /**
     * Test of remove method, of class LinkedArrayBlockList.
     */
    @Test
    public void testRemove_0args() {
        
    }

    /**
     * Test of poll method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPoll() {
        
    }

    /**
     * Test of element method, of class LinkedArrayBlockList.
     */
    @Test
    public void testElement() {
        
    }

    /**
     * Test of peek method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPeek() {
        
    }

    /**
     * Test of push method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPush() {
        
    }

    /**
     * Test of pop method, of class LinkedArrayBlockList.
     */
    @Test
    public void testPop() {
        
    }

    /**
     * Test of descendingIterator method, of class LinkedArrayBlockList.
     */
    @Test
    public void testDescendingIterator() {
        
    }

    /**
     * Test of main method, of class LinkedArrayBlockList.
     */
    @Test
    public void testMain() {
        
    }
}
