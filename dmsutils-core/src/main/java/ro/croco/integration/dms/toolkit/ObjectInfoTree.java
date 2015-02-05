package ro.croco.integration.dms.toolkit;

//import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucian.Dragomir on 8/20/2014.
 */
public class ObjectInfoTree extends RequestIdentifier {
    private Tree<ObjectInfo> content;

    public ObjectInfoTree() {
        this.content = null;
    }

    public Tree<ObjectInfo> getContent() {
        return content;
    }

    public void setContent(Tree<ObjectInfo> content) {
        this.content = content;
    }

    public void print() {
        System.out.println("--------------------------------------------------------------------------------------------");
        print(this.content, 0);
        System.out.println("--------------------------------------------------------------------------------------------");
    }

    private void print(Tree<ObjectInfo> tree, int depth) {
        if (tree == null) {
            return;
        }
        String spaces = "" + StringUtils.repeat(" ", depth * 3);
        ObjectInfo item = tree.getItem();
        String A = "", B = "", itemName = "null";
        if (item == null) {
            A = "<";
            B = ">";
        } else {
            itemName = item.getName();
            boolean isFolder = (item instanceof FolderInfo);
            if (isFolder) {
                A = "[";
                B = "]";
            }
        }
        System.out.println(spaces + A + itemName + B);
        if (tree.getChildren() != null) {
            for (Tree<ObjectInfo> child : tree.getChildren()) {
                print(child, depth + 1);
            }
        }
    }

    private List<ObjectInfo> listContent(Tree<ObjectInfo> tree) {
        List<ObjectInfo> result = new ArrayList<ObjectInfo>();
        if (tree == null) {
            return result;
        }

        ObjectInfo item = tree.getItem();
        if (item == null) {
        } else {
            result.add(item);
        }
        if (tree.getChildren() != null) {
            for (Tree<ObjectInfo> child : tree.getChildren()) {
                List<ObjectInfo> childResult = listContent(child);
                result.addAll(childResult);
            }
        }
        return result;
    }

    public List<ObjectInfo> listContent() {
        return this.listContent(this.getContent());
    }

}
