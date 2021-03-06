package pl.ftims.ias.perfectbeta.exceptions;


public class UniqueConstraintAppException extends AbstractAppException {

    public static final String LOGIN_TAKEN = "Login is already taken";
    public static final String EMAIL_TAKEN = "Email is already taken";
    public static final String FAVOURITE_ALREADY = "The offer is already added to favourites";

    public static final String NOT_FAVOURITE_ALREADY = "The offer is already removed from favourites";


    private UniqueConstraintAppException(String message) {
        super(message);
    }

    public static LoginTakenAppException createLoginTakenException() {
        return new LoginTakenAppException(LOGIN_TAKEN);
    }

    public static EmailTakenAppException createEmailTakenException() {
        return new EmailTakenAppException(EMAIL_TAKEN);
    }

    public static FavouriteAlreadyAppException createFavouriteAlreadyException() {
        return new FavouriteAlreadyAppException(FAVOURITE_ALREADY);
    }

    public static NotFavouriteAlreadyAppException createNotFavouriteAlreadyException() {
        return new NotFavouriteAlreadyAppException(NOT_FAVOURITE_ALREADY);
    }

    public static class LoginTakenAppException extends UniqueConstraintAppException {

        private LoginTakenAppException(String message) {
            super(message);
        }
    }


    public static class EmailTakenAppException extends UniqueConstraintAppException {

        private EmailTakenAppException(String message) {
            super(message);
        }
    }


    public static class FavouriteAlreadyAppException extends UniqueConstraintAppException {

        private FavouriteAlreadyAppException(String message) {
            super(message);
        }
    }


    public static class NotFavouriteAlreadyAppException extends UniqueConstraintAppException {

        private NotFavouriteAlreadyAppException(String message) {
            super(message);
        }
    }

}
