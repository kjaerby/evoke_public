package moe.evoke.application.views.watch;

public enum IPFSGatewayType {
    BROWSER,
    EVOKE,
    CUSTOM;

    @Override
    public String toString() {
        switch (this) {
            case EVOKE:
                return "evoke";
            case CUSTOM:
                return "custom";
            case BROWSER:
                return "browser";
        }
        return super.toString();
    }
}
