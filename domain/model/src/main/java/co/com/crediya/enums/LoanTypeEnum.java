package co.com.crediya.enums;

public enum LoanTypeEnum {
    PERSONAL_LOAN("6e0c1f5a-3d2b-4f81-a7b2-10f8a846f332"),
    MORTGAGE_LOAN("9d4f23b1-5a2c-490b-9c71-2b0b5d53c8f4"),
    STUDENT_LOAN("3a8e9c7d-4b5f-4a6c-8e9d-1b2c3d4e5f60");

    private final String id;

    LoanTypeEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
