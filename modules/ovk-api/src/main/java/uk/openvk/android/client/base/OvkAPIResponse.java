package uk.openvk.android.client.base;

public class OvkAPIResponse {

    public static final int CONTENT_TYPE_HTML   = 0x400;
    public static final int CONTENT_TYPE_JSON   = 0x401;
    public static final int CONTENT_TYPE_BINARY = 0x402;

    private String server;
    private int statusCode;
    private String response;
    private String method;
    private String args;
    private final String errorReason;

    public OvkAPIResponse(
            String server, int statusCode,
            String method, String args,
            String response,
            String errorReason
    ) {
        this.server = server;
        this.statusCode = statusCode;
        this.response = response;
        this.method = method;
        this.args = args;
        this.errorReason = errorReason;
    }

    public OvkAPIResponse(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getMethodName() {
        return method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getArguments() {
        return args;
    }

    public String getErrorReason() {
        return errorReason;
    }

    @Override
    public String toString() {
        return response;
    }
}
