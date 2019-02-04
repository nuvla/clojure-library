package sixsq.slipstream.client;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import sixsq.slipstream.client.api.cimi.cimi;
import sixsq.slipstream.client.api.authn.authn;

public class CIMI {

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("sixsq.slipstream.client.api.cimi"));
        require.invoke(Clojure.read("sixsq.slipstream.client.api.authn"));
        require.invoke(Clojure.read("sixsq.slipstream.client.sync"));
        require.invoke(Clojure.read("sixsq.slipstream.client.impl.utils.java"));
        require.invoke(Clojure.read("taoensso.timbre"));
    }

    private static final IFn cimiClientConstructor = Clojure.var("sixsq.slipstream.client.sync", "instance");

    private static final IFn toClojure = Clojure.var("sixsq.slipstream.client.impl.utils.java", "to-clojure");
    private static final IFn toJava = Clojure.var("sixsq.slipstream.client.impl.utils.java", "to-java");

    private static final IFn keyword = Clojure.var("clojure.core", "keyword");
    private static final IFn setLoggingLevel = Clojure.var("taoensso.timbre", "set-level!");

    static {
        setLoggingLevel.invoke(keyword.invoke("info"));
    }

    private final cimi cimiSyncClient;
    private final authn authnSyncClient;

    public CIMI() {
        cimiSyncClient = (cimi) cimiClientConstructor.invoke();
        authnSyncClient = (authn) cimiSyncClient;
    }

    public CIMI(String endpoint) {
        cimiSyncClient = (cimi) cimiClientConstructor.invoke(endpoint);
        authnSyncClient = (authn) cimiSyncClient;
    }

    public Object login(Object loginParams) {
        return toJava.invoke(authnSyncClient.login(toClojure.invoke(loginParams)));
    }

    public Object logout() {
        return toJava.invoke(authnSyncClient.logout());
    }

    public Boolean isAuthenticated() {
        return (Boolean) authnSyncClient.authenticated_QMARK_();
    }

    public Object getCloudEntryPoint() {
        return toJava.invoke(cimiSyncClient.cloud_entry_point());
    }

    public Object add(String resourceType, Object data) {
        return toJava.invoke(cimiSyncClient.add(resourceType, toClojure.invoke(data)));
    }

    public Object add(String resourceType, Object data, Object options) {
        return toJava.invoke(cimiSyncClient.add(resourceType, toClojure.invoke(data), toClojure.invoke(options)));
    }

    public Object edit(String urlOrId, Object data) {
        return toJava.invoke(cimiSyncClient.edit(urlOrId, toClojure.invoke(data)));
    }

    public Object edit(String urlOrId, Object data, Object options) {
        return toJava.invoke(cimiSyncClient.edit(urlOrId, toClojure.invoke(data), toClojure.invoke(options)));
    }

    public Object delete(String urlOrId) {
        return toJava.invoke(cimiSyncClient.delete(urlOrId));
    }

    public Object delete(String urlOrId, Object options) {
        return toJava.invoke(cimiSyncClient.edit(urlOrId, toClojure.invoke(options)));
    }

    public Object get(String urlOrId) {
        return toJava.invoke(cimiSyncClient.get(urlOrId));
    }

    public Object get(String urlOrId, Object options) {
        return toJava.invoke(cimiSyncClient.get(urlOrId, toClojure.invoke(options)));
    }

    public Object search(String resourceType) {
        return toJava.invoke(cimiSyncClient.search(resourceType));
    }

    public Object search(String resourceType, Object options) {
        return toJava.invoke(cimiSyncClient.search(resourceType, toClojure.invoke(options)));
    }

}
