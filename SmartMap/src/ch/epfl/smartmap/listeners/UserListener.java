package ch.epfl.smartmap.listeners;

/**
 * This interface is defined for all the classes that need to listen to any change in any field of a specified
 * User, for example the DatabaseHelper.
 * 
 * @author jfperren
 */
public interface UserListener extends DisplayableListener, LocalisableListener {

    /**
     * Called when e-mail changes.
     */
    void onEmailChanged();

    /**
     * Called when timestamp is refreshed.
     */
    void onLastSeenChanged();

    /**
     * Called when location changes
     */
    @Override
    void onLocationStringChanged();

    /**
     * Called when phone number changes.
     */
    void onPhoneNumberChanged();
}