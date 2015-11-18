package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 6/30/2015.
 */
public interface ObjectFilter {

    public enum FilterType {
        Heavy,
        Light
    }

    public void doFilter(String methodName, Object... methodParameters);

    public FilterType getType();
}
