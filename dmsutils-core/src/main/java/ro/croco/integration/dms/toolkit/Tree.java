package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 11/30/2014.
 */

import java.util.List;

/**
 * Basic tree structure.
 */
public interface Tree<T> {

    /**
     * Returns the item on this level.
     */
    T getItem();

    /**
     * Returns the children.
     */
    List<Tree<T>> getChildren();
}
