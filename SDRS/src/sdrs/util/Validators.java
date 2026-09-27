package sdrs.util;

public final class Validators {

    private Validators() {
    }

    public static void requireText(String value, String label) throws AppException {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(label + " is required.");
        }
    }

    public static int requireInt(String value, String label) throws AppException {
        requireText(value, label);
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new AppException(label + " cannot be negative.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new AppException(label + " must be a whole number.");
        }
    }

    public static int requireIntAny(String value, String label) throws AppException {
        requireText(value, label);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new AppException(label + " must be a whole number.");
        }
    }

    public static int requireInt(String value, String label, int min, int max) throws AppException {
        int parsed = requireInt(value, label);
        if (parsed < min || parsed > max) {
            throw new AppException(label + " must be between " + min + " and " + max + ".");
        }
        return parsed;
    }

    public static void requirePhone(String value) throws AppException {
        requireText(value, "Phone number");
        if (!value.trim().matches("\\d{10}")) {
            throw new AppException("Phone number must be exactly 10 digits.");
        }
    }

    public static void requireId(String value, String label) throws AppException {
        requireText(value, label);
        if (!value.trim().matches("[A-Za-z0-9\\-]{2,20}")) {
            throw new AppException(label + " may contain only letters, digits and dashes (2-20 characters).");
        }
    }

    public static void requireSelection(Object selected, String label) throws AppException {
        if (selected == null) {
            throw new AppException("Please select a " + label + ".");
        }
    }
}
