package co.com.crediya.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum FundStatusEnum {

    PENDING("7f32a490-9eda-4393-89c2-cd3b9c49ecac"),
    APPROVED("5f59ed26-bf58-44e5-ad5f-15ad22e44498"),
    REJECTED("2e63f281-0c7e-4fd2-9e52-2ebdd6e3b5ef"),
    IN_PROGRESS("8274b096-ae8e-4f45-8b67-7b215f75ac6b"),
    COMPLETED("6eee879b-036e-420f-8300-958505399ef9"),
    ON_HOLD("8cfdf88a-05a6-461d-b0f1-3116df15f3b5");

    private final String id;

    FundStatusEnum(String id) {
        this.id = id;
    }

    public static String getIdFromName(String name){
        return Arrays.stream(FundStatusEnum.values())
                .filter(status -> Objects.equals(status.name(), name))
                .map(FundStatusEnum::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OBJECT_STATUS_ID_NOT_VALID"));
    }
}
