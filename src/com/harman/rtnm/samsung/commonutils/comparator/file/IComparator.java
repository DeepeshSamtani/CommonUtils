package com.harman.rtnm.samsung.commonutils.comparator.file;

/**
 * The comparator interface to be implemented by the <b>Different</b> comparators
 * DB basedcomparison, File based comparison
 * @author 608952343(AP)
 *
 */
public interface IComparator {
	
	int NEW = 0;
	int MOD = 2;
	int DEL = 1;
	int IGNORE = -1;
	
	/**
	 * <li>return  0, if its a new record 
	 * <li>return  1, if the records has to be deleted from the inventory
	 * <li>return  2, if the existing records has beeen modified
	 * <li>return -1, if existing but no change in the record
	 * @param src
	 * @param dest
	 * @return
	 */
	int compare(Object src, Object dest);
}
