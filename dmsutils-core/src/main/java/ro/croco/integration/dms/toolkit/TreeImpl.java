package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 11/30/2014.
 */
import java.util.List;

public class TreeImpl<T> implements Tree<T> {

    private final T item;
    private final List<Tree<T>> children;

    public TreeImpl(T item, List<Tree<T>> children) {
        if (item == null) {
            throw new IllegalArgumentException("Item must be set!");
        }
        this.item = item;
        this.children = children;
    }

    public T getItem() {
        return item;
    }

    public List<Tree<T>> getChildren() {
        return this.children;
    }
}
