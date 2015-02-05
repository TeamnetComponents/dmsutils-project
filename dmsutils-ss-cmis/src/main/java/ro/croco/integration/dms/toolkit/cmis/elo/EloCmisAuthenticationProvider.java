package ro.croco.integration.dms.toolkit.cmis.elo;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.cmis.server.elo.commons.EloCmisContextParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 2/2/2015.
 */
public class EloCmisAuthenticationProvider extends org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider {

    @Override
    protected void addSessionParameterHeadersToFixedHeaders() {
        super.addSessionParameterHeadersToFixedHeaders();

        //add custom elo cmis server parameters to the standard ones
        Map<String, List<String>> fixedHeaders = this.getFixedHeaders();

        //set custom parameters as headers on the request (to be sent on the elo-cmis-server)
        BindingSession session = getSession();
        Collection<String> keys = session.getKeys();
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                if (key.startsWith(EloCmisContextParameter.ELO_CMIS_CONTEXT_PARAMETER_PREFIX)) {
                    List<String> values = new ArrayList<String>();
                    values.add(session.get(key).toString());
                    fixedHeaders.put(key, values);
                }
            }
        }
    }
}
